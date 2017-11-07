package com.thinkbiganalytics.nifi.v2.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.ingest.TableMergeSyncSupport;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.v2.thrift.ThriftService;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.PartitionSpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.dbcp.hive.HiveDBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FEED_PARTITION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.FIELD_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.PARTITION_SPECIFICATION;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_FAILURE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.REL_SUCCESS;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.SOURCE_SCHEMA;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.SOURCE_TABLE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.TARGET_SCHEMA;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.TARGET_TABLE;
import static com.thinkbiganalytics.nifi.v2.ingest.IngestProperties.THRIFT_SERVICE;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"hive", "ddl", "merge", "sync", "thinkbig"})
@CapabilityDescription("Fully synchronize or Merge values from a feed partition into the target table optionally supporting de-dupe and overwriting partitions. Sync will overwrite the entire table "
                       + "to match the source."
)
public class MergeTable extends AbstractMergeTable {

    public static final PropertyDescriptor HIVE_CONNECTION_POOL = new PropertyDescriptor.Builder()
        .name("Hive Connection Pooling Service")
        .description("If specified this connection pool will be used in place of the Thrift Service")
        .required(false)
        .identifiesControllerService(HiveDBCPService.class)
        .build();


    public MergeTable() {
      super();
    }

    @Override
    public void addPropertyDescriptors(List<PropertyDescriptor> pds) {
        pds.add(HIVE_CONNECTION_POOL);
    }

    @Override
    public Connection getConnection(ProcessContext context) {
        ThriftService thriftService = context.getProperty(THRIFT_SERVICE).asControllerService(ThriftService.class);
        HiveDBCPService hiveConnectionPool = context.getProperty(HIVE_CONNECTION_POOL).asControllerService(HiveDBCPService.class);
        if(hiveConnectionPool != null){
            getLogger().info("Returning Connection from HiveConnectionPool");
            return hiveConnectionPool.getConnection();
        }
        else {
            getLogger().info("Returning Connection from ThriftConnectionPool");
            return thriftService.getConnection();
        }
    }
}
