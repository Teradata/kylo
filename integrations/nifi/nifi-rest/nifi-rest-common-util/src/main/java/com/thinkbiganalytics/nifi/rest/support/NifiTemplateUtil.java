package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-common-util
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

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.Collections;
import java.util.List;

/**
 * Utility class to support the NiFi Template dto object
 */
public class NifiTemplateUtil {

    /**
     * find all source level input processors in a template which dont have any connections coming into them
     *
     * @param template a nifi template
     * @return a list of processors in the template that dont have any incoming connections
     */
    public static List<ProcessorDTO> getInputProcessorsForTemplate(TemplateDTO template) {
        if (template != null && template.getSnippet() != null) {
            List<String> sourceIds = NifiConnectionUtil.getInputProcessorIds(template.getSnippet().getConnections());
            return NifiProcessUtil.findProcessorsByIds(template.getSnippet().getProcessors(), sourceIds);
        } else {
            return Collections.emptyList();
        }
    }

}
