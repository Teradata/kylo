package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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



public interface Inspection {

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
     * @return inspection status
     */
    InspectionStatus inspect(Configuration configuration);
}
