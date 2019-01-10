package com.thinkbiganalytics.metadata.api.project;

/*-
 * #%L
 * kylo-metadata-api
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

import com.thinkbiganalytics.metadata.api.Auditable;
import com.thinkbiganalytics.metadata.api.Iconable;
import com.thinkbiganalytics.metadata.api.SystemEntity;
import com.thinkbiganalytics.metadata.api.Taggable;
import com.thinkbiganalytics.security.AccessControlled;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Project extends AccessControlled, Auditable, Iconable, SystemEntity, Taggable {

    Project.ID getId();

    @Nullable
    String getProjectName();

    void setProjectName(@Nullable final String displayName);

    @Nonnull
    String getContainerImage();

    void setContainerImage(String image);

    interface ID extends Serializable {

    }
}
