package edu.uci.ics.deid.model;

import java.sql.Timestamp;

public class HistoryEntry {
    private String encryptedMac;
    private String encryptedId;
    private Timestamp startTime;
    private Timestamp endTime;

    public String getEncryptedMac() {
        return encryptedMac;
    }

    public void setEncryptedMac(String encryptedMac) {
        this.encryptedMac = encryptedMac;
    }

    public String getEncryptedId() {
        return encryptedId;
    }

    public void setEncryptedId(String encryptedId) {
        this.encryptedId = encryptedId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
