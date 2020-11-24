package edu.uci.ics.deid.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Epoch {
    public long epochDurationInMs;
    public SimpleDateFormat sdf;

    public Epoch(int epochDurationInMinutes) {
        this.epochDurationInMs = epochDurationInMinutes * 60 * 1000;
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public Epoch(long epochDurationInMs) {
        this.epochDurationInMs = epochDurationInMs;
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public  long getEpochIdByMs(long timeMs) {
        return timeMs - (timeMs % (epochDurationInMs));
    }

    public  String getEpochIdStrByMs(long timeMs) {
        long epochId = getEpochIdByMs(timeMs);
        Timestamp epochTs = new Timestamp(epochId);
        return epochTs.toString();
    }

    public  long getEpochIdByTimeStr(String timeStr) throws ParseException {
        Date date = sdf.parse(timeStr);
        long timeMs = date.getTime();
        return getEpochIdByMs(timeMs);
    }

    public  String getEpochIdStrByTimeStr(String timeStr) throws ParseException {
        long epochId = getEpochIdByTimeStr(timeStr);
        Timestamp epochTs = new Timestamp(epochId);
        return epochTs.toString();
    }

    public long parseTimeStrToMs(String timeStr) throws ParseException {
        Date date = sdf.parse(timeStr);
        long timeMs = date.getTime();
        return timeMs;
    }

    public Timestamp parseTimeStrToTs(String timeStr) throws ParseException {
        Date date = sdf.parse(timeStr);
        long timeMs = date.getTime();
        Timestamp ts = new Timestamp(timeMs);
        return ts;
    }

    public long getNextEpoch(long epochId){
        //just make sure the input is an epoch Id, otherwise converts to the corresponding epoch Id
        long realEpochId = getEpochIdByMs(epochId);

        return realEpochId + epochDurationInMs;
    }

    public boolean isInEpochRange(long timeMs, long epochId){
        if (timeMs >= epochId && timeMs < (epochId + epochDurationInMs)){
            return true;
        }
        else{
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        int epochMinutes = 15;
        Epoch epoch = new Epoch(epochMinutes);

        String testTimeStr = "2018-10-01 00:00:00.000000";
        long testTimeMs = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
        try {
            Date date = sdf.parse(testTimeStr);
            testTimeMs = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Timestamp ts = new Timestamp(testTimeMs);

        System.out.println("time parsed to string: " + ts.toString());
        System.out.println("epoch ID: " + String.valueOf(epoch.getEpochIdByMs(testTimeMs)));
        System.out.println("epoch ID Str: " + epoch.getEpochIdStrByMs(testTimeMs));
    }
}
