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

import java.util.Collection;

public class NiFiRemoteProcessGroup {

    private String parentGroupId;
    private String id;
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
    private Collection<String> authorizationIssues;
    private Collection<String> validationErrors;
    private Boolean transmitting;
    private Integer inputPortCount;
    private Integer outputPortCount;
    private Integer activeRemoteInputPortCount;
    private Integer inactiveRemoteInputPortCount;
    private Integer activeRemoteOutputPortCount;
    private Integer inactiveRemoteOutputPortCount;


    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(String targetUri) {
        this.targetUri = targetUri;
    }

    public String getTargetUris() {
        return targetUris;
    }

    public void setTargetUris(String targetUris) {
        this.targetUris = targetUris;
    }

    public Boolean getTargetSecure() {
        return targetSecure;
    }

    public void setTargetSecure(Boolean targetSecure) {
        this.targetSecure = targetSecure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCommunicationsTimeout() {
        return communicationsTimeout;
    }

    public void setCommunicationsTimeout(String communicationsTimeout) {
        this.communicationsTimeout = communicationsTimeout;
    }

    public String getYieldDuration() {
        return yieldDuration;
    }

    public void setYieldDuration(String yieldDuration) {
        this.yieldDuration = yieldDuration;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public String getLocalNetworkInterface() {
        return localNetworkInterface;
    }

    public void setLocalNetworkInterface(String localNetworkInterface) {
        this.localNetworkInterface = localNetworkInterface;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Collection<String> getAuthorizationIssues() {
        return authorizationIssues;
    }

    public void setAuthorizationIssues(Collection<String> authorizationIssues) {
        this.authorizationIssues = authorizationIssues;
    }

    public Collection<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Collection<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public Boolean getTransmitting() {
        return transmitting;
    }

    public void setTransmitting(Boolean transmitting) {
        this.transmitting = transmitting;
    }

    public Integer getInputPortCount() {
        return inputPortCount;
    }

    public void setInputPortCount(Integer inputPortCount) {
        this.inputPortCount = inputPortCount;
    }

    public Integer getOutputPortCount() {
        return outputPortCount;
    }

    public void setOutputPortCount(Integer outputPortCount) {
        this.outputPortCount = outputPortCount;
    }

    public Integer getActiveRemoteInputPortCount() {
        return activeRemoteInputPortCount;
    }

    public void setActiveRemoteInputPortCount(Integer activeRemoteInputPortCount) {
        this.activeRemoteInputPortCount = activeRemoteInputPortCount;
    }

    public Integer getInactiveRemoteInputPortCount() {
        return inactiveRemoteInputPortCount;
    }

    public void setInactiveRemoteInputPortCount(Integer inactiveRemoteInputPortCount) {
        this.inactiveRemoteInputPortCount = inactiveRemoteInputPortCount;
    }

    public Integer getActiveRemoteOutputPortCount() {
        return activeRemoteOutputPortCount;
    }

    public void setActiveRemoteOutputPortCount(Integer activeRemoteOutputPortCount) {
        this.activeRemoteOutputPortCount = activeRemoteOutputPortCount;
    }

    public Integer getInactiveRemoteOutputPortCount() {
        return inactiveRemoteOutputPortCount;
    }

    public void setInactiveRemoteOutputPortCount(Integer inactiveRemoteOutputPortCount) {
        this.inactiveRemoteOutputPortCount = inactiveRemoteOutputPortCount;
    }
}
