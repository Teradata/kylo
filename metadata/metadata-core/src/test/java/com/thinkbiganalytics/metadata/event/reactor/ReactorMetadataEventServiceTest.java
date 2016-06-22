/**
 * 
 */
package com.thinkbiganalytics.metadata.event.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.event.BaseMetadataEvent;

/**
 *
 * @author Sean Felten
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ReactorContiguration.class })
public class ReactorMetadataEventServiceTest {

    @Inject
    private MetadataEventService service;
    
    @Test
    public void testMatchingDataType() throws Exception {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        
        class TestEvent extends BaseMetadataEvent<Integer> {
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
        
        class TestEvent extends BaseMetadataEvent<Number> {
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
    
    
    @Test(expected=TimeoutException.class)
    public void testNotMatchingEventType() throws Exception {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        
        class TestEvent extends BaseMetadataEvent<Integer> {
            public TestEvent(Integer data) {
                super(data);
            }
        }
        
        class OtherEvent extends BaseMetadataEvent<Boolean> {
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
