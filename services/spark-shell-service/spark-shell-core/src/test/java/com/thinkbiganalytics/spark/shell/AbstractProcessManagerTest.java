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

import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;

import org.apache.spark.launcher.SparkAppHandle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbstractProcessManagerTest {

    /**
     * Client hostname for testing
     */
    private static final String CLIENT_HOST = "client.example.com";

    /**
     * Client port for testing
     */
    private static final int CLIENT_PORT = 8455;

    /**
     * Username for testing
     */
    private static final String USERNAME = "myuser";

    /**
     * Mock Spark app handle
     */
    private SparkAppHandle appHandle;

    /**
     * Spark Shell client properties for testing
     */
    private SparkShellProperties clientProperties;

    /**
     * Mock process listener
     */
    private MockProcessListener listener = new MockProcessListener();

    /**
     * Mock Spark Shell process
     */
    private SparkLauncherSparkShellProcess process;

    /**
     * Mock process manager
     */
    private MockProcessManager processManager;

    /**
     * Indicates that the process is ready
     */
    private boolean ready;

    /**
     * Mock user to password mapping
     */
    private final Properties users = new Properties();

    /**
     * Set up test environment.
     */
    @Before
    public void setUp() {
        // Mock app handle
        appHandle = Mockito.mock(SparkAppHandle.class);

        // Create client properties
        clientProperties = new SparkShellProperties();

        // Mock process
        process = Mockito.spy(new SparkLauncherSparkShellProcess(appHandle, "MY_CLIENT_ID", "MY_CLIENT_SECRET", 0, TimeUnit.SECONDS));
        Mockito.doAnswer(invocation -> {
            ready = true;
            invocation.callRealMethod();
            return null;
        }).when(process).setReady(Mockito.anyBoolean());
        Mockito.doAnswer(invocation -> {
            process.stateChanged(appHandle);
            return null;
        }).when(process).stop();
        Mockito.when(process.waitForReady()).then(invocation -> ready);
    }

    /**
     * Clean up test environment.
     */
    @After
    public void cleanUp() throws Exception {
        if (processManager != null) {
            processManager.shutdown();
        }
    }

    /**
     * Verify process events from a cluster.
     */
    @Test
    public void testCluster() throws InterruptedException {
        // Mock process
        Mockito.doNothing().when(process).stateChanged(Mockito.any());
        process.setUsername(USERNAME);

        // Create process manager
        processManager = new MockProcessManager(clientProperties, users);
        processManager.addListener(listener);
        processManager.getSystemProcess();  // wait for system process to start
        Assert.assertEquals(0, processManager.getProcesses().size());

        // Test adding a cluster process
        processManager.updateProcess(process);
        Assert.assertEquals(process, processManager.getProcessByClientId("MY_CLIENT_ID").orElseThrow(IllegalStateException::new));
        Assert.assertFalse(process.waitForReady());

        // Test updating a cluster process
        final SparkLauncherSparkShellProcess clusterProcess1 = Mockito.mock(SparkLauncherSparkShellProcess.class);
        Mockito.when(clusterProcess1.getClientId()).thenReturn("MY_CLIENT_ID");
        Mockito.when(clusterProcess1.getHostname()).thenReturn(CLIENT_HOST);
        Mockito.when(clusterProcess1.getPort()).thenReturn(CLIENT_PORT);
        Mockito.when(clusterProcess1.isReady()).thenReturn(true);
        processManager.updateProcess(clusterProcess1);

        Assert.assertTrue(process.waitForReady());
        Assert.assertEquals(CLIENT_HOST, process.getHostname());
        Assert.assertEquals(CLIENT_PORT, process.getPort());

        // Test removing a cluster process
        processManager.removeProcess("MY_CLIENT_ID");
        Assert.assertFalse(processManager.getProcessByClientId("MY_CLIENT_ID").isPresent());
    }

    /**
     * Verify a successful start of the system process.
     */
    @Test
    public void testSystemReady() {
        // Mock process
        final RegistrationRequest registration = new RegistrationRequest();
        registration.setHost(CLIENT_HOST);
        registration.setPort(CLIENT_PORT);

        Mockito.doAnswer(invocation -> {
            final Object result = invocation.callRealMethod();
            processManager.register(process.getClientId(), registration);
            return result;
        }).when(process).addListener(Mockito.any(SparkShellProcessListener.class));

        // Start system process
        processManager = new MockProcessManager(clientProperties, users);
        processManager.addListener(listener);

        // Try to get system process
        Assert.assertEquals(process, processManager.getSystemProcess());
        Assert.assertTrue("Process should be ready", process.waitForReady());
        Assert.assertEquals("Process hostname should be set", CLIENT_HOST, process.getHostname());
        Assert.assertEquals("Process port should be set", CLIENT_PORT, process.getPort());

        listener.assertCounts(1, 1, 0);
        Assert.assertTrue("User should have been added", users.containsKey(process.getClientId()));
    }

    /**
     * Verify timeout of starting the system process.
     */
    @Test
    public void testSystemTimeout() throws InterruptedException {
        // Mock app handle
        Mockito.when(appHandle.getState()).thenReturn(SparkAppHandle.State.FINISHED);

        // Start system process
        processManager = new MockProcessManager(clientProperties, users);
        processManager.addListener(listener);

        // Try to get system process
        Assert.assertEquals(process, processManager.getSystemProcess());
        Assert.assertFalse("Process should not be ready", process.waitForReady());

        listener.assertCounts(1, 0, 1);
        Assert.assertEquals("User should have been removed", 0, users.size());

        // Re-try to get system process
        ready = true;
        Assert.assertEquals(process, processManager.getSystemProcess());
        Assert.assertTrue("Process should be ready", process.waitForReady());

        listener.assertCounts(1, 0, 0);
        Assert.assertTrue("User should have been added", users.containsKey(process.getClientId()));
    }

    /**
     * Verify a successful start of a user process.
     */
    @Test
    public void testUserReady() throws Exception {
        // Create process manager
        processManager = new MockProcessManager(clientProperties, users) {
            @Nonnull
            @Override
            public synchronized SparkShellProcess getSystemProcess() {
                return Mockito.mock(SparkShellProcess.class);
            }

            @Override
            public void start(@Nonnull String username) {
                super.start(username);
                final RegistrationRequest registration = new RegistrationRequest();
                registration.setHost(CLIENT_HOST);
                registration.setPort(CLIENT_PORT);
                register(process.getClientId(), registration);
            }
        };
        processManager.addListener(listener);

        // Try to get user process
        Assert.assertEquals(process, processManager.getProcessForUser(USERNAME));
        Assert.assertTrue("Process should be ready", process.waitForReady());
        Assert.assertEquals("Process hostname should be set", CLIENT_HOST, process.getHostname());
        Assert.assertEquals("Process port should be set", CLIENT_PORT, process.getPort());

        listener.assertCounts(1, 1, 0);
        Assert.assertTrue("User should have been added", users.containsKey(process.getClientId()));
    }

    /**
     * Verify timeout of starting a user process.
     */
    @Test
    public void testUserTimeout() throws Exception {
        // Mock app handle
        Mockito.when(appHandle.getState()).thenReturn(SparkAppHandle.State.FINISHED);

        // Create process manager
        processManager = new MockProcessManager(clientProperties, users) {
            @Nonnull
            @Override
            public synchronized SparkShellProcess getSystemProcess() {
                return Mockito.mock(SparkShellProcess.class);
            }
        };
        processManager.addListener(listener);

        // Try to get user process
        try {
            processManager.getProcessForUser(USERNAME);
            Assert.fail("Process should not be ready");
        } catch (final IllegalStateException e) {
            Assert.assertFalse("Process should not be ready", process.waitForReady());
        }

        listener.assertCounts(1, 0, 1);
        Assert.assertEquals("User should have been removed", 0, users.size());

        // Re-try to get user process
        ready = true;
        Assert.assertEquals(process, processManager.getProcessForUser(USERNAME));

        listener.assertCounts(1, 0, 0);
        Assert.assertTrue("User should have been added", users.containsKey(process.getClientId()));
    }

    /**
     * A mock {@link SparkShellProcessListener} that counts the calls to each method.
     */
    private class MockProcessListener implements SparkShellProcessListener {

        /**
         * Number of calls to {@link #processReady(SparkShellProcess)}
         */
        private int readyCount;

        /**
         * Number of calls to {@link #processStarted(SparkShellProcess)}
         */
        private int startedCount;

        /**
         * Number of calls to {@link #processStopped(SparkShellProcess)}
         */
        private int stoppedCount;

        /**
         * Asserts that the counts for each method equal the actual number of method calls.
         *
         * @param startedCount the expected number of calls to {@link #processStarted(SparkShellProcess)}
         * @param readyCount   the expected number of calls to {@link #processReady(SparkShellProcess)}
         * @param stoppedCount the expected number of calls to {@link #processStopped(SparkShellProcess)}
         */
        void assertCounts(final int startedCount, final int readyCount, final int stoppedCount) {
            Assert.assertEquals("Listener started counts do not match", startedCount, this.startedCount);
            Assert.assertEquals("Listener ready counts do not match", readyCount, this.readyCount);
            Assert.assertEquals("Listener stopped counts do not match", stoppedCount, this.stoppedCount);
        }

        @Override
        public void processReady(@Nonnull final SparkShellProcess process) {
            ++readyCount;
        }

        @Override
        public void processStarted(@Nonnull final SparkShellProcess process) {
            ++startedCount;
        }

        @Override
        public void processStopped(@Nonnull final SparkShellProcess process) {
            ++stoppedCount;
        }

        /**
         * Resets the counts of method calls.
         */
        void reset() {
            readyCount = 0;
            startedCount = 0;
            stoppedCount = 0;
        }
    }

    /**
     * A mock {@link SparkShellProcessManager} that creates mock {@link SparkShellProcess} objects.
     */
    private class MockProcessManager extends DefaultProcessManager {

        /**
         * Constructs a {@code MockProcessManager} with the specified configuration.
         *
         * @param properties the client configuration
         * @param users      the username to password mapping
         */
        MockProcessManager(@Nonnull final SparkShellProperties properties, @Nonnull final Properties users) {
            super(properties, new KerberosSparkProperties(), users);
        }

        @Nonnull
        @Override
        protected SparkShellProcessBuilder createProcessBuilder(@Nullable final String username) {
            // Reset listener counts
            listener.reset();

            // Create a mock process builder
            final SparkShellProcessBuilder builder = Mockito.mock(SparkShellProcessBuilder.class);
            Mockito.when(builder.deployMode(Mockito.anyString())).thenReturn(builder);
            Mockito.when(builder.idleTimeout(Mockito.anyLong(), Mockito.any())).thenReturn(builder);
            Mockito.when(builder.master(Mockito.anyString())).thenReturn(builder);
            try {
                Mockito.when(builder.build()).thenAnswer(invocation -> process);
            } catch (IOException e) {
                // ignored
            }
            return builder;
        }
    }
}
