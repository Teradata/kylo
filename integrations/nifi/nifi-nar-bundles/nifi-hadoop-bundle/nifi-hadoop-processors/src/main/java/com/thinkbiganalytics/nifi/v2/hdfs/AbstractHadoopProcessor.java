/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thinkbiganalytics.nifi.v2.hdfs;


import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SecurityUtil;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.exception.ProcessException;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.SocketFactory;

/**
 * This is a base class that is helpful when building processors interacting with HDFS.
 */
public abstract class AbstractHadoopProcessor extends AbstractNiFiProcessor {

    // properties
    public static final PropertyDescriptor HADOOP_CONFIGURATION_RESOURCES = new PropertyDescriptor.Builder().name("Hadoop Configuration Resources")
        .description("A file or comma separated list of files which contains the Hadoop file system configuration. Without this, Hadoop "
                     + "will search the classpath for a 'core-site.xml' and 'hdfs-site.xml' file or will revert to a default configuration.")
        .required(false).addValidator(createMultipleFilesExistValidator()).build();

    public static final String DIRECTORY_PROP_NAME = "Directory";

    private static final Object RESOURCES_LOCK = new Object();
    // variables shared by all threads of this processor
    // Hadoop Configuration, Filesystem, and UserGroupInformation (optional)
    private final AtomicReference<HdfsResources> hdfsResources = new AtomicReference<>();
    /**
     * Property for Kerberos service keytab file
     */
    protected PropertyDescriptor kerberosKeytab;
    /**
     * Property for Kerberos service principal
     */
    protected PropertyDescriptor kerberosPrincipal;
    private long kerberosReloginThreshold;
    private long lastKerberosReloginTime;
    /**
     * List of properties
     */
    private List<PropertyDescriptor> properties;

    private static Configuration getConfigurationFromResources(String configResources) throws IOException {
        boolean foundResources = false;
        final Configuration config = new Configuration();
        if (null != configResources) {
            String[] resources = configResources.split(",");
            for (String resource : resources) {
                config.addResource(new Path(resource.trim()));
                foundResources = true;
            }
        }

        if (!foundResources) {
            // check that at least 1 non-default resource is available on the classpath
            String configStr = config.toString();
            for (String resource : configStr.substring(configStr.indexOf(":") + 1).split(",")) {
                if (!resource.contains("default") && config.getResource(resource.trim()) != null) {
                    foundResources = true;
                    break;
                }
            }
        }

        if (!foundResources) {
            throw new IOException("Could not find any of the " + HADOOP_CONFIGURATION_RESOURCES.getName() + " on the classpath");
        }
        return config;
    }

