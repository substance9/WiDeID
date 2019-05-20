package edu.uci.ics.deid.service.dispatcher;

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

import edu.uci.ics.deid.model.DeidConnectionEvent;
import edu.uci.ics.deid.model.DeidConnectionEventMsg;

@Component
public class DeidConnectionEventReceiver implements DisposableBean, Runnable {

    private Thread thread;
    private volatile boolean running;

    private ZContext context;
    private ZMQ.Socket subscriber;

    @Autowired
    DeidConnectionEventRecvQueue recvQueue;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DeidConnectionEventReceiver() {
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        DeidConnectionEvent evt = null;
        DeidConnectionEventMsg evtMsg = null;
        String msg = null;

        subscriber.connect("tcp://127.0.0.1:5681");

        while (this.running) {
            msg = new String(subscriber.recv(0));

            try {
                evtMsg = mapper.readValue(msg, DeidConnectionEventMsg.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            evt = new DeidConnectionEvent(evtMsg);

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