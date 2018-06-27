/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.client;

/*-
 * #%L
 * kylo-catalog-credential-client
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Thrown when there is an error involved with Data Source credentials.
 */
public class DataSourceCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final Set<String> credentalNames;
    
    public DataSourceCredentialsException(String message, String... credNames) {
        super(message);
        this.credentalNames = Collections.unmodifiableSet(Arrays.stream(credNames).collect(Collectors.toSet()));
    }
    
    public DataSourceCredentialsException(String message, Collection<String> credNames) {
        super(message);
        this.credentalNames = Collections.unmodifiableSet(new HashSet<>(credNames));
    }

    public DataSourceCredentialsException(String message, Throwable cause) {
        super(message, cause);
        this.credentalNames = Collections.emptySet();
    }
    
    /**
     * @return the credentalNames names involved with the error (if any.)
     */
    public Set<String> getCredentalNames() {
        return credentalNames;
    }

}
