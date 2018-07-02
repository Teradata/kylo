package com.thinkbiganalytics.integration;

/*-
 * #%L
 * kylo-commons-test
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
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class KubernetesConfig {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesConfig.class);

    private String kubernetesNamespace;
    private String hadoopPodName;

    @PostConstruct
    public void initIt() throws Exception {
        LOG.info(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                     .append("kubernetesNamespace", kubernetesNamespace)
                     .append("hadoopPodName", hadoopPodName)
                     .toString());
    }

    public String getKubernetesNamespace() {
        return kubernetesNamespace;
    }

    public void setKubernetesNamespace(String kubernetesNamespace) {
        this.kubernetesNamespace = kubernetesNamespace;
    }

    public String getHadoopPodName() {
        return hadoopPodName;
    }

    public void setHadoopPodName(String hadoopPodName) {
        this.hadoopPodName = hadoopPodName;
    }
}