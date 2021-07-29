package com.ntw.oms.order.queue;

import com.ntw.oms.order.processor.OrderProcessor;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class LocalMQ implements MQClient, MQReciever {

    private static final Logger logger = LoggerFactory.getLogger(LocalMQ.class);

    private static LocalMQ messageQueue = new LocalMQ();

    private Queue<String> queue;
    private OrderProcessor orderProcessor;
    private Tracer tracer;

    private LocalMQ() {
        queue =new LinkedList<>();
    }

    public static LocalMQ getInstance() {
        return messageQueue;
    }

    public OrderProcessor getOrderProcessor() {
        return orderProcessor;
    }

    @Override
    public void setOrderProcessor(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void send(String message) {
        if (!queue.offer(message)) {
            logger.error("Unable to insert message into queue: message={}", message);
        }
    }

    @Override
    public void startReceiver() {
        (new Thread(new LocalMQProcessor())).start();
    }

    public void processMessage(String message) {
        logger.debug("Received message from order queue: message={}", message);
        Span span = tracer.buildSpan("order-processing").start();
        tracer.activateSpan(span);
        orderProcessor.processOrder(message);
    }

    class LocalMQProcessor implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("LocalMQProcessor sleep interrupted: {}", e);
                }
                String message = queue.poll();
                if (message != null) {
                    processMessage(message);
                }
            }
        }
    }
}