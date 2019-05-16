package edu.uci.ics.deid.service.devicefilter.filter;

import edu.uci.ics.deid.model.RawConnectionEvent;

public abstract class Filter {
    
    public Filter() {
    }
    
    public abstract boolean isForwarding(RawConnectionEvent evt);

    public boolean isDiscarding(RawConnectionEvent evt){
        return !isForwarding(evt);
    }
 }