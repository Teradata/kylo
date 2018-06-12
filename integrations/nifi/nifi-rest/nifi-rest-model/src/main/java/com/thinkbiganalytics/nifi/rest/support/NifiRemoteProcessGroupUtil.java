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

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptor;
import com.thinkbiganalytics.nifi.rest.model.NiFiRemoteProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;


public class NifiRemoteProcessGroupUtil {

    /*
        private String targetUri;
    private String targetUris;
    private Boolean targetSecure;
    private String name;
    private String comments;
    private String communicationsTimeout;
    private String yieldDuration;
    private String transportProtocol;
    private String localNetworkInterface;
    private String proxyHost;
    private Integer proxyPort;
    private String proxyUser;
    private String proxyPassword;
     */
    public static List<NiFiPropertyDescriptor> REMOTE_PROCESS_GROUP_PROPERTIES = Lists.newArrayList(propertyDescriptorDTO("targetUris", "URLs", true, false),
                                                                                                    propertyDescriptorDTO("transportProtocol"),
                                                                                                    propertyDescriptorDTO("localNetworkInterface"),
                                                                                                    propertyDescriptorDTO("proxyHost", "HTTP Proxy Server Hostname"),
                                                                                                    propertyDescriptorDTO("proxyPort", "HTTP Proxy Server Port"),
                                                                                                    propertyDescriptorDTO("proxyUser", "HTTP Proxy User"),
                                                                                                    propertyDescriptorDTO("proxyPassword", "HTTP Proxy Password", false, true),
                                                                                                    propertyDescriptorDTO("communicationsTimeout"),
                                                                                                    propertyDescriptorDTO("yieldDuration"));

    private static NiFiPropertyDescriptor propertyDescriptorDTO(String key, String label, boolean required, boolean sensitive) {
        NiFiPropertyDescriptor descriptorDTO = new NiFiPropertyDescriptor();
        descriptorDTO.setName(key);
        descriptorDTO.setDisplayName(label);
        descriptorDTO.setRequired(required);
        descriptorDTO.setSensitive(sensitive);
        return descriptorDTO;
    }

    private static NiFiPropertyDescriptor hiddenOnlyPropertyDescriptor(String key, String label) {
        NiFiPropertyDescriptor descriptorDTO = new NiFiPropertyDescriptor();
        descriptorDTO.setName(key);
        descriptorDTO.setDisplayName(label);
        descriptorDTO.setHidden(true);
        return descriptorDTO;
    }

    private static NiFiPropertyDescriptor propertyDescriptorDTO(String key, String label) {
        return propertyDescriptorDTO(key, label, false, false);
    }

    private static NiFiPropertyDescriptor propertyDescriptorDTO(String key) {
        return propertyDescriptorDTO(key, false, false);
    }

