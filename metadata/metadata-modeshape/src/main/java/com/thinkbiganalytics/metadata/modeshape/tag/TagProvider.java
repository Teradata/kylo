package com.thinkbiganalytics.metadata.modeshape.tag;

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
 * Created by sr186054 on 6/8/16.
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
