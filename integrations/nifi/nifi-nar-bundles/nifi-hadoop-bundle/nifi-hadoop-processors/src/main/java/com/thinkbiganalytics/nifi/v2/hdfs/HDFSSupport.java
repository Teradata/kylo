package com.thinkbiganalytics.nifi.v2.hdfs;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Provides support for HDFS folder creation
 */
public class HDFSSupport {

    private static final Logger logger = LoggerFactory.getLogger(HDFSSupport.class);

    private FileSystem hdfs;

    public HDFSSupport(FileSystem hdfs) {
        this.hdfs = hdfs;
    }


    /**
     * Creates an HDFS folder
     *
     * @param path  The location in the HDFS file system
     * @param owner The owener of the new HDFS folder
     * @param group The group of the new HDFS folder
     * @throws IOException if creating the folder fails
     */
    public void createFolder(Path path, String owner, String group) throws IOException {
        try {
            // Create destination directory if it does not exist
            logger.info("Creating path {} ", path);
            try {
                if (!hdfs.getFileStatus(path).isDirectory()) {
                    throw new IOException(path.toString() + " already exists and is not a directory");
                }
            } catch (FileNotFoundException fe) {
                if (!hdfs.mkdirs(path)) {
                    throw new IOException(path.toString() + " could not be created");
                }
                changeOwner(path, owner, group);
                logger.info("Successfully created path {}", path);
            }
        } catch (Exception e) {
            logger.error("Failed to create path {} due to {}", path, e);
            throw new RuntimeException("Failed to create path [" + path + "]", e);
        }
    }

    /**
     * Changes the owner of an existing file or directory found at path
     *
     * @param path  The path of the file or directory
     * @param owner The owner to set
     * @param group The group to set
     */
    public void changeOwner(final Path path, String owner, String group) {
        try {
            if (owner != null || group != null) {
                hdfs.setOwner(path, owner, group);
            }
        } catch (Exception e) {
            logger.warn("Could not change owner or group of {} on HDFS due to {}", new Object[]{path, e});
        }
    }

}
