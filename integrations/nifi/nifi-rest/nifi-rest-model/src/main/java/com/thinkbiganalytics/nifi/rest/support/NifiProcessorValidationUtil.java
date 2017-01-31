package com.thinkbiganalytics.nifi.rest.support;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import com.thinkbiganalytics.nifi.rest.model.NiFiComponentErrors;
import com.thinkbiganalytics.nifi.rest.model.NifiError;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utility to extract validation errors from Processors
 */
public class NifiProcessorValidationUtil {


    public static List<NifiError> getValidationErrors(ProcessGroupDTO group) {
        List<NiFiComponentErrors> processorValidationErrors = getProcessorValidationErrors(group);
        List<NifiError> errors = new ArrayList<>();
        for (NiFiComponentErrors dto : processorValidationErrors) {
            errors.addAll(dto.getValidationErrors());
        }
        return errors;

    }

    public static List<NiFiComponentErrors> getProcessorValidationErrors(Collection<ProcessorDTO> processors) {
        return getProcessorValidationErrors(processors, true);
    }

    public static List<NiFiComponentErrors> getProcessorValidationErrors(Collection<ProcessorDTO> processors, boolean ignoreDisabled) {
        List<NiFiComponentErrors> errors = new ArrayList<>();
        for (ProcessorDTO dto : processors) {
            //only validate those that are not disabled
            NiFiComponentErrors processorDTO = getProcessorValidationErrors(dto, ignoreDisabled);
            if (processorDTO != null && !processorDTO.getValidationErrors().isEmpty()) {
                errors.add(processorDTO);
            }
        }
        return errors;
    }

    public static NiFiComponentErrors getProcessorValidationErrors(ProcessorDTO dto, boolean ignoreDisabled) {
        if (!ignoreDisabled || (ignoreDisabled && (!NifiProcessUtil.PROCESS_STATE.DISABLED.name().equalsIgnoreCase(dto.getState()))) && dto.getValidationErrors() != null) {
            NiFiComponentErrors processorDTO = new NiFiComponentErrors(dto.getName(), dto.getId(), dto.getParentGroupId());
            processorDTO.addValidationErrors(dto.getValidationErrors());
            return processorDTO;
        }
        return null;
    }


    public static List<NiFiComponentErrors> getProcessorValidationErrors(ProcessGroupDTO group) {
        List<NiFiComponentErrors> errors = new ArrayList<>();
        errors.addAll(getProcessorValidationErrors(group.getContents().getProcessors()));
        if (group.getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                errors.addAll(getProcessorValidationErrors(groupDTO));
            }
        }
        return errors;
    }
}
