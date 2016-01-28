/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.controller;

import com.thinkbiganalytics.metadata.MetadataClient;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.processor.exception.ProcessException;

import java.sql.Connection;

@Tags({"thinkbig", "metadata",  "connection", "store"})
@CapabilityDescription("Provides connection to Think Big platform metadata service.")
public interface MetadataService extends ControllerService {
    MetadataClient getClient()  throws ProcessException;
}