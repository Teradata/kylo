package com.thinkbiganalytics.modeshape.index.elasticsearch;

/*-
 * #%L
 * kylo-modeshape-elasticsearch-index-provider
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
 * A simple class to store relevant properties for modeshape node raising a change event
 */
public class KyloEsNodeDetails {

    private String extendedId;
    private String id;
    private String allowIndexing;
    private static final Integer START_INDEX_UUID_EXTRACTION = 14;

    public String getExtendedId() {
        return extendedId;
    }

    public void setExtendedId(String extendedId) {
        this.extendedId = extendedId;
        this.id = extendedId.substring(START_INDEX_UUID_EXTRACTION, extendedId.length());
    }

    public String getId() {
        return id;
    }

    public String getAllowIndexing() {
        return allowIndexing;
    }

    public void setAllowIndexing(String allowIndexing) {
        this.allowIndexing = allowIndexing;
    }

    @Override
    public String toString() {
        return "KyloEsNodeDetails{" +
               "extendedId='" + extendedId + '\'' +
               ", id='" + id + '\'' +
               ", allowIndexing='" + allowIndexing + '\'' +
               '}';
    }
}
