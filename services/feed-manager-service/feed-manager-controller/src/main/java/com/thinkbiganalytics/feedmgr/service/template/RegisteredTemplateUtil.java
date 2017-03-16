package com.thinkbiganalytics.feedmgr.service.template;

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
