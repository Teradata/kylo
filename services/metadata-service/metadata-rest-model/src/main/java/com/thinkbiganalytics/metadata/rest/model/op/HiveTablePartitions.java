/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.op;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.data.HiveTablePartition;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HiveTablePartitions extends ChangeSet {

    private static final long serialVersionUID = 301189054569959895L;

    List<HiveTablePartition> partitions = new ArrayList<>();

    public List<HiveTablePartition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<HiveTablePartition> partitions) {
        this.partitions = partitions;
    }

    public void addPartition(HiveTablePartition hiveTablePartition) {
        this.partitions.add(hiveTablePartition);
    }


}
