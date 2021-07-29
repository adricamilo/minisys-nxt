package com.ntw.oms.order.queue;

import com.ntw.oms.order.processor.OrderProcessor;
import io.opentracing.Tracer;

public interface MQReciever {
    void startReceiver() throws Exception;

    void setOrderProcessor(OrderProcessor orderProcessor);
    void setTracer(Tracer tracer);
}
