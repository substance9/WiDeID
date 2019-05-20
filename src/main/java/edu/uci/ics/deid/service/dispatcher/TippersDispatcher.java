package edu.uci.ics.deid.service.dispatcher;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.DeidConnectionEvent;
import edu.uci.ics.deid.model.DeidConnectionEventMsg;
import edu.uci.ics.deid.model.EncryptedDispatchingMsg;
import edu.uci.ics.deid.util.AES;

@Component
public class TippersDispatcher implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running; 

    @Value("${tippers_dispatcher.secret}")
    private String secret;

    @Value("${tippers_dispatcher.port}")
    private Integer port;

    private ZContext context;
    private ZMQ.Socket publisher;

    //....Debugging Purpose.....
    private long numMsgReceived;
    //..........................

    @Autowired
    DeidConnectionEventRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public TippersDispatcher(){
        numMsgReceived = 0;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        DeidConnectionEvent evt = null;
        DeidConnectionEventMsg evtMsg = null;
        String msg = null;

        // Main Loop
        while (this.running) {
            logger.debug(String.format("Number of RawConnectionEvents Received: %d", numMsgReceived));
            //1. Get RawConnectionEvent From Recv Queue
            try {
                evt = recvQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (evt == null) {
                logger.error("NULL DeidConnectionEvent, skip");
                continue;
            }

            numMsgReceived++;

            evtMsg = new DeidConnectionEventMsg(evt);
            String generatedString = RandomStringUtils.randomAlphanumeric(20);

            EncryptedDispatchingMsg encMsg = new EncryptedDispatchingMsg();
            encMsg.setHeader(generatedString);
            String encBodyStr = "";
             
            try {
                encBodyStr = AES.encrypt(mapper.writeValueAsString(evtMsg), secret, generatedString);
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
            encMsg.setBody(encBodyStr);

            try {
                msg = mapper.writeValueAsString(encMsg);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            //logger.debug(msg);

            publisher.send(msg.getBytes(ZMQ.CHARSET), 0);

            // logger.debug(String.format("DeIDEngine Get Event, AP MAC: %s, AP ID: %s, Client MAC: %s, Time: %d", 
            //                             rawEvt.getApMac().getMacAddrStr(),
            //                             rawEvt.getApId(),
            //                             rawEvt.getClientMac().getMacAddrStr(),
            //                             rawEvt.getTimestamp().getTime()));

            
        }
    }

    @Override
    public void destroy(){
        running = false;
    }

    @PostConstruct
    private  void init(){
        context = new ZContext();
        publisher = context.createSocket(ZMQ.PUB);
        publisher.bind("tcp://*:"+Integer.toString(port));
        this.running = true;
        this.thread.start();
    }
}