package edu.uci.ics.deid.service.devicefilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeviceFilterLog;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


@Component
public class DeviceFilterLogQueue {
    private BlockingQueue<DeviceFilterLog> lQueue;

    //Logging
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeviceFilterLogQueue() {
        lQueue = new ArrayBlockingQueue<>(1024);
    }

    public void put(DeviceFilterLog evt) {
        try {
            lQueue.put(evt);
            logger.debug("SendQueue Len: {}", lQueue.size());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public DeviceFilterLog take() {
        DeviceFilterLog evt = null;
        try {
            evt = lQueue.take();
            return evt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evt;
    }

}