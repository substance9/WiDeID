package edu.uci.ics.deid.model;

import java.sql.Timestamp;
//import java.text.SimpleDateFormat;

public class RawConnectionEvent {
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private String apId;
    private MacAddress apMac;
    private MacAddress clientMac;
    private Timestamp timestamp;
    private String type;

    public RawConnectionEvent() {
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public RawConnectionEvent(RawConnectionEventMsg evtMsg) {
        apId = evtMsg.getApId();
        apMac = new MacAddress(evtMsg.getApMac());
        clientMac = new MacAddress(evtMsg.getClientMac());
        timestamp = new Timestamp(evtMsg.getTimestamp());
        type = evtMsg.getType();
    }

    public String getApId() {
        return apId;
    }

    public void setApId(String apId) {
        this.apId = apId;
    }

    public MacAddress getApMac() {
        return apMac;
    }

    public void setApMacWithStr(String apMacStr) {
        this.apMac = new MacAddress(apMacStr);
    }

    public void setApMac(MacAddress apMac) {
        this.apMac = apMac;
    }

    public MacAddress getClientMac() {
        return clientMac;
    }

    public void setClientMacWithStr(String clientMacStr) {
        this.clientMac = new MacAddress(clientMacStr);
    }

    public void setClientMac(MacAddress clientMac) {
        this.clientMac = clientMac;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
