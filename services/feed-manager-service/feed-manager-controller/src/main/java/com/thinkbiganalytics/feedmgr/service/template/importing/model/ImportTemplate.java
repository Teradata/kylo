package com.thinkbiganalytics.feedmgr.service.template.importing.model;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.util.UniqueIdentifier;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.rest.model.NiFiComponentErrors;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 12/11/17.
 */
public class ImportTemplate {

    public static enum TYPE {
        XML, REUSABLE_TEMPLATE, ARCHIVE
    }

    public static final String NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE = "nifiConnectingReusableTemplate";
    public static final String NIFI_TEMPLATE_XML_FILE = "nifiTemplate.xml";
    public static final String TEMPLATE_JSON_FILE = "template.json";
    public static final String REUSABLE_TEMPLATE_OUTPUT_CONNECTION_FILE = "templateConnections.json";
    private String versionIdentifier;

    String fileName;
    String templateName;
    boolean success;
    private boolean valid;

    private NifiProcessGroup templateResults;
    private List<NiFiComponentErrors> controllerServiceErrors;
    private String templateId;
    private String nifiTemplateId;
    private boolean zipFile;
    private String nifiTemplateXml;
    private String templateJson;
    private List<String> nifiConnectingReusableTemplateXmls = new ArrayList<>();
    private boolean verificationToReplaceConnectingResuableTemplateNeeded;
    private ImportTemplateOptions importOptions;
    private boolean reusableFlowOutputPortConnectionsNeeded;
    private List<ReusableTemplateConnectionInfo> reusableTemplateConnections;

    @JsonIgnore
    private RegisteredTemplate templateToImport;

    public ImportTemplate() {
        createVersionId();
    }

    public ImportTemplate(String fileName) {
        this.fileName = fileName;
        createVersionId();
    }

    public ImportTemplate(String templateName, boolean success) {
        this.templateName = templateName;
        this.success = success;
        createVersionId();
    }

    public void createVersionId() {
        this.versionIdentifier = UniqueIdentifier.encode(System.currentTimeMillis());
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @JsonIgnore
    public boolean hasValidComponents() {
        return StringUtils.isNotBlank(templateJson) && StringUtils.isNotBlank(nifiTemplateXml);
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getNifiTemplateXml() {
        return nifiTemplateXml;
    }

    public void setNifiTemplateXml(String nifiTemplateXml) {
        this.nifiTemplateXml = nifiTemplateXml;
    }

    public String getTemplateJson() {
        return templateJson;
    }

    public void setTemplateJson(String templateJson) {
        this.templateJson = templateJson;
    }

    public NifiProcessGroup getTemplateResults() {
        if (templateResults == null) {
            templateResults = new NifiProcessGroup();
        }
        return templateResults;
    }

    public void setTemplateResults(NifiProcessGroup templateResults) {
        this.templateResults = templateResults;
        inspectForControllerServiceErrors();
    }


    public String getFileName() {
        return fileName;
    }

    public boolean isZipFile() {
        return zipFile;
    }

    public void setZipFile(boolean zipFile) {
        this.zipFile = zipFile;
    }

    private void inspectForControllerServiceErrors() {
        if (templateResults != null) {
            List<NiFiComponentErrors> errors = templateResults.getControllerServiceErrors();
            this.controllerServiceErrors = errors;
        }
    }

    public List<NiFiComponentErrors> getControllerServiceErrors() {
        return controllerServiceErrors;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getNifiTemplateId() {
        return nifiTemplateId;
    }

    public void setNifiTemplateId(String nifiTemplateId) {
        this.nifiTemplateId = nifiTemplateId;
    }

    public List<String> getNifiConnectingReusableTemplateXmls() {
        return nifiConnectingReusableTemplateXmls;
    }

    public void addNifiConnectingReusableTemplateXml(String nifiConnectingReusableTemplateXml) {
        this.nifiConnectingReusableTemplateXmls.add(nifiConnectingReusableTemplateXml);
    }

    public void addReusableTemplateConnectionInformation(List<ReusableTemplateConnectionInfo> reusableTemplateConnectionInfos) {
        this.reusableTemplateConnections = reusableTemplateConnectionInfos;
    }

    public boolean hasConnectingReusableTemplate() {
        return !nifiConnectingReusableTemplateXmls.isEmpty();
    }

    public boolean isVerificationToReplaceConnectingResuableTemplateNeeded() {
        return verificationToReplaceConnectingResuableTemplateNeeded;
    }

    public void setVerificationToReplaceConnectingResuableTemplateNeeded(boolean verificationToReplaceConnectingResuableTemplateNeeded) {
        this.verificationToReplaceConnectingResuableTemplateNeeded = verificationToReplaceConnectingResuableTemplateNeeded;
    }

    public ImportTemplateOptions getImportOptions() {
        return importOptions;
    }

    public void setImportOptions(ImportTemplateOptions importOptions) {
        this.importOptions = importOptions;
    }

    @JsonIgnore
    public RegisteredTemplate getTemplateToImport() {
        if (templateToImport == null && StringUtils.isNotBlank(templateJson)) {
            templateToImport = ObjectMapperSerializer.deserialize(getTemplateJson(), RegisteredTemplate.class);

        }
        return templateToImport;
    }

    public boolean isReusableFlowOutputPortConnectionsNeeded() {
        return reusableFlowOutputPortConnectionsNeeded;
    }

    public void setReusableFlowOutputPortConnectionsNeeded(boolean reusableFlowOutputPortConnectionsNeeded) {
        this.reusableFlowOutputPortConnectionsNeeded = reusableFlowOutputPortConnectionsNeeded;
    }

    public void addReusableTemplateConnection(ReusableTemplateConnectionInfo connectionInfo) {
        getReusableTemplateConnections().add(connectionInfo);
    }

    public List<ReusableTemplateConnectionInfo> getReusableTemplateConnections() {
        if (reusableTemplateConnections == null) {
            reusableTemplateConnections = new ArrayList<>();
        }
        return reusableTemplateConnections;
    }

    public void setReusableTemplateConnections(List<ReusableTemplateConnectionInfo> reusableTemplateConnections) {
        this.reusableTemplateConnections = reusableTemplateConnections;
    }

    @JsonIgnore
    public void setTemplateToImport(RegisteredTemplate templateToImport) {
        this.templateToImport = templateToImport;
    }

    public String getVersionIdentifier() {
        return versionIdentifier;
    }
}
