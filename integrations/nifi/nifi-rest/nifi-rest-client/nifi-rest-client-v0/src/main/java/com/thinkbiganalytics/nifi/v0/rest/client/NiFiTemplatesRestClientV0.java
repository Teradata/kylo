package com.thinkbiganalytics.nifi.v0.rest.client;

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

    /** Base path for template requests */
    private static final String BASE_PATH = "/controller/templates";

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiTemplatesRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiTemplatesRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<TemplateDTO> delete(@Nonnull final String id) {
        try {
            return Optional.of(client.delete(BASE_PATH + "/" + id, null, TemplateEntity.class).getTemplate());
        } catch (NotFoundException e) {
            return Optional.empty();
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
            return Optional.of(client.get(BASE_PATH + "/" + id, null, TemplateDTO.class));
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
