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
import edu.uci.ics.deid.model.Space;
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

    @Value("${occupancy.input_data.spaceMetadata}")
    private String spaceMetadata;

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

    
    Map<String, List<Integer>> apEvents = new HashMap<String, List<Integer>>();//ap_name to events array list; for semantic location: string(space_id) to events array list
    Map<Integer, Integer> spaceIDIndex = new HashMap<Integer, Integer>();

    Map<String, Integer> staticDevice = new HashMap<String, Integer>();
    List<Space> spaces = new ArrayList<>();
    List<String> aps = new ArrayList<>();//wifi aps for all room/region location

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
        readSpaceMetadata();

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

    public void readSpaceMetadata(){
        //read Space Metadata from files
        String line = null;
        int id = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(spaceMetadata)))) {
            while ((line=br.readLine()) != null) {
                String[] array = line.split(",");
                List<String> str = Arrays.asList(array);
                Space space = new Space();
                space.setSpace_id(Integer.valueOf(str.get(0)));
                space.setAp_name(str.get(1).substring(1,str.get(1).length()-1));
                space.setSpace_type(str.get(2).substring(1,str.get(2).length()-1));
                if(str.get(3).equals("NULL")){
                    space.setBuilding_id(-1);
                }
                else{
                    space.setBuilding_id(Integer.valueOf(str.get(3)));
                }
                if(str.get(4).equals("NULL")){
                    space.setFloor_id(-1);
                }
                else{
                    space.setFloor_id(Integer.valueOf(str.get(4)));
                }
                if(space.getSpace_type().equals("Room")){
                    aps.add(space.getAp_name());
                }
                spaceIDIndex.put(space.getSpace_id(), id++);
                spaces.add(space);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        /*for(int i=0;i<aps.size();i++){
            System.out.println(aps.get(i));
        }
        for(int i=0;i<spaces.size();i++){
            System.out.print(spaces.get(i).getSpace_id() + " " + spaces.get(i).getAp_name() + " " + spaces.get(i).getSpace_type() + " " + spaces.get(i).getBuilding_id() + " " + spaces.get(i).getFloor_id());
            System.out.println("");
        }*/
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
                    List<Integer> macs = new ArrayList<>();
                    macs.add(SO.findCluster(clientMac));
                    apEvents.put(ap, macs);
                }
                else{
                    apEvents.get(ap).add(SO.findCluster(clientMac));
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
        Occupancy occupancyOutput = new Occupancy();
        List<OccupancyUnit> occus = new ArrayList<>();
        occupancyOutput.setStartTimeStamp(lastTimestamp);
        occupancyOutput.setEndTimeStamp(currentTimestamp);

        //for all physical regions
        for(int i=0;i<spaces.size();i++){
            //only deal with physical regions
            if(spaces.get(i).getSpace_type().equals("Floor") || spaces.get(i).getSpace_type().equals("Building")) {
                continue;
            }
            OccupancyUnit occu = new OccupancyUnit();
            int count = 0;
            String ap = spaces.get(i).getAp_name();
            occu.setApid(ap);
            occu.setSpaceid(spaces.get(i).getSpace_id());
            if(apEvents.containsKey(ap)){
                //slim list
                Set<Integer> uniqueINT = new HashSet<Integer>(apEvents.get(ap));
                List<Integer> slimList = new ArrayList<>();
                slimList.addAll(uniqueINT);
                //get occupancy as the number of distinct macs
                count = uniqueINT.size();
                //push up slimList to father node
                MergeFatherNode(i,slimList);
            }
            occu.setCount(count);
            occus.add(occu);
        }

        //for all floors
        for(int i=0;i<spaces.size();i++){
            if(spaces.get(i).getSpace_type().equals("Floor")){
                OccupancyUnit occu = new OccupancyUnit();
                int count = 0;
                String space_id = String.valueOf(spaces.get(i).getSpace_id());
                String ap = spaces.get(i).getAp_name();
                occu.setApid(ap);
                occu.setSpaceid(spaces.get(i).getSpace_id());
                if(apEvents.containsKey(space_id)){
                    //slim list
                    Set<Integer> uniqueINT = new HashSet<Integer>(apEvents.get(space_id));
                    List<Integer> slimList = new ArrayList<>();
                    slimList.addAll(uniqueINT);
                    //get occupancy as the number of distinct macs
                    count = uniqueINT.size();
                    //push up slimList to father node
                    MergeFatherNode(i,slimList);
                }
                occu.setCount(count);
                occus.add(occu);
            }
        }
        //for all buildings
        for(int i=0;i<spaces.size();i++){
            if(spaces.get(i).getSpace_type().equals("Building")){
                OccupancyUnit occu = new OccupancyUnit();
                int count = 0;
                String space_id = String.valueOf(spaces.get(i).getSpace_id());
                String ap = spaces.get(i).getAp_name();
                occu.setApid(ap);
                occu.setSpaceid(spaces.get(i).getSpace_id());
                
                if(apEvents.containsKey(space_id)){
                    //slim list
                    Set<Integer> uniqueINT = new HashSet<Integer>(apEvents.get(space_id));
                    //get occupancy as the number of distinct macs
                    count = uniqueINT.size();
                }
                occu.setCount(count);
                occus.add(occu);
            }
        }

        occupancyOutput.setOccupancyArray(occus);
        sendQueue.put(occupancyOutput);

        //System.out.println("static device number: " + staticDevice.size());

        /*logger.debug("Try to send a occupancy output");
        System.out.println(occupancyOutput.getStartTimeStamp() + " " + occupancyOutput.getEndTimeStamp());
        for(int i=0;i<occupancyOutput.getOccupancyArray().size();i++){
            logger.debug("ap_id: " + occupancyOutput.getOccupancyArray().get(i).getApid() + " Count: " + occupancyOutput.getOccupancyArray().get(i).getCount());
        }*/
    }

    public void MergeFatherNode(int spaceIndex, List<Integer> slimArray){//merge slim array of current node with#spaceIndex to its father
        int fatherNode = -1;
        if(spaces.get(spaceIndex).getSpace_type().equals("Room")){
            fatherNode = spaces.get(spaceIndex).getFloor_id();
        }
        else if(spaces.get(spaceIndex).getSpace_type().equals("Floor")){
            fatherNode = spaces.get(spaceIndex).getBuilding_id();
        }
        String fatherName = String.valueOf(fatherNode);
        if(!apEvents.containsKey(fatherName)){
            apEvents.put(fatherName, slimArray);
        }
        else{
            apEvents.get(fatherName).addAll(slimArray);
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
