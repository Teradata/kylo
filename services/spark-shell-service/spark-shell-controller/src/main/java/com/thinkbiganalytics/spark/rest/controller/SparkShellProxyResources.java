package com.thinkbiganalytics.spark.rest.controller;

/*-
 * #%L
 * Spark Shell Service Controllers
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
 * Resource bundle keys for the Spark Shell Proxy controller.
 */
class SparkShellProxyResources {

    /**
     * An error occurred while attempting to download the results.
     */
    static final String DOWNLOAD_ERROR = "download.error";

    /**
     * The requested download could not be found.
     */
    static final String DOWNLOAD_NOT_FOUND = "download.notFound";

    /**
     * An error occurred while attempting to save the results.
     */
    static final String SAVE_ERROR = "save.error";

    /**
     * The request must include the output format.
     */
    static final String SAVE_MISSING_FORMAT = "save.missingFormat";

    /**
     * The requested transformation could not be found.
     */
    static final String SAVE_NOT_FOUND = "save.notFound";

    /**
     * An error occurred during a transform request.
     */
    static final String TRANSFORM_ERROR = "transform.error";

    /**
     * Instances of {@code SparkShellProxyResources} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private SparkShellProxyResources() {
        throw new UnsupportedOperationException();
    }
}
