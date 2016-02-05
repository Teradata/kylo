/**
 * 
 */
package com.thinkbiganalytics.controller;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;

import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
@Tags({"thinkbig", "metadata", "feed", "dataset", "operation"})
@CapabilityDescription("Exposes the metadata providers to access and manipulate metadata related to "
        + "feeds, datasets, and data operations.")
public interface MetadataProviderService {

    FeedProvider getFeedProvider();
    
    DatasetProvider getDatasetProvider();
    
    DataOperationsProvider getDataOperationsProvider();
}
