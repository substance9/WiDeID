package edu.uci.ics.deid.service.devicefilter;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.model.RawConnectionEventMsg;

@Component
public class RawEventSender implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running;

    private ZContext context;
    private ZMQ.Socket publisher;

    @Autowired
    RawEventSendQueue sendQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RawEventSender() {
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        RawConnectionEvent evt = null;
        RawConnectionEventMsg evtMsg = null;
        String msg = null;

        while (this.running) {
            try {
                evt = sendQueue.take();
            } catch (Exception e) {
                e.printStackTrace();
            }

            evtMsg = new RawConnectionEventMsg(evt);

            try {
                msg = mapper.writeValueAsString(evtMsg);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            logger.debug(msg);

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
        publisher.bind("tcp://*:5680");
        this.running = true;

        this.thread.start();

    }

}