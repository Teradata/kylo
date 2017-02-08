package com.thinkbiganalytics.nifi.security;

/*-
 * #%L
 * thinkbig-nifi-security-api
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

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.nifi.logging.ComponentLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.security.PrivilegedExceptionAction;

import javax.net.SocketFactory;

/**
 * Apply Kerberos policies
 */
public class ApplySecurityPolicy {

    private static final Object RESOURCES_LOCK = new Object();
    private long lastKerberosReloginTime;

    public static Configuration getConfigurationFromResources(String configResources) throws IOException {
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
            throw new IOException("Could not find any of the " + "hadoop conf" + " on the classpath");
        }
        return config;
    }

    public boolean validateUserWithKerberos(ComponentLog loggerInstance, String HadoopConfigurationResources, String Principal, String KeyTab) throws Exception {

        ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {

            loggerInstance.info("Start of hadoop configuration read");
            Configuration config = getConfigurationFromResources(HadoopConfigurationResources);
            config.set("hadoop.security.authentication", "Kerberos");

            loggerInstance.info("End of hadoop configuration read");

            // first check for timeout on HDFS connection, because FileSystem has a hard coded 15 minute timeout
            loggerInstance.info("Start of HDFS timeout check");
            checkHdfsUriForTimeout(config);
            loggerInstance.info("End of HDFS timeout check");

            // disable caching of Configuration and FileSystem objects, else we cannot reconfigure the processor without a complete
            // restart
            String disableCacheName = String.format("fs.%s.impl.disable.cache", FileSystem.getDefaultUri(config).getScheme());

            // If kerberos is enabled, create the file system as the kerberos principal
            // -- use RESOURCE_LOCK to guarantee UserGroupInformation is accessed by only a single thread at at time

            FileSystem fs;
            UserGroupInformation ugi;

            synchronized (RESOURCES_LOCK) {

                if (SecurityUtil.isSecurityEnabled(config)) {
                    loggerInstance.info("Start of Kerberos Security Check");
                    UserGroupInformation.setConfiguration(config);
                    UserGroupInformation.loginUserFromKeytab(Principal, KeyTab);
                    loggerInstance.info("End of Kerberos Security Check");
                } else {
                    config.set("ipc.client.fallback-to-simple-auth-allowed", "true");
                    config.set("hadoop.security.authentication", "simple");
                    ugi = SecurityUtil.loginSimple(config);
                    fs = getFileSystemAsUser(config, ugi);
                }
            }
            config.set(disableCacheName, "true");
            return true;
        } catch (Exception e) {
            loggerInstance.error("Unable to validate user : " + e.getMessage());
            return false;

        } finally {
            Thread.currentThread().setContextClassLoader(savedClassLoader);
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