    private static NiFiPropertyDescriptor propertyDescriptorDTO(String key, boolean required, boolean sensitive) {
        String label = WordUtils.capitalizeFully(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key), '_');
        label = Arrays.stream(label.split("_")).collect(Collectors.joining(" "));
        return propertyDescriptorDTO(key, label, required, sensitive);
    }

    private static Map<String, NiFiPropertyDescriptor>
        remoteProcessGroupPropertiesMap =
        REMOTE_PROCESS_GROUP_PROPERTIES.stream().collect(Collectors.toMap(NiFiPropertyDescriptor::getName, Function.identity()));

    /**
     * Updates the remoteProcessGroupDTO from the list of properties
     * @param remoteProcessGroupDTO
     * @param properties
     * @return
     */
    public static boolean updateRemoteProcessGroup(RemoteProcessGroupDTO remoteProcessGroupDTO,List<NifiProperty> properties){

        //if the incoming properties contains 'targetUris' then null out the 'targetUri' value as it will be picked up by Nifi on read
        boolean containsTargetUris = properties.stream().anyMatch(p-> p.getKey().equalsIgnoreCase("targetUris"));
        boolean containsTargetUri = properties.stream().anyMatch(p-> p.getKey().equalsIgnoreCase("targetUri"));
        if(!containsTargetUri && containsTargetUris)
        {
            remoteProcessGroupDTO.setTargetUri(null);
        }
        properties.stream().forEach( property -> {
            String key = property.getKey();
            try {
                PropertyUtils.setProperty(remoteProcessGroupDTO, key, property.getValue());
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        return true;

    }

    public static NiFiRemoteProcessGroup toRemoteProcessGroup(RemoteProcessGroupDTO groupDTO) {
        NiFiRemoteProcessGroup remoteProcessGroup = new NiFiRemoteProcessGroup();

        remoteProcessGroup.setId(groupDTO.getId());
        //the TemplateDTO will not have the name set, just the targetURIs
        //because of this we will not use the name, but rather the uri as the name (by default NiFi does use the targeturi as the name anyway during flow creation)
        String name = groupDTO.getTargetUri();
        String connectedInputPort = groupDTO.getContents().getInputPorts().stream()
            .filter(port -> port.isConnected())
            .map(port -> port.getName()).findFirst().orElse(null);
        if(connectedInputPort != null){
            name += "- "+connectedInputPort;
        }
        remoteProcessGroup.setName(name);

        //1.1.2 doesnt have 'targetUris' property.  1.2.x and up does.
        try {
            Object targetUris = PropertyUtils.getProperty(groupDTO, "targetUris");
            if(targetUris != null){
                PropertyUtils.setProperty(remoteProcessGroup,"targetUris",targetUris);
            }
        }catch (Exception e){
            //not really needed.
        }
        remoteProcessGroup.setActiveRemoteInputPortCount(groupDTO.getActiveRemoteInputPortCount());
        remoteProcessGroup.setActiveRemoteOutputPortCount(groupDTO.getActiveRemoteOutputPortCount());
        remoteProcessGroup.setInactiveRemoteInputPortCount(groupDTO.getInactiveRemoteInputPortCount());
        remoteProcessGroup.setInactiveRemoteOutputPortCount(groupDTO.getInactiveRemoteOutputPortCount());
        remoteProcessGroup.setComments(groupDTO.getComments());
        remoteProcessGroup.setAuthorizationIssues(groupDTO.getAuthorizationIssues());
        //    remoteProcessGroup.setValidationErrors(groupDTO.getValidationErrors());
        remoteProcessGroup.setCommunicationsTimeout(groupDTO.getCommunicationsTimeout());
        remoteProcessGroup.setOutputPortCount(groupDTO.getOutputPortCount());
        remoteProcessGroup.setInputPortCount(groupDTO.getInputPortCount());
        remoteProcessGroup.setTransportProtocol(groupDTO.getTransportProtocol());
        //  remoteProcessGroup.setTargetUris(groupDTO.getTargetUris());
        remoteProcessGroup.setTargetUri(groupDTO.getTargetUri());
        remoteProcessGroup.setProxyHost(groupDTO.getProxyHost());
        remoteProcessGroup.setProxyPort(groupDTO.getProxyPort());
        remoteProcessGroup.setProxyPassword(groupDTO.getProxyPassword());
        remoteProcessGroup.setProxyUser(groupDTO.getProxyUser());
        remoteProcessGroup.setCommunicationsTimeout(groupDTO.getCommunicationsTimeout());
        remoteProcessGroup.setYieldDuration(groupDTO.getYieldDuration());
        remoteProcessGroup.setTargetSecure(groupDTO.isTargetSecure());
        remoteProcessGroup.setParentGroupId(groupDTO.getParentGroupId());

        return remoteProcessGroup;
    }



    /**
     * Return remote process groups for a given template
     * Recursively search the template for all child remote process groups
     */
    public static List<RemoteProcessGroupDTO> remoteProcessGroupDtos(TemplateDTO templateDTO) {
        List<RemoteProcessGroupDTO> groups = templateDTO.getSnippet().getRemoteProcessGroups().stream().collect(Collectors.toList());
        templateDTO.getSnippet().getProcessGroups().stream().forEach(groupDTO -> groups.addAll(remoteProcessGroups(groupDTO)));
        return groups;

    }

    public static List<NiFiRemoteProcessGroup> niFiRemoteProcessGroup(TemplateDTO templateDTO) {
        List<RemoteProcessGroupDTO> groups = templateDTO.getSnippet().getRemoteProcessGroups().stream().collect(Collectors.toList());
        templateDTO.getSnippet().getProcessGroups().stream().forEach(groupDTO -> groups.addAll(remoteProcessGroups(groupDTO)));
        return groups.stream().map(group -> toRemoteProcessGroup(group)).collect(Collectors.toList());

    }

    public static boolean hasRemoteProcessGroups(TemplateDTO templateDTO){
        return templateDTO != null && templateDTO.getSnippet() != null && templateDTO.getSnippet().getRemoteProcessGroups() != null && remoteProcessGroupDtos(templateDTO).size() >1;
    }

    public static boolean hasRemoteProcessGroups(ProcessGroupDTO groupDTO){
        if(!groupDTO.getContents().getRemoteProcessGroups().isEmpty()){
            return true;
        }
        else {
            return   groupDTO.getContents().getProcessGroups().stream().anyMatch(g -> hasRemoteProcessGroups(g));
        }

    }

    public static List<RemoteProcessGroupDTO> remoteProcessGroups(ProcessGroupDTO groupDTO) {
        List<RemoteProcessGroupDTO> remoteProcessGroupDTOS = new ArrayList<>();
        remoteProcessGroupDTOS.addAll(groupDTO.getContents().getRemoteProcessGroups());
        groupDTO.getContents().getProcessGroups().forEach(groupDTO1 -> remoteProcessGroupDTOS.addAll(remoteProcessGroups(groupDTO1)));
        return remoteProcessGroupDTOS;
    }


    @Nullable
    private static String getPropertyAsString(RemoteProcessGroupDTO remoteProcessGroupDTO, PropertyDescriptor propertyDescriptor) {
        Object value = null;
        try {
                value = PropertyUtils.getProperty(remoteProcessGroupDTO, propertyDescriptor.getName());
        } catch (Exception e) {
            //TODO LOG
        }
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }

    }

    /**
     * Return the Remote Process groups as lists of properties related to each group
     */
    public static List<NifiProperty> remoteProcessGroupProperties(TemplateDTO templateDTO) {
        return remoteProcessGroupDtos(templateDTO).stream().flatMap(remoteProcessGroupDTO -> remoteProcessGroupProperties(remoteProcessGroupDTO).stream()).collect(Collectors.toList());
    }

    public static List<NifiProperty> remoteProcessGroupProperties(RemoteProcessGroupDTO remoteProcessGroupDTO) {
        List<NifiProperty> list = Arrays.stream(BeanUtils.getPropertyDescriptors(RemoteProcessGroupDTO.class))
            .filter(propertyDescriptor -> remoteProcessGroupPropertiesMap.containsKey(propertyDescriptor.getName()))
            .map(propertyDescriptor -> {
                NifiProperty
                    property =
                    new NifiProperty(remoteProcessGroupDTO.getParentGroupId(), remoteProcessGroupDTO.getId(), propertyDescriptor.getName(),
                                     getPropertyAsString(remoteProcessGroupDTO, propertyDescriptor));
                property.setProcessorType(NifiConstants.NIFI_COMPONENT_TYPE.REMOTE_PROCESS_GROUP.name());
                property.setProcessGroupId(remoteProcessGroupDTO.getParentGroupId());
                property.setProcessorName(remoteProcessGroupDTO.getName() != null ? remoteProcessGroupDTO.getName() : remoteProcessGroupDTO.getTargetUri());
                property.setProcessGroupName(NifiConstants.NIFI_COMPONENT_TYPE.REMOTE_PROCESS_GROUP.name());
                NiFiPropertyDescriptor propertyDescriptorDTO = remoteProcessGroupPropertiesMap.get(propertyDescriptor.getName());
                property.setPropertyDescriptor(propertyDescriptorDTO);
                return property;


            }).collect(Collectors.toList());

        //add in the connected input port name as a hidden descriptor
        if(remoteProcessGroupDTO.getContents() != null){
           NifiProperty connectedRemoteInputPort = remoteProcessGroupDTO.getContents().getInputPorts().stream()
                .filter(port -> port.isConnected())
                .map(port -> {
                    NifiProperty property = new NifiProperty(remoteProcessGroupDTO.getParentGroupId(), remoteProcessGroupDTO.getId(),"Remote Input Port",port.getName());
                    property.setProcessorType(NifiConstants.NIFI_COMPONENT_TYPE.REMOTE_PROCESS_GROUP.name());
                    property.setProcessGroupId(remoteProcessGroupDTO.getParentGroupId());
                    property.setProcessorName(remoteProcessGroupDTO.getName() != null ? remoteProcessGroupDTO.getName() : remoteProcessGroupDTO.getTargetUri());
                    property.setProcessGroupName(NifiConstants.NIFI_COMPONENT_TYPE.REMOTE_PROCESS_GROUP.name());
                    property.setPropertyDescriptor(hiddenOnlyPropertyDescriptor("Remote Input Port",port.getName()));
                    property.setHidden(true);
                    return property;
                }).findFirst().orElse(null);
           if(connectedRemoteInputPort != null){
               list.add(connectedRemoteInputPort);
           }
        }

        return list;
    }

}
