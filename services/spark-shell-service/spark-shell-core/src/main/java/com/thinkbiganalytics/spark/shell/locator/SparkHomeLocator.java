package com.thinkbiganalytics.spark.shell.locator;

/*-
 * #%L
 * Spark Shell Core
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

import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Locates the Spark client home directory.
 */
public interface SparkHomeLocator {

    /**
     * Attempts to locate the Spark client home directory.
     *
     * @return the directory, if found
     */
    @Nonnull
    Optional<File> locate();
}
