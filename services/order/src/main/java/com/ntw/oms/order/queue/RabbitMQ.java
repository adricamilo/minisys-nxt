package com.ntw.oms.order.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQ implements MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQ.class);

    private String queueName;
    private Connection connection;
    private Channel channel;

    public RabbitMQ(String hostName, String queueName) throws IOException, TimeoutException {
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
    public void send(String message) {
        try {
            channel.basicPublish("", queueName, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("Published message queue; order={}", message);
    }

    @Override
    public void accept(Object callback) throws IOException {
        DeliverCallback deliverCallback = (DeliverCallback) callback;
        try {
            logger.info("Waiting for messages.");
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
        } catch (Exception e) {
            logger.error("Unable to initialize order queue: ", e);
            throw e;
        }
    }
}
