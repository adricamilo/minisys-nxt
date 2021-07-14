package com.ntw.oms.order.queue;

import com.ntw.oms.order.processor.OrderProcessor;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

class OrderQueueCallback implements DeliverCallback {

    private static final Logger logger = LoggerFactory.getLogger(OrderQueueProcessor.class);
    private Tracer tracer;
    private OrderProcessor orderProcessor;

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public void setOrderProcessor(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void handle(String s, Delivery delivery) throws IOException {
        Span span = tracer.buildSpan("order-processing").start();
        tracer.activateSpan(span);
        String message = new String(delivery.getBody(), "UTF-8");
        logger.debug("Received message from order queue: message={}", message);
        orderProcessor.processOrder(message);
        span.finish();
    }

}

@Component
public class OrderQueueProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OrderQueueProcessor.class);

    @Autowired
    @Qualifier("messageQueueConsumer")
    MessageQueue messageQueue;

    @Autowired
    OrderProcessor orderProcessor;

    @Autowired
    private Tracer tracer;

    public void start() throws Exception {
        OrderQueueCallback callback = new OrderQueueCallback();
        callback.setOrderProcessor(orderProcessor);
        callback.setTracer(tracer);
        try {
            logger.info("Initializing Order Queue. Waiting for messages.");
            messageQueue.accept(callback);
        } catch (Exception e) {
            logger.error("Unable to initialize order queue: ", e);
            throw e;
        }
    }

}
