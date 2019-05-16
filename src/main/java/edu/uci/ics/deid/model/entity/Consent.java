package edu.uci.ics.deid.model.entity;

import edu.uci.ics.deid.model.MacAddress;

public class Consent {
    private int id;
    private int serviceId;
    private MacAddress macAddress;
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceID) {
        this.serviceId = serviceID;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }


}
