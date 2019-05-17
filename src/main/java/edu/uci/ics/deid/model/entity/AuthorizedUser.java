package edu.uci.ics.deid.model.entity;


public class AuthorizedUser {
    private String ucinetid;
    private String name;
    private String department;

    /**
     * @return the ucinetid
     */
    public String getUcinetid() {
        return ucinetid;
    }

    /**
     * @param ucinetid the ucinetid to set
     */
    public void setUcinetid(String ucinetid) {
        this.ucinetid = ucinetid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

}
