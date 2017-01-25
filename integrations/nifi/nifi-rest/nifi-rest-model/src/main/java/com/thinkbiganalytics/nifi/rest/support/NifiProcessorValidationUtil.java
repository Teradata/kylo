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

import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorDTO;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 8/18/16.
 */
public class NifiProcessorValidationUtil {


    public static List<NifiError> getValidationErrors(ProcessGroupDTO group) {
        List<NifiProcessorDTO> processorValidationErrors = getProcessorValidationErrors(group);
        List<NifiError> errors = new ArrayList<>();
        for (NifiProcessorDTO dto : processorValidationErrors) {
            errors.addAll(dto.getValidationErrors());
        }
        return errors;

    }

    public static List<NifiProcessorDTO> getProcessorValidationErrors(Collection<ProcessorDTO> processors) {
        return getProcessorValidationErrors(processors, true);
    }

    public static List<NifiProcessorDTO> getProcessorValidationErrors(Collection<ProcessorDTO> processors, boolean ignoreDisabled) {
        List<NifiProcessorDTO> errors = new ArrayList<>();
        for (ProcessorDTO dto : processors) {
            //only validate those that are not disabled
            NifiProcessorDTO processorDTO = getProcessorValidationErrors(dto, ignoreDisabled);
            if (processorDTO != null && !processorDTO.getValidationErrors().isEmpty()) {
                errors.add(processorDTO);
            }
        }
        return errors;
    }

    public static NifiProcessorDTO getProcessorValidationErrors(ProcessorDTO dto, boolean ignoreDisabled) {
        if (!ignoreDisabled || (ignoreDisabled && (!NifiProcessUtil.PROCESS_STATE.DISABLED.name().equalsIgnoreCase(dto.getState()))) && dto.getValidationErrors() != null) {
            NifiProcessorDTO processorDTO = new NifiProcessorDTO(dto.getName(), dto.getId(), dto.getParentGroupId());
            processorDTO.addValidationErrors(dto.getValidationErrors());
            return processorDTO;
        }
        return null;
    }


    public static List<NifiProcessorDTO> getProcessorValidationErrors(ProcessGroupDTO group) {
        List<NifiProcessorDTO> errors = new ArrayList<>();
        errors.addAll(getProcessorValidationErrors(group.getContents().getProcessors()));
        if (group.getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                errors.addAll(getProcessorValidationErrors(groupDTO));
            }
        }
        return errors;
    }
}
