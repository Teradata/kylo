/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.core.api.precondition;

public interface PreconditionEventConsumer {

    void addListener(String datasourceName, PreconditionListener listener);

    void removeListener(PreconditionListener listener);
}
