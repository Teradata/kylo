/**
 *
 */
package com.thinkbiganalytics.metadata.api;

/*-
 * #%L
 * thinkbig-metadata-api
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
 * Wraps any unhandled exception that is thrown within a MetadataCommand or MetadataAction.
 */
public class MetadataExecutionException extends MetadataException {

    private static final long serialVersionUID = -8584258657040637162L;

    /**
     * @param message
     * @param cause
     */
    public MetadataExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MetadataExecutionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MetadataExecutionException(Throwable cause) {
        super(cause);
    }

}
