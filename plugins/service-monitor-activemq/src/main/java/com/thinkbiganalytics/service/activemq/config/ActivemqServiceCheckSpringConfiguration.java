package com.thinkbiganalytics.service.activemq.config;


/*-
 * #%L
 * service-monitor-activemq
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.thinkbiganalytics.service.activemq.ActivemqServiceStatusCheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;

@Configuration
public class ActivemqServiceCheckSpringConfiguration {

    @Autowired(required = true)
    @Qualifier("activemqConnectionPool")
    ConnectionFactory connectionFactory;

    @Bean(name = "activemqServiceStatus")
    public ActivemqServiceStatusCheck activemqServiceStatusCheck( ) {

        return new ActivemqServiceStatusCheck(this.connectionFactory);

    }
}
