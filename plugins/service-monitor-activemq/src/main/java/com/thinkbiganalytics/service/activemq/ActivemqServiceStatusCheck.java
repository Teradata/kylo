package com.thinkbiganalytics.service.activemq;

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

import com.thinkbiganalytics.servicemonitor.check.ServiceStatusCheck;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.apache.activemq.jms.pool.PooledConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * Monitor Activemq service and return status to Kylo platform.
 */

public class ActivemqServiceStatusCheck implements ServiceStatusCheck{

    private static final Logger log = LoggerFactory.getLogger(ActivemqServiceStatusCheck.class);

    private  PooledConnectionFactory activemqPoolConnection = null;
    static final  String SERVICE_NAME = "Activemq"; 

    public ActivemqServiceStatusCheck()
    {
    }

    public ActivemqServiceStatusCheck(ConnectionFactory connectionFactory) {

        /**
         * Create Poolable Class Object and create multiple instance of connection 
         */
        this.activemqPoolConnection = (PooledConnectionFactory) connectionFactory;

    }

    @Override
    public ServiceStatusResponse healthCheck() {

        String serviceName = SERVICE_NAME;
        return new DefaultServiceStatusResponse(serviceName, Arrays.asList(activemqStatus()));
    }


    /**
     * Check if Activemq is running
     * 
     * @return Status of Activemq
     */

    private ServiceComponent activemqStatus() {


        String componentName = "Activemq";
        String serviceName = SERVICE_NAME;
        ServiceComponent component = null;
        Connection activemqConnection = null;

        /**
         * Prepare Alert Message
         */
        ServiceAlert alert = null;
        alert = new DefaultServiceAlert();
        alert.setLabel(componentName);
        alert.setServiceName(serviceName);
        alert.setComponentName(componentName);


        String finalServiceMessage = ""; 

        try {

            if ( this.activemqPoolConnection != null)
            {

                /**
                 * Create Instance of Connection from Pool
                 */

                activemqConnection =  this.activemqPoolConnection.createConnection();

                /**
                 *  On successful connection , return status to Kylo 
                 */

                finalServiceMessage = "Activemq is running.";
                alert.setMessage(finalServiceMessage);
                alert.setState(ServiceAlert.STATE.OK);

                component = new DefaultServiceComponent.Builder(componentName , ServiceComponent.STATE.UP)
                                .message(finalServiceMessage).addAlert(alert).build();


            }

        }
        catch(Exception jmsConnectionException)
        {
            finalServiceMessage = "Activemq is down.";
            alert.setMessage(finalServiceMessage);
            alert.setState(ServiceAlert.STATE.CRITICAL);

            component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).message(finalServiceMessage).exception(jmsConnectionException).addAlert(alert).build();
        }
        finally
        {
            /**
             * Close Connection
             */

            if(activemqConnection != null)
                try {
                    activemqConnection.close();
                } catch (JMSException e) {
                    log.error("Unable to close activemq connection" ,e);
                }
        }
        return component;
    }
}

