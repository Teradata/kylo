/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.spi;

/*-
 * #%L
 * kylo-catalog-credential-api
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

/**
 * Throw where there was no DataSourceCredentialProvider found that accepts the specified data source.
 */
public class CredentialProviderNotFoundExeption extends CredentialProviderException {

    private static final long serialVersionUID = 1L;

    /**
     * @param ds
     * @param message
     */
    public CredentialProviderNotFoundExeption(DataSource ds) {
        super(ds, "A credential accepting the specified DataSource was not found");
    }

}
