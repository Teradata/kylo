package com.thinkbiganalytics.service.activemq;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.InactivityIOException;

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
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


public class ActivemqServiceStatusCheck implements ServiceStatusCheck{

	 private static final Logger log = LoggerFactory.getLogger(ActivemqServiceStatusCheck.class);
	
	@Autowired
    private Environment env;
	
	public static void main(String args[]) 
	
	{
		String uri = "tcp://127.0.0.1:61616";
		
		try{
		System.out.println("Debug 1");
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(uri);
		
		System.out.println("Debug 2");
		factory.setTransportListener(new ActivemqTransportListner());
		
		Connection connection = factory.createConnection();
		
		//ActiveMQConnection con = ActiveMQConnection()_factory.createConnection();
	
		
		
		
		System.out.println("Debug 3");
		//connection.setClientID("my_client_id"); 
		
		}
		catch(JMSException jms)
		{
			System.out.println("Could not connect to Activemq");
			jms.printStackTrace();
			
		}
		
	}

	@Override
	public ServiceStatusResponse healthCheck() {
		// TODO Auto-generated method stub
		return null;
	}

}
