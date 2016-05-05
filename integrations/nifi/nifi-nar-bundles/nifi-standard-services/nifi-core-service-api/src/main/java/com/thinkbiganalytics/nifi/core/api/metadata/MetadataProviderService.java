/*
 * Copyright (c) 2016. Teradata Inc.
 */

/**
 *
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

/**
 * @author Sean Felten
 */
@Tags({"thinkbig", "metadata", "client", "feed", "dataset", "operation"})
@CapabilityDescription("Exposes the metadata providers to access and manipulate metadata related to "
        + "feeds, datasets, and data operations.")
public interface MetadataProviderService extends ControllerService {

    MetadataProvider getProvider();
    
    MetadataRecorder getRecorder();
}
