package edu.uci.ics.deid.service.occupancy;

import javax.xml.crypto.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class occupancy {
    public static void main(String[] args) {
        //clustering.readEdge();
        //clustering.clusterMerge();
        //clustering.writeToFile();
        List<String> sequentialMacs = new ArrayList<>();
        streamingOccupancy.readGraph();
        streamingOccupancy.clusterAnslysis();
    }

    public static void test_deduplication(){
        //note: device with larger connections first
        deduplication.addUser();
        deduplication.readRawObservationsUsers();

        for(int i=0;i<deduplication.users.size();i++){
            for(int j=i+1;j<deduplication.users.size();j++){

                //G.add_edge(L[1],L[2], weight= 0.3)
                String da = deduplication.users.get(i).name+"."+deduplication.users.get(i).deviceType;
                String db = deduplication.users.get(j).name +"."+ deduplication.users.get(j).deviceType;
                String a = String.format("G.add_edge('%s','%s',",da,db);
                String b;
                double value = 0;
                if(deduplication.observations.get(i).Observation.size()>deduplication.observations.get(j).Observation.size()){
                    b= String.format("length= %f)",deduplication.Similarity(10,i,j));
                    value = deduplication.Similarity(10,i,j);
                }
                else{
                    b= String.format("length= %f)",deduplication.Similarity(10,j,i));
                    value = deduplication.Similarity(10,j,i);
                }
                String c=a+b;
                if(value>=0.2){
                    System.out.println(c);
                }

            }
        }
    }

    public static void test_occupancy(int threshold){
        deduplication.readFrequentDevices(threshold);
        deduplication.readRawObservationsDevices();

        String outputFilePath = "/Users/linyiming/eclipse-workspace/occupancy/DeviceKnowledgeGraph.txt";
        try (PrintWriter logger = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath, true)))) {

        //print all devices(nodes) in the graph
        for(int i=0;i<deduplication.devices.size();i++){
            logger.println(deduplication.devices.get(i));
        }

        //print all edges in the graph
        for(int i=0;i<deduplication.devices.size();i++){
            if(i%500==0){
                System.out.println(i);
            }
            logger.println("#" + i);
            for(int j=i+1;j<deduplication.devices.size();j++){
                String da = String.valueOf(i);
                String db = String.valueOf(j);
                String a = String.format("G.add_edge('%s','%s',",da,db);
                String b;
                double value = 0;
                if(deduplication.observations.get(i).Observation.size()>deduplication.observations.get(j).Observation.size()){
                    b= String.format("length= %f)",deduplication.Similarity(10,i,j));
                    value = deduplication.Similarity(10,i,j);
                }
                else{
                    b= String.format("length= %f)",deduplication.Similarity(10,j,i));
                    value = deduplication.Similarity(10,j,i);
                }

                logger.println(i+ " " + j + " " + value);

                String c=a+b;
                if(value>=0.3){
                    System.out.println(c);
                }
            }
        }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
