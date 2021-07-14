package com.ntw.oms.order.processor;

import com.google.gson.Gson;
import com.ntw.oms.order.dao.OrderDao;
import com.ntw.oms.order.dao.OrderDaoFactory;
import com.ntw.oms.order.entity.InventoryReservation;
import com.ntw.oms.order.entity.Order;
import com.ntw.oms.order.entity.OrderLine;
import com.ntw.oms.order.entity.OrderStatus;
import com.ntw.oms.order.queue.QueueOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Component
public class OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessor.class);

    @Autowired
    private OrderDaoFactory orderDaoFactory;

    private OrderDao orderDaoBean;

    @Value("${database.type}")
    private String orderDBType;

    @Autowired
    private InventoryClient inventoryClientBean;

    @PostConstruct
    public void postConstruct() throws Exception {
        this.orderDaoBean = orderDaoFactory.getOrderDao(orderDBType);
    }

    public OrderDao getOrderDaoBean() {
        return orderDaoBean;
    }

    public void setOrderDaoBean(OrderDao orderDaoBean) {
        this.orderDaoBean = orderDaoBean;
    }

    public InventoryClient getInventoryClientBean() {
        return inventoryClientBean;
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

    public boolean processOrder(QueueOrder queueOrder) {
        // reserve inventory
        if (! reserveInventory(queueOrder.getOrder(), queueOrder.getAuthHeader())) {
            logger.error("Unable to reserve inventory for order; context={}", queueOrder);
            return false;
        }
        logger.info("Processed order: {}", queueOrder.getOrder());
        return true;
    }

    private boolean reserveInventory(Order order, String authHeader) {
        List<OrderLine> orderLines = order.getOrderLines();
        InventoryReservation inventoryReservation = new InventoryReservation();
        for (OrderLine ol : orderLines) {
            inventoryReservation.addInvResLine(ol.getProductId(), ol.getQuantity());
        }
        try {
            if (!getInventoryClientBean().reserveInventory(inventoryReservation, authHeader)) {
                logger.error("Unable to reserve inventory; context={}", inventoryReservation);
                return false;
            }
        } catch (IOException e) {
            logger.error("Unable to reserve inventory for {}; exception={}", inventoryReservation, e);
            return false;
        }
        // persist order
        order.setStatus(OrderStatus.CREATED);
        if (!getOrderDaoBean().saveOrder(order)) {
            logger.error("Unable to create order; context={}", order);
            // ToDo: Async Rollback inventory reservations
        }
        logger.debug("Created order; context={}", order);
        return true;
    }

}
