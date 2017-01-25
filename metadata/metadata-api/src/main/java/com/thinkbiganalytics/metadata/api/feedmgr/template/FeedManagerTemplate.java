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

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerTemplate {

    interface ID extends Serializable { }

    List<FeedManagerFeed> getFeeds();

    boolean addFeed(FeedManagerFeed<?> feed);

    boolean removeFeed(FeedManagerFeed<?> feed);

    enum State {
        ENABLED, DISABLED
    }

    ID getId();

    String getName();

    String getNifiTemplateId();

    String getDescription();

    boolean isDefineTable();

    boolean isDataTransformation();

    boolean isAllowPreconditions();

    String getIcon();

    String getIconColor();

    String getJson();

    DateTime getCreatedTime();

    DateTime getModifiedTime();


    void setNifiTemplateId(String nifiTemplateId);

    void setAllowPreconditions(boolean allowedPreconditions);

    void setDefineTable(boolean defineTable);

    void setDataTransformation(boolean dataTransformation);

    void setName(String name);

    void setIcon(String icon);

    void setIconColor(String iconColor);

    void setDescription(String description);

    void setJson(String json);

    void setState(State state);

    State getState();

    Long getOrder();

    void setOrder(Long order);

    boolean isStream();

    void setStream(boolean stream);

}
