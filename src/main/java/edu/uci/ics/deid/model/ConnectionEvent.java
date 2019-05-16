package edu.uci.ics.deid.model;

import edu.uci.ics.deid.model.entity.Ap;

import java.sql.Timestamp;

public class ConnectionEvent {
    private Ap ap;
    private String clientId;
    private Timestamp timestamp;
    private String apId;
    private String type;

    public ConnectionEvent(RawConnectionEvent rawEvt, Ap ap, String clientID){
        this.ap = ap;
        this.clientId = clientID;
        this.timestamp = rawEvt.getTimestamp();
        this.apId = rawEvt.getApId();
        this.type = rawEvt.getType();
    }

    public Ap getAp() {
        return ap;
    }

    public void setAp(Ap ap) {
        this.ap = ap;
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
}
