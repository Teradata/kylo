package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Created by sr186054 on 5/5/16.
 */
public abstract class AbstractNifiProcessorToFeedSource implements NifiProcessorToFeedSource {

    protected FeedMetadata metadata = null;

    public  AbstractNifiProcessorToFeedSource(FeedMetadata feedMetadata){
        this.metadata = feedMetadata;
    }

    public NifiProperty getSourceProperty(String key){
        return NifiPropertyUtil.findFirstPropertyMatchingKey(getSourceProperties(getNifiProcessorType()),key);
    }

    public NifiProperty getSourceProperty(String processorType, String key){
       return NifiPropertyUtil.findFirstPropertyMatchingKey(getSourceProperties(processorType),key);
    }

    public List<NifiProperty> getSourceProperties(String processorType){
      return NifiPropertyUtil.findInputPropertyMatchingType(metadata.getProperties(), processorType);
    }

    @Override
    public String getNifiProcessorType() {
        NifiFeedSourceProcessor nifiFeedSourceProcessor = (NifiFeedSourceProcessor) this.getClass().getAnnotation(NifiFeedSourceProcessor.class);
        return nifiFeedSourceProcessor.nifiProcessorType();
    }
}
