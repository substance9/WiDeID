package edu.uci.ics.deid.model;

import java.sql.Timestamp;
import java.util.List;

public class Occupancy {
    List<OccupancyUnit> occupancyArray;
    Timestamp startTimeStamp;
    Timestamp endTimeStamp;

    public getStartTimeStamp() {
        return this.startTimeStamp;
    }

    public void setStartTimeStamp(Timestamp startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public getEndTimeStamp() {
        return this.endTimeStamp;
    }

    public void setEndTimeStamp(Timestamp endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public List<OccupancyUnit> getOccupancyArray() {
        return occupancyArray;
    }

    public void setOccupancyArray(List<OccupancyUnit> occupancyArray) {
        this.occupancyArray = occupancyArray;
    }

}