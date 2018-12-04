/**
 * 
 */
package com.thinkbiganalytics.auth.jaas.http;

/*-
 * #%L
 * kylo-security-auth
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.security.auth.callback.Callback;

/**
 * A callback for obtaining the value of a particular header in the authenticating 
 * HTTP request.
 */
public class HttpHeaderCallback implements Callback, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<String> values;

    /**
     * Creates a callback for requesting a particular header in the HTTP request.
     * @param header the header name
     */
    public HttpHeaderCallback(String header) {
        this.name = header;
    }
    
    /**
     * @return the header name to find in the request
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return true if the header was found and it had multiple values
     */
    public boolean isMultiValued() {
        return this.values != null && this.values.size() > 1;
    }
    
    /**
     * Returns a list of header values found in the request if the header was multi-valued, 
     * otherwise a list of a single value.  Returns an empty list if no matching header was found.
     * @return the list of values
     */
    public List<String> getValues() {
        return Collections.unmodifiableList(this.values);
    }
    
    /**
     * Returns an optional value of the header if a matching one was found in the request, otherwise 
     * an empty optional.  If the header was multi-valued then the result string will contain all values
     * joined by commas.
     * @return the optional header value
     */
    public Optional<String> getValue() {
        if (this.values.size() == 0) {
            return Optional.empty();
        } else {
            if (this.values.size() > 0) {
                return Optional.of(this.values.stream().collect(Collectors.joining(",")));
            } else {
                return Optional.of(this.values.get(0));
            }
        }
    }
    
    /**
     * @param value the values of a multi-valued headers
     */
    public void setValues(Collection<String> values) {
        this.values = values.stream().collect(Collectors.toList());
    }
    
    /**
     * Sets the value of the header.  If the value is comma-separated then it
     * is parsed into multiple values.
     * @param value the header value(s)
     */
    public void setValue(String value) {
        if (value == null) {
            this.values = Collections.emptyList();
        } else if (value.contains(",")) {
            Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(str -> str.length() > 0)
                .collect(Collectors.toList());
        } else {
            this.values = Collections.singletonList(value);
        }
    }
}
