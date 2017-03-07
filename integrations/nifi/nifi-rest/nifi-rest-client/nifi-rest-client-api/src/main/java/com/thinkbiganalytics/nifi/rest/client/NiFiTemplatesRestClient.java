package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exposes the NiFi Templates REST endpoint as a Java class.
 */
public interface NiFiTemplatesRestClient {

    /**
     * Imports a template.
     *
     * @param name the name of the template
     * @param xml  an XML document representing the template
     * @return the new template
     */
    @Nonnull
    TemplateDTO create(@Nullable String name, @Nonnull String xml);

    /**
     * Deletes a template.
     *
     * @param id the template id
     * @return {@code true} if the template was found, or {@code false} otherwise
     */
    boolean delete(@Nonnull String id);

    /**
     * Exports a template.
     *
     * @param id the template id
     * @return the template, if found
     */
    @Nonnull
    Optional<String> download(@Nonnull String id);

    /**
     * Gets all templates. Only the basic template details are included.
     *
     * @return all templates
     */
    @Nonnull
    Set<TemplateDTO> findAll();

    /**
     * Gets the template with the specified id. The flow snippet is included in the response.
     *
     * @param id the template id
     * @return the template, if found
     */
    @Nonnull
    Optional<TemplateDTO> findById(@Nonnull String id);

    /**
     * Gets all templates that have an input port with the specified name. The flow snippet is included in the response.
     *
     * @param inputPortName the name of the input port
     * @return the matching templates
     */
    @Nonnull
    Set<TemplateDTO> findByInputPortName(@Nonnull String inputPortName);

    /**
     * Gets the template with the specified name. Only the basic template details are included.
     *
     * @param name the name of the template
     * @return the template, if found
     */
    @Nonnull
    Optional<TemplateDTO> findByName(@Nonnull String name);

}
