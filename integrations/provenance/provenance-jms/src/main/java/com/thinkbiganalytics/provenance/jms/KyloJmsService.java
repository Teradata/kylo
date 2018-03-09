package com.thinkbiganalytics.provenance.jms;
/*-
 * #%L
 * kylo-provenance-jms
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
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Service to connect to JMS and send a serialized message
 */
public class KyloJmsService {


    private Connection connection;
    private Session session;
    private MessageProducer producer;



    public void sendMessage(String url, String queueName,Serializable msg) throws Exception{
        Connection connection = getOrCreateJmsConnection(url);
        Session session = getOrCreateSession(connection);
        MessageProducer producer = getOrCreateProducer(session,queueName);
        sendMessage(session,producer,msg);
    }




    private Connection getOrCreateJmsConnection(String url) throws Exception {
        if(connection == null) {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            this.connection = connection;
        }
        return connection;
    }

    private Session getOrCreateSession(Connection connection) throws Exception {
        if(session == null) {
            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.session = session;
        }
        return session;
    }


    private MessageProducer getOrCreateProducer(Session session, String queueName) throws Exception{

        if(producer == null) {
            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            this.producer = producer;
        }
        return producer;
    }

    private void sendMessage(Session session, MessageProducer producer, Serializable message) throws Exception{
        Message m = session.createObjectMessage(message);
        producer.send(m);
    }

    public  void closeConnection(){
        if(this.producer != null) {
            try {
            this.producer.close();
            }catch (Exception e){

            }
        }
        if(this.session != null) {
            try {
                this.session.close();
            }catch (Exception e){

            }
        }
        if(this.connection != null) {
            try {
            this.connection.close();
            }catch (Exception e){

            }
        }
    }


}
