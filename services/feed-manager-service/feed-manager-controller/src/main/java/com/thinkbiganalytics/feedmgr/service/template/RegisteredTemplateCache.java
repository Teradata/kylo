package com.thinkbiganalytics.feedmgr.service.template;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import java.util.List;

/**
 * Cache to store processor data to speed up saving of feeds when applying the derived datasources
 * @see com.thinkbiganalytics.feedmgr.service.feed.datasource.DerivedDatasourceFactory
 */
public class RegisteredTemplateCache {

    private Cache<String, RegisteredTemplate> registeredTemplateCache = CacheBuilder.newBuilder().build();

    private Cache<String, List<RegisteredTemplate.Processor>> registeredTemplateProcessorCache = CacheBuilder.newBuilder().build();


    public void putProcessors(String templateId, List<RegisteredTemplate.Processor> processors){
        registeredTemplateProcessorCache.put(templateId,processors);
    }

    public List<RegisteredTemplate.Processor> getProcessors(String templateId){
        return registeredTemplateProcessorCache.getIfPresent(templateId);
    }

    public void invalidateProcessors(String templateId){
        registeredTemplateProcessorCache.invalidate(templateId);
    }

    public void invalidateAllProcessors()
    {
        registeredTemplateProcessorCache.invalidateAll();
    }

    //createFeedBuilderCache

}
