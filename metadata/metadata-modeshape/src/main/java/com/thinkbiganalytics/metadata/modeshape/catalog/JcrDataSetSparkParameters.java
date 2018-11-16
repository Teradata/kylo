/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.catalog;

/*-
 * #%L
 * kylo-metadata-modeshape
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
        return getProperty(FORMAT, "");
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#setFormat(java.lang.String)
     */
    @Override
    public void setFormat(String format) {
        setProperty(FORMAT, format);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getFiles()
     */
    @Override
    public List<String> getFiles() {
        return JcrPropertyUtil.getPropertyValuesList(getNode(), FILES);
    }

    public void setJars(List<String> jars){
        if(jars != null){
            jars.stream().forEach(jar -> addJar(jar));
        }
    }

    public void addJar(String jar){
        JcrPropertyUtil.addToSetProperty(getNode(),JARS,jar);
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
                .filter(entry -> ! entry.getKey().startsWith("jcr:"))
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().toString()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#addOption(java.lang.String, java.lang.String)
     */
    @Override
    public boolean addOption(String name, String value) {
        String existing = getProperty(name, null);
        setProperty(name, value);
        return existing == null || ! existing.equals(value);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#removeOption(java.lang.String)
     */
    @Override
    public String removeOption(String name) {
        String existing = getProperty(name, null);
        removeProperty(name);
        return existing;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#clearOptions()
     */
    @Override
    public boolean clearOptions() {
        Map<String, Object> existing = getProperties();
        clearAdditionalProperties();
        return existing.size() > 0;
    }
}
