package com.thinkbiganalytics.nifi.rest.support;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorDTO;

import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 1/13/16.
 */
public class NifiProcessUtil {

    public static enum PROCESS_STATE {
        RUNNING,STOPPED,DISABLED;
    }

    public static enum SERVICE_STATE {
        ENABLED,DISABLED;
    }

    public static String SYSTEM_PROPERTY_PREFIX = "system:";

    public static String SYSTEM_PROPERTY_JSON = SYSTEM_PROPERTY_PREFIX+"json";

    public static String PROPERTIES_PROCESSOR ="PropertiesProcessor";


    private  static class ProcessorByTypePredicate implements Predicate<ProcessorDTO> {

        private String type;
        public ProcessorByTypePredicate(String type){
            this.type = type;
        }
        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return processorDTO.getType().equalsIgnoreCase(type);
        }

    }

    private static class ProcessorByNamePredicate implements Predicate<ProcessorDTO> {

        private String name;
        public ProcessorByNamePredicate(String name){
            this.name = name;
        }
        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return processorDTO.getName().equalsIgnoreCase(name);
        }

    }

    private static class ProcessorByIdPredicate implements Predicate<ProcessorDTO> {

        private String id;
        public ProcessorByIdPredicate(String id){
            this.id = id;
        }
        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return processorDTO.getId().equalsIgnoreCase(id);
        }

    }

    private static class ProcessorByIdsPredicate implements Predicate<ProcessorDTO> {

        private List<String> ids;
        public ProcessorByIdsPredicate(List<String> ids){
            this.ids = ids;
        }
        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return ids.contains(processorDTO.getId());
        }

    }

    public  static List<ProcessorDTO> findProcessorsByType(Collection<ProcessorDTO> processors, String type){
        Predicate<ProcessorDTO> byType = new ProcessorByTypePredicate(type);
        return Lists.newArrayList(Iterables.filter(processors,byType));
    }

    public static ProcessorDTO findFirstProcessorsByType(Collection<ProcessorDTO> processors, String type){
        if(type != null) {
            List<ProcessorDTO> list = findProcessorsByType(processors, type);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public static List<ProcessorDTO> findProcessorsByName(Collection<ProcessorDTO> processors, String name){
        Predicate<ProcessorDTO> byName = new ProcessorByNamePredicate(name);
        return Lists.newArrayList(Iterables.filter(processors,byName));
    }
    public static ProcessorDTO findFirstProcessorsByName(Collection<ProcessorDTO> processors, String name){
        List<ProcessorDTO> list = findProcessorsByName(processors, name);
        if(list != null && !list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    public static List<ProcessorDTO> findProcessorsById(Collection<ProcessorDTO> processors, String id){
        Predicate<ProcessorDTO> byId = new ProcessorByIdPredicate(id);
        return Lists.newArrayList(Iterables.filter(processors,byId));
    }

    public static ProcessorDTO findFirstProcessorsById(Collection<ProcessorDTO> processors, String id){
        List<ProcessorDTO> list = findProcessorsById(processors, id);
        if(list != null && !list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    public static List<ProcessorDTO> findProcessorsByIds(Collection<ProcessorDTO> processors, List<String> ids){
        Predicate<ProcessorDTO> byIds = new ProcessorByIdsPredicate(ids);
        return Lists.newArrayList(Iterables.filter(processors, byIds));
    }


    public static Set<ProcessorDTO> getProcessors(TemplateDTO template) {
        return getProcessors(template, false);
    }

    public static Set<ProcessorDTO> getProcessors(TemplateDTO template, boolean excludeInputs) {
        Set<ProcessorDTO> processors = new HashSet<>();
        for (ProcessorDTO processorDTO : template.getSnippet().getProcessors()) {
            processors.add(processorDTO);
        }
        if(template.getSnippet().getProcessGroups() != null){
            for(ProcessGroupDTO groupDTO : template.getSnippet().getProcessGroups()) {
                processors.addAll(getProcessors(groupDTO));
            }
        }

        if(excludeInputs) {
            final List<ProcessorDTO> inputs = NifiTemplateUtil.getInputProcessorsForTemplate(template);
            Iterables.removeIf(processors, new Predicate<ProcessorDTO>() {
                @Override
                public boolean apply(ProcessorDTO processorDTO) {
                    return (inputs.contains(processorDTO));
                }
            });
        }
        return processors;
    }

    public static Collection<ProcessGroupDTO> getProcessGroups(TemplateDTO template) {
       return getProcessGroupsMap(template).values();
    }

    public static Map<String,ProcessGroupDTO> getProcessGroupsMap(TemplateDTO template) {
        Map<String,ProcessGroupDTO> groups = new HashMap<>();
        if(template.getSnippet().getProcessGroups() != null){
            for(ProcessGroupDTO groupDTO : template.getSnippet().getProcessGroups()) {
                groups.putAll(getProcessGroupsMap(groupDTO));
            }
        }
        return groups;
    }

    private static Collection<ProcessGroupDTO> getProcessGroups(ProcessGroupDTO group) {
       return getProcessGroupsMap(group).values();
    }

    private static Map<String,ProcessGroupDTO> getProcessGroupsMap(ProcessGroupDTO group) {
        Map<String,ProcessGroupDTO> groups = new HashMap<>();
        groups.put(group.getId(), group);
        if(group.getContents().getProcessGroups() != null){
            for(ProcessGroupDTO groupDTO: group.getContents().getProcessGroups()){
                groups.putAll(getProcessGroupsMap(groupDTO));
            }
        }
        return groups;
    }


    public static Map<String,ProcessorDTO> getProcessorsMap(ProcessGroupDTO group) {
        Map<String,ProcessorDTO> processors = new HashMap<>();
        if (group != null) {
            for (ProcessorDTO processorDTO : group.getContents().getProcessors()) {
                processors.put(processorDTO.getId(), processorDTO);
            }
            if(group.getContents().getProcessGroups() != null) {
                for(ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                    processors.putAll(getProcessorsMap(groupDTO));
                }
            }
        }
        return processors;
    }


    private static Collection<ProcessorDTO> getProcessors(ProcessGroupDTO group) {
        return getProcessorsMap(group).values();
    }


    public static List<NifiError> getValidationErrors(ProcessGroupDTO group) {
        List<NifiProcessorDTO>  processorValidationErrors =getProcessorValidationErrors(group);
        List<NifiError> errors = new ArrayList<>();
        for(NifiProcessorDTO dto: processorValidationErrors){
            errors.addAll(dto.getValidationErrors());
        }
        return errors;

    }

    public static List<NifiProcessorDTO> getProcessorValidationErrors(Collection<ProcessorDTO> processors){
        return getProcessorValidationErrors(processors,true);
    }
    public static List<NifiProcessorDTO> getProcessorValidationErrors(Collection<ProcessorDTO> processors, boolean ignoreDisabled){
        List<NifiProcessorDTO>  errors = new ArrayList<>();
        for(ProcessorDTO dto: processors) {
            //only validate those that are not disabled
            NifiProcessorDTO processorDTO = getProcessorValidationErrors(dto,ignoreDisabled);
           if(processorDTO != null && !processorDTO.getValidationErrors().isEmpty()){
               errors.add(processorDTO);
           }
        }
        return errors;
    }

    public static NifiProcessorDTO getProcessorValidationErrors(ProcessorDTO dto, boolean ignoreDisabled){
        if( !ignoreDisabled || (ignoreDisabled && (!NifiProcessUtil.PROCESS_STATE.DISABLED.name().equalsIgnoreCase(dto.getState()))) && dto.getValidationErrors() != null){
            NifiProcessorDTO processorDTO = new NifiProcessorDTO(dto.getName(), dto.getId(),dto.getParentGroupId());
            processorDTO.addValidationErrors(dto.getValidationErrors());
            return processorDTO;
        }
        return null;
    }




    public static List<NifiProcessorDTO> getProcessorValidationErrors(ProcessGroupDTO group){
        List<NifiProcessorDTO>  errors = new ArrayList<>();
        errors.addAll(getProcessorValidationErrors(group.getContents().getProcessors()));
        if(group.getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                errors.addAll(getProcessorValidationErrors(groupDTO));
            }
        }
        return errors;
    }



    public static List<ProcessorDTO> getInputProcessors(ProcessGroupDTO group) {
        List<ProcessorDTO> processors = new ArrayList<>();
        List<String> processorIds= NifiConnectionUtil.getInputProcessorIds(group.getContents().getConnections());
        Map<String,ProcessorDTO> map = new HashMap<>();
        if(group.getContents().getProcessors() != null) {
            for (ProcessorDTO processor : group.getContents().getProcessors()) {
                map.put(processor.getId(), processor);
            }
        }
        for(String processorId: processorIds){
            processors.add(map.get(processorId));
        }
        return processors;
    }

    public static List<ProcessorDTO> getNonInputProcessors(ProcessGroupDTO group) {
        List<ProcessorDTO> processors = new ArrayList<>();
        final List<ProcessorDTO> inputProcessors = getInputProcessors(group);

        if(group.getContents().getProcessors() != null) {

            processors = Lists.newArrayList(Iterables.filter(group.getContents().getProcessors(), new Predicate<ProcessorDTO>() {
                @Override
                public boolean apply(ProcessorDTO processorDTO) {
                    return !inputProcessors.contains(processorDTO);
                }
            }));

        }

        if(group.getContents().getProcessGroups() != null){
            for(ProcessGroupDTO groupDTO: group.getContents().getProcessGroups()){
                processors.addAll(getNonInputProcessors(groupDTO));
            }
        }

        return processors;
    }


}
