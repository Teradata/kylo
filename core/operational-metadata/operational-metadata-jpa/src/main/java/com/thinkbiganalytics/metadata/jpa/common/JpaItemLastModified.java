package com.thinkbiganalytics.metadata.jpa.common;

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

import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import com.thinkbiganalytics.metadata.api.common.ItemLastModified;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "ITEM_LAST_MODIFIED")
public class JpaItemLastModified implements Serializable, ItemLastModified {


    @Id
    @Column(name = "ITEM_KEY")
    private String key;

    @Column(name = "ITEM_VALUE")
    private String value;

    @Column(name = "MODIFIED_TIME", insertable = false, updatable = false)
    private Long lastModifiedMillis;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "MODIFIED_TIME")
    @QueryType(PropertyType.COMPARABLE)
    private DateTime modifiedTime;


    public JpaItemLastModified() {

    }

    public JpaItemLastModified(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaItemLastModified that = (JpaItemLastModified) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}