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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.FlowDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class to gather Processor information from NiFi dto objects
 */
public class NifiProcessUtil {

    /**
     * Type of the cleanup processor
     */
    public static final String CLEANUP_TYPE = "com.thinkbiganalytics.nifi.v2.metadata.TriggerCleanup";

    public static final String TRIGGER_FEED_TYPE = "com.thinkbiganalytics.nifi.v2.metadata.TriggerFeed";

    /**
     * Return processors matching a supplied processor type matching {@link ProcessorDTO#getType()} against the supplied {@code type}
     *
     * @param processors a collection of processors
     * @param type       a type to match against
     * @return a list of processors that are of the supplied type
     */
    public static List<ProcessorDTO> findProcessorsByType(Collection<ProcessorDTO> processors, String type) {
        Predicate<ProcessorDTO> byType = new ProcessorByTypePredicate(type);
        return Lists.newArrayList(Iterables.filter(processors, byType));
    }

    /**
     * Return the first processor that matches a supplied type {@link ProcessorDTO#getType()}
     *
     * @param processors a collection of processors
     * @param type       a processor type
     * @return the first processor in the collection matching the supplied type, or null if not found
     */
    public static ProcessorDTO findFirstProcessorsByType(Collection<ProcessorDTO> processors, String type) {
        if (type != null) {
            List<ProcessorDTO> list = findProcessorsByType(processors, type);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return null;
    }

    public static ProcessorDTO findFirstProcessorsByTypeAndName(Collection<ProcessorDTO> processors, String type, String name) {
        ProcessorDTO processorDTO = null;
        if (type != null) {
            List<ProcessorDTO> list = findProcessorsByType(processors, type);
            if (list != null && !list.isEmpty()) {
                if(StringUtils.isNotBlank(name)){
                    processorDTO = list.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(list.get(0));
                }
                if( processorDTO == null) {
                    processorDTO = list.get(0);
                }
            }
        }
        return processorDTO;
    }

    /**
     * Return the list of processors that match a given name
     *
     * @param processors a collection of processors
     * @param name       a name to match
     * @return a list of processors with the supplied name
     */
    public static List<ProcessorDTO> findProcessorsByName(Collection<ProcessorDTO> processors, String name) {
        Predicate<ProcessorDTO> byName = new ProcessorByNamePredicate(name);
        return Lists.newArrayList(Iterables.filter(processors, byName));
    }

    /**
     * Return the first processor in the collection with the supplied name
     *
     * @param processors a collection of processors
     * @param name       the name to match
     * @return a processor with the name, or null if not found
     */
    public static ProcessorDTO findFirstProcessorsByName(Collection<ProcessorDTO> processors, String name) {
        List<ProcessorDTO> list = findProcessorsByName(processors, name);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Return a list of processors matching a given id
     *
     * @param processors a collection of processors
     * @param id         an id to match
     * @return a list of processors with the supplied id
     */
    public static List<ProcessorDTO> findProcessorsById(Collection<ProcessorDTO> processors, String id) {
        Predicate<ProcessorDTO> byId = new ProcessorByIdPredicate(id);
        return Lists.newArrayList(Iterables.filter(processors, byId));
    }

    /**
     * Return the first processor in the collection that matches the supplied id
     *
     * @param processors a collection of processors
     * @param id         an id to match
     * @return the first processor in the collection matching the id
     */
    public static ProcessorDTO findFirstProcessorsById(Collection<ProcessorDTO> processors, String id) {
        List<ProcessorDTO> list = findProcessorsById(processors, id);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Return processors matching a list of ids
     *
     * @param processors a collection of processors
     * @param ids        a list of ids to match
     * @return a list of processors that have an id in the supplied {@code ids} list
     */
    public static List<ProcessorDTO> findProcessorsByIds(Collection<ProcessorDTO> processors, List<String> ids) {
        Predicate<ProcessorDTO> byIds = new ProcessorByIdsPredicate(ids);
        return Lists.newArrayList(Iterables.filter(processors, byIds));
    }

    /**
     * Return a set of processors for a given template
     *
     * @param template a template to parse
     * @return a set of processors in that template
     */
    public static Set<ProcessorDTO> getProcessors(TemplateDTO template) {
        return getProcessors(template, false);
    }

    /**
     * Return a set of processors in a template, optionally allowing the framework to traverse into a templates input ports to get the connecting process groups
     *
     * @param template      a template to parse
     * @param excludeInputs {@code true} will traverse down into the input ports and gather processors in the conencting groups, {@code false} will traverse input ports and their respective process
     *                      groups
     * @return return a set of processors
     */
    public static Set<ProcessorDTO> getProcessors(TemplateDTO template, boolean excludeInputs) {
        Set<ProcessorDTO> processors = new HashSet<>();
        for (ProcessorDTO processorDTO : template.getSnippet().getProcessors()) {
            processors.add(processorDTO);
        }
        if (template.getSnippet().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : template.getSnippet().getProcessGroups()) {
                processors.addAll(getProcessors(groupDTO));
            }
        }

        if (excludeInputs) {
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

    /**
     * Return all process groups in a template
     *
     * @param template a template to parse
     * @return a collection of all process groups in the template
     */
    public static Collection<ProcessGroupDTO> getProcessGroups(TemplateDTO template) {
        return getProcessGroupsMap(template).values();
    }

    /**
     * Return a map of processGroupId to process group
     *
     * @param template a template to parse
     * @return a map of processGroupId to process group
     */
    public static Map<String, ProcessGroupDTO> getProcessGroupsMap(TemplateDTO template) {
        Map<String, ProcessGroupDTO> groups = new HashMap<>();
        if (template.getSnippet().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : template.getSnippet().getProcessGroups()) {
                groups.putAll(getProcessGroupsMap(groupDTO));
            }
        }
        return groups;
    }

    /**
     * Return all the process groups under a given process group (including all their children)
     *
     * @param group a process group
     * @return a collection of all the groups and their children
     */
    public static Collection<ProcessGroupDTO> getProcessGroups(ProcessGroupDTO group) {
        return getProcessGroupsMap(group).values();
    }

    /**
     * Return a map of the process group and their children
     *
     * @param group a process group
     * @return a map of the process group and their children
     */
    private static Map<String, ProcessGroupDTO> getProcessGroupsMap(ProcessGroupDTO group) {
        Map<String, ProcessGroupDTO> groups = new HashMap<>();
        groups.put(group.getId(), group);
        if (group.getContents() != null && group.getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                groups.putAll(getProcessGroupsMap(groupDTO));
            }
        }
        return groups;
    }

    /**
     * Return a map of all the processors, by processorId under a process group, including all children
     *
     * @param group a process group
     * @return a map of all processors in the group, including all child process groups
     */
    public static Map<String, ProcessorDTO> getProcessorsMap(ProcessGroupDTO group) {
        Map<String, ProcessorDTO> processors = new HashMap<>();
        if (group != null) {
            if(group.getContents() != null) {
                for (ProcessorDTO processorDTO : group.getContents().getProcessors()) {
                    processors.put(processorDTO.getId(), processorDTO);
                }
                if (group.getContents().getProcessGroups() != null) {
                    for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                        processors.putAll(getProcessorsMap(groupDTO));
                    }
                }
            }
        }
        return processors;
    }

    /**
     * Return a collection of all processors, including all children, in a process group
     *
     * @param group a process group
     * @return a collection of all processors, including all children, in a process group
     */
    public static Collection<ProcessorDTO> getProcessors(ProcessGroupDTO group) {
        return getProcessorsMap(group).values();
    }

    /**
     * Return a list of all the first level input/source processors in a process group.  These processors dont have any connection incoming to them
     *
     * @param group a process group
     * @return a list of input processors
     */
    public static List<ProcessorDTO> getInputProcessors(ProcessGroupDTO group) {
        List<ProcessorDTO> processors = new ArrayList<>();
        List<String> processorIds = NifiConnectionUtil.getInputProcessorIds(group.getContents().getConnections());
        Map<String, ProcessorDTO> map = new HashMap<>();
        if (group.getContents() != null && group.getContents().getProcessors() != null) {
            for (ProcessorDTO processor : group.getContents().getProcessors()) {
                map.put(processor.getId(), processor);
            }
        }
        for (String processorId : processorIds) {
            if (map.containsKey(processorId)) {
                processors.add(map.get(processorId));
            }
        }
        return processors;
    }

    /**
     * Return a list of all processors under a process group that have connections coming into them.
     *
     * @param group a process group
     * @return a list of all processors under a process group that have connections coming into them
     */
    public static List<ProcessorDTO> getNonInputProcessors(ProcessGroupDTO group) {
        List<ProcessorDTO> processors = new ArrayList<>();
        final List<ProcessorDTO> inputProcessors = getInputProcessors(group);

        if (group.getContents().getProcessors() != null) {

            processors = Lists.newArrayList(Iterables.filter(group.getContents().getProcessors(), new Predicate<ProcessorDTO>() {
                @Override
                public boolean apply(ProcessorDTO processorDTO) {
                    return !inputProcessors.contains(processorDTO);
                }
            }));

        }

        if (group.getContents().getProcessGroups() != null) {
            for (ProcessGroupDTO groupDTO : group.getContents().getProcessGroups()) {
                processors.addAll(getNonInputProcessors(groupDTO));
            }
        }

        return processors;
    }

    /**
     * Finds the first process group with the specified name.
     *
     * @param processGroups the list of process groups to filter
     * @param name          the feed system name to match, case-insensitive
     * @return the matching process group, or {@code null} if not found
     */
    @Nullable
    public static ProcessGroupDTO findFirstProcessGroupByName(@Nonnull final Collection<ProcessGroupDTO> processGroups, @Nonnull final String name) {
        return processGroups.stream().filter(processGroup -> processGroup.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    /**
     * Filters the specified list of process groups for ones matching the specified feed name, including versioned process groups.
     *
     * @param processGroups the list of process groups to filter
     * @param feedName      the feed system name to match, case-insensitive
     * @return the matching process groups
     */
    @Nonnull
    public static Set<ProcessGroupDTO> findProcessGroupsByFeedName(@Nonnull final Collection<ProcessGroupDTO> processGroups, @Nonnull final String feedName) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(feedName) + "( - \\d+)?$", Pattern.CASE_INSENSITIVE);
        return processGroups.stream().filter(processGroup -> pattern.matcher(processGroup.getName()).matches()).collect(Collectors.toSet());
    }



    /**
     * common state of NiFi processors/components
     **/
    public static enum PROCESS_STATE {
        RUNNING, STOPPED, DISABLED, ENABLED;
    }


    /**
     * common state of NiFi controller services
     **/
    public static enum SERVICE_STATE {
        ENABLING, ENABLED, DISABLED;
    }

    /**
     * Find a processor by its type
     */
    private static class ProcessorByTypePredicate implements Predicate<ProcessorDTO> {

        private String type;

        public ProcessorByTypePredicate(String type) {
            this.type = type;
        }

        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return processorDTO.getType().equalsIgnoreCase(type);
        }

    }

    /**
     * find a processor by its name
     */
    private static class ProcessorByNamePredicate implements Predicate<ProcessorDTO> {

        private String name;

        public ProcessorByNamePredicate(String name) {
            this.name = name;
        }

        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return processorDTO.getName().equalsIgnoreCase(name);
        }

    }

    /**
     * find a processor by its id
     */
    private static class ProcessorByIdPredicate implements Predicate<ProcessorDTO> {

        private String id;

        public ProcessorByIdPredicate(String id) {
            this.id = id;
        }

        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return processorDTO.getId().equalsIgnoreCase(id);
        }

    }

    /**
     * find a processor by a set of ids
     */
    private static class ProcessorByIdsPredicate implements Predicate<ProcessorDTO> {

        private List<String> ids;

        public ProcessorByIdsPredicate(List<String> ids) {
            this.ids = ids;
        }

        @Override
        public boolean apply(ProcessorDTO processorDTO) {
            return ids.contains(processorDTO.getId());
        }

    }
}
