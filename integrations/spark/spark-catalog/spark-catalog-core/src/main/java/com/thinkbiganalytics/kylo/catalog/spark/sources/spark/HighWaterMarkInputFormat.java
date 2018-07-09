package com.thinkbiganalytics.kylo.catalog.spark.sources.spark;

/*-
 * #%L
 * Kylo Catalog Core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.google.common.annotations.VisibleForTesting;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A file input format that only reads input paths that have been modified since the previous read.
 *
 * <p>NOTE: This input format will not function properly as an {@code InputFormat}. It is meant to be used only by the Kylo Catalog.</p>
 */
public class HighWaterMarkInputFormat extends TextInputFormat {

    /**
     * Property for the high water mark value
     */
    public static final String HIGH_WATER_MARK = "kylo.catalog.fs.highWaterMark";

    /**
     * Property for the maximum file age
     */
    public static final String MAX_FILE_AGE = "kylo.catalog.fs.maxFileAge";

    /**
     * Property for the minimum file age
     */
    public static final String MIN_FILE_AGE = "kylo.catalog.fs.minFileAge";

    /**
     * Gets the high water mark value.
     */
    public static long getHighWaterMark(@Nonnull final JobContext job) {
        return job.getConfiguration().getLong(HIGH_WATER_MARK, Long.MIN_VALUE);
    }

    /**
     * Sets the high water mark value.
     */
    public static void setHighWaterMark(@Nonnull final Job job, final long highWaterMark) {
        job.getConfiguration().setLong(HIGH_WATER_MARK, highWaterMark);
    }

    /**
     * Gets the maximum file age.
     */
    public static long getMaxFileAge(@Nonnull final JobContext job) {
        return job.getConfiguration().getLong(MAX_FILE_AGE, Long.MAX_VALUE);
    }

    /**
     * Sets the maximum file age.
     */
    public static void setMaxFileAge(@Nonnull final Job job, final long maxFileAge) {
        job.getConfiguration().setLong(MAX_FILE_AGE, maxFileAge);
    }

    /**
     * Gets the minimum file age.
     */
    public static long getMinFileAge(@Nonnull final JobContext job) {
        return job.getConfiguration().getLong(MIN_FILE_AGE, Long.MIN_VALUE);
    }

    /**
     * Sets the minimum file age.
     */
    public static void setMinFileAge(@Nonnull final Job job, final long minFileAge) {
        job.getConfiguration().setLong(MIN_FILE_AGE, minFileAge);
    }

    /**
     * Last high water mark value
     */
    private long lastHighWaterMark = Long.MIN_VALUE;

    /**
     * Gets the last high water mark value.
     */
    public long getLastHighWaterMark() {
        return lastHighWaterMark;
    }

    @Nonnull
    @Override
    public List<FileStatus> listStatus(@Nonnull final JobContext job) throws IOException {
        // Get job configuration
        long highWaterMark = Math.max(lastHighWaterMark, getHighWaterMark(job));
        final long maxAge = HighWaterMarkInputFormat.getMaxFileAge(job);
        final long minAge = HighWaterMarkInputFormat.getMinFileAge(job);

        if (minAge > maxAge) {
            throw new IOException(MIN_FILE_AGE + " cannot be greater than " + MAX_FILE_AGE);
        }

        // List and filter files
        final List<FileStatus> allFiles = super.listStatus(job);
        final List<FileStatus> jobFiles = new ArrayList<>(allFiles.size());
        final long currentTime = currentTimeMillis();

        for (final FileStatus file : allFiles) {
            final long fileTime = file.getModificationTime();
            final long fileAge = currentTime - fileTime;

            if (!file.isDirectory() && fileAge >= minAge && fileAge <= maxAge && fileTime > highWaterMark) {
                jobFiles.add(file);

                if (fileTime > lastHighWaterMark) {
                    lastHighWaterMark = fileTime;
                }
            }
        }

        lastHighWaterMark = Math.max(lastHighWaterMark, highWaterMark);
        return jobFiles;
    }

    /**
     * Gets the current time in milliseconds.
     */
    @VisibleForTesting
    long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
