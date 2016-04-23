package com.thinkbiganalytics.nifi.rest.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 1/22/16.
 */
public class NifiProcessGroup {

    private ProcessGroupEntity processGroupEntity;

    private List<ProcessorDTO> activeProcessors;

    private ProcessorDTO  inputProcessor;

    private List<ProcessorDTO>  downstreamProcessors;

    private boolean success;

    private List<NifiProcessorDTO> errors;
    public NifiProcessGroup() {

    }

    public NifiProcessGroup(ProcessGroupEntity processGroupEntity, ProcessorDTO inputProcessor, List<ProcessorDTO> downstreamProcessors) {
        this.processGroupEntity = processGroupEntity;
        this.inputProcessor = inputProcessor;
        this.downstreamProcessors = downstreamProcessors;
        populateErrors();
        this.success =  !this.hasFatalErrors();
    }

    public NifiProcessGroup(ProcessGroupEntity processGroupEntity) {
        this.processGroupEntity = processGroupEntity;
        populateErrors();

        this.success = !this.hasFatalErrors();
    }

    private void populateErrors(){
        this.errors = new ArrayList<NifiProcessorDTO>();
        if(this.inputProcessor != null && this.processGroupEntity  != null && this.processGroupEntity.getProcessGroup() != null)
        {
            NifiProcessorDTO error = NifiProcessUtil.getProcessorValidationErrors(this.inputProcessor, false);
            if(error != null && !error.getValidationErrors().isEmpty()) {
                errors.add(error);
            }
        }
        if(this.downstreamProcessors != null && this.processGroupEntity  != null && this.processGroupEntity.getProcessGroup() != null)
        {
            List<NifiProcessorDTO> processorErrors = NifiProcessUtil.getProcessorValidationErrors(this.downstreamProcessors,true);
            if(processorErrors != null) {
                errors.addAll(processorErrors);
            }
        }

    }

    public void addError(String processGroupId, String processorId, NifiError.SEVERITY severity,String error, String errorType){
        final NifiProcessorDTO tmpDto = new NifiProcessorDTO("",processorId,processGroupId);
        final List<NifiProcessorDTO> dtos = Lists.newArrayList(Iterables.filter(errors, new Predicate<NifiProcessorDTO>() {
                @Override
            public boolean apply(NifiProcessorDTO nifiProcessorDTO) {
                return nifiProcessorDTO.equals(tmpDto);
            }
        }));
        if(dtos != null && !dtos.isEmpty()) {
            dtos.get(0).addError(severity,error,errorType);
        }
        else {
            tmpDto.addError(severity,error,errorType);
            this.errors.add(tmpDto);
        }

    }

    public void addError(NifiError.SEVERITY severity,String error, String errorType){
       addError(processGroupEntity.getProcessGroup().getName(),"",severity,error,errorType);

    }

    public void addError(NifiError error){
        addError(processGroupEntity.getProcessGroup().getName(),"",error.getSeverity(),error.getMessage(),error.getCategory());

    }

    public ProcessGroupEntity getProcessGroupEntity() {
        return processGroupEntity;
    }

    public List<NifiProcessorDTO> getErrors() {
        return errors;
    }

    public boolean hasFatalErrors() {
        List<NifiProcessorDTO> fatalErrors = new ArrayList<>();
        if(errors != null && !errors.isEmpty()) {
            for(NifiProcessorDTO processor : errors){
                List<NifiError> errors = processor.getFatalErrors();
             if(errors != null && !errors.isEmpty()) {
                 return true;
             }
            }
        }
        return false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
