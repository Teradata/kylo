package com.thinkbiganalytics.kylo.catalog.rest.model;

/*-
 * #%L
 * kylo-catalog-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiOption {

    private String key;
    private String type;
    private Boolean required;
    private boolean sensitive = false;
    private String label;
    private Integer flex;
    private List<UiSelectOption> selections;
    private String value;
    private String hint;
    private List<UiOptionValidator> validators;

    public UiOption() {}

    public UiOption(@Nonnull final UiOption other) {
        key = other.key;
        type = other.type;
        required = other.required;
        sensitive = other.sensitive;
        label = other.label;
        flex = other.flex;
        if(other.selections != null){
            List<UiSelectOption> selections = new ArrayList<>();
            for(UiSelectOption o: other.selections){
                selections.add(new UiSelectOption(o));
            }
            this.selections = selections;
        }
        value = other.value;
        hint = other.hint;
        if(other.validators != null){
            List<UiOptionValidator> validators = new ArrayList<>();
            for(UiOptionValidator o: other.validators){
                validators.add(new UiOptionValidator(o));
            }
            this.validators = validators;
        }
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public boolean isSensitive() {
        return sensitive;
    }
    
    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public List<UiSelectOption> getSelections() {
        return selections;
    }

    public void setSelections(List<UiSelectOption> selections) {
        this.selections = selections;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getFlex() {
        return flex;
    }

    public void setFlex(Integer flex) {
        this.flex = flex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<UiOptionValidator> getValidators() {
        return validators;
    }

    public void setValidators(List<UiOptionValidator> validators) {
        this.validators = validators;
    }
}
