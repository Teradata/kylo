package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-core
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
import com.thinkbiganalytics.kylo.exceptions.LivyException;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class SparkLivyProcess implements SparkShellProcess {
    private static final Logger logger = LoggerFactory.getLogger(SparkLivyProcess.class);

    private UUID id;

    private String hostname = "localhost";

    private int port = 8998;

    SparkLivyProcess(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
        this.id = UUID.randomUUID();
    }

    @Nonnull
    @Override
    public String getClientId() {
        return id.toString();
    }

    @Nonnull
    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    // package private for testing
    @VisibleForTesting
    void setHostname(String hostname) {
        this.hostname = hostname;
    }

    // package private for testing
    @VisibleForTesting
    void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean isLocal() {
        return true;
    }


    public static SparkLivyProcess newInstance(String hostname, Integer port) {
        if( ! StringUtils.isNotEmpty(hostname) ) {
            throw new LivyException("Attempt to contact Livy server when Livy hostname not configured");
        }
        if( port == null || port <= 0 ) {
            throw new LivyException("Attempt to contact Livy server when Livy port not configured, or invalid");
        }
        return new SparkLivyProcess(hostname,port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SparkLivyProcess that = (SparkLivyProcess) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SparkLivyProcess{");
        sb.append("id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
