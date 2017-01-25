package com.thinkbiganalytics.datalake.authorization.model;

/*-
 * #%L
 * thinkbig-hadoop-authorization-core
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class HivePolicy {

    private static final Logger log = LoggerFactory.getLogger(HivePolicy.class);
    private static ArrayList<String> permList = new ArrayList<String>();
    private String policyName;
    private String databases;
    private String tables;
    private String columns;
    private String udfs;
    private String description;
    private String repositoryName;
    private String repositoryType;
    private String tableType;
    private String columnType;
    private String isEnabled;
    private String isAuditEnabled;
    private ArrayList<String> userList = new ArrayList<String>();
    private ArrayList<String> groupList = new ArrayList<String>();

    public static ArrayList<String> getPermList() {
        return permList;
    }

    public static void setPermList(ArrayList<String> permList) {
        HivePolicy.permList = permList;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
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

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getUdfs() {
        return udfs;
    }

    public void setUdfs(String udfs) {
        this.udfs = udfs;
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

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getIsAuditEnabled() {
        return isAuditEnabled;
    }

    public void setIsAuditEnabled(String isAuditEnabled) {
        this.isAuditEnabled = isAuditEnabled;
    }

    public ArrayList<String> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    public ArrayList<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<String> groupList) {
        this.groupList = groupList;
    }

    /***
     * Method for forming JSON policy object to post to Ranger REST API
     */
    public JSONObject policyJson()

    {

        JSONObject policy = new JSONObject();
        JSONArray permMapList = new JSONArray();
        JSONObject permList = new JSONObject();
        JSONArray userValue = new JSONArray();
        JSONArray permValue = new JSONArray();
        JSONArray groupValue = new JSONArray();

        //Add users to list
        if (getUserList().isEmpty()) {
            System.out.println("empty");
            //Do not add anything to list
        } else {
            for (int userCnt = 0; userCnt < getUserList().size(); userCnt++) {
                userValue.add(getUserList().get(userCnt));
            }
            permList.put("userList", userValue);
        }

        //Add groups to list
        if (getGroupList().isEmpty()) {
            //Do not add anything to list
        } else {
            for (int groupCnt = 0; groupCnt < getGroupList().size(); groupCnt++) {
                groupValue.add(getGroupList().get(groupCnt));
            }
            permList.put("groupList", groupValue);
        }

        //Add permissions to list
        if (getPermList().isEmpty()) {
            //Do not add anything to list
        } else {
            for (int permissions = 0; permissions < getPermList().size(); permissions++) {
                permValue.add(getPermList().get(permissions));
            }
            permList.put("permList", permValue);
        }

        if (getUserList().isEmpty() && getGroupList().isEmpty() && getPermList().isEmpty()) {
            System.out.println("permMapList is empty");
            //Do not add anything to list
        } else {
            permMapList.add(permList);
            policy.put("permMapList", permMapList);
        }

        policy.put("policyName", getPolicyName());
        policy.put("databases", getDatabases());
        policy.put("tables", getTables());
        policy.put("columns", getColumns());
        policy.put("udfs", getUdfs());
        policy.put("description", getDescription());
        policy.put("repositoryName", getRepositoryName());
        policy.put("repositoryType", getRepositoryType());
        policy.put("tableType", getTableType());
        policy.put("columnType", getColumnType());
        policy.put("isEnabled", getIsEnabled());
        policy.put("isAuditEnabled", getIsAuditEnabled());

        return policy;

    }

}	
