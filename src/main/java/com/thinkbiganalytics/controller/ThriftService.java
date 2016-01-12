/*
 * Copyright (c) 2015. Teradata Inc.
 */

package com.thinkbiganalytics.controller;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.processor.exception.ProcessException;

import java.sql.Connection;

@Tags({"thrift", "hive", "spark", "jdbc", "database", "connection", "pooling", "store"})
@CapabilityDescription("Provides Database Connection Pooling Service. Connections can be asked from pool and returned after usage.")
public interface ThriftService extends ControllerService {
    public Connection getConnection()  throws ProcessException;
}