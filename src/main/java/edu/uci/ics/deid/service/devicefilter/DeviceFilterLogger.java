package edu.uci.ics.deid.service.devicefilter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeviceFilterLog;
import edu.uci.ics.deid.repository.DeviceFilterLogRepository;

@Component
public class DeviceFilterLogger implements DisposableBean, Runnable {

    @Autowired
    DeviceFilterLogRepository deviceFilterLogRepo;

    @Autowired
    DeviceFilterLogQueue logQueue;

    private Thread thread;
    private volatile boolean running;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeviceFilterLogger() {
        this.thread = new Thread(this);
    }


    @Override
    public void run() {
        DeviceFilterLog filterLog = null;
        // Main Loop
        while (this.running) {
            try {
                filterLog = logQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                deviceFilterLogRepo.addLog(filterLog.getTimestamp(),
                                        filterLog.getClientMac(),
                                        filterLog.getReasons());
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