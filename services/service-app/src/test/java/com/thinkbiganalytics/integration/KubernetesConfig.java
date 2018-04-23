package com.thinkbiganalytics.integration;

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