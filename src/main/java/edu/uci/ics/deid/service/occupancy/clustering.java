/*
input: pair-wise similarity from denseSimPair
output: cluster label for each node to clusterMergeLabel
 */

import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.*;

public class clustering {

    public static class EDGE {
        int start;
        int end;
        double sim;
    }
    public static List<streamingOccupancy.EDGE> edges = new ArrayList<>();
    public static List<Integer> clusterSize = new ArrayList<>();//maintain the size of each cluster
    public static int maxSize = 3;//max size of a cluster
    public static int maxN = 2014;//max ID of node
    public static int cluster_id = 0;
    public static Map<Integer, Integer> map = new HashMap<Integer, Integer>();

    public static void readEdge(){
        try {
            FileReader fin = new FileReader("denseSimPair.txt");
            Scanner src = new Scanner(fin);

            while(src.hasNext()){
                streamingOccupancy.EDGE edge = new streamingOccupancy.EDGE();
                edge.start = src.nextInt();
                edge.end = src.nextInt();
                edge.sim = src.nextDouble();
                edges.add(edge);
            }

            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clusterMerge(){
        int start, end, id;
        for(int i=edges.size()-1;i>=0;i--){//desc order
            start = edges.get(i).start;
            end = edges.get(i).end;
            if(!map.containsKey(start) && !map.containsKey(end)){//two end points do not belong to any cluster
                map.put(start,cluster_id);
                map.put(end,cluster_id);
                clusterSize.add(2);
                cluster_id++;
            }
            else if(map.containsKey(start) && !map.containsKey(end)){
                id = map.get(start);
                if(clusterSize.get(id)+1<=maxSize){
                    map.put(end,id);
                    clusterSize.set(id,clusterSize.get(id)+1);
                }
            }
            else if(!map.containsKey(start) && map.containsKey(end)){
                id = map.get(end);
                if(clusterSize.get(id)+1<=maxSize){
                    map.put(start,id);
                    clusterSize.set(id,clusterSize.get(id)+1);
                }
            }
        }
    }

    public static void writeToFile(){
        int singletonID = cluster_id;//singleton starts after cluster_id
        String outputFilePath = "/Users/linyiming/eclipse-workspace/occupancy/clusterMergeLabel.txt";
        try (PrintWriter logger = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath, true)))) {
            for(int i=0;i<maxN;i++){
                if(map.containsKey(i)){
                    logger.println(map.get(i));
                }
                else{
                    logger.println(singletonID++);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
