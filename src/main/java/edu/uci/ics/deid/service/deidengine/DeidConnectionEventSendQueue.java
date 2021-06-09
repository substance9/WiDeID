package edu.uci.ics.deid.service.deidengine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeidConnectionEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


@Component
public class DeidConnectionEventSendQueue {
    private BlockingQueue<DeidConnectionEvent> sQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeidConnectionEventSendQueue() {
        sQueue = new ArrayBlockingQueue<>(2048);
    }

    public void put(DeidConnectionEvent evt) {
        try {
            sQueue.put(evt);
            logger.debug("SendQueue Len: {}", sQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public DeidConnectionEvent take() {
        DeidConnectionEvent evt = null;
        try {
            evt = sQueue.take();
            return evt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evt;
    }

}