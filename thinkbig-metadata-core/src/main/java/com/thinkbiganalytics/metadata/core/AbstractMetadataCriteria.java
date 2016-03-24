/**
 * 
 */
package com.thinkbiganalytics.metadata.core;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

/**
 *
 * @author Sean Felten
 */
public abstract class AbstractMetadataCriteria<C extends MetadataCriteria<C>> implements MetadataCriteria<C> {

    private int limit = Integer.MAX_VALUE;
    
    @Override
    public C limit(int size) {
        this.limit = size;
        return self();
    }
    
    @SuppressWarnings("unchecked")
    protected C self() {
        return (C) this;
    }
    
    public int getLimit() {
        return limit;
    }
}
