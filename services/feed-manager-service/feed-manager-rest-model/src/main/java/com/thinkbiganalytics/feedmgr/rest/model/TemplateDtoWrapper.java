package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.util.DateTimeAdapter;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.swagger.annotations.ApiModelProperty;

/**
 */
public class TemplateDtoWrapper extends TemplateDTO {

    private TemplateDTO templateDto;
    private String registeredTemplateId;

    public TemplateDtoWrapper() {
        templateDto = new TemplateDTO();
    }

    public TemplateDtoWrapper(TemplateDTO dto) {
        this.templateDto = dto;
    }

    public TemplateDTO getTemplateDto() {
        return templateDto;
    }

    public void setTemplateDto(TemplateDTO templateDto) {
        this.templateDto = templateDto;
    }

    public String getRegisteredTemplateId() {
        return registeredTemplateId;
    }

    public void setRegisteredTemplateId(String registeredTemplateId) {
        this.registeredTemplateId = registeredTemplateId;
    }

    @ApiModelProperty("The id of the template.")
    public String getId() {
        return templateDto.getId();
    }

    public void setId(String id) {
        templateDto.setId(id);
    }

    @ApiModelProperty("The name of the template.")
    public String getName() {
        return templateDto.getName();
    }

    public void setName(String name) {
        templateDto.setName(name);
    }

    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @ApiModelProperty("The timestamp when this template was created.")
    public Date getTimestamp() {
        return templateDto.getTimestamp();
    }

    public void setTimestamp(Date timestamp) {
        templateDto.setTimestamp(timestamp);
    }

    @ApiModelProperty("The URI for the template.")
    public String getUri() {
        return templateDto.getUri();
    }

    public void setUri(String uri) {
        templateDto.setUri(uri);
    }

    @ApiModelProperty("The contents of the template.")
    public FlowSnippetDTO getSnippet() {
        return templateDto.getSnippet();
    }

    public void setSnippet(FlowSnippetDTO snippet) {
        templateDto.setSnippet(snippet);
    }

    @ApiModelProperty("The description of the template.")
    public String getDescription() {
        return templateDto.getDescription();
    }

    public void setDescription(String description) {
        templateDto.setDescription(description);
    }
}
