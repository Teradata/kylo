package com.thinkbiganalytics.kylo.hadoop;

/*-
 * #%L
 * kylo-commons-hadoop
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

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RawLocalFileSystem;

import java.io.File;
import java.net.URI;

/**
 * A mock {@link FileSystem} for testing.
 */
public class MockFileSystem extends RawLocalFileSystem {

    private static final URI NAME = URI.create("mock:///");

    @Override
    public String getScheme() {
        return "mock";
    }

    @Override
    public URI getUri() {
        return NAME;
    }

    @Override
    public File pathToFile(final Path path) {
        return new File(path.toUri().getPath());
    }
}
