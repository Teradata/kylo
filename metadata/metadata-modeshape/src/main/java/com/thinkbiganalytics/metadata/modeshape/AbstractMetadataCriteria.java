package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
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
            result = JcrQueryUtil.query(session, queryStr.toString(), bindParams);
            return (List<E>) JcrQueryUtil.queryResultToList(result, jcrClass);
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
