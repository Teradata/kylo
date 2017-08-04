/**
 * 
 */
package com.thinkbiganalytics.security.rest.model;

import java.util.HashMap;

/*-
 * #%L
 * kylo-security-rest-model
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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A set of role memberships grouped by role name and separated between
 * those assigned to an entity and those inherited by the entity.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleMemberships {

    private Map<String, RoleMembership> inherited;
    private Map<String, RoleMembership> assigned;
    
    public RoleMemberships() {
    }
    
    public RoleMemberships(Map<String, RoleMembership> assigned) {
        this(null, assigned);
    }
    
    public RoleMemberships(Map<String, RoleMembership> inherited, Map<String, RoleMembership> assigned) {
        super();
        this.inherited = inherited;
        this.assigned = assigned;
    }


    public Map<String, RoleMembership> getInherited() {
        return inherited;
    }
    public Map<String, RoleMembership> getAssigned() {
        return assigned;
    }

    public void setInherited(Map<String, RoleMembership> inherited) {
        this.inherited = new HashMap<>(inherited);
    }

    public void setAssigned(Map<String, RoleMembership> assigned) {
        this.assigned = new HashMap<>(assigned);;
    }
    
}
