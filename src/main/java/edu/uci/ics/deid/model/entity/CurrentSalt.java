package edu.uci.ics.deid.model.entity;

import java.sql.Timestamp;
import java.util.Base64;

import edu.uci.ics.deid.model.MacAddress;

public class CurrentSalt {
    private MacAddress macAddress;
    private String saltStr;
    private byte[] salt;
    private Timestamp lastChangeTime;

    public CurrentSalt(){

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

    public Timestamp getLastChangeTime() {
        return lastChangeTime;
    }

    public void setLastChangeTime(Timestamp lastChangeTime) {
        this.lastChangeTime = lastChangeTime;
    }



}
