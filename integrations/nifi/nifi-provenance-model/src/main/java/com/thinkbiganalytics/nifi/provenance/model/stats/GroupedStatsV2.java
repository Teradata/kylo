package com.thinkbiganalytics.nifi.provenance.model.stats;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 7/26/17.
 */
public class GroupedStatsV2 extends GroupedStats {

    protected Map<String,Object> additionalProperties = new HashMap<>();

    protected String latestFlowFileId;

    public String getLatestFlowFileId() {
        return latestFlowFileId;
    }

    public void setLatestFlowFileId(String latestFlowFileId) {
        this.latestFlowFileId = latestFlowFileId;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public GroupedStatsV2() {
        super();
    }

    public GroupedStatsV2(String sourceConnectionIdentifier) {
        super(sourceConnectionIdentifier);
    }

    public GroupedStatsV2(GroupedStats other) {
        super(other);
        if(other instanceof  GroupedStatsV2){
            this.latestFlowFileId = ((GroupedStatsV2)other).getLatestFlowFileId();
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.latestFlowFileId = null;
    }
}
