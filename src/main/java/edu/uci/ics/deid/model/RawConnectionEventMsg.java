package edu.uci.ics.deid.model;

public class RawConnectionEventMsg {

    private String apId;//AP name
    private long apMac;
    private long clientMac;
    private long timestamp;
    private String type;

    public RawConnectionEventMsg(RawConnectionEvent rawEvt) {
        apId = rawEvt.getApId();
        apMac = rawEvt.getApMac().getMacAddrLong();
        clientMac = rawEvt.getClientMac().getMacAddrLong();
        timestamp = rawEvt.getTimestamp().getTime();
        type = rawEvt.getType();
    }

    public RawConnectionEventMsg() {
        
    }

    public String getApId() {
        return apId;
    }

    public void setApId(String apId) {
        this.apId = apId;
    }

    public long getApMac() {
        return apMac;
    }

    public void setApMac(long apMac) {
        this.apMac = apMac;
    }

    public long getClientMac() {
        return clientMac;
    }


    public void setClientMac(long clientMac) {
        this.clientMac = clientMac;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
