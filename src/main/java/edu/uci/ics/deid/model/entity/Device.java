package edu.uci.ics.deid.model.entity;

import edu.uci.ics.deid.model.MacAddress;

public class Device {
    private MacAddress macAddress;
    private String name;

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}
