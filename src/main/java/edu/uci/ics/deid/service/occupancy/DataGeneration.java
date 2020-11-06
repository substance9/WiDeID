package edu.uci.ics.deid.service.occupancy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;

public class DataGeneration {
    public static class rawOccupancy{
        String timeStamp;
        int occupancy;
    }

    public static class rawObservation{
        String startTime;
        String endTime;
        int count;
    }

    public static List<rawOccupancy> occupancyList = new ArrayList<>();
    public static List<rawOccupancy> geOccupancy = new ArrayList<>();

    public static List<rawObservation> geObservation = new ArrayList<>();

    public static void readRawOccupancy(String startTime, String endTime, String sensor_id){
        connect con = new connect("local");
        Connection Servercon = con.get();
        ResultSet rs;
        try {
            Statement stmt = Servercon.createStatement();
            String sql = String.format("select timeStamp, occupancy from occupancy where timeStamp>='%s' and timeStamp <='%s' and sensor_id = '%s' order by timeStamp;", startTime, endTime, sensor_id);
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                rawOccupancy oc = new rawOccupancy();
                oc.timeStamp = rs.getString(1);
                oc.occupancy = rs.getInt(2);
                occupancyList.add(oc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.close();
    }

    public static void generateGroudtruth(String startTime, String endTime, String sensor_id, int increment){
        readRawOccupancy(startTime, endTime, sensor_id);
        String now = startTime;
        int cr = 0;
        while(now.compareTo(endTime)<0){
            rawOccupancy oc = new rawOccupancy();
            while(now.compareTo(occupancyList.get(cr).timeStamp)>0){
                if(cr == occupancyList.size()-1) break;
                cr ++;
            }
            if(cr == occupancyList.size()-1 || cr == 0){
                oc.occupancy = 0;
                oc.timeStamp = now;
            }else{
                oc.occupancy = occupancyList.get(cr-1).occupancy;
                oc.timeStamp = now;
            }
            geOccupancy.add(oc);
            now = getTimeSlot(now, increment);
        }
    }

    public static String getTimeSlot(String timeStamp, int increment) {
    //return timeStamp + increment(minutes)
        Date clock = new Date();
        SimpleDateFormat dataformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            clock = dataformat.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(clock);
        calendar.add(Calendar.MINUTE, increment);
        Date m = calendar.getTime();
        String time = dataformat.format(m);
        return time;
    }

    public static void generateObservationCount(String startTime, String endTime, String sensor_id, int increment){
        String now = startTime;
        while(now.compareTo(endTime)<=0){
            rawObservation ob = new rawObservation();
            ob.startTime = getTimeSlot(now,increment*(-1));
            ob.endTime = getTimeSlot(now, increment);
            ob.count = ObservationCount(ob.startTime, ob.endTime, sensor_id);
            now = ob.endTime;
            geObservation.add(ob);
        }
    }

    public static int ObservationCount(String startTime, String endTime, String sensor_id){
        connect con = new connect("server");
        Connection Servercon = con.get();
        ResultSet rs;
        int count= 0;
        try {
            Statement stmt = Servercon.createStatement();
            String sql = String.format("select count(distinct payload) from OBSERVATION \n" +
                    "where timeStamp>='%s' and timeStamp<='%s' \n" +
                    "and sensor_id = '%s'", startTime, endTime, sensor_id);
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.close();
        return count;
    }

    public static void printlist(String startTime, String endTime, String sensor_id){

        System.out.println(geOccupancy.size() + " " + geObservation.size());
        String occufile = "occupancy"+startTime.substring(0,10)+endTime.substring(0,10)+sensor_id+".txt";
        System.out.println(occufile);
        File file_occu = new File(occufile);
        try {
            FileWriter out = new FileWriter(file_occu);
            BufferedWriter bw=new BufferedWriter(out);
            for(int i=0;i<geOccupancy.size()-1;i++){
                bw.write(String.valueOf(geOccupancy.get(i).occupancy+1));
                bw.write(",");
            }
            bw.write(String.valueOf(geOccupancy.get(geOccupancy.size()-1).occupancy+1));
            bw.write("\n");
            bw.flush();
            bw.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        String obfile = "observation"+startTime.substring(0,10)+endTime.substring(0,10)+sensor_id+".txt";
        File file_observation = new File(obfile);
        try {
            FileWriter out = new FileWriter(file_observation);
            BufferedWriter bw=new BufferedWriter(out);
            for(int i=0;i<geObservation.size()-2;i++){
                bw.write(String.valueOf(geObservation.get(i).count+1));
                bw.write(",");
            }
            bw.write(String.valueOf(geOccupancy.get(geOccupancy.size()-2).occupancy+1));
            bw.write("\n");
            bw.flush();
            bw.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        /*System.out.println("");
        for(int i=0;i<geObservation.size()-1;i++){
            System.out.print((geObservation.get(i).count+1)+",");
        }*/
    }

    public static void statistics(int difference, String startTime, String endTime){
        //count the difference between connections and occupancy
        String outputFilePath = "/Users/linyiming/eclipse-workspace/occupancy/log.txt";
        try (PrintWriter logger = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath, true)))) {
            int count = 0;
            logger.println(startTime + " "  + endTime);
            for(int i=0;i<geOccupancy.size();i++){
                if(Math.abs(geObservation.get(i).count-geOccupancy.get(i).occupancy)<=difference){
                    count++;
                }
            }
            logger.println(geOccupancy.size() + " " + Double.valueOf(count)/Double.valueOf(geOccupancy.size())+ " " + difference);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
