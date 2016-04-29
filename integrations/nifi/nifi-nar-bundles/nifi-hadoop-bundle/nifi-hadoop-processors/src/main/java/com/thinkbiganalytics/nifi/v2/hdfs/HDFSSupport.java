/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.hdfs;

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
    public static Logger logger = LoggerFactory.getLogger(HDFSSupport.class);

    public FileSystem hdfs;

    public HDFSSupport(FileSystem hdfs) {
        this.hdfs = hdfs;
    }

    /**
     * Creates an HDFS folder
     *
     * @param path
     * @return
     */
    public boolean createFolder(Path path, String owner, String group) {
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
                return true;
            }

        } catch (Exception e) {
            logger.error("Failed to create path {} due to {}", path, e);
            throw new RuntimeException("Failed to create path [" + path + "]", e);
        }
        return false;
    }

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
