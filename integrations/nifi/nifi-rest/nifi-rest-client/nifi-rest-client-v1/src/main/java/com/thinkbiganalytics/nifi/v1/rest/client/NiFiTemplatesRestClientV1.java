package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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

import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiTemplatesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiTemplatesRestClient;

import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.entity.TemplateEntity;
import org.apache.nifi.web.api.entity.TemplatesEntity;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiTemplatesRestClient} for communicating with NiFi v1.0.
 */
public class NiFiTemplatesRestClientV1 extends AbstractNiFiTemplatesRestClient {

    private static final Logger log = LoggerFactory.getLogger(NiFiTemplatesRestClientV1.class);

    /**
     * Base path for template requests
     */
    private static final String BASE_PATH = "/templates/";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiTemplatesRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiTemplatesRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Override
    public boolean delete(@Nonnull final String id) {
        try {
            client.delete(BASE_PATH + id, null, TemplateEntity.class);
            return true;
        } catch (final NotFoundException e) {
            return false;
        }
    }

    @Nonnull
    @Override
    public Optional<String> download(@Nonnull final String id) {
        try {
            return Optional.of(client.get(BASE_PATH + id + "/download", null, String.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TemplateDTO> findByName(@Nonnull final String name) {
        Optional<TemplateDTO> dto = findAll().stream()
            .filter(template -> template.getName().equalsIgnoreCase(name))
            .findFirst();
        if (dto.isPresent()) {
            TemplateDTO templateDTO = dto.get();
            //populate the snippet
            if (templateDTO != null) {
                dto = findById(templateDTO.getId());
            }
        }
        return dto;
    }

    @Nonnull
    @Override
    public Set<TemplateDTO> findAll() {
        return getNifiTemplates().map(TemplateEntity::getTemplate)
            .collect(Collectors.toSet());
    }

    private Stream<TemplateEntity> getNifiTemplates() {
        return client.get("/flow/templates", null, TemplatesEntity.class)
            .getTemplates().stream();
    }

    @Nonnull
    @Override
    public Optional<TemplateDTO> findById(@Nonnull final String id) {
        try {
            final TemplateDTO template = client.get(BASE_PATH + id + "/download", null, TemplateDTO.class, false);
            if (template == null) {
                return Optional.empty();
            } else {
                template.setId(id);
                return Optional.of(template);
            }
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    protected TemplateDTO upload(@Nonnull final MultiPart template) {
        return client.postMultiPart("/process-groups/root/templates/upload", template, TemplateEntity.class).getTemplate();
    }


}
