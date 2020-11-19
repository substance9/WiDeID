package edu.uci.ics.deid.service.occupancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.Occupancy;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


@Component
public class OccupancySendQueue {
    private BlockingQueue<Occupancy> sQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public OccupancySendQueue() {
        sQueue = new ArrayBlockingQueue<>(1024);
    }

    public void put(Occupancy evt) {
        try {
            sQueue.put(evt);
            //logger.debug("SendQueue Len: {}", sQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Occupancy take() {
        Occupancy evt = null;
        try {
            evt = sQueue.take();
            return evt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evt;
    }

}