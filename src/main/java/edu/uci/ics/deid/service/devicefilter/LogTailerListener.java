package edu.uci.ics.deid.service.devicefilter;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTailerListener extends TailerListenerAdapter {
    StreamLineRecvQueue lineRecvQueue;

    public LogTailerListener(StreamLineRecvQueue lineRecvQueue) {
        this.lineRecvQueue = lineRecvQueue;
    }

    public void handle(String line){
        if(line != null){
            if (line.contains("14179.2.6.3.53") || line.contains("14179.2.6.3.1\t")) {
                lineRecvQueue.put(line);
            }
        }
    }

}