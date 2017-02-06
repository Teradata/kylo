package com.thinkbiganalytics.controller.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.v2.core.metadata.MetadataProviderSelectorService;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.ssl.SSLContextService;
import org.apache.nifi.ssl.StandardSSLContextService;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * To test with SSL 1. use the nifi-toolkit to generate selfsigned key and trust store tls-toolkit.sh standalone -n 'localhost' -C 'CN=kylo, OU=ThinkBig' -o . 2. set the application properties to make
 * thinkbig-services run under SSL  using the generated files from #1  (example below) server.ssl.key-store=/Users/sr186054/tools/test-ssl/test/localhost/keystore.jks
 * server.ssl.key-store-password=sxkJ96yw2ZZktkVFtflln2IqjxkXPCD+vh3gAPDhQ18 server.ssl.key-store-type=jks server.ssl.trust-store=/Users/sr186054/tools/test-ssl/test/localhost/truststore.jks
 * server.ssl.trust-store-password=S1+cc2FKMzk2td/p6OJE0U6FUM3fV5jnlrYj46CoUSU server.ssl.trust-store-type=JKS 3. setup the SSLContextService below to use the truststore and client keystore 4. run the
 * #testMetadataService method to assess the connections
 */
public class MetadataProviderSelectorServiceTest {

    private static String SSL_SERVICE_NAME = "ssl-service-1";
    private static String METADATA_SERVICE_NAME = "metadataService1";


    private SSLContextService createSslContextService(TestRunner runner) throws InitializationException {
        SSLContextService service = new StandardSSLContextService();
        runner.addControllerService(SSL_SERVICE_NAME, service);
        runner.setProperty(service, StandardSSLContextService.KEYSTORE.getName(), "/Users/sr186054/tools/test-ssl/test/CN=kylo_OU=ThinkBig.p12");
        runner.setProperty(service, StandardSSLContextService.KEYSTORE_PASSWORD.getName(), "RQDTSpm");
        runner.setProperty(service, StandardSSLContextService.KEYSTORE_TYPE.getName(), "PKCS12");
        runner.setProperty(service, StandardSSLContextService.TRUSTSTORE.getName(), "/Users/sr186054/tools/test-ssl/test/localhost/truststore.jks");
        runner.setProperty(service, StandardSSLContextService.TRUSTSTORE_PASSWORD.getName(), "S1+cc2FKMzk2td/p6OJE0U6FUM3fV5jnlrYj46CoUSU");
        runner.setProperty(service, StandardSSLContextService.TRUSTSTORE_TYPE.getName(), "JKS");
        runner.enableControllerService(service);
        return service;
    }

    private MetadataProviderSelectorService createMetadataProviderSelectorService(TestRunner runner, boolean useSsl) throws InitializationException {
        MetadataProviderSelectorService metadataProviderSelectorService = new MetadataProviderSelectorService();
        runner.addControllerService(METADATA_SERVICE_NAME, metadataProviderSelectorService);
        if (useSsl) {
            runner.setProperty(metadataProviderSelectorService, MetadataProviderSelectorService.SSL_CONTEXT_SERVICE.getName(), createSslContextService(runner).getIdentifier());
        }
        runner.setProperty(metadataProviderSelectorService, MetadataProviderSelectorService.CLIENT_URL.getName(), "https://localhost:8420/api/v1/metadata");
        runner.setProperty(metadataProviderSelectorService, MetadataProviderSelectorService.CLIENT_USERNAME.getName(), "dladmin");
        runner.setProperty(metadataProviderSelectorService, MetadataProviderSelectorService.CLIENT_PASSWORD.getName(), "thinkbig");
        runner.enableControllerService(metadataProviderSelectorService);
        return metadataProviderSelectorService;
    }

    // @Test
    public void testMetadataService() throws InitializationException {

        String category = "web_data";
        String feed = "web_signup";

        final TestRunner runner = TestRunners.newTestRunner(TestProcessor.class);
        MetadataProviderSelectorService metadataService = createMetadataProviderSelectorService(runner, true);

        runner.assertValid(metadataService);

        metadataService = (MetadataProviderSelectorService) runner.getProcessContext().getControllerServiceLookup().getControllerService(METADATA_SERVICE_NAME);
        Assert.assertNotNull(metadataService);
        String feedId = metadataService.getProvider().getFeedId(category, feed);
        Assert.assertNotNull(feedId);

    }

    public static class TestProcessor extends AbstractProcessor {


        public TestProcessor() {
            super();
        }

        @Override
        public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        }

        @Override
        protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
            List<PropertyDescriptor> propDescs = new ArrayList<>();
            propDescs.add(new PropertyDescriptor.Builder()
                              .name("Metadata CLientService")
                              .description("Metadata CLientService")
                              .identifiesControllerService(MetadataProviderService.class)
                              .required(true)
                              .build());
            return propDescs;
        }
    }

}
