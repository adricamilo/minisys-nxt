package com.ntw.oms.order.queue;

import com.google.gson.Gson;
import com.ntw.oms.order.entity.Order;
import com.ntw.oms.order.service.OrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {
    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);

    @Autowired
    private MQProducer MQProducer;

    public MQProducer getMessageQueue() {
        return MQProducer;
    }

    public void setMessageQueue(MQProducer MQProducer) {
        this.MQProducer = MQProducer;
    }

    public void enqueue(Order order) throws Exception {
        String authHeader = OrderServiceImpl.getThreadLocal().get();
        QueueOrder queueOrder = new QueueOrder(order, authHeader);
        String message = (new Gson()).toJson(queueOrder);
        try {
            MQProducer.send(message);
            logger.debug("Published order to message queue; order={}",queueOrder);
        } catch (Exception e) {
            logger.error("Exception publishing order to queue", e);
            throw e;
        }
    }
}
