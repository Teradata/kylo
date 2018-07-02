package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@SuppressWarnings("SpringFacetCodeInspection") //Spring context is created dynamically
@Configuration
@EnableConfigurationProperties
public class HiveConnectionInspectionConfiguration {

    private static final String KERBEROS_HIVE_HADOOP_CONFIGURATION_RESOURCES = "kerberos.hive.hadoopConfigurationResources";
    private static final String KERBEROS_HIVE_KEYTAB_LOCATION = "kerberos.hive.keytabLocation";
    private static final String KERBEROS_HIVE_KERBEROS_PRINCIPAL = "kerberos.hive.kerberosPrincipal";

    @Autowired
    private Environment env;

    @Bean(name = "kerberosHiveConfiguration")
    public KerberosTicketConfiguration kerberosTicketHiveConfiguration() {
        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equalsIgnoreCase(env.getProperty("kerberos.hive.kerberosEnabled")));
        config.setHadoopConfigurationResources(env.getProperty(KERBEROS_HIVE_HADOOP_CONFIGURATION_RESOURCES));
        config.setKerberosPrincipal(env.getProperty(KERBEROS_HIVE_KERBEROS_PRINCIPAL));
        config.setKeytabLocation(env.getProperty(KERBEROS_HIVE_KEYTAB_LOCATION));

        if (config.isKerberosEnabled()) {
            if (StringUtils.isBlank(config.getHadoopConfigurationResources())) {
                throw new IllegalStateException(String.format("Kerberos is enabled, but Hadoop configuration resources path is not provided. Check %s property", KERBEROS_HIVE_HADOOP_CONFIGURATION_RESOURCES));
            }
            if (StringUtils.isBlank(config.getKeytabLocation())) {
                throw new IllegalStateException(String.format("Kerberos is enabled, but keytab path is not provided. Check %s property", KERBEROS_HIVE_KEYTAB_LOCATION));
            }
            if (StringUtils.isBlank(config.getKerberosPrincipal())) {
                throw new IllegalStateException(String.format("Kerberos is enabled, but Kerberos Principal is not provided. Check %s property", KERBEROS_HIVE_KERBEROS_PRINCIPAL));
            }
        }
        return config;
    }

}
