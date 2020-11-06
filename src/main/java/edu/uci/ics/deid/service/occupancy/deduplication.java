import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class deduplication {

    public static class rawObservation{
        String timeStamp;
        String sensor_id;
    }

    public static class Observations{
        List<rawObservation> Observation = new ArrayList<>();
    }

    public static class User{
        String mac;
        String name;
        String deviceType;
    }

    public static List<Observations> observations = new ArrayList<>();
    public static List<User> users = new ArrayList<>();
    public static List<String> devices = new ArrayList<>();

    public static void addUser(){
        addOneUser("9867312b6133ba7e9832f2ce3c74236ed4be16fc","Yiming","phone");
        addOneUser("254fa56b205b10b48aaeb13b9dd1345918dc26c4","Yiming","ipad");
        addOneUser("93285e85e8721d1ff8c18e89058821c91bd61848","Yiming","laptop");
        addOneUser("7CA5B3ECC4C6618B1106B4BEADF355AE7D4904FC","Guoxi","phone");
        addOneUser("E96712D11FDBE9F33A91BB8CCA82B646A25C18CC","Guoxi","ipad");
        addOneUser("5D3F5BE1FCA49381BE5EDB5BC70CA215427B46CD","Guoxi","laptop");
        addOneUser("B4E6D44092ECCC47316760A3E47A66C873D90A63","Georgios","phone");
        addOneUser("66F63D28B478AE92EACC5C69C74585410DC428D7","Georgios","laptop");
        addOneUser("AEF9AAA822BA2763A670AB4F85CCC79C4C939A6C","Daokun","phone");
        addOneUser("E0626596823B602A563A9965D8ABB5B6054E1D7B","Daokun","laptop");
        addOneUser("11D58FD604E31332D0E061F9E445058AFB453291","Dhrub","phone");
        addOneUser("5F0362599530166447F11E8D7312F0EC0E92D246","Dhrub","laptop");
        //addOneUser("6322444DFFBB2995C9ECBA4A2C27C06016940A70","Primal","phone");
        //addOneUser("C3AAD8CF6280621DA86C10880D8C694114C329D3","Primal","laptop");
        addOneUser("099E54FA8F092119A3C4913970B0E007876F790E","Peeyush","phone");
        addOneUser("42F6C414E5AA135E5289D8545DE2279E0B92C28F","Peeyush","laptop");
        addOneUser("258e22fb74c43539a1eb05ad681e991cbdd6711a","Roberto","phone");
        addOneUser("7C751EEE0895494F0D4BF3E19A0BA7EF000CB357","Roberto","laptop");
    }

    public static void addOneUser(String mac, String name, String deviceType){
        User user = new User();
        user.name = name;
        user.deviceType = deviceType;
        user.mac = mac;
        users.add(user);
    }

    public static void readFrequentDevices(int threshold){
        connect con = new connect("server");
        Connection Servercon = con.get();
        ResultSet rs;

        try {
            Statement stmt = Servercon.createStatement();
            String sql = String.format("select C.payload from\n" +
                    "(select  payload, count(*) as connections from OBSERVATION_CLEAN\n" +
                    "group by payload) as C\n" +
                    "where C.connections > %d", threshold);
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                devices.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.close();
        System.out.println(devices.size());
    }

    public static void readRawObservationsUsers(){
        for(int i=0;i<users.size();i++){
            readRawObservationUser(i);
        }
    }

    public static void readRawObservationsDevices(){
        for(int i=0;i<devices.size();i++){
            System.out.println(i);
            readRawObservationDevice(i);
        }
    }

    public static void readRawObservationUser(int id){
        connect con = new connect("server");
        Connection Servercon = con.get();
        ResultSet rs;
        Observations obs = new Observations();
        try {
            Statement stmt = Servercon.createStatement();
            String sql = String.format("select timeStamp, sensor_id from OBSERVATION_CLEAN where  payload = '%s' order by timeStamp;", users.get(id).mac);
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                rawObservation ob = new rawObservation();
                ob.timeStamp = rs.getString(1);
                ob.sensor_id = rs.getString(2);
                obs.Observation.add(ob);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        observations.add(obs);
        con.close();
    }

    public static void readRawObservationDevice(int id){
        connect con = new connect("server");
        Connection Servercon = con.get();
        ResultSet rs;
        Observations obs = new Observations();
        try {
            Statement stmt = Servercon.createStatement();
            String sql = String.format("select timeStamp, sensor_id from OBSERVATION_CLEAN where  payload = '%s' order by timeStamp;", devices.get(id));
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                rawObservation ob = new rawObservation();
                ob.timeStamp = rs.getString(1);
                ob.sensor_id = rs.getString(2);
                obs.Observation.add(ob);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        observations.add(obs);
        con.close();
    }

    public static int Difference(String fromTime, String toTime) {
        //return the difference between two time stamps
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int minutes = 0;
        try {
            long from = simpleFormat.parse(fromTime).getTime();
            long to = simpleFormat.parse(toTime).getTime();
            minutes = (int) ((to - from) / 1000 / 60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return minutes;
    }


    public static Double Similarity(int eps, int devicea, int deviceb){
        //ensure lena>lenb
        int pa=0, pb=0;
        double similarity = 0;
        int count = 0, dif;
        int lena= observations.get(devicea).Observation.size();
        int lenb = observations.get(deviceb).Observation.size();
        while(pa<lena){
            if(pb>=lenb){
                break;
            }
            dif = Difference(observations.get(deviceb).Observation.get(pb).timeStamp,observations.get(devicea).Observation.get(pa).timeStamp);//a-b
            //System.out.println(dif+ " " + pa + " " + pb);
            if(Math.abs(dif)<=eps){//if time difference between events from two devices are less than eps
                if(observations.get(devicea).Observation.get(pa).sensor_id.equals(observations.get(deviceb).Observation.get(pb).sensor_id)){//they are in same region
                    //and they are together: connect to same wifi ap, count them
                    count++;
                    pb++;
                }
                pa++;
            }else if(dif>eps){//a-b>10
                pb++;
                if(pb>=lenb){
                    break;
                }
                while(Difference(observations.get(deviceb).Observation.get(pb).timeStamp,observations.get(devicea).Observation.get(pa).timeStamp)>eps){
                    pb++;
                    if(pb>=lenb){
                        break;
                    }
                }
            }else if(Difference(observations.get(deviceb).Observation.get(pb).timeStamp,observations.get(devicea).Observation.get(pa).timeStamp)<-eps){
                pa++;
            }
        }
        //System.out.println(count);
        similarity = Double.valueOf(count)/Math.min(observations.get(devicea).Observation.size(),observations.get(deviceb).Observation.size());
        return similarity;
    }



}
