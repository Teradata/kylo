package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
 * %%
 * %% Licensed under the Apache License, Version 2.0 (the "License");
 * %% you may not use this file except in compliance with the License.
 * %% You may obtain a copy of the License at
 * %%
 * %%     http://www.apache.org/licenses/LICENSE-2.0
 * %%
 * %% Unless required by applicable law or agreed to in writing, software
 * %% distributed under the License is distributed on an "AS IS" BASIS,
 * %% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * %% See the License for the specific language governing permissions and
 * %% limitations under the License.
 * #L%
 */


/**
 * Kylo configuration inspection, which is executed upon user requests.
 * It should provide two types which will be autowired with kylo-services and kylo-ui properties, e.g.
 * <pre>
 * class ServicesProperties {
 *    {@literal @}Value("${property-name}")
 *     private String property;
 * }
 * </pre>
 *
 * At the moment these types can only be autowired with <code>String</code> values.
 *
 * @param <SP> type which defines Kylo-Services properties
 * @param <UP> type which defines Kylo-UI properties
 */
public interface Inspection<SP, UP> {

    /**
     * @return unique id
     */
    int getId();

    void setId(int id);

    /**
     * @return human readable name
     */
    String getName();

    /**
     * @return human readable description
     */
    String getDescription();

    /**
     * This method is executed to inspect provided kylo-services and kylo-ui properties
     * @param servicesProperties kylo-services properties
     * @param uiProperties kylo-ui properties
     * @return inspection status
     */
    InspectionStatus inspect(SP servicesProperties, UP uiProperties);

    /**
     * @return a new blank instance of kylo-services properties which are of interest to this configuration
     */
    SP getServicesProperties();

    /**
     * @return a new blank instance of kylo-ui properties which are of interest to this configuration
     */
    UP getUiProperties();
}
