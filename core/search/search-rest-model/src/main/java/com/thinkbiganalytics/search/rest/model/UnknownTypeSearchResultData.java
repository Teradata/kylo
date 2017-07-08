package com.thinkbiganalytics.search.rest.model;

/*-
 * #%L
 * kylo-search-rest-model
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
 * Stores the search results coming from a type unknown to Kylo
 */
//This can happen if the index is not known to Kylo
public class UnknownTypeSearchResultData extends AbstractSearchResultData {

    public UnknownTypeSearchResultData() {
        final String ICON = "report";
        final String COLOR = "DarkKhaki";
        super.setIcon(ICON);
        super.setColor(COLOR);
        super.setType(SearchResultType.KYLO_UNKNOWN);
    }
}
