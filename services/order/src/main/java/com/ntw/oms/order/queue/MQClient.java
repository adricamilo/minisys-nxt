package com.ntw.oms.order.queue;

public interface MQClient {
    void send(String message);
}
