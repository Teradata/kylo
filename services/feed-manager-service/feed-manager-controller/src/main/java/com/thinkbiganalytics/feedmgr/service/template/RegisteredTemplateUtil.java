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
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import javax.inject.Inject;


public class RegisteredTemplateUtil {


    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    /**
     * convert a NiFi processor to a RegisteredTemplate processor object
     *
     * @param processorDTO  the NiFi processor
     * @param setProperties true to set the properties on the RegisteredTemplate.Processor, false to skip
     * @return a NiFi processor to a RegisteredTemplate processor object
     */
    public RegisteredTemplate.Processor toRegisteredTemplateProcessor(ProcessorDTO processorDTO, boolean setProperties) {
        RegisteredTemplate.Processor p = new RegisteredTemplate.Processor(processorDTO.getId());
        p.setGroupId(processorDTO.getParentGroupId());
        p.setType(processorDTO.getType());
        p.setName(processorDTO.getName());
        if (setProperties) {
            p.setProperties(NifiPropertyUtil.getPropertiesForProcessor(new ProcessGroupDTO(), processorDTO, propertyDescriptorTransform));
        }
        return p;
    }


}
