package com.thinkbiganalytics.nifi.rest.model;

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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessorValidationUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a NiFi process group that has been updated in Kylo and holds any error messages resulting from the NiFi update.
 */
public class NifiProcessGroup {

    public static final String CONTROLLER_SERVICE_CATEGORY = "Controller Service";
    private ProcessGroupDTO processGroupEntity;

    private List<ProcessorDTO> activeProcessors;

    private ProcessorDTO inputProcessor;

    private List<ProcessorDTO> downstreamProcessors;

    private boolean success;

    private List<NiFiComponentErrors> errors;

    private boolean rolledBack = false;

    private VersionedProcessGroup versionedProcessGroup;

    private boolean reusableFlowInstance;

    public NifiProcessGroup() {
        this(new ProcessGroupDTO());
    }

    public NifiProcessGroup(ProcessGroupDTO processGroupEntity, ProcessorDTO inputProcessor, List<ProcessorDTO> downstreamProcessors) {
        this.processGroupEntity = processGroupEntity;
        this.inputProcessor = inputProcessor;
        this.downstreamProcessors = downstreamProcessors;
        populateErrors();
        this.success = !this.hasFatalErrors();
    }

    public NifiProcessGroup(ProcessGroupDTO processGroupEntity) {
        this.processGroupEntity = processGroupEntity;
        populateErrors();

        this.success = !this.hasFatalErrors();
    }

    public void validateInputProcessor() {
        if (this.errors == null) {
            errors = new ArrayList<>();
        }
        if (processGroupEntity == null) {
            processGroupEntity = new ProcessGroupDTO();
            processGroupEntity.setName("Process Group");
            if (inputProcessor != null) {
                processGroupEntity.setId(inputProcessor.getParentGroupId());

            }
        }
        if (this.inputProcessor != null && this.processGroupEntity != null) {
            NiFiComponentErrors error = NifiProcessorValidationUtil.getProcessorValidationErrors(this.inputProcessor, false);
            if (error != null && !error.getValidationErrors().isEmpty()) {
                if (!isErrorAlreadyRecorded(error)) {
                    errors.add(error);
                }
            }
        }
    }

    private boolean isErrorAlreadyRecorded(NiFiComponentErrors error) {
        for (NiFiComponentErrors existingError : errors) {
            if ((error.getProcessGroupId().equals(existingError.getProcessGroupId())) && (error.getProcessorId().equals(existingError.getProcessorId()))) {
                return true;
            }
        }
        return false;
    }


    private void populateErrors() {
        this.errors = new ArrayList<NiFiComponentErrors>();
        validateInputProcessor();
        if (this.downstreamProcessors != null && this.processGroupEntity != null) {
            List<NiFiComponentErrors> processorErrors = NifiProcessorValidationUtil.getProcessorValidationErrors(this.downstreamProcessors, true);
            if (processorErrors != null) {
                errors.addAll(processorErrors);
            }
        }

    }

    public void addError(String processGroupId, String processorId, NifiError.SEVERITY severity, String error, String errorType) {
        final NiFiComponentErrors tmpDto = new NiFiComponentErrors("", processorId, processGroupId);
        final List<NiFiComponentErrors> dtos = Lists.newArrayList(Iterables.filter(errors, new Predicate<NiFiComponentErrors>() {
            @Override
            public boolean apply(NiFiComponentErrors nifiProcessorDTO) {
                return nifiProcessorDTO.equals(tmpDto);
            }
        }));
        if (dtos != null && !dtos.isEmpty()) {
            dtos.get(0).addError(severity, error, errorType);
        } else {
            tmpDto.addError(severity, error, errorType);
            this.errors.add(tmpDto);
        }

    }


    public void addError(NifiError.SEVERITY severity, String error, String errorType) {
        addError(processGroupEntity.getName(), "", severity, error, errorType);

    }

    public void addError(NifiError error) {
        addError(processGroupEntity.getName(), "", error.getSeverity(), error.getMessage(), error.getCategory());

    }

    public ProcessGroupDTO getProcessGroupEntity() {
        return processGroupEntity;
    }

    public void updateProcessGroupContent(ProcessGroupDTO dto) {
        if (processGroupEntity != null) {
            processGroupEntity.setContents(dto.getContents());
        } else {
            this.processGroupEntity = dto;
        }
    }

    public List<NiFiComponentErrors> getErrors() {
        if (errors == null) {
            errors = new ArrayList<NiFiComponentErrors>();
        }
        return errors;
    }

    public boolean hasErrors() {
        return this.errors != null && !this.errors.isEmpty();
    }

    public boolean hasFatalErrors() {
        List<NiFiComponentErrors> fatalErrors = new ArrayList<>();
        if (errors != null && !errors.isEmpty()) {
            for (NiFiComponentErrors processor : errors) {
                List<NifiError> errors = processor.getFatalErrors();
                if (errors != null && !errors.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isRolledBack() {
        return rolledBack;
    }

    public void setRolledBack(boolean rolledBack) {
        this.rolledBack = rolledBack;
    }

    public List<NiFiComponentErrors> getControllerServiceErrors() {
        return getErrorsForCategory(CONTROLLER_SERVICE_CATEGORY);
    }

    public List<NiFiComponentErrors> getErrorsForCategory(final String category) {
        if (StringUtils.isBlank(category)) {
            return null;
        }
        return Lists.newArrayList(Iterables.filter(getErrors(), new Predicate<NiFiComponentErrors>() {
            @Override
            public boolean apply(NiFiComponentErrors nifiProcessorDTO) {
                NifiError error = Iterables.tryFind(nifiProcessorDTO.getValidationErrors(), new Predicate<NifiError>() {
                    @Override
                    public boolean apply(NifiError nifiError) {
                        return category.equalsIgnoreCase(nifiError.getCategory());
                    }
                }).orNull();
                return error != null;
            }
        }));
    }


    public List<NifiError> getAllErrors() {
        List<NifiError> errors = new ArrayList<>();
        for (NiFiComponentErrors item : getErrors()) {
            if (item.getValidationErrors() != null && !item.getValidationErrors().isEmpty()) {
                errors.addAll(item.getValidationErrors());
            }
        }
        return errors;
    }


    public void setInputProcessor(ProcessorDTO inputProcessor) {
        this.inputProcessor = inputProcessor;
    }


    public ProcessorDTO getInputProcessor() {
        return inputProcessor;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public VersionedProcessGroup getVersionedProcessGroup() {
        return versionedProcessGroup;
    }

    public void setVersionedProcessGroup(VersionedProcessGroup versionedProcessGroup) {
        this.versionedProcessGroup = versionedProcessGroup;
    }

    public boolean isReusableFlowInstance() {
        return reusableFlowInstance;
    }

    public void setReusableFlowInstance(boolean reusableFlowInstance) {
        this.reusableFlowInstance = reusableFlowInstance;
    }
}
