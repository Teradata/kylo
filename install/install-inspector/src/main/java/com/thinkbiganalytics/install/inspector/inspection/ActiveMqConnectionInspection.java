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

import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.JMSException;

@Component
public class ActiveMqConnectionInspection extends AbstractInspection {

    private static final String JMS_ACTIVEMQ_BROKER_USERNAME = "jms.activemq.broker.username";
    private static final String JMS_ACTIVEMQ_BROKER_PASSWORD = "jms.activemq.broker.password";
    private static final String JMS_ACTIVEMQ_BROKER_URL = "jms.activemq.broker.url";
    private static final String SPRING_PROFILES_INCLUDE = "spring.profiles.include";
    private static final String JMS_ACTIVEMQ = "jms-activemq";
    private final Logger LOG = LoggerFactory.getLogger(ActiveMqConnectionInspection.class);

    @Override
    public String getName() {
        return "ActiveMQ Connection Check";
    }

    @Override
    public String getDescription() {
        return "Checks whether Kylo Services can connect to ActiveMQ";
    }

    @Override
    public InspectionStatus inspect(Configuration configuration) {
        LOG.debug("ActiveMqConnectionInspection.inspect");
        InspectionStatus connInspection = inspectConnection(configuration);
        InspectionStatus profileInspection = inspectProfile(configuration);
        InspectionStatus status = connInspection.and(profileInspection);
        status.setDocsLink("/how-to-guides/JmsProviders.html");
        return status;

    }

    private InspectionStatus inspectProfile(Configuration configuration) {
        String profilesProperty = configuration.getServicesProperty(SPRING_PROFILES_INCLUDE);
        String[] profiles = profilesProperty.split(",");
        boolean profileSet = Arrays.stream(profiles).anyMatch(JMS_ACTIVEMQ::equals);
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
            return new InspectionStatus(true);
        } catch (JMSException e) {
            InspectionStatus status = new InspectionStatus(false);
            status.addError(String.format("Failed to connect to ActiveMQ at %s: %s", brokerUrl, e.getMessage()));
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
