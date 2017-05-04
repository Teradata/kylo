package com.thinkbiganalytics.metadata.jpa.cluster;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.cluster.NiFiFlowCacheUpdateType;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterUpdateItem;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "NIFI_FLOW_CACHE_UPDATE_ITEM")
public class JpaNiFiFlowCacheClusterUpdateItem implements NiFiFlowCacheClusterUpdateItem {



    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name="UPDATE_KEY",unique = true)
    private String updateKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "UPDATE_TYPE")
    private NiFiFlowCacheUpdateType updateType;

    @Column(name="UPDATE_VALUE")
    @Lob
    private String updateValue;


    @Override
    public String getUpdateKey() {
        return updateKey;
    }

    public void setUpdateKey(String updateKey) {
        this.updateKey = updateKey;
    }

    @Override
    public NiFiFlowCacheUpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(NiFiFlowCacheUpdateType updateType) {
        this.updateType = updateType;
    }

    @Override
    public String getUpdateValue() {
        return updateValue;
    }

    public void setUpdateValue(String updateValue) {
        this.updateValue = updateValue;
    }
}
