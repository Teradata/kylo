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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset implements Serializable {

    private String time;
    private Datasource datasource;
    private ChangeType changeType;
    private ContentType contentType;
    private List<? extends ChangeSet> changeSets;

    public Dataset() {
        super();
    }

    public Dataset(Datasource src, ChangeType change, ContentType content, ChangeSet... changeSets) {
        this(new DateTime(), src, change, content, Arrays.asList(changeSets));
    }

    public Dataset(Datasource src, ChangeType change, ContentType content, List<? extends ChangeSet> changeSets) {
        this(new DateTime(), src, change, content, changeSets);
    }

    public Dataset(DateTime time, Datasource src, ChangeType change, ContentType content, ChangeSet... changeSets) {
        this(time, src, change, content, Arrays.asList(changeSets));
    }

    public Dataset(DateTime time, Datasource src, ChangeType change, ContentType content, List<? extends ChangeSet> changeSets) {
        super();
        this.time = Formatters.print(time);
        this.datasource = src;
        this.changeType = change;
        this.contentType = content;
        this.changeSets = changeSets;
    }

    @Override
    public String toString() {
        return "Dataset - datasource: " + Objects.toString(this.datasource.getName()) + ", content type: "
               + Objects.toString(this.contentType) + ", changes set: " + Objects.toString(this.changeSets);
    }

    public String getTime() {
        return time;
    }

    @JsonIgnore
    public void setTime(DateTime dateTime) {
        this.time = Formatters.print(dateTime);
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType getType) {
        this.changeType = getType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public List<? extends ChangeSet> getChangeSets() {
        return changeSets;
    }

    public void setChangeSets(List<? extends ChangeSet> changeSets) {
        this.changeSets = changeSets;
    }

    public enum ChangeType {
        UPDATE, DELETE
    }

    public enum ContentType {
        PARTITIONS, FILES
    }
}
