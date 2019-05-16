package edu.uci.ics.deid.service.deidengine;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeidConnectionEvent;
import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.RawConnectionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Component
public class DeidEngine implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running; 

    //....Debugging Purpose.....
    private long numMsgReceived;
    //..........................

    @Autowired
    RawEventRecvQueue recvQueue;

    @Autowired
    DeidConnectionEventSendQueue sendQueue;

    @Autowired
    SaltManager saltManager;

    @Autowired
    HashOperator hashOperator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeidEngine(){
        numMsgReceived = 0;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        RawConnectionEvent rawEvt = null;
        byte[] salt = null;
        // Main Loop
        while (this.running) {
            logger.debug(String.format("Number of RawConnectionEvents Received: %d", numMsgReceived));
            //1. Get RawConnectionEvent From Recv Queue
            try {
                rawEvt = recvQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rawEvt == null) {
                logger.error("NULL RawConnectionEvent, skip");
                continue;
            }

            numMsgReceived++;

            // logger.debug(String.format("DeIDEngine Get Event, AP MAC: %s, AP ID: %s, Client MAC: %s, Time: %d", 
            //                             rawEvt.getApMac().getMacAddrStr(),
            //                             rawEvt.getApId(),
            //                             rawEvt.getClientMac().getMacAddrStr(),
            //                             rawEvt.getTimestamp().getTime()));

            MacAddress devMac = rawEvt.getClientMac();
            salt = saltManager.getSalt(rawEvt);

            String clientId = hashOperator.getHashedID(devMac, salt);
            DeidConnectionEvent deidConnEvt = new DeidConnectionEvent(rawEvt, clientId);

            try {
                sendQueue.put(deidConnEvt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy(){
        running = false;
    }

    @PostConstruct
    private  void init(){
        this.running = true;
        this.thread.start();
    }
}