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

/***
 * Class for setting parameters for HDFS policy which returns JSON policy object
 *
 */
public class HDFSPolicy {

    private static final Logger log = LoggerFactory.getLogger(HDFSPolicy.class);
    private static ArrayList<String> permList = new ArrayList<String>();
    private ArrayList<String> userList = new ArrayList<String>();
    private ArrayList<String> groupList = new ArrayList<String>();
    private String policyName;
    private String resourceName;
    private String description;
    private String repositoryName;
    private String repositoryType;
    private String isEnabled;
    private String isRecursive;
    private String isAuditEnabled;

    public static ArrayList<String> getPermissions() {
        return permList;
    }

    public static void setPermissions(ArrayList<String> permList) {
        HDFSPolicy.permList = permList;
    }

    /**
     * getter and setter
     */
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

    public String getRepositorytype() {
        return repositoryType;
    }

    public void setRepositorytype(String repositoryType) {
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

    public ArrayList<String> getUsers() {
        return userList;
    }

    public void setUsers(ArrayList<String> userList) {
        this.userList = userList;
    }

    public ArrayList<String> getGroups() {
        return groupList;
    }

    public void setGroups(ArrayList<String> groupList) {
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
        if (getUsers().isEmpty()) {
            System.out.println("empty");
            //Do not add anything to list
        } else {
            for (int userCnt = 0; userCnt < getUsers().size(); userCnt++) {
                userValue.add(getUsers().get(userCnt));
            }
            permList.put("userList", userValue);
        }

        //Add groups to list
        if (getGroups().isEmpty()) {
            //Do not add anything to list
        } else {
            for (int groupCnt = 0; groupCnt < getGroups().size(); groupCnt++) {
                groupValue.add(getGroups().get(groupCnt));
            }
            permList.put("groupList", groupValue);
        }

        //Add permissions to list
        if (getPermissions().isEmpty()) {
            //Do not add anything to list
        } else {
            for (int permissions = 0; permissions < getPermissions().size(); permissions++) {
                permValue.add(getPermissions().get(permissions));
            }
            permList.put("permList", permValue);
        }

        if (getUsers().isEmpty() && getGroups().isEmpty() && getPermissions().isEmpty()) {
            System.out.println("permMapList is empty");
            //Do not add anything to list
        } else {
            permMapList.add(permList);
            policy.put("permMapList", permMapList);
        }

        policy.put("policyName", getPolicyName());
        policy.put("resourceName", getResourceName());
        policy.put("description", getDescription());
        policy.put("repositoryName", getRepositoryName());
        policy.put("repositoryType", getRepositorytype());
        policy.put("isEnabled", getIsEnabled());
        policy.put("isRecursive", getIsRecursive());
        policy.put("isAuditEnabled", getIsAuditEnabled());

        return policy;

    }

}
