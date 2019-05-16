package edu.uci.ics.deid.model.entity;

import java.sql.Timestamp;
import java.util.Base64;

import edu.uci.ics.deid.model.MacAddress;

public class HistoricalSalt {
    private MacAddress macAddress;
    private String saltStr;
    private byte[] salt;
    private Timestamp startTime;
    private Timestamp endTime;

    public HistoricalSalt() {

    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public MacAddress getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    public String getSaltStr() {
        return saltStr;
    }

    public void setSaltStr(String currentSaltStr) {
        this.saltStr = currentSaltStr;
        this.salt = Base64.getDecoder().decode(currentSaltStr);
    }

    public void setSalt(byte[] currentSalt) {
        this.salt = currentSalt;
        this.saltStr = Base64.getEncoder().encodeToString(currentSalt);
    }

    public byte[] getSalt() {
        return salt;
    }



}
