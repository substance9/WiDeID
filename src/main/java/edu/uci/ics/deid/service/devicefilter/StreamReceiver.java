package edu.uci.ics.deid.service.devicefilter;

import org.apache.commons.io.input.Tailer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

@Component
public class StreamReceiver implements DisposableBean {
    @Value("${device_filter.stream_receiver.in_pipe_path}")
    private String inPipeFileName;

    private Thread thread;
    private volatile boolean running;
    private LogTailerListener logListener;

    @Autowired
    StreamLineRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public StreamReceiver(){

    }

    @Override
    public void destroy(){
        running = false;
    }

    @PostConstruct
    private  void postConstruct(){
        logListener = new LogTailerListener(recvQueue);
        try {
            File pipeInFile = new File(inPipeFileName);
            Tailer tailer = Tailer.create(pipeInFile, logListener, 300, true, true);
            this.thread = new Thread(tailer);
        } catch (Exception e) {
            logger.error("Cannot Open Data Source Pipe File");
            this.running = false;
        }

        this.running = true;
        this.thread.start();

    }

}