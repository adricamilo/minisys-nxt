package com.ntw.oms.order.queue;

import com.google.gson.Gson;
import com.ntw.oms.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrderQueueClient {
    private static final Logger logger = LoggerFactory.getLogger(OrderQueueClient.class);

    @Autowired
    @Qualifier("messageQueueProducer")
    private MessageQueue messageQueue;

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public void enqueue(Order order, String authHeader) throws Exception {
        QueueOrder queueOrder = new QueueOrder(order, authHeader);
        String message = (new Gson()).toJson(queueOrder);
        try {
            messageQueue.send(message);
            logger.debug("Published order to message queue; order={}",queueOrder);
        } catch (Exception e) {
            logger.error("Exception publishing order to queue", e);
            throw e;
        }
    }
}
