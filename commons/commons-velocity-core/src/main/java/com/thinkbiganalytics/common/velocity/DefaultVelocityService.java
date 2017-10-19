package com.thinkbiganalytics.common.velocity;
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
import com.thinkbiganalytics.common.velocity.service.VelocityService;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by sr186054 on 10/4/17.
 */
public class DefaultVelocityService implements VelocityService {

    private static final Logger log = LoggerFactory.getLogger(DefaultVelocityService.class);
    @Inject
    @Named("kyloVelocityEngine")
    private VelocityEngine velocityEngine;

    private Set<String> registeredTemplates = new HashSet<>();

    @Override
    public Optional<Template> getTemplate(String name) {
        try {
            Optional<Template> t = Optional.of(velocityEngine.getTemplate(name));
            if (t.isPresent()) {
                registeredTemplates.add(t.get().getName());
            }
            return t;
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        } catch (ParseErrorException e) {
            //WARN!!!
            return Optional.empty();
        }
    }

    @Override
    public boolean isRegistered(String name) {
        return registeredTemplates.contains(name);
    }

    @Override
    public boolean registerTemplate(String name, String template) {
        StringResourceRepository repo = StringResourceLoader.getRepository(); //(StringResourceRepository) velocityEngine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.removeStringResource(name);
        repo.putStringResource(name, template);
        return getTemplate(name).isPresent();
    }

    @Override
    public void removeTemplate(String name) {
        StringResourceRepository repo = StringResourceLoader.getRepository(); //(StringResourceRepository) velocityEngine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.removeStringResource(name);
    }

    @Override
    public String mergeTemplate(String templateName, Map<String, Object> properties) {
        Optional<Template> template = getTemplate(templateName);
        if (template.isPresent()) {
            return parseVelocityTemplate(properties, template);
        }
        return null;
    }

    @Override
    public VelocityEmailTemplate mergeEmailTemplate(String templateName, Map<String, Object> properties) {
        Optional<Template> template = getTemplate(templateName);
        Optional<Template> subject = getTemplate(getEmailSubjectName(templateName));
        String emailBody = null;
        String emailSubject = null;
        if (template.isPresent()) {
            emailBody = parseVelocityTemplate(properties, template);
        }
        if (subject.isPresent()) {
            emailSubject = parseVelocityTemplate(properties, subject);
        }
        return new VelocityEmailTemplate(emailSubject, emailBody);
    }

    private String getEmailSubjectName(String templateName) {
        return templateName += "_subject";
    }

    @Override
    public boolean registerEmailTemplate(String name, String subject, String template) {
        String subjectTemplateName = getEmailSubjectName(name);
        try {
            registerTemplate(name, template);
            registerTemplate(subjectTemplateName, subject);
        } catch (Exception e) {
            removeTemplate(name);
            removeTemplate(subjectTemplateName);
            throw e;
        }
        return true;

    }


    private String parseVelocityTemplate(Map<String, Object> properties, Optional<Template> template) {
        VelocityContext context = new VelocityContext();
        if (properties != null) {
            properties.entrySet().stream().forEach(e -> {
                context.put(e.getKey(), e.getValue());
            });
        }
        StringWriter writer = new StringWriter();
        template.get().merge(context, writer);
        return writer.toString();
    }

    public String testTemplate(String template, Map<String, Object> properties) {
        String name = "test-" + RandomStringUtils.random(10) + "-" + System.nanoTime();

        String resolvedTemplate = template;
        try {
            registerTemplate(name, template);
            resolvedTemplate = mergeTemplate(name, properties);
        } catch (Exception e) {
            resolvedTemplate = "ERROR!!! " + e.getMessage();
        }
        removeTemplate(name);
        return resolvedTemplate;

    }

    public boolean ensureRegisteredTemplate(VelocityTemplate template) {
        if (!isRegistered(template.getSystemName())) {
            return registerWithVelocity(template);
        }
        return true;
    }


    public boolean registerWithVelocity(VelocityTemplate template) {
        boolean registered = false;
        if (StringUtils.isNotBlank(template.getTitle())) {
            registered = registerEmailTemplate(template.getSystemName(), template.getTitle(), template.getTemplate());
        } else {
            registered = registerTemplate(template.getName(), template.getTemplate());
        }
        return registered;
    }

    public VelocityEmailTemplate mergeEmailTemplate(VelocityTemplate template, VelocityTemplate.ID id, Map<String, Object> properties, VelocityEmailTemplate defaultTemplate) {
        //ensure they are registered with the Velocity Engine
        if (template == null || (template != null && !ensureRegisteredTemplate(template))) {
            if (defaultTemplate != null) {
                String testName = "default_" + id != null ? id.toString() : UUID.randomUUID().toString();
                registerEmailTemplate(testName, defaultTemplate.getSubject(), defaultTemplate.getBody());
                log.debug("Unable to find template.  Using the default template ");
                return mergeEmailTemplate(testName, properties);
            } else {
                return null;
            }
        } else {
            log.debug("Merging velocity template {} ", template.getName());
            return mergeEmailTemplate(template.getSystemName(), properties);
        }
    }


}
