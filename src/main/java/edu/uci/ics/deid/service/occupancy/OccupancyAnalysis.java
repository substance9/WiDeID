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
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.Occupancy;
import edu.uci.ics.deid.model.OccupancyUnit;
import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.model.RawConnectionEventMsg;

import java.time.Duration;
import java.time.Instant;

public class OccupancyAnalysis{

    @Value("${occupancy.parameter.interval}")
    private Long interval;

    @Autowired
    RawEventRecvQueue recvQueue;

    // @Autowired
    // DeidConnectionEventSendQueue sendQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    List<ArrayList<String>> events = new ArrayList<ArrayList<String>>();
    Map<String, Integer> hm = new HashMap<String, Integer>();
    List<String> aps = new ArrayList<>();

    private Instant lastTimestamp;
    private Instant currentTimestamp;
    private Duration timeElapsed;

    public void run(){
        lastTimestamp = Instant.now();
        streamingOccupancy.readGraph();
        while(true){
            RawConnectionEvent evt;
            //read event from queue
            try {
                evt = recvQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mapAP(){
        //read required AP from files
        try {
            FileReader fin = new FileReader("aps.txt");
            //TODO: do not hard code any parameter
            Scanner src = new Scanner(fin);
            String line;
            int id=0;
            while(src.hasNext()){
                line = src.next();
                aps.add(line);
                hm.put(line,id++);
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assignEvent(RawConnectionEvent evt){

        int id = hm.get(evt.getApId());
        currentTimestamp = evt.getTimestamp().toInstant();
        events.get(id).add(evt.getApMac().toString());
        timeElapsed = Duration.between(lastTimestamp, currentTimestamp);

        if(timeElapsed.toSeconds() > 60){
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
        Occupancy occupancyOutput = new Occupancy();
        occupancyOutput.setTimeStamp(currentTimestamp2.toString());
        for(int i=0;i<events.size();i++){
            OccupancyUnit occu = new OccupancyUnit();
            //occu.setApid(hm.getKey(i));
            //need to add another hashmap to map the "value" to "key"
            occu.setCount(streamingOccupancy.computeOccupancy(events.get(i)));
            occupancyOutput.getOccupancyArray().add(occu);
        }
        try {
            //sendQueue.put(occupancyOutput);
            logger.debug("Try to send a occupancy output");
            //logger.info(occupancyOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}