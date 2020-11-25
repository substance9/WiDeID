package edu.uci.ics.deid.service.occupancy;

import java.io.IOException;
import java.sql.Timestamp;
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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import java.time.Duration;
import java.time.Instant;

@Component
public class OccupancyAnalysis implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running; 

    @Value("${occupancy.parameter.interval}")
    private double interval;

    @Value("${occupancy.parameter.staticDeviceThreshold}")
    private long staticDeviceThreshold;

    @Value("${occupancy.input_data.WiFiAPs}")
    private String wifiAPFile;

    @Value("${occupancy.parameter.staticStart}")
    private Integer staticStart;

    @Value("${occupancy.parameter.staticEnd}")
    private Integer staticEnd;

    @Value("${occupancy.input_data.deviceGraph}")
    private String graphFile;

    @Value("${occupancy.input_data.cluterLabel}")
    private String clusterLabelFile;

    @Value("${occupancy.parameter.updateInterval}")
    private long updateInterval;

    @Autowired
    RawEventRecvQueue recvQueue;

    @Autowired
    OccupancySendQueue sendQueue; 

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    
    Map<String, List<String>> apEvents = new HashMap<String, List<String>>();
    Map<String, Integer> staticDevice = new HashMap<String, Integer>();
    List<String> aps = new ArrayList<>();

    private Timestamp lastWeekTimestamp;//use to update staticDevices weekly
    private Timestamp lastTimestamp;
    private Timestamp currentTimestamp;
    private long timeElapsed;

    public OccupancyAnalysis(){
        this.thread = new Thread(this);
    }

    @Autowired
    streamingOccupancy SO;

    //for testing 
    private boolean flag = false;

    @Override
    public void run(){
        RawConnectionEvent evt = null;
        //lastTimestamp = new Timestamp(System.currentTimeMillis());//modify to first event for testing
        lastWeekTimestamp = new Timestamp(System.currentTimeMillis());
        SO.readGraph(graphFile, clusterLabelFile);
        mapAP();

        while(this.running){
            //read event from queue
            try {
                evt = recvQueue.take();
                //System.out.println("event info: " + evt.getTimestamp() + " " + evt.getApId() + " " + evt.getClientMac().getMacAddrStr());
                filterStaticDevice(evt);
                assignEvent(evt);
                updateStaticDeviceWeekly(evt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void filterStaticDevice(RawConnectionEvent evt){
        int hour = evt.getTimestamp().toLocalDateTime().getHour();
        String clientMac = evt.getClientMac().getInitHashId();
        if(hour >= staticStart && hour <= staticEnd){
            if(!staticDevice.containsKey(clientMac)){
                staticDevice.put(clientMac, 0);
            }
            else{
                staticDevice.put(clientMac, staticDevice.get(clientMac)+1);
            }
        }
    }

    public void updateStaticDeviceWeekly(RawConnectionEvent evt){
        if((evt.getTimestamp().getTime() - lastWeekTimestamp.getTime()) >= updateInterval*24*60*60*1000){
            for(Map.Entry<String, Integer> entry : staticDevice.entrySet()){
                if(entry.getValue() < staticDeviceThreshold){
                    staticDevice.remove(entry.getKey());
                }
            }
            lastWeekTimestamp = evt.getTimestamp();
        }
    }

    public void mapAP(){
        //read required AP from files
        try {
            FileReader fin = new FileReader(wifiAPFile);
            Scanner src = new Scanner(fin);
            String line;
            while(src.hasNext()){
                line = src.next();
                aps.add(line.substring(1,line.length()-1));//remove quote
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assignEvent(RawConnectionEvent evt){
        //for testing 
        if(!flag){
            lastTimestamp = evt.getTimestamp();
            flag = true;
        }

        String ap = evt.getApId();
        String clientMac = evt.getClientMac().getInitHashId();

        if(!staticDevice.containsKey(clientMac)){//not a static device
            if(aps.contains(ap)){//is target ap
                if(!apEvents.containsKey(ap)){
                    List<String> macs = new ArrayList<>();
                    macs.add(clientMac);
                    apEvents.put(ap, macs);
                }
                else{
                    apEvents.get(ap).add(clientMac);
                }
            }
        }

        currentTimestamp = evt.getTimestamp();
        timeElapsed = currentTimestamp.getTime() - lastTimestamp.getTime();//milliseconds = 0.001 second
        if(timeElapsed > interval*60*1000){
            computeOccupancy();
            //clear current list
            apEvents.clear();
            //update timeStamp
            lastTimestamp = currentTimestamp;
        }
    }

    public void computeOccupancy(){
        //System.out.println("timestamps: "+ lastTimestamp + " " + currentTimestamp);
        Occupancy occupancyOutput = new Occupancy();
        List<OccupancyUnit> occus = new ArrayList<>();
        occupancyOutput.setStartTimeStamp(lastTimestamp);
        occupancyOutput.setEndTimeStamp(currentTimestamp);

        for(int i=0;i<aps.size();i++){
            OccupancyUnit occu = new OccupancyUnit();
            int count = 0;
            String ap = aps.get(i);
            occu.setApid(ap);
            if(apEvents.containsKey(ap)){
                count = SO.computeOccupancy(apEvents.get(ap));
            }
            occu.setCount(count);
            occus.add(occu);
        }
        occupancyOutput.setOccupancyArray(occus);

        sendQueue.put(occupancyOutput);

        /*logger.debug("Try to send a occupancy output");
        System.out.println(occupancyOutput.getStartTimeStamp() + " " + occupancyOutput.getEndTimeStamp());
        for(int i=0;i<occupancyOutput.getOccupancyArray().size();i++){
            logger.debug("ap_id: " + occupancyOutput.getOccupancyArray().get(i).getApid() + " Count: " + occupancyOutput.getOccupancyArray().get(i).getCount());
        }*/
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
