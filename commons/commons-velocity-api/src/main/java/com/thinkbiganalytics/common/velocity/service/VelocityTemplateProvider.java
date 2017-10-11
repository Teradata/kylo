package com.thinkbiganalytics.common.velocity.service;
/*-
 * #%L
 * kylo-commons-velocity-api
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 10/4/17.
 */
public interface VelocityTemplateProvider {

    List<? extends VelocityTemplate> findAll();

    List<? extends VelocityTemplate> findByType(String type);

    VelocityTemplate findDefault(String type);

    List<? extends VelocityTemplate> findEnabledByType(String type);

    VelocityTemplate findById(VelocityTemplate.ID id);

    VelocityTemplate.ID resolveId(Serializable id);

    VelocityTemplate save(VelocityTemplate template);

    String testTemplate(String template, Map<String,Object> properties);

    VelocityEmailTemplate mergeEmailTemplate(String velocityTemplateId,Map<String,Object>properties, VelocityEmailTemplate defaultTemplate);
}
