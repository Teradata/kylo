package com.thinkbiganalytics.metadata.api.feedmgr.template;

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

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.security.AccessControlled;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 */
public interface FeedManagerTemplate extends AccessControlled {

    List<FeedManagerFeed> getFeeds();

    boolean addFeed(FeedManagerFeed<?> feed);

    boolean removeFeed(FeedManagerFeed<?> feed);

    ID getId();

    String getName();

    void setName(String name);

    String getNifiTemplateId();

    void setNifiTemplateId(String nifiTemplateId);

    String getDescription();

    void setDescription(String description);

    boolean isDefineTable();

    void setDefineTable(boolean defineTable);

    boolean isDataTransformation();

    void setDataTransformation(boolean dataTransformation);

    boolean isAllowPreconditions();

    void setAllowPreconditions(boolean allowedPreconditions);

    String getIcon();

    void setIcon(String icon);

    String getIconColor();

    void setIconColor(String iconColor);

    String getJson();

    void setJson(String json);

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    State getState();

    void setState(State state);

    Long getOrder();

    void setOrder(Long order);

    boolean isStream();

    void setStream(boolean stream);

    enum State {
        ENABLED, DISABLED
    }

    interface ID extends Serializable {

    }

}
