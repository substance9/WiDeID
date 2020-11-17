package edu.uci.ics.deid.service.occupancy;

import java.io.IOException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.Occupancy;
import edu.uci.ics.deid.model.OccupancyUnit;
import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.model.RawConnectionEventMsg;


import java.time.Duration;
import java.time.Instant;

@Component
public class OccupancyAnalysis implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running; 

    @Value("${occupancy.parameter.interval}")
    private Long interval;

    @Value("${occupancy.input_data.WiFiAPs}")
    private String wifiAPFile;

    @Autowired
    RawEventRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    List<ArrayList<String>> events = new ArrayList<ArrayList<String>>();
    Map<String, Integer> hm = new HashMap<String, Integer>();
    Map<Integer, String> rehm = new HashMap<Integer, String>();
    List<String> aps = new ArrayList<>();

    private Instant lastTimestamp;
    private Instant currentTimestamp;
    private Duration timeElapsed;

    public void run(){
        RawConnectionEvent evt = null;
        lastTimestamp = Instant.now();
        streamingOccupancy.readGraph();
        while(this.running){
            //read event from queue
            try {
                evt = recvQueue.take();
                assignEvent(evt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mapAP(){
        //read required AP from files
        try {
            FileReader fin = new FileReader(wifiAPFile);
            Scanner src = new Scanner(fin);
            String line;
            int id=0;
            while(src.hasNext()){
                line = src.next();
                aps.add(line);
                hm.put(line,id);
                rehm.put(id, line);
                id++;
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assignEvent(RawConnectionEvent evt){

        int id = hm.get(evt.getApId());//get id of ap
        currentTimestamp = evt.getTimestamp().toInstant();
        events.get(id).add(evt.getApMac().toString());
        logger.debug("current timestamp: " + currentTimestamp + " last timestamp: " + lastTimestamp);
        timeElapsed = Duration.between(lastTimestamp, currentTimestamp);

        if(timeElapsed.toSeconds() > interval*60){
            //compute occupancy: run async
            computeOccupancy(events, currentTimestamp);
            //clear current list
            for(int i=0;i<events.size();i++){
                events.get(i).clear();
            }
            //update timeStamp
            lastTimestamp = currentTimestamp;
        }
    }

    public void computeOccupancy(List<ArrayList<String>> events, Instant currentTimestamp2){//to change it using a thread
        logger.debug("current timestamp: "+ currentTimestamp2);

        Occupancy occupancyOutput = new Occupancy();
        occupancyOutput.setTimeStamp(currentTimestamp2.toString());
        for(int i=0;i<events.size();i++){
            OccupancyUnit occu = new OccupancyUnit();
            occu.setApid(rehm.get(i));
            occu.setCount(streamingOccupancy.computeOccupancy(events.get(i)));
            occupancyOutput.getOccupancyArray().add(occu);
        }
        try {
            //sendQueue.put(occupancyOutput);
            logger.debug("Try to send a occupancy output");
            logger.debug("timestamp: " + occupancyOutput.getTimeStamp());
            for(int i=0;i<occupancyOutput.getOccupancyArray().size();i++){
                logger.debug("ap_id: " + occupancyOutput.getOccupancyArray().get(i).getApid() + " Count: " + occupancyOutput.getOccupancyArray().get(i).getCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
