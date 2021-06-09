package edu.uci.ics.deid.service.dispatcher;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeidConnectionEvent;

@Component
public class DeidConnectionEventRecvQueue {
    private BlockingQueue<DeidConnectionEvent> rQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeidConnectionEventRecvQueue() {
        rQueue = new ArrayBlockingQueue<>(2048);
    }

    public void put(DeidConnectionEvent evt) {
        try {
            rQueue.put(evt);
            logger.debug("RecvQueue Len: {}", rQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public DeidConnectionEvent take() {
        DeidConnectionEvent ret = null;
        try {
            ret = rQueue.take();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}