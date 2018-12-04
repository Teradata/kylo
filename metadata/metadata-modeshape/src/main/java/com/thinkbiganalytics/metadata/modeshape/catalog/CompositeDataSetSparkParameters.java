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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A composite of multiple DataSetSparkParameters provided in precedence order.
 */
public class CompositeDataSetSparkParameters implements DataSetSparkParameters {
    
    private List<DataSetSparkParameters> paramsChain = new ArrayList<>();
    
    /**
     * Constructs an instance from a chain of DataSetSparkParameters.  The precedence
     * of any parameter is based on the order of the provided list from index 0..n.
     * @param chain the DataSetSparkParameters
     */
    public CompositeDataSetSparkParameters(List<DataSetSparkParameters> chain) {
        this.paramsChain.addAll(chain);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getFormat()
     */
    @Override
    public String getFormat() {
        return this.paramsChain.stream()
                .filter(params -> params.getFormat() != null)
                .map(params -> params.getFormat())
                .findFirst()
                .orElse(null);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#setFormat(java.lang.String)
     */
    @Override
    public void setFormat(String format) {
        throw new UnsupportedOperationException("This DataSetSparkParameters instance is unmodifiable");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getFiles()
     */
    @Override
    public List<String> getFiles() {
        return new ArrayList<>(this.paramsChain.stream().flatMap(params -> params.getFiles().stream()).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getJars()
     */
    @Override
    public List<String> getJars() {
        return new ArrayList<>(this.paramsChain.stream().flatMap(params -> params.getJars().stream()).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getPaths()
     */
    @Override
    public List<String> getPaths() {
        return new ArrayList<>(this.paramsChain.stream().flatMap(params -> params.getPaths().stream()).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#getOptions()
     */
    @Override
    public Map<String, String> getOptions() {
        return this.paramsChain.stream()
                .flatMap(params -> params.getOptions().entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (existingVal, newVal) -> existingVal));  // existing values in the map take precedence.
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#addOption(java.lang.String, java.lang.String)
     */
    @Override
    public boolean addOption(String name, String value) {
        // Updates not supported
        throw new UnsupportedOperationException("Updates not supported");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#removeOption(java.lang.String)
     */
    @Override
    public String removeOption(String name) {
        // Updates not supported
        throw new UnsupportedOperationException("Updates not supported");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.catalog.DataSetSparkParameters#clearOptions()
     */
    @Override
    public boolean clearOptions() {
        // Updates not supported
        throw new UnsupportedOperationException("Updates not supported");
    }
}
