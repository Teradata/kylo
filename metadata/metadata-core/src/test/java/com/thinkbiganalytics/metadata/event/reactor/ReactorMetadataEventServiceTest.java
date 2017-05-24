/**
 *
 */
package com.thinkbiganalytics.metadata.event.reactor;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ReactorConfiguration.class})
public class ReactorMetadataEventServiceTest {

    @Inject
    private MetadataEventService service;

    @Test
    public void testMatchingDataType() throws Exception {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        class TestEvent extends AbstractMetadataEvent<Integer> {

            public TestEvent(Integer data) {
                super(data);
            }
        }

        class TestEventListener implements MetadataEventListener<TestEvent> {

            @Override
            public void notify(TestEvent event) {
                future.complete(event.getData());
            }
        }

        service.addListener(new TestEventListener());
        service.notify(new TestEvent(1));

        Integer result = future.get();

        assertThat(result).isInstanceOf(Integer.class);
    }

    @Test
    public void testMatchingDataSubtype() throws Exception {
        final CompletableFuture<Number> future = new CompletableFuture<>();

        class TestEvent extends AbstractMetadataEvent<Number> {

            public TestEvent(Number data) {
                super(data);
            }
        }

        class TestEventListener implements MetadataEventListener<TestEvent> {

            @Override
            public void notify(TestEvent event) {
                future.complete(event.getData());
            }
        }

        service.addListener(new TestEventListener());
        service.notify(new TestEvent(new Integer(1)));

        Number result = future.get();

        assertThat(result).isInstanceOf(Integer.class);
    }


    @Test(expected = TimeoutException.class)
    public void testNotMatchingEventType() throws Exception {
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        class TestEvent extends AbstractMetadataEvent<Integer> {

            public TestEvent(Integer data) {
                super(data);
            }
        }

        class OtherEvent extends AbstractMetadataEvent<Boolean> {

            public OtherEvent(Boolean data) {
                super(data);
            }
        }

        class TestEventListener implements MetadataEventListener<TestEvent> {

            @Override
            public void notify(TestEvent event) {
                future.complete(event.getData());
            }
        }

        service.addListener(new TestEventListener());
        service.notify(new OtherEvent(true));

        future.get(1, TimeUnit.SECONDS);
    }
}
