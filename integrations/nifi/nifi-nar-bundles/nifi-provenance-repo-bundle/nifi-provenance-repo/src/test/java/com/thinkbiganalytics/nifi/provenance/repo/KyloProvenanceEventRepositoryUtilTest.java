package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * kylo-nifi-provenance-repo
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

import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ru186002 on 21/07/2017.
 */
public class KyloProvenanceEventRepositoryUtilTest {

    private static final Logger log = LoggerFactory.getLogger(KyloProvenanceEventRepositoryUtilTest.class);

    @Test
    public void testLoadDynamicSpringProfile() {
        String configFilePath = getClass().getClassLoader().getResource("ext-config/config.properties").getPath();
        System.setProperty("kylo.nifi.configPath", configFilePath.substring(0, configFilePath.indexOf("config.properties") - 1));

        KyloProvenanceEventRepositoryUtil util = new KyloProvenanceEventRepositoryUtil();
        util.loadSpring();

        String jmsActivemqBrokerUrl = SpringApplicationContext.getInstance().getApplicationContext().getEnvironment().getProperty("jms.activemq.broker.url");

        Assert.assertEquals("tcp://external:61616", jmsActivemqBrokerUrl);
    }

}
