package com.thinkbiganalytics.service.activemq.util;

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

import com.thinkbiganalytics.service.activemq.config.ActivemqTransportListner;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;


/**
 * 
 * This class is not used. Connection will be comming from Activemq JMS provider.
 *
 */
public class ActivemqPoolableConnectionProvider {

    /**
     * 
     * @param activemqBrokerUrl  - Activemq Broker Connection String
     * @return pooled connection
     */
    public PooledConnectionFactory activemqPoolableConnection(String activemqBrokerUrl)
    {

        /**
         * Initialise Activemq Factory for Connection
         */
        ActiveMQConnectionFactory factory = initializeActiveMqFactoryConnection(activemqBrokerUrl);

        /**
         * Create Connection Pool using PooledConnectionFactory and add connectionFactory to it.
         */

        PooledConnectionFactory poolConnection = new PooledConnectionFactory();
        poolConnection.setConnectionFactory(factory);


        /**
         * Return connection from Pool
         */
        return poolConnection;

    }

    /**
     * 
     * @param activemqConnectionUri - Activeq connection string
     * @return activemq factory 
     */
    private ActiveMQConnectionFactory initializeActiveMqFactoryConnection(String activemqConnectionUri) {

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(activemqConnectionUri);

        /**
         * Add Transport Listener for any unusual failure
         */
        factory.setTransportListener(new ActivemqTransportListner());

        return factory;
    }

}
