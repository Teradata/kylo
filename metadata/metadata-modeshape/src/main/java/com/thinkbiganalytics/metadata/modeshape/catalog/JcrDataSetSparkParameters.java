/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog;

import com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.PropertiedMixin;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.jcr.Node;

/**
 *
 */
public class JcrDataSetSparkParameters extends JcrObject implements DataSetSparkParameters, PropertiedMixin {
    
    public static final String NODE_TYPE = "tba:DataSetSparkParams";
    public static final String FORMAT = "tba:format";
    public static final String PATHS = "tba:paths";
    public static final String FILES = "tba:files";
    public static final String JARS = "tba:jars";

    /**
     * @param node
     */
    public JcrDataSetSparkParameters(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getFormat()
     */
    @Override
    public String getFormat() {
        return getProperty(FORMAT);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getFiles()
     */
    @Override
    public List<String> getFiles() {
        return JcrPropertyUtil.getPropertyValuesList(getNode(), FILES);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getJars()
     */
    @Override
    public List<String> getJars() {
        return JcrPropertyUtil.getPropertyValuesList(getNode(), JARS);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getPaths()
     */
    @Override
    public List<String> getPaths() {
        return JcrPropertyUtil.getPropertyValuesList(getNode(), PATHS);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getOptions()
     */
    @Override
    public Map<String, String> getOptions() {
        return getProperties().entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toString()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#addOption(java.lang.String, java.lang.String)
     */
    @Override
    public boolean addOption(String name, String value) {
        String existing = getProperty(name);
        setProperty(name, value);
        return existing == null || ! existing.equals(value);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#removeOption(java.lang.String)
     */
    @Override
    public String removeOption(String name) {
        String existing = getProperty(name);
        removeProperty(name);
        return existing;
    }

}
