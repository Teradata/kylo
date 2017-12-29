package com.thinkbiganalytics.spark.shell;

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

import org.apache.commons.io.serialization.ValidatingObjectInputStream;
import org.apache.spark.launcher.SparkAppHandle;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

public class SparkLauncherSparkShellProcessTest {

    /**
     * Mock client identifier for testing
     */
    private static final String CLIENT_ID = "MY_CLIENT_ID";

    /**
     * Mock client secret for testing
     */
    private static final String CLIENT_SECRET = "MY_CLIENT_SECRET";

    /**
     * Mock hostname for testing
     */
    private static final String HOSTNAME = "myhost.example.com";

    /**
     * Mock port for testing
     */
    private static final int PORT = 80;

    /**
     * Test a cluster process.
     */
    @Test
    public void testCluster() throws Exception {
        // Mock process listener
        final AtomicInteger processReady = new AtomicInteger(0);
        final AtomicInteger processStarted = new AtomicInteger(0);
        final AtomicInteger processStopped = new AtomicInteger(0);

        final SparkShellProcessListener listener = new SparkShellProcessListener() {
            @Override
            public void processReady(@Nonnull SparkShellProcess process) {
                processReady.incrementAndGet();
            }

            @Override
            public void processStarted(@Nonnull SparkShellProcess process) {
                processStarted.incrementAndGet();
            }

            @Override
            public void processStopped(@Nonnull SparkShellProcess process) {
                processStopped.incrementAndGet();
            }
        };

        // Create deserialized process
        final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutStream = new ObjectOutputStream(byteOutStream);
        objectOutStream.writeObject(new SparkLauncherSparkShellProcess(Mockito.mock(SparkAppHandle.class), CLIENT_ID, CLIENT_SECRET, 0, TimeUnit.MILLISECONDS));

        final ByteArrayInputStream byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
        final ValidatingObjectInputStream objectInStream = new ValidatingObjectInputStream(byteInStream);
        objectInStream.accept(SparkLauncherSparkShellProcess.class);
        final SparkLauncherSparkShellProcess process = (SparkLauncherSparkShellProcess) objectInStream.readObject();

        // Test default process state
        process.addListener(listener);
        Assert.assertFalse(process.waitForReady());

        // Test registration
        process.setHostname(HOSTNAME);
        process.setPort(PORT);
        process.setReady(true);
        Assert.assertEquals(1, processReady.get());

        // Test retrieving hostname and port
        Assert.assertTrue(process.waitForReady());
        Assert.assertEquals(HOSTNAME, process.getHostname());
        Assert.assertEquals(PORT, process.getPort());
    }

    /**
     * Test a local process.
     */
    @Test
    public void testLocal() {
        // Mock Spark app handle
        final SparkAppHandle handle = Mockito.mock(SparkAppHandle.class);
        Mockito.when(handle.getState()).thenReturn(SparkAppHandle.State.FINISHED);

        // Mock process listener
        final AtomicInteger processReady = new AtomicInteger(0);
        final AtomicInteger processStarted = new AtomicInteger(0);
        final AtomicInteger processStopped = new AtomicInteger(0);

        final SparkShellProcessListener listener = new SparkShellProcessListener() {
            @Override
            public void processReady(@Nonnull SparkShellProcess process) {
                processReady.incrementAndGet();
            }

            @Override
            public void processStarted(@Nonnull SparkShellProcess process) {
                processStarted.incrementAndGet();
            }

            @Override
            public void processStopped(@Nonnull SparkShellProcess process) {
                processStopped.incrementAndGet();
            }
        };

        // Test default process state
        final SparkLauncherSparkShellProcess process = new SparkLauncherSparkShellProcess(handle, CLIENT_ID, CLIENT_SECRET, 0, TimeUnit.MILLISECONDS);
        process.addListener(listener);
        Assert.assertFalse(process.waitForReady());

        // Test registration
        process.setHostname(HOSTNAME);
        process.setPort(PORT);
        process.setReady(true);
        Assert.assertEquals(1, processReady.get());

        // Test retrieving hostname and port
        Assert.assertTrue(process.waitForReady());
        Assert.assertEquals(HOSTNAME, process.getHostname());
        Assert.assertEquals(PORT, process.getPort());

        // Test client stopped
        process.stateChanged(handle);
        Assert.assertEquals(1, processStopped.get());
    }
}
