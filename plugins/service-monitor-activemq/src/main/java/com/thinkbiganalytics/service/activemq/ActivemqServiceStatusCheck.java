package com.thinkbiganalytics.service.activemq;

import com.thinkbiganalytics.service.activemq.config.ActivemqPoolableConnectionProvider;

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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import javax.jms.JMSException;

public class ActivemqServiceStatusCheck implements ServiceStatusCheck{

    private static final Logger log = LoggerFactory.getLogger(ActivemqServiceStatusCheck.class);

    @Value("${jms.activemq.broker.url:#{null}}")
    private String activemqBrokerUrl;


    static final  String SERVICE_NAME = "Activemq"; 

    @Autowired
    private Environment env;

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


            if ( StringUtils.isNotBlank(activemqBrokerUrl) )
            {

                /**
                 *  Create Pool Object
                 */
                ActivemqPoolableConnectionProvider activemqPoolableConnection = new ActivemqPoolableConnectionProvider ();

                /**
                 * Create Connection from Pool
                 */
                activemqPoolableConnection.activemqPoolableConnection(activemqBrokerUrl).createConnection();

                /**
                 *  On successful connection , return status to Kylo 
                 */
              
                finalServiceMessage = "Activemq is running.";
                alert.setMessage(finalServiceMessage);
                alert.setState(ServiceAlert.STATE.OK);
                component =
                                new DefaultServiceComponent.Builder(componentName + " - " + "", ServiceComponent.STATE.UP)
                                .message(finalServiceMessage).addAlert(alert).build();

            }

        }
        catch(Exception jmsConnectionException)
        {
            System.out.println("************************ Caught exception ---------------------");

            JMSException jmsException;

            if(jmsConnectionException.getCause() != null && jmsConnectionException.getCause().getCause() instanceof JMSException) {
                jmsException = (JMSException)jmsConnectionException.getCause().getCause();

                finalServiceMessage = jmsException.getMessage();
                alert.setMessage(finalServiceMessage);
                alert.setState(ServiceAlert.STATE.CRITICAL);

                component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).message(finalServiceMessage).exception(jmsConnectionException).addAlert(alert).build();

            } else {

                finalServiceMessage = "Activemq is down.";
                alert.setMessage(finalServiceMessage);
                alert.setState(ServiceAlert.STATE.CRITICAL);

                component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).message(finalServiceMessage).exception(jmsConnectionException).addAlert(alert).build();


            }



        }
        //        catch (Exception e) {
        //
        //            
        //            System.out.println("--------------------- kuchh to jhol hai ---------" );
        //            
        //            finalServiceMessage = "Activemq is down";
        //            alert.setMessage(finalServiceMessage);
        //            alert.setState(ServiceAlert.STATE.CRITICAL);
        //
        //            component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).message(finalServiceMessage).exception(e).addAlert(alert).build();
        //        }

        return component;
    }



    //    private UserCredentialsConnectionFactoryAdapter getCredentialsAdapter(ConnectionFactory connectionFactory) {
    //        UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
    //        adapter.setTargetConnectionFactory(connectionFactory);
    //        String username = env.getProperty("jms.activemq.broker.username");
    //        String password = env.getProperty("jms.activemq.broker.password");
    //        adapter.setUsername(username);
    //        adapter.setPassword(password);
    //
    //        log.info("Connecting to ActiveMQ {} ", username != null ? "as " + username : "anonymously");
    //
    //        return adapter;
    //    }


}
