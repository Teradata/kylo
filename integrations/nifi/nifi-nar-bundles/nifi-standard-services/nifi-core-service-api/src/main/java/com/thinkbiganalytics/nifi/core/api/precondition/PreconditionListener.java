/*
 * Copyright (c) 2016. Teradata Inc.
 */

/**
 *
 */
package com.thinkbiganalytics.nifi.core.api.precondition;

import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;

/**
 * @author Sean Felten
 */
public interface PreconditionListener {

    void triggered(FeedPreconditionTriggerEvent event);
}
