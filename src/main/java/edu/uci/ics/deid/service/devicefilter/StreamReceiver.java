package edu.uci.ics.deid.service.devicefilter;

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
public class StreamReceiver implements DisposableBean, Runnable {
    @Value("${device_filter.stream_receiver.in_pipe_path}")
    private String inPipeFileName;

    private Thread thread;
    private volatile boolean running;
    private static InputStream pipeIn;
    private static InputStreamReader streamReader;
    private static BufferedReader bufferReader;

    @Autowired
    StreamLineRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public StreamReceiver(){
        this.thread = new Thread(this);
    }

    private void createPipeReader(String pipeNameStr) throws Exception{
        File pipeInFile = new File(pipeNameStr);
        pipeIn = new FileInputStream(pipeInFile);
        streamReader = new InputStreamReader(pipeIn);
        bufferReader = new BufferedReader(streamReader);
    }

    @Override
    public void run() {
        String pipeName = inPipeFileName;
        do {
            try {
                TimeUnit.SECONDS.sleep(1);
                createPipeReader(pipeName);
            } catch (Exception e) {
                logger.error("Cannot Open Data Source Pipe File");
                this.running = false;
            }
        } while (!this.running);

        while (this.running) {
            try {
                String line = bufferReader.readLine();
                //logger.debug("Data Receiver Get Data: " + line);
                recvQueue.put(line);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy(){
        running = false;
    }

    @PostConstruct
    private  void init(){
        this.running = true;

        this.thread.start();

    }

}