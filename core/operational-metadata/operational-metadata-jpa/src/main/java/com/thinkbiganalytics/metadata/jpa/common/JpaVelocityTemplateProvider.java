package com.thinkbiganalytics.metadata.jpa.common;
/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.google.common.base.Throwables;
import com.thinkbiganalytics.common.velocity.model.VelocityEmailTemplate;
import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;
import com.thinkbiganalytics.common.velocity.service.VelocityService;
import com.thinkbiganalytics.common.velocity.service.VelocityTemplateProvider;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 10/4/17.
 */
@Service
public class JpaVelocityTemplateProvider implements VelocityTemplateProvider {

    private VelocityTemplateRepository velocityTemplateRepository;

    @Inject
    private VelocityService velocityService;

    @Autowired
    public JpaVelocityTemplateProvider(VelocityTemplateRepository velocityTemplateRepository) {
        this.velocityTemplateRepository = velocityTemplateRepository;
    }

    @Override
    public List<? extends VelocityTemplate> findAll() {
        return getRegisteredTemplates();
    }

    @Override
    public List<? extends VelocityTemplate> findByType(String type) {
        return getRegisteredTemplates().stream().filter(t -> t.getType().equals(type)).collect(Collectors.toList());
    }

    public List<? extends VelocityTemplate> findEnabledByType(String type) {
        return findByType(type).stream().filter(t -> t.isEnabled()).collect(Collectors.toList());
    }

    public List<VelocityTemplate> getRegisteredTemplates() {
        List<VelocityTemplate> list = new ArrayList<>();
        List<? extends VelocityTemplate> dbList = velocityTemplateRepository.findAll();
        list.addAll(dbList);
        return list;
    }

    public VelocityTemplate findById(VelocityTemplate.ID id) {
        return getRegisteredTemplates().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    private boolean ensureRegisteredTemplate(VelocityTemplate template) {
        return velocityService.ensureRegisteredTemplate(template);
    }


    public VelocityTemplate findDefault(String type) {
        return velocityTemplateRepository.findDefault(type);
    }

    private boolean registerWithVelocity(VelocityTemplate template) {
        return velocityService.registerWithVelocity(template);
    }

    public VelocityTemplate save(VelocityTemplate template) {
        if (template.getId() == null) {
            ((JpaVelocityTemplate) template).setId(JpaVelocityTemplate.JpaVelocityTemplateId.create());
            ((JpaVelocityTemplate) template).setSystemName(template.getName());
            VelocityTemplate check = velocityTemplateRepository.findBySystemName(template.getSystemName());
            if (check != null) {
                throw new RuntimeException("Error a template with this Name already exists.  Please provide a new name");
            }
        }

        //ensure the default flag is set correctly
        JpaVelocityTemplate existingDefault = velocityTemplateRepository.findDefault(template.getType());
        if (existingDefault != null) {
            if (template.isDefault() && !existingDefault.getId().equals(template.getId())) {
                existingDefault.setDefault(false);
                velocityTemplateRepository.save(existingDefault);
            }
            ((JpaVelocityTemplate) template).setDefault(template.isDefault());
        } else {
            ((JpaVelocityTemplate) template).setDefault(true);
        }

        template = velocityTemplateRepository.save((JpaVelocityTemplate) template);
        try {
            registerWithVelocity(template);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return template;

    }


    public VelocityTemplate.ID resolveId(Serializable id) {
        return new JpaVelocityTemplate.JpaVelocityTemplateId(id);
    }


    public String testTemplate(String template, Map<String, Object> properties) {
        return velocityService.testTemplate(template, properties);
    }


    @Override
    public VelocityEmailTemplate mergeEmailTemplate(String velocityTemplateId, Map<String, Object> properties, VelocityEmailTemplate defaultTemplate) {
        VelocityTemplate template = findById(resolveId(velocityTemplateId));
        VelocityTemplate.ID id = StringUtils.isNotBlank(velocityTemplateId) ? resolveId(velocityTemplateId) : null;

        return velocityService.mergeEmailTemplate(template, id, properties, defaultTemplate);
    }


}
