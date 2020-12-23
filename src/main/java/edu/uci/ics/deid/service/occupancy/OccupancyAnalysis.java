package edu.uci.ics.deid.service.occupancy;

import java.io.IOException;
import java.sql.Timestamp;
import javax.annotation.PostConstruct;
import java.lang.Math; 
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
import javafx.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.io.FileWriter;
import java.io.PrintWriter;

@Component
public class OccupancyAnalysis implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running; 

    @Value("${occupancy.parameter.interval}")
    private double interval;

    @Value("${occupancy.parameter.staticDeviceThreshold}")
    private long staticDeviceThreshold;

    @Value("${occupancy.parameter.staticFlushToDeviceInterval}")
    private long staticFlushToDeviceInterval;

    @Value("${occupancy.input_data.spaceMetadata}")
    private String spaceMetadata;

    @Value("${occupancy.input_data.overlappingRegion}")
    private String overlappingRegionFile;

    @Value("${occupancy.parameter.staticStart}")
    private Integer staticStart;

    @Value("${occupancy.parameter.staticEnd}")
    private Integer staticEnd;

    @Value("${occupancy.input_data.deviceGraph}")
    private String graphFile;

    @Value("${occupancy.input_data.staticDevice}")
    private String staticDeviceFile;

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

    Map<String, Integer> staticDevice = new HashMap<String, Integer>();//store only the static devices and its corresponding frequency
    List<Space> spaces = new ArrayList<>();
    Map<String, Integer> APID = new HashMap<String, Integer>();//(ap name, index of spaces array)
    Map<Integer, Pair<Integer, Double>> overlappingRegion = new HashMap<Integer, Pair<Integer, Double>>();//store data from overlappingRegions. [coverage_id, Pair: (region_id, percentage)]
    Map<Integer, Boolean> activeBuilding = new HashMap<Integer, Boolean>();//store those buildings which receive conectivity data

    private Timestamp lastWeekTimestamp;//use to update staticDevices weekly
    private Timestamp lastDayTimestamp;//use to flush staticDevices to disk weekly
    private Timestamp lastTimestamp;
    private Timestamp currentTimestamp;
    private long timeElapsed;
    private Boolean readStaticDeviceFlag = false;//when first run codes, read static devices from files, and then set flag as true

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
        readOverlappingRegion();
        //readStaticDevice();//ihe: test for running only first time

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

    public void staticDevicesToDisk(RawConnectionEvent evt){
        //flush static devices to disk daily 
        //the structure of static device: 
        /*
        first line: timestamp - timestamp when the data is flushed
        several lines: each line contains device_mac(or id) and its frequency
        */
        if((evt.getTimestamp().getTime() - lastDayTimestamp.getTime()) >= staticFlushToDeviceInterval*24*60*60*1000){
            File file = new File(staticDeviceFile);
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(evt.getTimestamp().toString());
                out.newLine();
                for(Map.Entry<String, Integer> entry : staticDevice.entrySet()){
                    String output = String.valueOf(entry.getKey()) + " " + String.valueOf(entry.getValue());
                    out.write(output);
                    out.newLine();
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastDayTimestamp = evt.getTimestamp();
        }
    }

    public void readStaticDevice(){
        if(readStaticDeviceFlag) {
            return;
        }
        readStaticDeviceFlag = true;
        String line = null;
        int id = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(staticDeviceFile)))) {
            line=br.readLine();
            if((line=br.readLine()) == null){//file is empty
                return;
            }
            while ((line=br.readLine()) != null) {
                String[] array = line.split(" ");
                List<String> str = Arrays.asList(array);
                staticDevice.put(str.get(0), Integer.valueOf(str.get(1)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readSpaceMetadata(){
        //read Space Metadata from files
        //schema: space_id,space_name,type,floor_id,building_id,bbtlx,bbtly,bbbrx,bbbry
        String line = null;
        int id = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(spaceMetadata)))) {
            while ((line=br.readLine()) != null) {
                String[] array = line.split(";");
                List<String> str = Arrays.asList(array);
                Space space = new Space();
                space.setSpace_id(Integer.valueOf(str.get(0)));
                space.setName(str.get(1));
                space.setSpace_type(str.get(2));
                //System.out.println(str.get(0) + " " + str.get(1) + " " + str.get(2) + " " + str.get(3));
                if(str.get(3).equals("NULL")){
                    space.setFloor_id(-1);
                }
                else{
                    space.setFloor_id(Integer.valueOf(str.get(3)));
                }
                if(str.get(4).equals("NULL")){
                    space.setBuilding_id(-1);
                }
                else{
                    space.setBuilding_id(Integer.valueOf(str.get(4)));
                }
                if(space.getSpace_type().equals("coverage")){
                    APID.put(space.getName(), id);
                }
                double lx, ly, rx, ry;
                lx = Double.valueOf(str.get(5));
                ly = Double.valueOf(str.get(6));
                rx = Double.valueOf(str.get(7));
                ry = Double.valueOf(str.get(8));
                space.setArea(Math.abs(lx-rx)*Math.abs(ly-ry));
                spaceIDIndex.put(space.getSpace_id(), id);
                id++;
                spaces.add(space);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*System.out.println(spaces.size());

        for(int i=0;i<spaces.size();i++){
            System.out.print(spaces.get(i).getSpace_id() + " " + spaces.get(i).getName() + " " + spaces.get(i).getSpace_type() + " " + spaces.get(i).getBuilding_id() + " " + spaces.get(i).getFloor_id() + " " + spaces.get(i).getArea());
            System.out.println("");
        }*/
    }

    public void readOverlappingRegion(){
        //read overlapping region from files
        //region_id,coverage_id,area
        String line = null;
        int id = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(overlappingRegionFile)))) {
            while ((line=br.readLine()) != null) {
                String[] array = line.split(",");
                List<String> str = Arrays.asList(array);
                overlappingRegion.put(Integer.valueOf(str.get(1)), new Pair(Integer.valueOf(str.get(0)), Double.valueOf(str.get(2))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*for(Map.Entry<Integer, Pair<Integer, Double>> entry : overlappingRegion.entrySet()){
            System.out.print(entry.getKey() + " " + entry.getValue().getKey() + " " + entry.getValue().getValue());
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

        //udpate active buildings
        if(APID.containsKey(ap)){//target ap
            int space_index = APID.get(ap);
            int building_id = spaces.get(space_index).getBuilding_id();
            if(!activeBuilding.containsKey(building_id)){
                activeBuilding.put(building_id, true);
            }

            if(!staticDevice.containsKey(clientMac)){//not a static device
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
        else{
            //System.out.println("Ap name in raw logs is inconsistent with metadata: " + ap);
        }

        currentTimestamp = evt.getTimestamp();
        timeElapsed = currentTimestamp.getTime() - lastTimestamp.getTime();//milliseconds = 0.001 second
        
        if(timeElapsed > interval*60*1000){
            computeOccupancy();
            //System.out.println("before clear Memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
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
            //filter out those inactive space
            if(!activeBuilding.containsKey(spaces.get(i).getBuilding_id())){
                continue;
            }

            //only deal with physical regions: type, "coverage"
            if(!spaces.get(i).getSpace_type().equals("coverage")) {
                continue;
            }
            OccupancyUnit occu = new OccupancyUnit();
            int count = 0;
            String ap = spaces.get(i).getName();
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
                //push up to semantic node
                MergeSemanticNode(i, slimList);
            }
            occu.setCount(count);
            occus.add(occu);
        }

        //for all semantic locations
        for(int i=0;i<spaces.size();i++){
            //filter out those inactive space
            if(!activeBuilding.containsKey(spaces.get(i).getBuilding_id())){
                continue;
            }
            if(spaces.get(i).getSpace_type().equals("region")){//ihe check name
                OccupancyUnit occu = new OccupancyUnit();
                int count = 0;
                String space_id = String.valueOf(spaces.get(i).getSpace_id());
                String ap = spaces.get(i).getName();
                occu.setApid(ap);
                occu.setSpaceid(spaces.get(i).getSpace_id());
                if(apEvents.containsKey(space_id)){
                    //slim list
                    Set<Integer> uniqueINT = new HashSet<Integer>(apEvents.get(space_id));
                    List<Integer> slimList = new ArrayList<>();
                    slimList.addAll(uniqueINT);
                    //get occupancy as the number of distinct macs
                    count = uniqueINT.size();
                }
                occu.setCount(count);
                occus.add(occu);
            }
        }

        //for all floors
        for(int i=0;i<spaces.size();i++){
            //filter out those inactive space
            if(!activeBuilding.containsKey(spaces.get(i).getBuilding_id())){
                continue;
            }
            if(spaces.get(i).getSpace_type().equals("floor")){
                OccupancyUnit occu = new OccupancyUnit();
                int count = 0;
                String space_id = String.valueOf(spaces.get(i).getSpace_id());
                String ap = spaces.get(i).getName();
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
            //filter out those inactive space
            if(!activeBuilding.containsKey(spaces.get(i).getBuilding_id())){
                continue;
            }
            if(spaces.get(i).getSpace_type().equals("building")){
                OccupancyUnit occu = new OccupancyUnit();
                int count = 0;
                String space_id = String.valueOf(spaces.get(i).getSpace_id());
                String ap = spaces.get(i).getName();
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

        System.out.println(occupancyOutput.getOccupancyArray().size());

        logger.debug("Try to send a occupancy output");
        System.out.println(occupancyOutput.getStartTimeStamp() + " " + occupancyOutput.getEndTimeStamp());
        for(int i=0;i<occupancyOutput.getOccupancyArray().size();i++){
            logger.debug("ap_id: " + occupancyOutput.getOccupancyArray().get(i).getApid() + " Count: " + occupancyOutput.getOccupancyArray().get(i).getCount());
        }
    }

    public void MergeFatherNode(int spaceIndex, List<Integer> slimArray){//merge slim array of current node with#spaceIndex to its father
        int fatherNode = -1;
        if(spaces.get(spaceIndex).getSpace_type().equals("coverage")){
            fatherNode = spaces.get(spaceIndex).getFloor_id();
        }
        else if(spaces.get(spaceIndex).getSpace_type().equals("floor")){
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

    public void MergeSemanticNode(int spaceIndex, List<Integer> slimArray){
        int space_id = spaces.get(spaceIndex).getSpace_id();
        if(overlappingRegion.containsKey(space_id)){
            int region_id = overlappingRegion.get(space_id).getKey();
            double overlapping_area = overlappingRegion.get(space_id).getValue();
            String region_name = String.valueOf(region_id);
            int region_index = spaceIDIndex.get(region_id);
            double area = spaces.get(region_index).getArea();
            System.out.println("region_id: " + region_id + " region area: "  + area + " physical region id: " + space_id + " overlapping area: " + overlapping_area);
            if((overlapping_area/area)>=0.5){
                if(!apEvents.containsKey(region_name)){
                    apEvents.put(region_name, slimArray);
                }
                else{
                    apEvents.get(region_name).addAll(slimArray);
                }
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
