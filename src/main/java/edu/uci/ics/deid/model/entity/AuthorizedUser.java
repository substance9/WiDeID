package edu.uci.ics.deid.model.entity;


public class AuthorizedUser {
    private String uciNetId;
    private String name;

    /**
     * @return the uciNetId
     */
    public String getUciNetId() {
        return uciNetId;
    }

    /**
     * @param uciNetId the uciNetId to set
     */
    public void setUciNetId(String uciNetId) {
        this.uciNetId = uciNetId;
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

    
}
