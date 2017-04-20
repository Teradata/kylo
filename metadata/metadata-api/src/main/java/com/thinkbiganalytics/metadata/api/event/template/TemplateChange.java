/**
 *
 */
package com.thinkbiganalytics.metadata.api.event.template;

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

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate.ID;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate.State;

import java.util.Objects;

/**
 *
 */
public class TemplateChange extends MetadataChange {

    private static final long serialVersionUID = 1L;

    private final FeedManagerTemplate.ID templateId;
    private final FeedManagerTemplate.State templateState;

    public TemplateChange(ChangeType change, String descr, ID templateId, State state) {
        super(change, descr);
        this.templateId = templateId;
        this.templateState = state;
    }

    public TemplateChange(ChangeType change, ID templateId, State state) {
        this(change, "", templateId, state);
    }


    public FeedManagerTemplate.ID getTemplateId() {
        return templateId;
    }

    public FeedManagerTemplate.State getTemplateState() {
        return templateState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.templateId, this.templateState);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemplateChange) {
            TemplateChange that = (TemplateChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.templateId, this.templateId) &&
                   Objects.equals(this.templateState, that.templateState);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Template change ");
        return sb
            .append("(").append(getChange()).append(") - ")
            .append("ID: ").append(this.templateId)
            .append(" state: ").append(this.templateState)
            .append(" desc: ").append(this.getDescription())
            .toString();

    }

}
