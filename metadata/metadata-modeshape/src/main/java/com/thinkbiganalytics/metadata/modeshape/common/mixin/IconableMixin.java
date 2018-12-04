/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.common.mixin;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.Iconable;

/**
 * A mixin interface to be implemented by classes that wrap nodes extending the "tba:Iconed" mixin types.
 */
public interface IconableMixin extends WrappedNodeMixin, Iconable {
    
    String ICON = "tba:icon";
    String ICON_COLOR = "tba:iconColor";


    @Override
    default String getIcon() {
        return getProperty(ICON, String.class);
    }

    @Override
    default void setIcon(String icon) {
        setProperty(ICON, icon);
    }

    @Override
    default String getIconColor() {
        return getProperty(ICON_COLOR, String.class);
    }

    @Override
    default void setIconColor(String iconColor) {
        setProperty(ICON_COLOR, iconColor);
    }
}
