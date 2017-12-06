package com.thinkbiganalytics.ui.api.template;

/*-
 * #%L
 * kylo-ui-api
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.metadata.MetadataField;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default impl for the TemplateTableOption
 * This is used for parsing the json configuration
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTemplateTableOption implements TemplateTableOption {


    private String preStepperTemplateUrl;
    private String preFeedDetailsTemplateUrl;
    private int totalPreSteps = 0;


    private String stepperTemplateUrl;
    private String feedDetailsTemplateUrl;

    private int totalCoreSteps;
    private int totalSteps;

    private List<AnnotatedFieldProperty<MetadataField>> metadataProperties;

    private String description;

    private String displayName;

    private String type;

    private String initializeScript;

    private String initializeServiceName;

    private String resourceContext;

    public String getPreStepperTemplateUrl() {
        return preStepperTemplateUrl;
    }

    public void setPreStepperTemplateUrl(String preStepperTemplateUrl) {
        this.preStepperTemplateUrl = preStepperTemplateUrl;
    }

    @Override
    public int getTotalPreSteps() {
        return totalPreSteps;
    }

    public void setTotalPreSteps(int totalPreSteps) {
        this.totalPreSteps = totalPreSteps;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return description;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Nonnull
    @Override
    public String getType() {
        return type;
    }

    @Nullable
    @Override
    public String getStepperTemplateUrl() {
        return stepperTemplateUrl;
    }

    public void setStepperTemplateUrl(String stepperTemplateUrl) {
        this.stepperTemplateUrl = stepperTemplateUrl;
    }

    @Nullable
    @Override
    public String getFeedDetailsTemplateUrl() {
        return feedDetailsTemplateUrl;
    }

    public void setFeedDetailsTemplateUrl(String feedDetailsTemplateUrl) {
        this.feedDetailsTemplateUrl = feedDetailsTemplateUrl;
    }

    @Override
    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Nonnull
    @Override
    @JsonDeserialize(using = AnnotatedFieldPropertyListDeserializer.class)
    public List<AnnotatedFieldProperty<MetadataField>> getMetadataProperties() {
        return metadataProperties;
    }

    public void setMetadataProperties(List<AnnotatedFieldProperty<MetadataField>> metadataProperties) {
        this.metadataProperties = metadataProperties;
    }

    @Override
    public String getInitializeScript() {
        return initializeScript;
    }

    public void setInitializeScript(String initializeScript) {
        this.initializeScript = initializeScript;
    }


    public String getInitializeServiceName() {
        return initializeServiceName;
    }

    public void setInitializeServiceName(String initializeServiceName) {
        this.initializeServiceName = initializeServiceName;
    }

    @Override
    public String getResourceContext() {
        return resourceContext;
    }

    public void setResourceContext(String resourceContext) {
        this.resourceContext = resourceContext;
    }

    public int getTotalCoreSteps() {
        return totalCoreSteps;
    }

    public void setTotalCoreSteps(int totalCoreSteps) {
        this.totalCoreSteps = totalCoreSteps;
    }

    @Override
    public String getPreFeedDetailsTemplateUrl() {
        return preFeedDetailsTemplateUrl;
    }

    public void setPreFeedDetailsTemplateUrl(String preFeedDetailsTemplateUrl) {
        this.preFeedDetailsTemplateUrl = preFeedDetailsTemplateUrl;
    }

    public void updateTotalSteps(){
        this.totalSteps = totalPreSteps+totalCoreSteps;
    }


}
