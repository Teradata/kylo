package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import java.util.Collections;
import java.util.List;

public class ImportFeedOptions extends ImportOptions {

    private boolean overwriteFeedTemplate;

    private List<ImportProperty> feedProperties;

    public boolean isOverwriteFeedTemplate() {
        return overwriteFeedTemplate;
    }

    public void setOverwriteFeedTemplate(boolean overwriteFeedTemplate) {
        this.overwriteFeedTemplate = overwriteFeedTemplate;
    }


    public List<ImportProperty> getFeedProperties() {
        if(feedProperties == null){
            feedProperties = Collections.emptyList();
        }
        return feedProperties;
    }

    public void setFeedProperties(List<ImportProperty> feedProperties) {
        this.feedProperties = feedProperties;
    }
}
