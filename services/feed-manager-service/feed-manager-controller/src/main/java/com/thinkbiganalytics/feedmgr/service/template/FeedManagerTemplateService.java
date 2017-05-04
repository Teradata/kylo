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

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.nifi.web.api.dto.PortDTO;

import java.util.List;
import java.util.Set;

/**
 */
public interface FeedManagerTemplateService {


    /**
     * Register a template, save it, and return
     *
     * @param registeredTemplate a template to register/update
     * @return the registered template
     */
    RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate);

    /**
     * Return all properties registered for a template
     *
     * @param templateId a template id
     * @return all properties registered for a template
     */
    List<NifiProperty> getTemplateProperties(String templateId);

    /**
     * Return a registered template by its id
     *
     * @param templateId a template id
     * @return a registered template, or null if not found
     */
    RegisteredTemplate getRegisteredTemplate(String templateId);


    /**
     * finds a template by its name
     * @param templateName the template name
     * @return a registered template
     */
    RegisteredTemplate findRegisteredTemplateByName(final String templateName);

    /**
     * Deletes a template
     *
     * @param templateId a registered template id
     */
    boolean deleteRegisteredTemplate(String templateId);

    /**
     * change the state of the template to be {@link com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate.State#ENABLED}
     *
     * @param templateId the template id
     * @return the updated template
     */
    RegisteredTemplate enableTemplate(String templateId);

    /**
     * change the state of the template to be {@link com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate.State#DISABLED}
     *
     * @param templateId the template id
     * @return the updated template
     */
    RegisteredTemplate disableTemplate(String templateId);


    /**
     * Return all registered templates
     *
     * @return a list of all registered templates
     */
    List<RegisteredTemplate> getRegisteredTemplates();

    /**
     * Ensures the {@link RegisteredTemplate#inputProcessors} contains processors contains all properties, both that have been exposed for the end user input and those that don't
     *
     * @param registeredTemplate the template to inspect
     */
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate);



    /**
     * Returns the Ports available in the Reusable_templates process group
     *
     * @return the Ports available in the Reusable_templates process group
     */
    Set<PortDTO> getReusableFeedInputPorts();


    /**
     * Return all the processors that are connected to a given NiFi input port
     *
     * @param inputPortIds the ports to inspect
     * @return all the processors that are connected to a given NiFi input port
     */
    List<RegisteredTemplate.Processor> getReusableTemplateProcessorsForInputPorts(List<String> inputPortIds);

    /**
     * Return processors for a template and optionally walk the graph and obtain those connected to the reusable flows, if any
     *
     * @param templateId                the template id to inspect
     * @param includeReusableProcessors true if it shouls walk the connections to reusable process groups, false if not
     * @return the list of processors
     */
    List<RegisteredTemplate.Processor> getRegisteredTemplateProcessors(String templateId, boolean includeReusableProcessors);

    /**
     * For a given Template and its related connection info to the reusable templates, walk the graph to return the Processors.
     * The system will first walk the incoming templateid.  If the {@code connectionInfo} parameter is set it will make the connections to the incoming template and continue walking those processors
     *
     * @param templateId the NiFi templateId required to start walking the flow
     * @param connectionInfo the connections required to connect
     * @return a list of all the processors for a template and possible connections
     */
    List<RegisteredTemplate.FlowProcessor> getNiFiTemplateFlowProcessors(String templateId, List<ReusableTemplateConnectionInfo> connectionInfo);

    /**
     * Return a list of Processors and their properties for the incoming template
     *
     * @param templateId a NiFi template id
     * @return a list of Processors and their properties for the incoming template
     */
    List<RegisteredTemplate.Processor> getNiFiTemplateProcessorsWithProperties(String templateId);


    /**
     * saves the order as indicated via index in the supplied {@code orderedTemplateIds}
     *
     * @param orderedTemplateIds a list of the template ids in order
     * @param exclude            a list of ids it should skip and not save
     */
    void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude);


}
