package edu.uci.ics.deid.service.finder;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeidConnectionEvent;
import edu.uci.ics.deid.model.MacAddress;
import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.util.Epoch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Component
public class DataProcessor implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running; 

    @Value("${finder.epoch_duration}")
    private Integer epochDuration;

    private Epoch epoch;

    //....Debugging Purpose.....
    private long numMsgReceived;
    //..........................

    @Autowired
    RawEventRecvQueue recvQueue;

    //TODO: Define output class and corresponding send queue
    // @Autowired
    // RawEventRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DataProcessor(){
        numMsgReceived = 0;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        epoch = new Epoch(epochDuration);
        logger.warn(Integer.toString(epochDuration) + "minutes epoch duration");
        RawConnectionEvent rawEvt = null;
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

            // TODO: Add data to batch (buffer)

            // TODO: Process batch every 5 minutes


            try {
                logger.warn("Finder Processor try to send:");
                //TODO: put finder output to the send queue
                //sendQueue.put(deidConnEvt);
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