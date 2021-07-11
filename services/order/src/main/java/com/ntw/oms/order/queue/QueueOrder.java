package com.ntw.oms.order.queue;

import com.ntw.oms.order.entity.Order;

public class QueueOrder {
    private Order order;
    private String authHeader;

    public QueueOrder() {
    }

    public QueueOrder(Order order, String authHeader) {
        this.order = order;
        this.authHeader = authHeader;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }

    @Override
    public String toString() {
        return "{" +
                "\"order\":" + (order == null ? "null" : order) + ", " +
                "\"authHeader\":" + (authHeader == null ? "null" : "\"" + authHeader + "\"") +
                "}";
    }
}
