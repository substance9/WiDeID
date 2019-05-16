package edu.uci.ics.deid.model;

import java.sql.Timestamp;

public class DeidConnectionEvent {
    private MacAddress apMac;
    private String clientId;
    private Timestamp timestamp;
    private String apId;
    private String type;

    public DeidConnectionEvent(RawConnectionEvent rawEvt, String clientID){
        this.clientId = clientID;
        this.timestamp = rawEvt.getTimestamp();
        this.apMac = rawEvt.getApMac();
        this.apId = rawEvt.getApId();
        this.type = rawEvt.getType();
    }

    public DeidConnectionEvent(DeidConnectionEventMsg evtMsg) {
        apId = evtMsg.getApId();
        apMac = new MacAddress(evtMsg.getApMac());
        clientId = evtMsg.getClientId();
        timestamp = new Timestamp(evtMsg.getTimestamp());
        type = evtMsg.getType();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getApId() {
        return apId;
    }

    public void setApId(String apId) {
        this.apId = apId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MacAddress getApMac() {
        return apMac;
    }

    public void setApMac(MacAddress apMac) {
        this.apMac = apMac;
    }
}
