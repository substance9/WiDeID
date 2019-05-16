package edu.uci.ics.deid.model;

public class DeidConnectionEventMsg {
    private long apMac;
    private String clientId;
    private long timestamp;
    private String apId;
    private String type;

    public DeidConnectionEventMsg(DeidConnectionEvent deidConnEvt){
        this.clientId = deidConnEvt.getClientId();
        this.timestamp = deidConnEvt.getTimestamp().getTime();
        this.apMac = deidConnEvt.getApMac().getMacAddrLong();
        this.apId = deidConnEvt.getApId();
        this.type = deidConnEvt.getType();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
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

    public long getApMac() {
        return apMac;
    }

    public void setApMac(long apMac) {
        this.apMac = apMac;
    }
}
