//////////////////////////////////////////////////////////////////////////////
// Copyright 2020 Anurag Yadav (anurag.yadav@newtechways.com)               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//////////////////////////////////////////////////////////////////////////////

package com.ntw.oms.order;

import com.ntw.oms.order.queue.MessageQueue;
import com.ntw.oms.order.queue.OrderQueueProcessor;
import com.ntw.oms.order.queue.RabbitMQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by anurag on 28/07/20.
 */
@Configuration
@PropertySource(value = { "classpath:config.properties" })
public class OrderMQConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    @Qualifier("messageQueueProducer")
    public MessageQueue getMessageQueueProducerBean() throws IOException, TimeoutException {
        return new RabbitMQ(environment.getProperty("order.queue.host"),
                environment.getProperty("order.queue.name"));
    }

    @Bean
    @Qualifier("messageQueueConsumer")
    public MessageQueue getMessageQueueConsumerBean() throws IOException, TimeoutException {
        return new RabbitMQ(environment.getProperty("order.queue.host"),
                environment.getProperty("order.queue.name"));
    }

    @Autowired
    private OrderQueueProcessor orderQueueProcessor;

    @Bean
    @ConditionalOnProperty(name = "order.queue.consumer.enabled", havingValue = "true")
    public void startMessageQueueConsumer() throws Exception {
        orderQueueProcessor.start();
    }
}
