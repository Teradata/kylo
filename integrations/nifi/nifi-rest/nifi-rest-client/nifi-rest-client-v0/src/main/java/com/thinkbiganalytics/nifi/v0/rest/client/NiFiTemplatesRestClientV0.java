package com.thinkbiganalytics.nifi.v0.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v0
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

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiTemplatesRestClient} for communicating with NiFi v0.6.
 */
public class NiFiTemplatesRestClientV0 extends AbstractNiFiTemplatesRestClient {

    /**
     * Base path for template requests
     */
    private static final String BASE_PATH = "/controller/templates";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiTemplatesRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiTemplatesRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Override
    public boolean delete(@Nonnull final String id) {
        try {
            client.delete(BASE_PATH + "/" + id, null, TemplateEntity.class);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Nonnull
    @Override
    public Optional<String> download(@Nonnull final String id) {
        try {
            return Optional.of(client.get(BASE_PATH + "/" + id, null, String.class));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public Set<TemplateDTO> findAll() {
        return client.get(BASE_PATH, null, TemplatesEntity.class)
            .getTemplates();
    }

    @Nonnull
    @Override
    public Optional<TemplateDTO> findById(@Nonnull final String id) {
        try {
            final TemplateDTO template = client.get(BASE_PATH + "/" + id, null, TemplateDTO.class);
            template.setId(id);
            return Optional.of(template);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    protected TemplateDTO upload(@Nonnull final MultiPart template) {
        return client.postMultiPart(BASE_PATH, template, TemplateEntity.class)
            .getTemplate();
    }
}
