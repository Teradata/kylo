package com.thinkbiganalytics.kylo.metadata.file;
/*-
 * #%L
 * kylo-file-metadata-api
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
import java.util.List;

/**
 * Extract metadata from a list of files
 */
public interface FileMetadataExtractor {

    /**
     * For the path of files extract the metadata
     *
     * @param paths an array of file paths to inspect
     * @return a list of metadata about the files
     */
    List<FileMetadata> parse(String[] paths);
}
