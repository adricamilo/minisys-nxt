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

import com.ntw.oms.order.queue.*;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    OrderConsumer orderConsumer;

    @Bean
    public MQProducer getMessageQueueProducerBean() throws IOException, TimeoutException {
        if (environment.getProperty("order.queue.type").equals("rabbitmq")) {
            return new RabbitMQProducer(environment.getProperty("order.queue.host"),
                    environment.getProperty("order.queue.name"));
        }
        // order.queue.type=local
        return LocalMQ.getInstance();
    }

    @Bean
    public MQConsumer getMessageQueueConsumerBean() throws IOException, TimeoutException {
        MQConsumer mqConsumer =
        (environment.getProperty("order.queue.type").equals("rabbitmq")) ?
            new RabbitMQConsumer(environment.getProperty("order.queue.host"),
                    environment.getProperty("order.queue.name")) :
                LocalMQ.getInstance();
        mqConsumer.setOrderConsumer(orderConsumer);
        try {
            mqConsumer.startConsumer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mqConsumer;
    }

}
