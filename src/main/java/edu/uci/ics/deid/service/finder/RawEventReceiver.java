package edu.uci.ics.deid.service.finder;

import java.io.IOException;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import edu.uci.ics.deid.model.RawConnectionEvent;
import edu.uci.ics.deid.model.RawConnectionEventMsg;

@Component
public class RawEventReceiver implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running;

    private ZContext context;
    private ZMQ.Socket subscriber;

    @Autowired
    RawEventRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RawEventReceiver() {
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        RawConnectionEvent evt = null;
        RawConnectionEventMsg evtMsg = null;
        String msg = null;

        subscriber.connect("tcp://127.0.0.1:5680");

        while (this.running) {
            msg = new String(subscriber.recv(0));

            try {
                evtMsg = mapper.readValue(msg, RawConnectionEventMsg.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            evt = new RawConnectionEvent(evtMsg);

            try {
                recvQueue.put(evt);
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
        context = new ZContext();
        subscriber = context.createSocket(ZMQ.SUB);
        subscriber.subscribe("".getBytes(ZMQ.CHARSET));
        
        this.running = true;

        this.thread.start();

    }

}