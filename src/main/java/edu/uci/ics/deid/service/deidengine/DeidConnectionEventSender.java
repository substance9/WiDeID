package edu.uci.ics.deid.service.deidengine;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.DeidConnectionEvent;
import edu.uci.ics.deid.model.DeidConnectionEventMsg;

@Component
public class DeidConnectionEventSender implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running;

    private ZContext context;
    private ZMQ.Socket publisher;

    @Autowired
    DeidConnectionEventSendQueue sendQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeidConnectionEventSender() {
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        DeidConnectionEvent evt = null;
        DeidConnectionEventMsg evtMsg = null;
        String msg = null;

        while (this.running) {
            try {
                evt = sendQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }

            evtMsg = new DeidConnectionEventMsg(evt);

            try {
                msg = mapper.writeValueAsString(evtMsg);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            //logger.debug(msg);

            publisher.send(msg.getBytes(ZMQ.CHARSET), 0);
        }
    }

    @Override
    public void destroy(){
        running = false;
    }

    @PostConstruct
    private  void init(){
        context = new ZContext();
        publisher = context.createSocket(ZMQ.PUB);
        publisher.bind("tcp://*:5681");
        this.running = true;

        this.thread.start();

    }

}