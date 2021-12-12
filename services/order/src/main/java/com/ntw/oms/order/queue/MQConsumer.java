package com.ntw.oms.order.queue;

import io.opentracing.Tracer;

public interface MQConsumer {
    void startConsumer() throws Exception;

    void setOrderConsumer(OrderConsumer orderProcessor);
    void setTracer(Tracer tracer);
}
