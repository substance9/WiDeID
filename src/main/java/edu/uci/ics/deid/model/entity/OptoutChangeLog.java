package edu.uci.ics.deid.model.entity;

import java.sql.Timestamp;

import edu.uci.ics.deid.model.MacAddress;

public class OptoutChangeLog {
    private MacAddress devMac;
    private String operator;
    private Timestamp time;
    private String action;

    /**
     * @return the devMac
     */
    public MacAddress getDevMac() {
        return devMac;
    }

    /**
     * @param devMac the devMac to set
     */
    public void setDevMac(MacAddress devMac) {
        this.devMac = devMac;
    }

    /**
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return the time
     */
    public Timestamp getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void setTimeLong(Long timeLong) {
        time = new Timestamp(timeLong);
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }


}
