package edu.uci.ics.deid.model;

import java.util.List;

public class Occupancy {
    List<OccupancyUnit> occupancyArray;
    String timeStamp;

    public List<OccupancyUnit> getOccupancyArray() {
        return occupancyArray;
    }

    public void setOccupancyArray(List<OccupancyUnit> occupancyArray) {
        this.occupancyArray = occupancyArray;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}