    /**
     * Validates that one or more files exist, as specified in a single property.
     *
     * @return a validator instance that validates the files given
     */
    public static Validator createMultipleFilesExistValidator() {
        return new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                final String[] files = input.split(",");
                for (String filename : files) {
                    try {
                        final File file = new File(filename.trim());
                        final boolean valid = file.exists() && file.isFile();
                        if (!valid) {
                            final String message = "File " + file + " does not exist or is not a file";
                            return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                        }
                    } catch (SecurityException e) {
                        final String message = "Unable to access " + filename + " due to " + e.getMessage();
                        return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                    }
                }
                return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
            }

        };
    }

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);
        hdfsResources.set(new HdfsResources(null, null, null));

        // Create Kerberos properties
        final SpringSecurityContextLoader securityContextLoader = SpringSecurityContextLoader.create(context);
        final KerberosProperties kerberosProperties = securityContextLoader.getKerberosProperties();
        kerberosKeytab = kerberosProperties.createKerberosKeytabProperty();
        kerberosPrincipal = kerberosProperties.createKerberosPrincipalProperty();

        // Create list of properties
        final List<PropertyDescriptor> props = new ArrayList<>();
        props.add(HADOOP_CONFIGURATION_RESOURCES);
        props.add(kerberosPrincipal);
        props.add(kerberosKeytab);
        props.add(KerberosProperties.KERBEROS_RELOGIN_PERIOD);
        properties = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    /**
     * If your subclass also has an @OnScheduled annotated method and you need hdfsResources in that method,
     * then be sure to call super.abstractOnScheduled(context)
     *
     * @param context the context of the processor
     * @throws IOException if configuration cannot be set for the HDFS resource
     */
    @OnScheduled
    public final void abstractOnScheduled(ProcessContext context) throws IOException {
        try {
            // This value will be null when called from ListHDFS, because it overrides all of the default
            // properties this processor sets. TODO: re-work ListHDFS to utilize Kerberos
            if (context.getProperty(KerberosProperties.KERBEROS_RELOGIN_PERIOD).getValue() != null) {
                kerberosReloginThreshold = context.getProperty(KerberosProperties.KERBEROS_RELOGIN_PERIOD).asTimePeriod(TimeUnit.SECONDS);
            }
            HdfsResources resources = hdfsResources.get();
            if (resources.getConfiguration() == null) {
                String configResources = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();
                String dir = context.getProperty(DIRECTORY_PROP_NAME).getValue();
                dir = dir == null ? "/" : dir;
                resources = resetHDFSResources(configResources, dir, context);
                hdfsResources.set(resources);
            }
        } catch (IOException ex) {
            getLog().error("HDFS Configuration error - {}", new Object[]{ex});
            hdfsResources.set(new HdfsResources(null, null, null));
            throw ex;
        }
    }

    @OnStopped
    public final void abstractOnStopped() {
        hdfsResources.set(new HdfsResources(null, null, null));
    }

    /**
     * Reset Hadoop Configuration and FileSystem based on the supplied configuration resources.
     *
     * @param configResources for configuration
     * @param dir             the target directory
     * @param context         for context, which gives access to the principal
     * @return An HdfsResources object
     * @throws IOException if unable to access HDFS
     */
    HdfsResources resetHDFSResources(String configResources, String dir, ProcessContext context) throws IOException {
        // org.apache.hadoop.conf.Configuration saves its current thread context class loader to use for threads that it creates
        // later to do I/O. We need this class loader to be the NarClassLoader instead of the magical
        // NarThreadContextClassLoader.
        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {
            Configuration config = getConfigurationFromResources(configResources);

            // first check for timeout on HDFS connection, because FileSystem has a hard coded 15 minute timeout
            checkHdfsUriForTimeout(config);

            // disable caching of Configuration and FileSystem objects, else we cannot reconfigure the processor without a complete
            // restart
            String disableCacheName = String.format("fs.%s.impl.disable.cache", FileSystem.getDefaultUri(config).getScheme());

            // If kerberos is enabled, create the file system as the kerberos principal
            // -- use RESOURCE_LOCK to guarantee UserGroupInformation is accessed by only a single thread at at time
            FileSystem fs = null;
            UserGroupInformation ugi = null;
            synchronized (RESOURCES_LOCK) {
                if (config.get("hadoop.security.authentication").equalsIgnoreCase("kerberos")) {
                    String principal = context.getProperty(kerberosPrincipal).getValue();
                    String keyTab = context.getProperty(kerberosKeytab).getValue();
                    UserGroupInformation.setConfiguration(config);
                    ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keyTab);
                    fs = getFileSystemAsUser(config, ugi);
                    lastKerberosReloginTime = System.currentTimeMillis() / 1000;
                } else {
                    config.set("ipc.client.fallback-to-simple-auth-allowed", "true");
                    config.set("hadoop.security.authentication", "simple");
                    fs = getFileSystem(config);
                }
            }
            config.set(disableCacheName, "true");
            getLog().info("Initialized a new HDFS File System with working dir: {} default block size: {} default replication: {} config: {}",
                          new Object[]{fs.getWorkingDirectory(), fs.getDefaultBlockSize(new Path(dir)), fs.getDefaultReplication(new Path(dir)), config.toString()});
            return new HdfsResources(config, fs, ugi);

        } finally {
            Thread.currentThread().setContextClassLoader(savedClassLoader);
        }
    }

    /**
     * This exists in order to allow unit tests to override it so that they don't take several minutes waiting for UDP packets to be received
     *
     * @param config the configuration to use
     * @return the FileSystem that is created for the given Configuration
     * @throws IOException if unable to create the FileSystem
     */
    protected FileSystem getFileSystem(final Configuration config) throws IOException {
        return FileSystem.get(config);
    }

    protected FileSystem getFileSystemAsUser(final Configuration config, UserGroupInformation ugi) throws IOException {
        try {
            return ugi.doAs(new PrivilegedExceptionAction<FileSystem>() {
                @Override
                public FileSystem run() throws Exception {
                    return FileSystem.get(config);
                }
            });
        } catch (InterruptedException e) {
            throw new IOException("Unable to create file system: " + e.getMessage());
        }
    }

    /*
     * Drastically reduce the timeout of a socket connection from the default in FileSystem.get()
     */
    protected void checkHdfsUriForTimeout(Configuration config) throws IOException {
        URI hdfsUri = FileSystem.getDefaultUri(config);
        String address = hdfsUri.getAuthority();
        int port = hdfsUri.getPort();
        if (address == null || address.isEmpty() || port < 0) {
            return;
        }
        InetSocketAddress namenode = NetUtils.createSocketAddr(address, port);
        SocketFactory socketFactory = NetUtils.getDefaultSocketFactory(config);
        Socket socket = null;
        try {
            socket = socketFactory.createSocket();
            NetUtils.connect(socket, namenode, 1000); // 1 second timeout
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }

    protected Configuration getConfiguration() {
        return hdfsResources.get().getConfiguration();
    }

    protected FileSystem getFileSystem() {
        // if kerberos is enabled, check if the ticket should be renewed before returning the FS
        if (hdfsResources.get().getUserGroupInformation() != null && isTicketOld()) {
            tryKerberosRelogin(hdfsResources.get().getUserGroupInformation());
        }
        return hdfsResources.get().getFileSystem();
    }

    /**
     * Gets the Hadoop file system for the specified context.
     *
     * @param context the process context
     * @return the Hadoop file system, or {@code null} if an error occurred
     */
    @Nullable
    protected FileSystem getFileSystem(@Nonnull final ProcessContext context) {
        // Get Hadoop configuration
        final Configuration configuration = getConfiguration();
        if (configuration == null) {
            getLog().error("Missing Hadoop configuration.");
            return null;
        }

        // Validate user if security is enabled
        if (SecurityUtil.isSecurityEnabled(configuration)) {
            // Get properties
            String hadoopConfigurationResources = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();
            String keyTab = context.getProperty(kerberosKeytab).getValue();
            String principal = context.getProperty(kerberosPrincipal).getValue();

            if (keyTab.isEmpty() || principal.isEmpty()) {
                getLog().error("Kerberos keytab or principal information missing in Kerberos enabled cluster.");
                return null;
            }

            // Authenticate
            try {
                getLog().debug("User authentication initiated.");
                if (new ApplySecurityPolicy().validateUserWithKerberos(getLog(), hadoopConfigurationResources, principal, keyTab)) {
                    getLog().debug("User authenticated successfully.");
                } else {
                    getLog().error("User authentication failed.");
                    return null;
                }
            } catch (Exception e) {
                getLog().error("Failed to authenticate:" + e, e);
                return null;
            }
        }

        // Get file system
        final FileSystem fileSystem = getFileSystem();
        if (fileSystem != null) {
            return fileSystem;
        } else {
            getLog().error("Hadoop FileSystem not properly configured.");
            return null;
        }
    }

    protected void tryKerberosRelogin(UserGroupInformation ugi) {
        try {
            getLog().info("Kerberos ticket age exceeds threshold [{} seconds] " +
                          "attempting to renew ticket for user {}", new Object[]{
                kerberosReloginThreshold, ugi.getUserName()});
            ugi.checkTGTAndReloginFromKeytab();
            lastKerberosReloginTime = System.currentTimeMillis() / 1000;
            getLog().info("Kerberos relogin successful or ticket still valid");
        } catch (IOException e) {
            // Most likely case of this happening is ticket is expired and error getting a new one,
            // meaning dfs operations would fail
            getLog().error("Kerberos relogin failed", e);
            throw new ProcessException("Unable to renew kerberos ticket", e);
        }
    }

    protected boolean isTicketOld() {
        return (System.currentTimeMillis() / 1000 - lastKerberosReloginTime) > kerberosReloginThreshold;
    }


    static protected class HdfsResources {

        private final Configuration configuration;
        private final FileSystem fileSystem;
        private final UserGroupInformation userGroupInformation;

        public HdfsResources(Configuration configuration, FileSystem fileSystem, UserGroupInformation userGroupInformation) {
            this.configuration = configuration;
            this.fileSystem = fileSystem;
            this.userGroupInformation = userGroupInformation;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public FileSystem getFileSystem() {
            return fileSystem;
        }

        public UserGroupInformation getUserGroupInformation() {
            return userGroupInformation;
        }
    }
}
