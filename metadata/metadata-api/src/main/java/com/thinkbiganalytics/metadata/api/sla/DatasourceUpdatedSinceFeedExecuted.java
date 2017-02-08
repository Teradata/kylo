/**
 *
 */
package com.thinkbiganalytics.metadata.api.sla;

/*-
 * #%L
 * thinkbig-metadata-api
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

import java.beans.Transient;

/**
 *
 */
public class DatasourceUpdatedSinceFeedExecuted extends DependentDatasource {

    public DatasourceUpdatedSinceFeedExecuted() {
    }

    public DatasourceUpdatedSinceFeedExecuted(String datasourceName, String feedName) {
        super(feedName, datasourceName);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    @Transient
    public String getDescription() {
        return "Datasource " + getDatasourceName() + " has been updated since feed " + getFeedName() + " was last executed";
    }
}
