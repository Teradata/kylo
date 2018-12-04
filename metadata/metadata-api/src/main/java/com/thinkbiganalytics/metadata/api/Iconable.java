/**
 * 
 */
package com.thinkbiganalytics.metadata.api;

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

/**
 * Identifies an entity that has an associated icon and icon color.
 */
public interface Iconable {

    String getIcon();

    void setIcon(String icon);

    String getIconColor();

    void setIconColor(String iconColor);

}
