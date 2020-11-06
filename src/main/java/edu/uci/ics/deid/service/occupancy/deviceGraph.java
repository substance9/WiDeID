import java.io.*;
import java.util.*;
import java.io.FileWriter;
import java.io.PrintWriter;

public class deviceGraph {

    public static class NODE {
        int id;//id of mac address of device
        List<Integer> neighbors = new ArrayList<>();
        List<Double> similarities = new ArrayList<>();
    }

    public static class EDGE {
        int start;
        int end;
        double sim;
    }

    public static class sortedge implements Comparator<EDGE> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(EDGE a, EDGE b) {
            return Double.compare(a.sim, b.sim);
        }
    }

    public static List<String> macs = new ArrayList<>();
    public static List<NODE> nodes = new ArrayList<>();
    public static List<NODE> completeNodes = new ArrayList<>();
    public static List<EDGE> edges = new ArrayList<>();

    public static void buildGraph(double SimThreshold) {//threshold for similarity
        try {
            FileReader fin = new FileReader("NewDeviceKnowledgeGraph.txt");
            Scanner src = new Scanner(fin);
            String line;
            int a = 0, b = 0;
            Double sim;
            int first = 0;
            while (src.hasNext()) {
                line = src.next();
                if (line.substring(0, 1).equals("#")) {
                    break;
                } else {
                    macs.add(line);
                }
            }

            int flag = 0;
            //read similarity
            while (src.hasNext()) {
                if (flag == 0) {
                    a = src.nextInt();
                    b = src.nextInt();
                    sim = src.nextDouble();
                }
                if (a == -1 && b == -1) {//start a new node
                    NODE node = new NODE();
                    while (src.hasNext()) {
                        a = src.nextInt();
                        b = src.nextInt();
                        sim = src.nextDouble();
                        //System.out.println(a + " " + b + " " + sim);
                        if (a == -1 && b == -1) {
                            nodes.add(node);
                            flag = 1;
                            break;
                        }
                        if (sim >= SimThreshold) {
                            enrichNode(a, b, sim, node);
                        }
                    }
                }
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enrichNode(int a, int b, double sim, NODE node) {
        node.id = a;
        node.neighbors.add(b);
        node.similarities.add(sim);
    }

    public static void cleanFile() {
        File file = new File("DeviceKnowledgeGraph.txt");
        File fileNew = new File("NewDeviceKnowledgeGraph.txt");
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            BufferedWriter out = new BufferedWriter(new FileWriter(fileNew));

            String line;
            int flag = 0;
            while ((line = in.readLine()) != null) {
                if (line.substring(0, 1).equals("#")) {
                    if (flag == 0) {//first time #
                        flag = 1;
                        out.write("#");
                        out.newLine();
                        out.write("-1 -1 0");
                        out.newLine();
                    } else {
                        out.write("-1 -1 ");
                        out.write(line.substring(1, 2));
                        out.newLine();
                    }
                } else if (flag == 0) {//mac addresses
                    out.write(line);
                    out.newLine();
                } else {//similarity
                    out.write(line);
                    out.newLine();
                }
            }
            out.flush();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void graph_statistics() {
        int clusterSize = 0;
        int maxclusterSize = 0;
        for (int i = 0; i < nodes.size(); i++) {
            clusterSize += nodes.get(i).neighbors.size();
            if (nodes.get(i).neighbors.size() > maxclusterSize) {
                maxclusterSize = nodes.get(i).neighbors.size();
            }
        }
        System.out.println(Double.valueOf(clusterSize) / Double.valueOf(nodes.size()) + " " + maxclusterSize);
    }

    public static void generateDenseGraphFile() {

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.get(i).neighbors.size(); j++) {
                EDGE edge = new EDGE();
                edge.start = i;
                edge.end = nodes.get(i).neighbors.get(j);
                edge.sim = 1.0 - nodes.get(i).similarities.get(j);
                edges.add(edge);
            }
        }
        Collections.sort(edges, new sortedge());
        System.out.println(edges.size());

        String outputFilePath = "/Users/linyiming/eclipse-workspace/occupancy/denseGraph.txt";


        try (PrintWriter logger = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath, true)))) {
            //output node
            //System.out.println(macs.size() + " " + nodes.size());
            for(int i=0;i<macs.size();i++){
                logger.println(macs.get(i));
            }
            logger.println("#");//end of the nodes
            //output edges
            for (int i = 0; i < edges.size(); i++) {
                logger.println(edges.get(i).start + " " + edges.get(i).end + " " + edges.get(i).sim);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateEdgeFile(){

    }

    public static void readCluster(){
        try {
            FileReader fin = new FileReader("clusterLabel.txt");
            Scanner src = new Scanner(fin);



            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
