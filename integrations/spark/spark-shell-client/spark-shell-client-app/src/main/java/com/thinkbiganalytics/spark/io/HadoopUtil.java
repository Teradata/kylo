package com.thinkbiganalytics.spark.io;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Static support methods for interacting with the Hadoop API.
 */
public class HadoopUtil {

    private static final Logger log = LoggerFactory.getLogger(HadoopUtil.class);

    /**
     * Deletes a file or directory using a background thread.
     */
    public static void deleteLater(@Nonnull final Path path, @Nonnull final FileSystem fs) {
        new Thread() {
            @Override
            public void run() {
                if (!deleteSilently(path, true, fs)) {
                    if (!deleteOnExitSilently(path, fs)) {
                        log.warn("Failed to delete path: {}", path);
                    }
                }
            }
        }.start();
    }

    /**
     * Mark a path to be deleted when FileSystem is closed. Swallows any thrown IOException.
     *
     * @return {@code true} if deleteOnExit is successful, otherwise {@code false}
     */
    public static boolean deleteOnExitSilently(@Nonnull final Path path, @Nonnull final FileSystem fs) {
        try {
            return fs.deleteOnExit(path);
        } catch (final IOException e) {
            log.debug("Failed to delete on exit silently: {}", path, e);
            return false;
        }
    }

    /**
     * Delete a file. Swallows any thrown IOException.
     *
     * @return {@code true} if delet is successful, otherwise {@code false}
     */
    public static boolean deleteSilently(@Nonnull final Path path, final boolean recursive, @Nonnull final FileSystem fs) {
        try {
            return fs.delete(path, recursive);
        } catch (final IOException e) {
            log.debug("Failed to delete silently: {}", path, e);
            return false;
        }
    }

    /**
     * Instances of {@code HadoopUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private HadoopUtil() {
        throw new UnsupportedOperationException();
    }
}
