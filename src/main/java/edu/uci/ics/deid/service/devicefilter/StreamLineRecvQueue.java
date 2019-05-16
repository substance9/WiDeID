package edu.uci.ics.deid.service.devicefilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class StreamLineRecvQueue {
    private BlockingQueue<String> rQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public StreamLineRecvQueue() {
        rQueue = new ArrayBlockingQueue<>(1024);
    }

    public void put(String evtStr) {
        try {
            rQueue.put(evtStr);
            logger.debug("RecvQueue Len: {}", rQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String take() {
        String ret = "";
        try {
            ret = rQueue.take();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}