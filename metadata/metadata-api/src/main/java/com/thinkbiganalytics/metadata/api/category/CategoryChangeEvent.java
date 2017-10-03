package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChange;

import org.joda.time.DateTime;

import java.security.Principal;

/**
 * Created by sr186054 on 10/3/17.
 */
public class CategoryChangeEvent extends AbstractMetadataEvent<CategoryChange> {

    private static final long serialVersionUID = 1L;

    public CategoryChangeEvent(CategoryChange data) {
        super(data);
    }

    public CategoryChangeEvent(CategoryChange data, Principal user) {
        super(data, user);
    }

    public CategoryChangeEvent(CategoryChange data, DateTime time, Principal user) {
        super(data, time, user);


    }
}
