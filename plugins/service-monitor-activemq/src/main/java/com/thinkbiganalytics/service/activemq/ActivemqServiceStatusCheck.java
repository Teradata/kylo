package com.thinkbiganalytics.service.activemq;


/*-
 * #%L
 * thinkbig-service-monitor-nifi
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;


import java.util.Arrays;

public class ActivemqServiceStatusCheck implements ServiceStatusCheck{

	private static final Logger log = LoggerFactory.getLogger(ActivemqServiceStatusCheck.class);
	
	 @Value("${jms.activemq.broker.url:#{null}}")
	 private String activemqBrokerUrl;

	private ActiveMQConnection connection = null;
	
	@Autowired
	private Environment env;

	@Override
	public ServiceStatusResponse healthCheck() {

		
		String serviceName = "Activemq";

		return new DefaultServiceStatusResponse(serviceName, Arrays.asList(activemqStatus()));
	}

	/**
	 * Check if Activemq is running
	 * 
	 * @return Status of Activemq
	 */

	private ServiceComponent activemqStatus() {

		String componentName = "Activemq";
		String serviceName = "Activemq";
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
			String activemqConnectionUri = activemqBrokerUrl;

			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(activemqConnectionUri);

			/**
			 * Exception is thrown if connection failed.
			 */
			connection = (ActiveMQConnection)factory.createConnection();
			connection.addTransportListener(new ActivemqTransportListner());


			finalServiceMessage = "Activemq is running.";
			alert.setMessage(finalServiceMessage);
			alert.setState(ServiceAlert.STATE.OK);
			component =
					new DefaultServiceComponent.Builder(componentName + " - " + "", ServiceComponent.STATE.UP)
					.message(finalServiceMessage).addAlert(alert).build();;

			}

		} catch (Exception e) {

			finalServiceMessage = "Activemq is down";
			alert.setMessage(finalServiceMessage);
			alert.setState(ServiceAlert.STATE.CRITICAL);

			component = new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.DOWN).message(finalServiceMessage).exception(e).addAlert(alert).build();
		}
		
		return component;
	}

}
