package com.thinkbiganalytics.common.velocity.service;
/*-
 * #%L
 * kylo-commons-velocity-core
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

import com.thinkbiganalytics.common.velocity.model.VelocityEmailTemplate;
import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 10/9/17.
 */
public class InMemoryVelocityTemplateProvider implements VelocityTemplateProvider {

    Map<VelocityTemplate.ID, VelocityTemplate> map = new ConcurrentHashMap<>();

    @Inject
    private VelocityService velocityService;

    @Override
    public List<? extends VelocityTemplate> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public List<? extends VelocityTemplate> findByType(String type) {
        return map.values().stream().filter(t -> t.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
    }

    @Override
    public List<? extends VelocityTemplate> findEnabledByType(String type) {
        return map.values().stream().filter(t -> t.getType().equalsIgnoreCase(type) && t.isEnabled()).collect(Collectors.toList());
    }

    @Override
    public VelocityTemplate findById(VelocityTemplate.ID id) {
        return map.get(id);
    }

    @Override
    public VelocityTemplate findDefault(String type) {
        return map.values().stream().filter(t -> t.isDefault()).findFirst().orElse(null);
    }

    @Override
    public VelocityTemplate.ID resolveId(Serializable id) {
        return new VelocityTemplateId(id);
    }

    @Override
    public VelocityTemplate save(VelocityTemplate template) {
        map.put(template.getId(), template);
        return template;
    }

    @Override
    public String testTemplate(String template, Map<String, Object> properties) {
        return velocityService.testTemplate(template, properties);
    }

    @Override
    public VelocityEmailTemplate mergeEmailTemplate(String velocityTemplateId, Map<String, Object> properties, VelocityEmailTemplate defaultTemplate) {
        VelocityTemplate template = findById(resolveId(velocityTemplateId));
        VelocityTemplate.ID id = StringUtils.isNotBlank(velocityTemplateId) ? resolveId(velocityTemplateId) : null;
        return velocityService.mergeEmailTemplate(template, id, properties, defaultTemplate);
    }

    public class VelocityTemplateId implements VelocityTemplate.ID {

        private UUID uuid;

        public VelocityTemplateId(Serializable id) {
            if (id instanceof String) {
                uuid = UUID.fromString((String) id);
            } else if (id instanceof UUID) {
                uuid = (UUID) id;
            } else {
                uuid = UUID.randomUUID();
            }
        }

        public VelocityTemplateId create() {
            return new VelocityTemplateId(UUID.randomUUID());
        }

    }

}
