package com.thinkbiganalytics.metadata.modeshape;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;

import org.modeshape.jcr.api.JcrTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.persistence.Query;

/**
 * Created by sr186054 on 6/7/16.
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
    public <E, J extends JcrObject> List<E> select(Session session, String typeName, Class<E> type, Class<J> jcrClass) {
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder queryStr = new StringBuilder("select e.* from [" + typeName + "] as e ");

        applyFilter(queryStr, params);
        applyLimit(queryStr);

        Map<String, String> bindParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            bindParams.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
        }

        QueryResult result = null;
        try {
            result = JcrUtil.query(session, queryStr.toString(), bindParams);
            return (List<E>) JcrUtil.queryResultToList(result, jcrClass);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to execute Criteria Query for " + type + ".  Query is: " + queryStr.toString(), e);
        }

    }

    protected abstract void applyFilter(StringBuilder query, HashMap<String, Object> params);

    protected void applyLimit(StringBuilder query) {
        if (getLimit() >= 0) {
            query.append("limit ").append(getLimit());
        }
    }
}