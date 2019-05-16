package edu.uci.ics.deid.model;

import java.sql.Timestamp;

public class DeviceFilterLog {

    private MacAddress clientMac;
    private Timestamp timestamp;
    private String reasons;

    public DeviceFilterLog(RawConnectionEvent rawEvt, String reasons) {
        clientMac = rawEvt.getClientMac();
        this.timestamp = rawEvt.getTimestamp();
        this.reasons = reasons;
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

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the reasons
     */
    public String getReasons() {
        return reasons;
    }

    /**
	 * @param reasons the reasons to set
	 */
	public void setReasons(String reasons) {
		this.reasons = reasons;
    }
}

