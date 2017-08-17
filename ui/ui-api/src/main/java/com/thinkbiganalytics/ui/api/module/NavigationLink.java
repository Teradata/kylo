package com.thinkbiganalytics.ui.api.module;

/*-
 * #%L
 * kylo-ui-api
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
import java.util.List;

/**
 * Created by sr186054 on 8/16/17.
 */
public interface NavigationLink {

    String getToggleGroupName();

    String getSref();

    LINK_TYPE getType();

    String getIcon();

    String getText();

    String getNarrowText();

    boolean isDefaultActive();

    boolean isFullscreen();

    List<String> getPermission();

    boolean isExpanded();

    String getElementId();

    public enum LINK_TYPE {
        TOGGLE, LINK;
    }

    List<NavigationLink> getLinks();
}
