package com.thinkbiganalytics.metadata.modeshape.tag;

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

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrQueryUtil;

import org.modeshape.jcr.api.JcrTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

/**
 */
public class TagProvider {


    protected Session getSession() {
        return JcrMetadataAccess.getActiveSession();
    }


    public List<JcrObject> findByTag(String tag) {

        String query = "SELECT * FROM [tba:taggable] AS taggable\n"
                       + "WHERE taggable.[tba:tags] = $tag ";

        JcrTools tools = new JcrTools();
        Map<String, String> params = new HashMap<>();
        params.put("tag", tag);
        try {

            QueryResult result = JcrQueryUtil.query(getSession(), query, params);
            return JcrQueryUtil.queryResultToList(result, JcrObject.class);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Unable to find objects by tag " + tag, e);
        }

    }

}
