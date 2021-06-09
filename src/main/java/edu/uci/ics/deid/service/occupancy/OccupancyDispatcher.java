package edu.uci.ics.deid.service.occupancy;

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
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.Occupancy;
import edu.uci.ics.deid.model.OccupancyUnit;
import edu.uci.ics.deid.model.EncryptedDispatchingMsg;
import edu.uci.ics.deid.util.AES;

@Component
public class OccupancyDispatcher implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running;

    @Value("${tippers_dispatcher.host}")
    private String host;

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
    OccupancySendQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OccupancyDispatcher(){
        numMsgReceived = 0;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        Occupancy evt = null;
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
                logger.error("NULL OccupancyEvent, skip");
                continue;
            }

            numMsgReceived++;

            String generatedString = RandomStringUtils.randomAlphanumeric(20);

            EncryptedDispatchingMsg encMsg = new EncryptedDispatchingMsg();
            encMsg.setHeader(generatedString);
            String encBodyStr = "";
             
            try {
                encBodyStr = AES.encrypt(mapper.writeValueAsString(evt), secret, generatedString);
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
            encMsg.setBody(encBodyStr);
            encMsg.setType("oc");

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
        publisher = context.createSocket(SocketType.PUB);
        publisher.connect("tcp://"+host+":"+Integer.toString(port));
        this.running = true;
        this.thread.start();
    }
}