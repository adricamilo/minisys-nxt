package com.ntw.oms.order.queue;

import com.ntw.oms.order.processor.OrderProcessor;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("Processor")
public class OrderQueueProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OrderQueueProcessor.class);
    private final static String QUEUE_NAME = "order-queue";

    @Autowired
    OrderProcessor orderProcessor;

    @Autowired
    private Tracer tracer;

    public void initialize() throws Exception {

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            Span span = tracer.buildSpan("order-processing").start();
            tracer.activateSpan(span);
            String message = new String(delivery.getBody(), "UTF-8");
            logger.debug("Received message from order queue: message={}", message);
            orderProcessor.processOrder(message);
            span.finish();
        };

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("rabbitmq-1");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            logger.info("Initializing Order Queue. Waiting for messages.");
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            logger.error("Unable to initialize order queue: ", e);
            throw e;
        }
    }
}
