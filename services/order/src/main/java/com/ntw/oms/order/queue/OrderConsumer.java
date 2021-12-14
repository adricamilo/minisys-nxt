package com.ntw.oms.order.queue;

import com.google.gson.Gson;
import com.ntw.oms.order.processor.OrderProcessor;
import com.ntw.oms.order.service.OrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    OrderProcessor orderProcessor;

    public OrderProcessor getOrderProcessor() {
        return orderProcessor;
    }

    public void setOrderProcessor(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    public QueueOrder getOrder(String serializedOrderString) {
        return (new Gson()).fromJson(serializedOrderString, QueueOrder.class);
    }

    public boolean processOrder(String orderJSON) {
        QueueOrder queueOrder = getOrder(orderJSON);
        if (queueOrder == null) {
            logger.error("Unable to deserialize order received for the order queue.");
            return false;
        }
        return processOrder(queueOrder);
    }

    private boolean processOrder(QueueOrder queueOrder) {
        // reserve inventory
        OrderServiceImpl.getThreadLocal().set(queueOrder.getAuthHeader());
        if (! orderProcessor.processOrder(queueOrder.getOrder())) {
            logger.error("Unable to reserve inventory for order; context={}", queueOrder);
            return false;
        }
        logger.info("Processed order: {}", queueOrder.getOrder());
        return true;
    }


}
