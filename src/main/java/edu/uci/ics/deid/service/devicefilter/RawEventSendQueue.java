package edu.uci.ics.deid.service.devicefilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.RawConnectionEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


@Component
public class RawEventSendQueue {
    private BlockingQueue<RawConnectionEvent> sQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RawEventSendQueue() {
        sQueue = new ArrayBlockingQueue<>(2048);
    }

    public void put(RawConnectionEvent evt) {
        try {
            sQueue.put(evt);
            logger.debug("SendQueue Len: {}", sQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public RawConnectionEvent take() {
        RawConnectionEvent evt = null;
        try {
            evt = sQueue.take();
            return evt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evt;
    }

}