package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * kylo-feed-manager-rest-model
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import org.joda.time.DateTime;

public class TemplateChangeComment implements Comparable<TemplateChangeComment> {

    DateTime createDateTime;
    String comment;

    public TemplateChangeComment() {
    }

    public TemplateChangeComment(String comment, DateTime createDateTime) {
        this.comment = comment;
        this.createDateTime = createDateTime;
    }

    public DateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(DateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int compareTo(TemplateChangeComment o) {
        return o.getCreateDateTime().compareTo(this.getCreateDateTime());
    }
}
