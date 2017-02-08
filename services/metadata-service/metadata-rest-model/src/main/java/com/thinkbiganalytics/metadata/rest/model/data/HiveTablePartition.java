/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HiveTablePartition {

    private String name;
    private String formula;
    private List<String> values = new ArrayList<>();

    public HiveTablePartition() {
    }

    public HiveTablePartition(String name, String formula, List<String> values) {
        super();
        this.name = name;
        this.formula = formula;
        this.values = new ArrayList<>(values);
    }

    public HiveTablePartition(String name, String formula, String value, String... more) {
        super();
        this.name = name;
        this.formula = formula;
        this.values = Lists.asList(value, more);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void addValue(String value) {
        this.values.add(value);
    }

}
