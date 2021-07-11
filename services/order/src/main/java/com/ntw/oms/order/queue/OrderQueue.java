package com.ntw.oms.order.queue;

import com.google.gson.Gson;
import com.ntw.oms.order.entity.Order;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderQueue {
    private static final Logger logger = LoggerFactory.getLogger(OrderQueue.class);
    public final static String QUEUE_NAME = "order-queue";
    public static void enqueue(Order order, String authHeader) throws Exception {
        QueueOrder queueOrder = new QueueOrder(order, authHeader);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq-1");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = (new Gson()).toJson(queueOrder);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.debug("Published order to message queue; order={}",queueOrder);
        } catch (Exception e) {
            logger.error("Exception publishing order to queue", e);
            throw e;
        }
    }
}
