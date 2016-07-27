package com.thinkbiganalytics.nifi.v2.core.cleanup;

import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;

import org.junit.Test;
import org.mockito.Mockito;

public class JmsCleanupEventConsumerTest {

    /** Test consuming and dispatching a cleanup trigger event. */
    @Test
    public void test() {
        // Mock event and listener
        final FeedCleanupTriggerEvent event = new FeedCleanupTriggerEvent("FEEDID");
        event.setCategoryName("cat");
        event.setFeedName("feed");

        final CleanupListener listener = Mockito.mock(CleanupListener.class);

        // Test receiving and triggering event
        final JmsCleanupEventConsumer consumer = new JmsCleanupEventConsumer();
        consumer.addListener("cat", "feed", listener);
        consumer.receiveEvent(event);
        Mockito.verify(listener).triggered(event);

        // Test removing listener
        consumer.removeListener(listener);
        consumer.receiveEvent(event);
        Mockito.verifyNoMoreInteractions(listener);
    }
}
