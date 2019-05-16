package edu.uci.ics.deid.service.deidengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.RawConnectionEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class RawEventRecvQueue {
    private BlockingQueue<RawConnectionEvent> rQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RawEventRecvQueue() {
        rQueue = new ArrayBlockingQueue<>(1024);
    }

    public void put(RawConnectionEvent evt) {
        try {
            rQueue.put(evt);
            logger.debug("RecvQueue Len: {}", rQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public RawConnectionEvent take() {
        RawConnectionEvent ret = null;
        try {
            ret = rQueue.take();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}