/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

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

import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedDestination;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation;
import com.thinkbiganalytics.metadata.rest.model.op.DataOperation.State;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.DEST_DATASET_ID_PROP;
import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.FEED_ID_PROP;
import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.OPERATON_START_PROP;
import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.OPERATON_STOP_PROP;

/**
 */
public abstract class AbstractTerminateFeed extends AbstractFeedProcessor {

    public static final PropertyDescriptor DEST_DATASET_NAME = new PropertyDescriptor.Builder()
        .name(DEST_DATASET_ID_PROP)
        .displayName("Destination destinationDatasource name")
        .description("The unique name of the destinationDatasource that the feed will update")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor COMPLETION_RESULT = new PropertyDescriptor.Builder()
        .name("completion_result")
        .displayName("Completion result of this feed terminating processor")
        .description("The completion result of the feed")
        .allowableValues("SUCCESS", "FAILURE")
        .required(true)
        .build();

//    public static final Relationship SUCCESS = new Relationship.Builder()
//            .name("Success")
//            .description("Relationship followdd on successful metadata capture.")
//            .build();
//    public static final Relationship FAILURE = new Relationship.Builder()
//            .name("Failure")
//            .description("Relationship followdd on failed metadata capture.")
//            .build();

    @OnScheduled
    public Datasource ensureDatasourceMetadata(ProcessContext context) {
        return ensureDestinationDatasource(context);

//        if (this.destinationDatasource.get() == null) {
//            this.destinationDatasource.compareAndSet(null, ensureDestinationDatasource(context));
//        }
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        try {
            FlowFile flowFile = session.get();

            if (flowFile != null) {
                MetadataProvider provider = getProviderService(context).getProvider();
                // TODO Remove when we do more intelligent handling when the feed and datasource info has been
                // removed from the metadata store.
//                FeedDestination destination = this.feedDestination.get();
                Datasource ds = ensureDatasourceMetadata(context);
                FeedDestination destination = ensureFeedDestination(context, flowFile, ds);

                // TODO Begin re-caching this when the correct error can be surfaced from the client
                // when the destination no longer exists.
//                if (destination == null) {
//                    Datasource ds = this.destinationDatasource.get();
//                    this.feedDestination.compareAndSet(null, destination);
//                }

                String timeStr = flowFile.getAttribute(OPERATON_START_PROP);
                DateTime opStart = timeStr != null ? Formatters.parseDateTime(timeStr) : new DateTime();

                DataOperation op = provider.beginOperation(destination, opStart);
                op = completeOperation(context, flowFile, ds, op, getState(context, op));

                updateFeedState(context, flowFile);

                flowFile = session.putAttribute(flowFile, OPERATON_STOP_PROP, Formatters.print(new DateTime()));

                session.remove(flowFile);
//            session.transfer(flowFile, SUCCESS);
            } else {
                context.yield();
            }
        } catch (Exception e) {
            context.yield();
            session.rollback();
            getLog().error("Unexpected error processing feed completion", e);
            throw new ProcessException(e);
        }
    }

    private void updateFeedState(ProcessContext context, FlowFile flowFile) {
        MetadataProvider provider = getProviderService(context).getProvider();
        String feedId = flowFile.getAttribute(FEED_ID_PROP);

        if (feedId != null) {
            Properties props = deriveFeedProperties(flowFile);
            provider.updateFeedProperties(feedId, props);
        }
    }

    private Properties deriveFeedProperties(FlowFile flowFile) {
        Properties props = new Properties();
        Map<String, String> attrs = flowFile.getAttributes();

        for (Entry<String, String> entry : attrs.entrySet()) {
            if (entry.getKey().startsWith(MetadataConstants.LAST_LOAD_TIME_PROP)) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
        }

        return props;
    }

    protected State getState(ProcessContext context, DataOperation op) {
        String result = context.getProperty(COMPLETION_RESULT).getValue();
        return State.valueOf(result.toUpperCase());
    }

    protected abstract Datasource createDestinationDatasource(ProcessContext context, String name, String descr);

    protected abstract DataOperation completeOperation(ProcessContext context,
                                                       FlowFile flowFile,
                                                       Datasource destDatasource,
                                                       DataOperation op,
                                                       DataOperation.State state);

//    protected abstract ChangeSet<DirectoryDatasource, FileList> createChangeSet(ProcessContext context, FlowFile flowFile, Datasource destDatasource);

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(DEST_DATASET_NAME);
        props.add(COMPLETION_RESULT);
    }

    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
//        rels.add(SUCCESS);
//        rels.add(FAILURE);
    }

    protected State getOperationState(ProcessContext context) {
        String result = context.getProperty(COMPLETION_RESULT).getValue();

        if (result.equals("FAILURE")) {
            return State.FAILURE;
        } else {
            return State.SUCCESS;
        }
    }

    protected Datasource ensureDestinationDatasource(ProcessContext context) {
        String datasetName = context.getProperty(DEST_DATASET_NAME).getValue();
        Datasource dataset = findDatasource(context, datasetName);

        if (dataset != null) {
            return dataset;
        } else {
            getLog().info("No destinationDatasource exists with the givn name, creating: " + datasetName);

            return createDestinationDatasource(context, datasetName, "");
        }
    }

    protected FeedDestination ensureFeedDestination(ProcessContext context, FlowFile flowFile, Datasource destDatasource) {
        MetadataProvider provider = getProviderService(context).getProvider();
        String feedId = flowFile.getAttribute(FEED_ID_PROP);

        if (feedId != null) {
            Feed feed = provider.ensureFeedDestination(feedId, destDatasource.getId());
            return feed.getDestination(destDatasource.getId());
        } else {
            throw new ProcessException("Feed ID property missing from flow file (" + FEED_ID_PROP + ")");
        }
    }

}
