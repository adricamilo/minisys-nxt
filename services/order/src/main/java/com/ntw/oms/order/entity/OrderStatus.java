package com.ntw.oms.order.entity;

public enum OrderStatus {
    IN_PROCESS("In Process"), CREATED("Created");
    private String status;

    OrderStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
