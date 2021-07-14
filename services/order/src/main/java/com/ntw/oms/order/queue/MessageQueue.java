package com.ntw.oms.order.queue;

import java.io.IOException;

public interface MessageQueue {
    void send(String message);
    void accept(Object callback) throws IOException;
}
