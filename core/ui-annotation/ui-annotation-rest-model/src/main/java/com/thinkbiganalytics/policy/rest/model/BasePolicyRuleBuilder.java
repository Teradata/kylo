package com.thinkbiganalytics.policy.rest.model;

/*-
 * #%L
 * thinkbig-ui-annotation-rest-model
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


import java.util.ArrayList;
import java.util.List;

/**
 * a builder for the User interface rule objects
 */
public abstract class BasePolicyRuleBuilder<T, B extends BasePolicyRuleBuilder> {

    protected String name;
    protected String displayName;
    protected String description;
    protected String shortDescription;
    protected List<FieldRuleProperty> properties;
    protected String objectClassType;
    protected Integer sequence;

    public BasePolicyRuleBuilder(String name) {
        this.name = name;
        this.displayName = name;
        this.properties = new ArrayList<>();

    }

    public B displayName(String displayName) {
        this.displayName = displayName;
        return (B) this;
    }

    public B description(String description) {
        this.description = description;
        return (B) this;
    }

    public B shortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
        return (B) this;
    }

    public B addProperty(FieldRuleProperty property) {
        this.properties.add(property);
        return (B) this;
    }

    public B addProperties(List<FieldRuleProperty> properties) {
        this.properties.addAll(properties);
        return (B) this;
    }

    public B objectClassType(String clazz) {
        this.objectClassType = clazz;
        return (B) this;
    }

    public B objectClassType(Class clazz) {
        this.objectClassType = clazz.getName();
        return (B) this;
    }

    public B shortDescription(Integer sequence) {
        this.sequence = sequence;
        return (B) this;
    }

    public abstract T build();


}
