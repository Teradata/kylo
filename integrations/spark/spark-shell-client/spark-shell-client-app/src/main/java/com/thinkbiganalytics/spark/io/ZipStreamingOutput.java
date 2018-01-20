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

import com.google.common.io.ByteStreams;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;

/**
 * Packages the files in a folder as a ZIP file and writes to an output stream.
 *
 * <p>The folder will be automatically deleted after it is outputted.</p>
 */
public class ZipStreamingOutput implements StreamingOutput {

    /**
     * Folder to be packaged
     */
    @Nonnull
    private final Path folder;

    /**
     * Source file system
     */
    @Nonnull
    private final FileSystem fileSystem;

    /**
     * Constructs a {@code ZipStreamingOutput} that packages the specified folder.
     */
    public ZipStreamingOutput(@Nonnull final Path folder, @Nonnull final FileSystem fs) {
        this.folder = folder;
        this.fileSystem = fs;
    }

    @Override
    public void write(@Nonnull final OutputStream output) throws IOException {
        try {
            writeZip(output);
        } finally {
            HadoopUtil.deleteLater(folder, fileSystem);
        }
    }

    /**
     * Creates a new ZIP entry for the specified path.
     */
    @Nonnull
    private ZipEntry createEntry(@Nonnull final Path path) {
        return new ZipEntry(path.getName());
    }

    /**
     * Writes the contents of the folder to the specified output stream.
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeZip(@Nonnull final OutputStream output) throws IOException {
        final ZipOutputStream zip = new ZipOutputStream(output);

        final RemoteIterator<LocatedFileStatus> iter = fileSystem.listFiles(folder, false);
        while (iter.hasNext()) {
            // Create zip entry
            final Path path = iter.next().getPath();
            final ZipEntry entry = createEntry(path);
            zip.putNextEntry(entry);

            // Write zip entry
            try (final FSDataInputStream file = fileSystem.open(path)) {
                ByteStreams.copy(file, zip);
            }
            zip.closeEntry();
        }

        zip.close();
    }
}
