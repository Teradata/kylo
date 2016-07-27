package com.thinkbiganalytics.nifi.core.api.cleanup;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * Service that manages the cleanup of feeds.
 */
@CapabilityDescription("Service that manages the cleanup of feeds.")
@Tags({"thinkbig", "feed", "precondition", "event", "trigger"})
public interface CleanupEventService extends ControllerService, CleanupEventConsumer {
}
