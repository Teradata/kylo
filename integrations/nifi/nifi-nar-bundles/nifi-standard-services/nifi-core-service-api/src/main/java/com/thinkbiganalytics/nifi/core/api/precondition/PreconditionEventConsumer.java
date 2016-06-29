/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.core.api.precondition;

public interface PreconditionEventConsumer {

    void addListener(String category, String feedName, PreconditionListener listener);

    void removeListener(PreconditionListener listener);
}
