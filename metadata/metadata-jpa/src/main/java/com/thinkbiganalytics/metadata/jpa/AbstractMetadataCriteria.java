/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

/**
 *
 * @author Sean Felten
 */
public abstract class AbstractMetadataCriteria<C extends MetadataCriteria<C>> implements MetadataCriteria<C> {

    private int limit = -1;
    
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

    protected void applyLimit(StringBuilder query) {
        if (getLimit() >= 0) {
            query.append("limit ").append(getLimit());
        }
    }
}
