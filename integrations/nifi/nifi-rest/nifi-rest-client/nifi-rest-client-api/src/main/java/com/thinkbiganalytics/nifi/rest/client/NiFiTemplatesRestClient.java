package com.thinkbiganalytics.nifi.rest.client;

import org.apache.nifi.web.api.dto.TemplateDTO;

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
     * @param xml an XML document representing the template
     * @return the new template
     */
    @Nonnull
    TemplateDTO create(@Nullable String name, @Nonnull String xml);

    /**
     * Deletes a template.
     *
     * @param id the template id
     * @return the template, if deleted
     */
    @Nonnull
    Optional<TemplateDTO> delete(@Nonnull String id);

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
