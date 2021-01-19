package edu.uci.ics.deid.model;

public class OccupancyUnit{
    String Apid;
    int Spaceid;
    int count;

    public int getSpaceid(){
        return this.Spaceid;
    }

    public void setSpaceid(int Spaceid){
        this.Spaceid = Spaceid;
    }
    
    public String getApid() {
        return this.Apid;
    }

    public void setApid(String apid) {
        this.Apid = apid;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}