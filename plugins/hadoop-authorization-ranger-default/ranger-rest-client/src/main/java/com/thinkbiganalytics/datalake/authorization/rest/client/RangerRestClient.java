package com.thinkbiganalytics.datalake.authorization.rest.client;

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

import com.thinkbiganalytics.datalake.authorization.model.HadoopAuthorizationGroup;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerCreateOrUpdatePolicy;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerGroup;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerGroups;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerPolicies;
import com.thinkbiganalytics.datalake.authorization.rest.model.RangerPolicy;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.rest.JerseyRestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

/**
 * This class has all the functions for implementing REST calls like createPolicy, getPolicy, updatePolicy etc.
 */

public class RangerRestClient extends JerseyRestClient {

    private static final String API_PATH = "/service";

    public RangerRestClient(RangerRestClientConfig config) {
        super(config);
    }

    protected WebTarget getBaseTarget() {
        WebTarget target = super.getBaseTarget();
        return target.path(API_PATH);
    }

    public void createPolicy(RangerCreateOrUpdatePolicy policy) {
        try {
            post("/public/api/policy/", policy, String.class);
        } catch (Exception e) {
            throw new RangerRestClientException("Unable to create a ranger policy.", e);
        }
    }

    public void updatePolicy(RangerCreateOrUpdatePolicy obj, int policyId) throws RangerRestClientException {
        try {
            put("/public/api/policy/" + policyId, obj, String.class);
        } catch (Exception e) {
            throw new RangerRestClientException("Unable to update policy.", e);
        }
    }

    public String deletePolicy(int policyId) throws RangerRestClientException {
        try {
            return delete("/public/api/policy/" + policyId, null, String.class);
        } catch (Exception e) {
            throw new RangerRestClientException("Unable to delete policy .", e);
        }
    }

    public List<RangerPolicy> searchPolicies(Map<String, Object> searchCriteria) throws RangerRestClientException {
        try {
            String results = get("/public/api/policy/", searchCriteria, String.class);
            RangerPolicies rangerPoliciesfromJson = ObjectMapperSerializer.deserialize(results, RangerPolicies.class);

            List<RangerPolicy> rangerPolicies = new ArrayList<>();
            for (RangerPolicy rangerPolicy : rangerPoliciesfromJson.getvXPolicies()) {
                rangerPolicies.add(rangerPolicy);
            }

            return rangerPolicies;

        } catch (Exception e) {
            throw new RangerRestClientException("Unable to search policy.", e);
        }
    }

    /***
     * Functions for getting user/groups information in Ranger
     */

    public String getAllUsers() throws RangerRestClientException {
        try {
            return get("/xusers/users", null, String.class);
        } catch (Exception e) {
            throw new RangerRestClientException("Unable to get all user.", e);
        }
    }

    public List<HadoopAuthorizationGroup> getAllGroups() {
        try {
            String results = get("/xusers/groups", null, String.class);
            RangerGroups groupsFromJson = ObjectMapperSerializer.deserialize(results, RangerGroups.class);
            List<HadoopAuthorizationGroup> rangerGroups = new ArrayList<>();
            for (RangerGroup rangerGroup : groupsFromJson.getvXGroups()) {
                rangerGroups.add(rangerGroup);
            }
            return rangerGroups;
        } catch (Exception e) {
            throw new RangerRestClientException("Unable to get all user..", e);
        }
    }

    public RangerGroup getGroupByName(String groupName) {
        try {
            String result = get("/xusers/groups/groupName/" + groupName, null, String.class);
            return ObjectMapperSerializer.deserialize(result, RangerGroup.class);
        } catch (Exception e) {
            throw new RangerRestClientException("Unable to get group by name.", e);
        }

    }
}
