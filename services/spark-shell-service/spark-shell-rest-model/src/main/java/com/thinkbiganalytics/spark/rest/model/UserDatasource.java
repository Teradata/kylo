package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

import javax.annotation.Nonnull;

/**
 * A user-created data source that is accessible in Spark.
 */
public class UserDatasource extends Datasource implements com.thinkbiganalytics.metadata.datasource.UserDatasource {

    private static final long serialVersionUID = -1995038633214108566L;

    private String type;

    /**
     * Constructs a {@code UserDatasource} with null values.
     */
    @SuppressWarnings("unused")
    public UserDatasource() {
    }

    /**
     * Constructs a {@code UserDatasource} by copying another data source.
     *
     * @param other the other data source
     */
    public UserDatasource(@Nonnull final com.thinkbiganalytics.metadata.datasource.UserDatasource other) {
        super(other);
        setType(other.getType());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
