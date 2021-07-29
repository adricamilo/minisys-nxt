package com.ntw.oms.order.service;

import com.google.gson.Gson;
import com.ntw.oms.order.entity.Order;
import com.ntw.oms.order.queue.MQClient;
import com.ntw.oms.order.queue.QueueOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderQueueClient {
    private static final Logger logger = LoggerFactory.getLogger(OrderQueueClient.class);

    @Autowired
    private MQClient MQClient;

    public MQClient getMessageQueue() {
        return MQClient;
    }

    public void setMessageQueue(MQClient MQClient) {
        this.MQClient = MQClient;
    }

    public void enqueue(Order order, String authHeader) throws Exception {
        QueueOrder queueOrder = new QueueOrder(order, authHeader);
        String message = (new Gson()).toJson(queueOrder);
        try {
            MQClient.send(message);
            logger.debug("Published order to message queue; order={}",queueOrder);
        } catch (Exception e) {
            logger.error("Exception publishing order to queue", e);
            throw e;
        }
    }
}
