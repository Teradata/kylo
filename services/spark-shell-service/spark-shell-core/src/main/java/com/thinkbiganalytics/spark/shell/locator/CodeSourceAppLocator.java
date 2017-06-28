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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Locates a Kylo app by using this class's code source location to find the Kylo Services home directory.
 */
public class CodeSourceAppLocator implements AppLocator {

    @Nonnull
    @Override
    public Optional<File> locate(@Nonnull final String filename) {
        return Optional.ofNullable(getClass().getProtectionDomain().getCodeSource())
            .map(CodeSource::getLocation)
            .map(url -> {
                try {
                    return url.toURI();
                } catch (final URISyntaxException e) {
                    return null;
                }
            })
            .map(URI::getPath)
            .map(File::new)
            .map(File::getParentFile)
            .map(parent -> new File(parent, "app" + File.separator + filename));
    }
}
