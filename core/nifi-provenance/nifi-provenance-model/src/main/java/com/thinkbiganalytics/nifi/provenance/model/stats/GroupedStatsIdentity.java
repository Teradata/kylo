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

import java.io.Serializable;

public class GroupedStatsIdentity implements Serializable{


    private static final long serialVersionUID = 7545615440353964029L;

    private String processorId;
    private String processorName;

    public GroupedStatsIdentity() {
    }

    public GroupedStatsIdentity(String processorId, String processorName) {
        this.processorId = processorId;
        this.processorName = processorName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupedStatsIdentity)) {
            return false;
        }

        GroupedStatsIdentity that = (GroupedStatsIdentity) o;

        if (processorId != null ? !processorId.equals(that.processorId) : that.processorId != null) {
            return false;
        }
        if (processorName != null ? !processorName.equals(that.processorName) : that.processorName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = processorId != null ? processorId.hashCode() : 0;
        result = 31 * result + (processorName != null ? processorName.hashCode() : 0);
        return result;
    }
}
