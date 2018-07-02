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


import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

public class NifiPropertyGroup {

    public static enum GroupType {
        PROCESSOR, REMOTE_PROCESS_GROUP
    }


    /**
     * the list of properties in the group
     */
    private List<NifiProperty> propertyList;

    /**
     * the type of group
     */
    private GroupType groupType;

    /**
     * The id of the processor or remote process group
     */
    private String id;

    public NifiPropertyGroup() {
    }

    public NifiPropertyGroup(GroupType groupType, String id) {
        this.groupType = groupType;
        this.id = id;
    }

    public NifiPropertyGroup(String id) {
        this.id = id;
    }

    public List<NifiProperty> getPropertyList() {
        if(propertyList == null){
            propertyList = new ArrayList<>();
        }
        return propertyList;
    }

    public void setPropertyList(List<NifiProperty> propertyList) {
        this.propertyList = propertyList;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void add(NifiProperty property) {
        if(this.groupType == null){
            if(property.isRemoteProcessGroupProperty()){
                this.groupType = GroupType.REMOTE_PROCESS_GROUP;
            }
            else {
                this.groupType = GroupType.PROCESSOR;
            }
        }
        getPropertyList().add(property);
    }

    public boolean isRemoteProcessGroup(){
        return GroupType.REMOTE_PROCESS_GROUP == this.groupType;
    }

}
