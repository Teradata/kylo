/**
 *
 */
package com.thinkbiganalytics.metadata.api.extension;

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

/**
 *
 */
public interface FieldDescriptor {

    Type getType();  //

    String getName();

    String getDisplayName();

    String getDescription();

    boolean isCollection();

    boolean isRequired();

    enum Type {STRING, BOOLEAN, INTEGER, LONG, DOUBLE, DATE, ENTITY, WEAK_REFERENCE} // TODO need BINARY?

    // TODO do we need a default value.  If so then how do we represent it; especially for "entity" type
}
