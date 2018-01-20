package com.thinkbiganalytics.feedmgr.service.template;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by sr186054 on 7/7/17.
 */
public class NiFiTemplateCache {

    private static final Logger log = LoggerFactory.getLogger(NiFiTemplateCache.class);

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    /**
     * A cache of the NiFi template properties.
     * Properties for a given template are cached and updated when the template changes
     */
    private Cache<String, TemplatePropertiesCache> templatePropertiesCache = CacheBuilder.newBuilder().build();

    private Cache<String, TemplateDTO> templateByIdCache = CacheBuilder.newBuilder().build();
    private Cache<String, TemplateDTO> templateByNameCache = CacheBuilder.newBuilder().build();

    public NiFiTemplateCache() {

    }

    private Set<TemplateDTO> getTemplateSummaries() {
        return nifiRestClient.getNiFiRestClient().templates().findAll();
    }

    private TemplateDTO findSummaryById(String templateId) {
        return nifiRestClient.getNiFiRestClient().templates().findAll().stream().filter(t -> t.getId().equalsIgnoreCase(templateId)).findFirst().orElse(null);
    }

    private TemplateDTO findSummaryByName(String name) {
        return nifiRestClient.getNiFiRestClient().templates().findAll().stream().filter(t -> t.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean needsUpdate(TemplateDTO summary, TemplateDTO cached) {
        return ((summary != null && cached == null) || (summary != null && cached != null && summary.getTimestamp().getTime() > cached.getTimestamp().getTime()));
    }

    public TemplateDTO getCachedTemplateById(String nifiTemplateId) {
        return templateByIdCache.getIfPresent(nifiTemplateId);
    }

    public TemplateDTO getCachedTemplateByName(String name) {
        return templateByIdCache.getIfPresent(name);
    }

    /**
     * Gets the populated Template with flow information first looking at the cache and then getting it from NiFi if it is stale
     *
     * @param nifiTemplateId the nifi template id
     * @param templateName   the name of the template
     * @return the populated template
     */
    public TemplateDTO geTemplate(String nifiTemplateId, String templateName) {
        TemplateDTO templateDTO = null;
        TemplateDTO nifiTemplate = null;
        if (StringUtils.isNotBlank(nifiTemplateId)) {
            templateDTO = templateByIdCache.getIfPresent(nifiTemplateId);
            nifiTemplate = findSummaryById(nifiTemplateId);
        }
        if (templateDTO == null && StringUtils.isNotBlank(templateName)) {
            templateDTO = templateByNameCache.getIfPresent(templateName);
        }
        if (nifiTemplate == null && StringUtils.isNotBlank(templateName)) {
            nifiTemplate = findSummaryByName(templateName);
        }
        if (nifiTemplate != null) {
            if (StringUtils.isBlank(templateName)) {
                templateName = nifiTemplate.getName();
            }
            if (needsUpdate(nifiTemplate, templateDTO)) {
                log.info("Fetching NiFi template from NiFi {}, {}", nifiTemplateId, templateName);
                templateDTO = getPopulatedTemplate(nifiTemplateId, templateName);
                if (templateDTO != null) {
                    log.info("Caching NiFi template {}, {}", nifiTemplateId, templateName);
                    if (StringUtils.isNotBlank(nifiTemplateId)) {
                        templateByIdCache.put(nifiTemplateId, templateDTO);
                    }
                    if (StringUtils.isNotBlank(templateName)) {
                        templateByNameCache.put(templateName, templateDTO);
                    }
                }
            } else {
                log.info("Returning Cached NiFi template {}, {}", nifiTemplateId, templateName);
            }
        }

        return templateDTO;

    }


    /**
     * Gets a template from NiFi populated with flow information
     *
     * @param nifiTemplateId   the nifi template id
     * @param nifiTemplateName the name of the template
     * @return a populated template
     */
    private TemplateDTO getPopulatedTemplate(String nifiTemplateId, String nifiTemplateName) {
        TemplateDTO templateDTO = null;
        try {
            try {
                templateDTO = nifiRestClient.getTemplateById(nifiTemplateId);
            } catch (NifiComponentNotFoundException e) {
                //this is fine... we can safely proceeed if not found.
            }
            if (templateDTO == null) {
                templateDTO = nifiRestClient.getTemplateByName(nifiTemplateName);
                if (templateDTO != null) {
                    //getting the template by the name will not get all the properties.
                    //refetch it by the name to get the FlowSnippet
                    //populate the snippet
                    templateDTO = nifiRestClient.getTemplateById(templateDTO.getId());

                }
            }

        } catch (NifiClientRuntimeException e) {
            log.error("Error attempting to get the NifiTemplate TemplateDTO object for {} using nifiTemplateId of {} ", nifiTemplateName,
                      nifiTemplateId);
        }
        return templateDTO;
    }


    /**
     * Return the NiFi {@link TemplateDTO} object fully populated and sets this to the incoming {@link RegisteredTemplate#nifiTemplate}
     * If at first looking at the {@link RegisteredTemplate#nifiTemplateId} it is unable to find the template it will then fallback and attempt to find the template by its name
     *
     * @param registeredTemplate a registered template object
     * @return the NiFi template
     */
    public TemplateDTO ensureNifiTemplate(RegisteredTemplate registeredTemplate) {
        if (registeredTemplate.getNifiTemplate() == null) {
            TemplateDTO templateDTO = geTemplate(registeredTemplate.getNifiTemplateId(), registeredTemplate.getTemplateName());
            if (templateDTO != null) {
                registeredTemplate.setNifiTemplate(templateDTO);
                registeredTemplate.setNifiTemplateId(registeredTemplate.getNifiTemplate().getId());
            }

        }
        return registeredTemplate.getNifiTemplate();
    }

    private String cacheKey(TemplateDTO templateDTO, boolean includePropertyDescriptors) {
        return templateDTO.getName() + includePropertyDescriptors;
    }

    /**
     * Cache the Template properties.  Return the cached properties if the template hasnt been updated
     *
     * @param templateDTO                the nifi template
     * @param includePropertyDescriptors true to include descriptors, false to not include the descriptors
     * @return a list of properties
     */
    public List<NifiProperty> getTemplateProperties(TemplateDTO templateDTO, boolean includePropertyDescriptors, RegisteredTemplate registeredTemplate) {
        String cacheKey = cacheKey(templateDTO, includePropertyDescriptors);
        TemplatePropertiesCache cachedProperties = templatePropertiesCache.getIfPresent(cacheKey);
        if (cachedProperties == null || templateDTO.getTimestamp().getTime() > cachedProperties.getLastUpdated()) {
            List<NifiProperty> properties = nifiRestClient.getPropertiesForTemplate(templateDTO, includePropertyDescriptors);
            if (cachedProperties == null) {
                cachedProperties = new TemplatePropertiesCache(templateDTO.getId(), includePropertyDescriptors, templateDTO.getTimestamp().getTime());
                templatePropertiesCache.put(cacheKey, cachedProperties);
            }
            if (registeredTemplate != null) {
                //merge in the saved state of the template
                NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties(),
                                                                    NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
            }
            cachedProperties.setProperties(properties);
            cachedProperties.setLastUpdated(templateDTO.getTimestamp().getTime());
        }
        return cachedProperties.getProperties();

    }

    public void updateSelectedProperties(RegisteredTemplate registeredTemplate) {
        if (registeredTemplate.getNifiTemplate() != null) {
            //   Map<String, NifiProperty> selectedProperties = registeredTemplate.getProperties().stream().filter(p -> p.isSelected()).collect(Collectors.toMap(p -> p.getProcessorNameTypeKey(), p -> p));
            List<NifiProperty> cachedProperties = getTemplateProperties(registeredTemplate.getNifiTemplate(), true, registeredTemplate);
            cachedProperties.stream().forEach(p -> p.setSelected(false));
            NifiPropertyUtil.matchAndSetPropertyByProcessorName(cachedProperties, registeredTemplate.getProperties(),
                                                                NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
            //also update the non property descriptor cache if present
            String cacheKey = cacheKey(registeredTemplate.getNifiTemplate(), false);
            TemplatePropertiesCache cache = templatePropertiesCache.getIfPresent(cacheKey);
            if (cache != null) {
                cache.getProperties().stream().forEach(p -> p.setSelected(false));
                NifiPropertyUtil.matchAndSetPropertyByProcessorName(cache.getProperties(), registeredTemplate.getProperties(),
                                                                    NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
            }

            //    cachedProperties.forEach(p -> p.setSelected(selectedProperties.containsKey(p.getProcessorNameTypeKey())));
        }
    }


    public class TemplatePropertiesCache {

        private String templateId;
        private boolean includePropertyDescriptors;
        private Long lastUpdated;
        private List<NifiProperty> properties;

        public TemplatePropertiesCache(String templateId, boolean includePropertyDescriptors, Long lastUpdated) {
            this.templateId = templateId;
            this.includePropertyDescriptors = includePropertyDescriptors;
            this.lastUpdated = lastUpdated;
        }

        public String getTemplateId() {
            return templateId;
        }

        public boolean isIncludePropertyDescriptors() {
            return includePropertyDescriptors;
        }

        public Long getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(Long lastUpdated) {
            this.lastUpdated = lastUpdated;
        }

        public List<NifiProperty> getProperties() {
            return properties;
        }

        public void setProperties(List<NifiProperty> properties) {
            this.properties = properties;
        }
    }


}
