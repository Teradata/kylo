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

import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

/**
 * Provides a standard implementation of {@link NiFiTemplatesRestClient} that can be extended for different NiFi versions.
 */
public abstract class AbstractNiFiTemplatesRestClient implements NiFiTemplatesRestClient {

    @Nonnull
    @Override
    public TemplateDTO create(@Nullable final String name, @Nonnull final String xml) {
        // Build template body part
        final FormDataBodyPart templatePart = new FormDataBodyPart("template", xml, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        FormDataContentDisposition.FormDataContentDispositionBuilder disposition = FormDataContentDisposition.name(templatePart.getName());
        disposition.fileName((name == null) ? "import_template_" + System.currentTimeMillis() : name);
        templatePart.setFormDataContentDisposition(disposition.build());

        // Combine parts
        MultiPart multiPart = new MultiPart();
        multiPart.bodyPart(templatePart);
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        // Upload template
        return upload(multiPart);
    }

    @Nonnull
    @Override
    public Set<TemplateDTO> findByInputPortName(@Nonnull final String inputPortName) {
        return findAll().stream()
            .map(template -> findById(template.getId()))
            .filter(template -> {
                final Set<PortDTO> ports = template
                    .map(TemplateDTO::getSnippet)
                    .map(FlowSnippetDTO::getInputPorts)
                    .orElse(Collections.emptySet());
                return ports.stream()
                    .filter(port -> port.getName().equalsIgnoreCase(inputPortName))
                    .findFirst()
                    .isPresent();
            })
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Optional<TemplateDTO> findByName(@Nonnull final String name) {
        return findAll().stream()
            .filter(template -> template.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    /**
     * Uploads a template.
     *
     * @param template the template to upload
     * @return the template
     */
    @Nonnull
    protected abstract TemplateDTO upload(@Nonnull MultiPart template);
}
