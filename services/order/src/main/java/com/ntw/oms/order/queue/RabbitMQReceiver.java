package com.ntw.oms.order.queue;

import com.ntw.oms.order.processor.OrderProcessor;
import com.rabbitmq.client.*;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

class RabbitMQCallback implements DeliverCallback {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQCallback.class);
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

public class RabbitMQReceiver implements MQReciever {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQReceiver.class);

    private String queueName;
    private Connection connection;
    private Channel channel;

    private OrderProcessor orderProcessor;
    private Tracer tracer;

    public OrderProcessor getOrderProcessor() {
        return orderProcessor;
    }

    @Override
    public void setOrderProcessor(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    public Tracer getTracer() {
        return tracer;
    }

    @Override
    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public RabbitMQReceiver(String hostName, String queueName)
            throws IOException, TimeoutException {
        this.queueName = queueName;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, false, false, false, null);
        } catch (Exception e) {
            logger.error("Exception publishing order to queue", e);
            throw e;
        }
    }

    @Override
    public void startReceiver() throws IOException {
        RabbitMQCallback callback = new RabbitMQCallback();
        callback.setOrderProcessor(getOrderProcessor());
        callback.setTracer(getTracer());
        try {
            logger.info("Waiting for messages.");
            channel.basicConsume(queueName, true, callback, consumerTag -> {});
        } catch (Exception e) {
            logger.error("Unable to initialize order queue: ", e);
            throw e;
        }
    }

}
