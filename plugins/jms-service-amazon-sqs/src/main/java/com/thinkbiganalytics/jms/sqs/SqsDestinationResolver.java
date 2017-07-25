package com.thinkbiganalytics.jms.sqs;

/*-
 * #%L
 * kylo-jms-service-amazon-sqs
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Amazon SQS does not allow for dots in Queue names, which have historically appeared in Kylo Queue names.
 * This class replaces dots with dashes so that Amazon SQS does not complain.
 */
public class SqsDestinationResolver extends DynamicDestinationResolver {

    private static final Logger LOG = LoggerFactory.getLogger(SqsDestinationResolver.class);

    @Override
    public Destination resolveDestinationName(Session session, String destinationName, boolean pubSubDomain) throws JMSException {
        String name = resolveName(destinationName);
        return super.resolveDestinationName(session, name, pubSubDomain);
    }

    String resolveName(String destinationName) {
        LOG.info("Resolving destination name {}", destinationName);
        return destinationName.replace(".", "-");
    }
}
