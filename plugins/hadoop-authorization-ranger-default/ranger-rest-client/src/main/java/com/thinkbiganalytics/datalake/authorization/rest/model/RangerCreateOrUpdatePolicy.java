package com.thinkbiganalytics.datalake.authorization.rest.model;

/*-
 * #%L
 * thinkbig-ranger-rest-client
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */

public class RangerCreateOrUpdatePolicy {


    private static final String GROUP_LIST = "groupList";
    private static final String PERMISSIONS_LIST = "permList";


    private final ArrayList<Map<String, List<String>>> permMapList = new ArrayList<>();

    private int id;
    private String createDate;
    private String updateDate;
    private String owner;
    private String updatedBy;
    private String policyName;
    private String resourceName;
    private String description;
    private String repositoryName;
    private String repositoryType;
    private String isEnabled;
    private String isRecursive;
    private String isAuditEnabled;
    private String version;
    private String replacePerm;
    private String databases;
    private String tables;
    private String columns;
    private String udfs;


    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getIsRecursive() {
        return isRecursive;
    }

    public void setIsRecursive(String isRecursive) {
        this.isRecursive = isRecursive;
    }

    public String getIsAuditEnabled() {
        return isAuditEnabled;
    }

    public void setIsAuditEnabled(String isAuditEnabled) {
        this.isAuditEnabled = isAuditEnabled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List getPermMapList() {
        return this.permMapList;
    }

    public void setPermMapList(List<String> groupList, List<String> permissionList) {

        Map<String, List<String>> permList = new HashMap<>();

        permList.put(GROUP_LIST, groupList);
        permList.put(PERMISSIONS_LIST, permissionList);

        this.permMapList.add(permList);

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReplacePerm() {
        return replacePerm;
    }

    public void setReplacePerm(String replacePerm) {
        this.replacePerm = replacePerm;
    }

    public String getDatabases() {
        return databases;
    }

    public void setDatabases(String databases) {
        this.databases = databases;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public String getUdfs() {
        return udfs;
    }

    public void setUdfs(String udfs) {
        this.udfs = udfs;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

}
