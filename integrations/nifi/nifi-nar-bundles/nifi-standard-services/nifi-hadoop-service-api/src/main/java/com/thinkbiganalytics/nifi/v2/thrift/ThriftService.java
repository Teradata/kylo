package com.thinkbiganalytics.nifi.v2.thrift;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.processor.exception.ProcessException;

import java.sql.Connection;

@Tags({"thinkbig", "thrift", "hive", "spark", "jdbc", "database", "connection", "pooling"})
@CapabilityDescription("Provides Database Connection Pooling Service. Connections can be asked from pool and returned after usage.")
public interface ThriftService extends ControllerService {
    Connection getConnection() throws ProcessException;
}
