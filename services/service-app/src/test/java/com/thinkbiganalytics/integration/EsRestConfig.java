package com.thinkbiganalytics.integration;

/*-
 * #%L
 * kylo-service-app
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * Configuration required to connect to running Elasticsearch instance
 */
public class EsRestConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EsRestConfig.class);

    private String esHost;
    private int esRestPort;

    @PostConstruct
    public void init() {
        LOG.info(new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                     .append("es host", esHost)
                     .append("es rest port", esRestPort)
                     .toString());
    }

    public String getEsHost() {
        return esHost;
    }

    public void setEsHost(String esHost) {
        this.esHost = esHost;
    }

    public int getEsRestPort() {
        return esRestPort;
    }

    public void setEsRestPort(int esRestPort) {
        this.esRestPort = esRestPort;
    }
}
