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

import org.apache.velocity.Template;

import java.util.Map;
import java.util.Optional;

/**
 * Created by sr186054 on 10/9/17.
 */
public interface VelocityService {

    Optional<Template> getTemplate(String name);

    boolean isRegistered(String name);

    boolean registerTemplate(String name, String template);

    void removeTemplate(String name);

    String mergeTemplate(String templateName, Map<String, Object> properties);

    VelocityEmailTemplate mergeEmailTemplate(String templateName, Map<String, Object> properties);

    VelocityEmailTemplate mergeEmailTemplate(VelocityTemplate template, VelocityTemplate.ID id, Map<String, Object> properties, VelocityEmailTemplate defaultTemplate);

    boolean registerEmailTemplate(String name, String subject, String template);

    String testTemplate(String template, Map<String,Object> properties);

    boolean ensureRegisteredTemplate(VelocityTemplate template);

    boolean registerWithVelocity(VelocityTemplate template);
}
