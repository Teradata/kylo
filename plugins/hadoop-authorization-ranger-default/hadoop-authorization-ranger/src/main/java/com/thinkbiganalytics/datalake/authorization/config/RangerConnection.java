package com.thinkbiganalytics.datalake.authorization.config;

/*-
 * #%L
 * thinkbig-hadoop-authorization-ranger
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

/**
 */
public class RangerConnection implements AuthorizationConfiguration {

    private String hostName;
    private int port;
    private String username;
    private String password;
    private String hdfsRepositoryName;
    private String hiveRepositoryName;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHdfsRepositoryName() {
        return hdfsRepositoryName;
    }

    public void setHdfsRepositoryName(String hdfsRepositoryName) {
        this.hdfsRepositoryName = hdfsRepositoryName;
    }

    public String getHiveRepositoryName() {
        return hiveRepositoryName;
    }

    public void setHiveRepositoryName(String hiveRepositoryName) {
        this.hiveRepositoryName = hiveRepositoryName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
