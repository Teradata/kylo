package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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


import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jms.Connection;
import javax.jms.JMSException;

import static com.thinkbiganalytics.install.inspector.inspection.Configuration.SPRING_PROFILES_INCLUDE;

@Component
public class ActiveMqConnectionInspection extends InspectionBase {

    private static final String JMS_ACTIVEMQ_BROKER_USERNAME = "jms.activemq.broker.username";
    private static final String JMS_ACTIVEMQ_BROKER_PASSWORD = "jms.activemq.broker.password";
    private static final String JMS_ACTIVEMQ_BROKER_URL = "jms.activemq.broker.url";
    private static final String JMS_ACTIVEMQ = "jms-activemq";
    private final Logger LOG = LoggerFactory.getLogger(ActiveMqConnectionInspection.class);


    public ActiveMqConnectionInspection() {
        setName("ActiveMQ Connection Check");
        setDescription("Checks whether Kylo Services can connect to ActiveMQ");
        setDocsUrl("/installation/KyloApplicationProperties.html#jms");
    }

    @Override
    public InspectionStatus inspect(Configuration configuration) {
        LOG.debug("ActiveMqConnectionInspection.inspect");
        InspectionStatus connInspection = inspectConnection(configuration);
        InspectionStatus profileInspection = inspectProfile(configuration);
        return connInspection.and(profileInspection);

    }

    private InspectionStatus inspectProfile(Configuration configuration) {
        boolean profileSet = configuration.getServicesProfiles().stream().anyMatch(JMS_ACTIVEMQ::equals);
        InspectionStatus status = new InspectionStatus(profileSet);
        if (!profileSet) {
            status.addError(String.format("ActiveMQ profile is not enabled in kylo-services configuration file %s. "
                                          + "To enable ActiveMQ profile add '%s' to '%s' property, e.g. '%s=<all-other-profiles>,%s'",
                                          configuration.getServicesConfigLocation(), JMS_ACTIVEMQ, SPRING_PROFILES_INCLUDE, SPRING_PROFILES_INCLUDE, JMS_ACTIVEMQ));
        }
        return status;
    }

    private InspectionStatus inspectConnection(Configuration configuration) {
        String brokerUrl = configuration.getServicesProperty(JMS_ACTIVEMQ_BROKER_URL);
        String username = configuration.getServicesProperty(JMS_ACTIVEMQ_BROKER_USERNAME);
        String password = configuration.getServicesProperty(JMS_ACTIVEMQ_BROKER_PASSWORD);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = null;
        try {
            connection = factory.createConnection(username, password);
            connection.setClientID("kylo-install-inspector"); //this will fail if connection is not set up correctly
            InspectionStatus status = new InspectionStatus(true);
            status.addDescription(String.format("Successfully connected to ActiveMQ running at '%s'", brokerUrl));
            return status;
        } catch (JMSException e) {
            InspectionStatus status = new InspectionStatus(false);
            status.addError(String.format("Failed to connect to ActiveMQ at %s: %s. Check property values for properties starting with 'jms.activemq' in '%s'",
                                          brokerUrl, e.getMessage(), configuration.getServicesConfigLocation()));
            return status;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOG.error("An error occurred while closing JMS connection", e);
                }
            }
        }
    }
}
