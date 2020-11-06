import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.*;
import javafx.util.Pair;

public class streamingOccupancy {

    public static class EDGE {
        int start;
        int end;
        double sim;
    }

    public static class CLUSTER{
        List<Integer> cluster = new ArrayList<>();
    }

    public static List<EDGE> edges = new ArrayList<>();
    public static List<String> macs = new ArrayList<>();
    public static List<Integer> labels = new ArrayList<>();
    public static Map<String, Integer> hm = new HashMap<String, Integer>();
    public static Map<Pair<Integer, Integer>, Double> map = new HashMap<Pair<Integer, Integer>, Double>();//map the similarity to edge, for test purpose



    public static void readGraph(){
        try {
            FileReader fin = new FileReader("denseGraph.txt");
            Scanner src = new Scanner(fin);
            FileReader finLabel = new FileReader("clusterMergeLabel.txt");
            Scanner srcL = new Scanner(finLabel);
            String line;
            //read nodes
            while(src.hasNext()){
                line = src.next();
                if(line.equals("#")){
                    break;
                }
                macs.add(line);
            }
            //read edges
            while(src.hasNext()){
                EDGE edge = new EDGE();
                edge.start = src.nextInt();
                edge.end = src.nextInt();
                edge.sim = src.nextDouble();
                Pair<Integer, Integer> pair = new Pair<>(edge.start, edge.end);
                map.put(pair,edge.sim);
                edges.add(edge);
            }
            //read cluster label
            while(srcL.hasNext()){
                labels.add(srcL.nextInt());
            }
            fin.close();
            finLabel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //create map
        for(int i=0;i<macs.size();i++){
            hm.put(macs.get(i),labels.get(i));
        }
    }

    public static int findCluster(String macAddress){
        return hm.get(macAddress);
    }

    public static int computeOccupancy(List<String> sequentialMacs){
        int occupancy = 0;
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for(int i=0;i<sequentialMacs.size();i++){
            int id = findCluster(sequentialMacs.get(i));
            if(!map.containsKey(id)){
                map.put(id,occupancy++);
            }
        }
        return occupancy;
    }

    public static void clusterAnslysis(){
        List<CLUSTER> clusters = new ArrayList<>();
        int label;
        //initial clusters
        for(int i=0;i<labels.size();i++){
            CLUSTER cluster = new CLUSTER();
            cluster.cluster.add(0);
            clusters.add(cluster);
        }
        //load cluster
        for(int i=0;i<labels.size();i++){
            label = labels.get(i);
            clusters.get(label).cluster.add(i);
        }
        //cluster analysis
        int clustercount = 0;
        int bigclustercount = 0;
        for(int i=0;i<clusters.size();i++){
            if(clusters.get(i).cluster.size()==1) continue;
            clustercount++;
            if(clusters.get(i).cluster.size()>2) bigclustercount++;
            System.out.println("cluster: " + i);
            for(int j=1;j<clusters.get(i).cluster.size();j++){
                System.out.print(clusters.get(i).cluster.get(j) + " ");
            }
            System.out.println("");
        }
        System.out.println(clustercount + " " + bigclustercount);
    }

}