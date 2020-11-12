package edu.uci.ics.deid.service.devicefilter;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.uci.ics.deid.model.DeviceFilterLog;
import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.service.devicefilter.filter.OptoutDeviceFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

@Component
public class DeviceFilter implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running;

    //....Debugging Purpose.....
    private long numMsgReceived;
    private long numMsgForwarded;
    private long numMsgDiscarded;
    //..........................

    @Autowired
    StreamLineRecvQueue recvQueue;

    @Autowired 
    RawEventSendQueue sendQueue;

    @Autowired
    DeviceFilterLogQueue logQueue;

    @Autowired
    Parser parser;

    @Autowired
    OptoutDeviceFilter optOutDevFilter;

    @Autowired
    DeviceFilterLogger deviceFilterLogger;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeviceFilter(){

        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ExecutorService optOutFilterExecutor = Executors.newSingleThreadExecutor();
        String evtStr = null;
        RawConnectionEvent rawEvt = null;
        DeviceFilterLog filterLog = null;
        //ExecutorService regDevFilterExecutor = Executors.newSingleThreadExecutor();
        // Main Loop
        while (this.running) {
            logger.debug(String.format("Number of Event Received:%d, Forwarded:%d, Discarded:%d", numMsgReceived,numMsgForwarded,numMsgDiscarded));
            //1. Get Line From Recv Queue
            try {
                evtStr = recvQueue.take();

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (evtStr == null) {
                logger.error("Empty Event String, skip");
                continue;
            }
            numMsgReceived++;

            //2. Parse the event line
            try{
                rawEvt = parser.parse(evtStr);
            }catch(Exception e){
                e.printStackTrace();
                logger.error("Unable to parse the event string: " + evtStr + " , skip");
                continue;
            }
            
            if (rawEvt == null){
                logger.error("Unable to parse the event string: " + evtStr + " , skip");
                continue;
            }

            //For occupancy experiment, no opt-out is needed
            sendQueue.put(rawEvt);
            numMsgForwarded++;

            //3. Check Filter Policies
            // optOutDevFilter.setEventForChecking(rawEvt);
            // Future<Boolean> result1 = optOutFilterExecutor.submit(optOutDevFilter);

            // boolean forwardingDecision1 = false;
            // try{
            //     forwardingDecision1 = result1.get();
            // }catch (InterruptedException|ExecutionException e) {
            //     logger.error("Error when try to get result from opt-out filter");
            //     e.printStackTrace();
            // }

            // boolean discardFlag = false;
            // String discardReasons = "";

            // if (forwardingDecision1 == false){
            //     discardFlag = true;
            //     discardReasons += optOutDevFilter.getFilterReason();
            // }

            // if(discardFlag == false){
            //     sendQueue.put(rawEvt);
            //     numMsgForwarded++;
            // }
            // else{
            //     //Discard the data and generate the log for attestation in the future
            //     filterLog = new DeviceFilterLog(rawEvt, discardReasons);
            //     logQueue.put(filterLog);
            //     logger.debug("Device " + rawEvt.getClientMac().getMacAddrStr() + " is opted-out device, Discard data.");
            //     numMsgDiscarded++;
            //     continue;
            // }
            
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