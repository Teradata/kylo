package com.thinkbiganalytics.nifi.v2.core.cleanup;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventConsumer;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventService;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;
import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;

import java.util.Collection;
import java.util.List;

public class JmsCleanupEventServiceTest {

    /**
     * Identifier for the cleanup event service
     */
    private static final String CLEANUP_SERVICE_IDENTIFIER = "cleanupEventService";

    /**
     * Processor property for the cleanup event service
     */
    private static final PropertyDescriptor CLEANUP_SERVICE_PROPERTY = new PropertyDescriptor.Builder()
        .name("Feed Cleanup Event Service")
        .description("Service that manages the cleanup of feeds.")
        .identifiesControllerService(CleanupEventService.class)
        .required(true)
        .build();

    /**
     * Identifier for the spring context service
     */
    private static final String SPRING_SERVICE_IDENTIFIER = "springContextService";

    /**
     * Cleanup event service for testing
     */
    private final JmsCleanupEventService cleanupService = new JmsCleanupEventService();

    /**
     * Mock cleanup event consumer
     */
    private final CleanupEventConsumer consumer = Mockito.mock(CleanupEventConsumer.class);

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(new AbstractProcessor() {
        @Override
        protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
            return ImmutableList.of(CLEANUP_SERVICE_PROPERTY);
        }

        @Override
        public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
            // do nothing
        }
    });

    /**
     * Initialize instance variables.
     */
    @Before
    public void setUp() throws Exception {
        // Setup controllers
        final SpringContextService springService = new MockSpringContextService();

        // Setup test runner
        runner.addControllerService(CLEANUP_SERVICE_IDENTIFIER, cleanupService);
        runner.addControllerService(SPRING_SERVICE_IDENTIFIER, springService);
        runner.setProperty(CLEANUP_SERVICE_PROPERTY, CLEANUP_SERVICE_IDENTIFIER);
        runner.setProperty(cleanupService, JmsCleanupEventService.SPRING_SERVICE, SPRING_SERVICE_IDENTIFIER);
        runner.enableControllerService(cleanupService);
        runner.enableControllerService(springService);
    }

    /**
     * Verify property validators.
     */
    @Test
    public void testValidators() {
        // Test with no properties
        runner.disableControllerService(cleanupService);
        runner.setProperty(cleanupService, JmsCleanupEventService.SPRING_SERVICE, (String) null);
        runner.enableControllerService(cleanupService);
        runner.enqueue(new byte[0]);
        Collection<ValidationResult> results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(1, results.size());

        String expected = "'Feed Cleanup Event Service' validated against 'cleanupEventService' is invalid because Controller Service is not valid: 'Spring Context Service' is invalid because Spring "
                          + "Context Service is required";
        Assert.assertEquals(expected, Iterables.getOnlyElement(results).toString());

        // Test with valid properties
        runner.disableControllerService(cleanupService);
        runner.setProperty(cleanupService, JmsCleanupEventService.SPRING_SERVICE, SPRING_SERVICE_IDENTIFIER);
        runner.enableControllerService(cleanupService);
        runner.enqueue(new byte[0]);
        results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(0, results.size());
    }

    /**
     * Verify adding and removing cleanup event listeners.
     */
    @Test
    public void test() throws Exception {
        // Test adding listener
        final CleanupListener listener = Mockito.mock(CleanupListener.class);
        cleanupService.addListener("cat", "feed", listener);
        Mockito.verify(consumer).addListener("cat", "feed", listener);

        // Test removing listener
        cleanupService.removeListener(listener);
        Mockito.verify(consumer).removeListener(listener);
    }

    /**
     * A mock implementation of {@link SpringContextService}.
     */
    private class MockSpringContextService extends AbstractControllerService implements SpringContextService {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getBean(Class<T> requiredType) throws BeansException {
            if (CleanupEventConsumer.class.equals(requiredType)) {
                return (T) consumer;
            }
            throw new IllegalArgumentException();
        }

        @Override
        public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
            throw new UnsupportedOperationException();
        }
    }
}
