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

import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Iterators;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.Seekable;
import org.apache.hadoop.fs.permission.FsPermission;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;

public class ZipStreamingOutputTest {

    /**
     * Verify streaming output.
     */
    @Test
    public void test() throws Exception {
        // Mock file system
        final FileSystem fs = Mockito.mock(FileSystem.class);
        final Path source = new Path("/tmp/source");

        final LocatedFileStatus file1 = createFile("_SUCCESS", source);
        final LocatedFileStatus file2 = createFile("part-0", source);
        Mockito.when(fs.listFiles(source, false)).thenReturn(new ForwardingRemoteIterator<>(Iterators.forArray(file1, file2)));

        final FSDataInputStream fileStream = new FSDataInputStream(new SeekableNullInputStream());
        Mockito.when(fs.open(file1.getPath())).thenReturn(fileStream);
        Mockito.when(fs.open(file2.getPath())).thenReturn(fileStream);

        final CountDownLatch deleteLatch = new CountDownLatch(1);
        Mockito.when(fs.delete(source, true)).then(new Answer<Boolean>() {
            @Override
            public Boolean answer(final InvocationOnMock invocation) {
                deleteLatch.countDown();
                return true;
            }
        });

        // Write ZIP to output stream
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ZipStreamingOutput zip = new ZipStreamingOutput(source, fs);
        zip.write(out);

        // Verify output stream
        final ZipInputStream in = new ZipInputStream(out.toInputStream());

        ZipEntry entry = in.getNextEntry();
        Assert.assertNotNull("Missing _SUCCESS entry", entry);
        Assert.assertEquals("_SUCCESS", entry.getName());

        entry = in.getNextEntry();
        Assert.assertNotNull("Missing part-0 entry", entry);
        Assert.assertEquals("part-0", entry.getName());

        entry = in.getNextEntry();
        Assert.assertNull("Unexpected entry", entry);

        // Verify path deleted
        deleteLatch.await(1, TimeUnit.SECONDS);
        Mockito.verify(fs).delete(source, true);
    }

    /**
     * Creates a new file with the specified name and parent directory.
     */
    @Nonnull
    private LocatedFileStatus createFile(@Nonnull final String name, @Nonnull final Path parent) {
        return new LocatedFileStatus(0, false, 0, 0, 0, 0, FsPermission.getDefault(), "root", "root", null, new Path(parent, name), null);
    }

    /**
     * A remote iterator which forwards its method calls to another iterator.
     */
    private static class ForwardingRemoteIterator<T> extends ForwardingIterator<T> implements RemoteIterator<T> {

        @Nonnull
        private final Iterator<T> delegate;

        ForwardingRemoteIterator(@Nonnull final Iterator<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Iterator<T> delegate() {
            return delegate;
        }
    }

    /**
     * A seekable null input stream.
     */
    private static class SeekableNullInputStream extends NullInputStream implements PositionedReadable, Seekable {

        SeekableNullInputStream() {
            super(0);
        }

        @Override
        public long getPos() {
            return 0;
        }

        @Override
        public int read(long position, byte[] buffer, int offset, int length) {
            return 0;
        }

        @Override
        public void readFully(long position, byte[] buffer, int offset, int length) {
            // ignored
        }

        @Override
        public void readFully(long position, byte[] buffer) {
            // ignored
        }

        @Override
        public void seek(long pos) throws IOException {
            throw new IOException();
        }

        @Override
        public boolean seekToNewSource(long targetPos) {
            return false;
        }
    }
}
