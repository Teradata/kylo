package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

public interface FeedProvider {

    Feed createFeed(String name, String descr, Dataset.ID source, Dataset.ID dest);
    
    // TODO Methods to add content specific details to dataset
 
    // TODO Methods to add policy info to source
}
