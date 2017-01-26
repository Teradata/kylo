/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
    
    @SuppressWarnings("unchecked")
    public <E> List<E> select(EntityManager emgr, Class<E> type) {
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder queryStr = new StringBuilder("select e from ");
        
        queryStr.append(type.getSimpleName()).append(" e ");
        applyFilter(queryStr, params);
        applyLimit(queryStr);
        
        Query query = emgr.createQuery(queryStr.toString());
        
        for (Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        
        return query.getResultList();
    }
    
    protected abstract void applyFilter(StringBuilder query, HashMap<String, Object> params);

    protected void applyLimit(StringBuilder query) {
        if (getLimit() >= 0) {
            query.append("limit ").append(getLimit());
        }
    }
}
