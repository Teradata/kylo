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
import java.util.StringJoiner;

import javax.annotation.Nonnull;

/**
 * Locates the specified app by using the {@code KYLO_SERVICES_HOME} environment variable.
 */
public class EnvironmentAppLocator implements AppLocator {

    @Nonnull
    @Override
    public Optional<File> locate(@Nonnull final String filename) {
        return Optional.ofNullable(System.getenv("KYLO_SERVICES_HOME"))
            .map(servicesHome -> new StringJoiner(File.separator).add(servicesHome).add("lib").add("app").add(filename).toString())
            .map(File::new);
    }
}
