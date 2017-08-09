package com.thinkbiganalytics.kerberos;

/*-
 * #%L
 * thinkbig-service-app
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
public class KerberosConfiguration {

    @Autowired
    private Environment env;

    @Bean(name = "kerberosHiveConfiguration")
    public KerberosTicketConfiguration kerberosTicketHiveConfiguration() {
        String kerberosEnabled = env.getProperty("kerberos.hive.kerberosEnabled");
        String hadoopConfigurationResources = env.getProperty("kerberos.hive.hadoopConfigurationResources");
        String kerberosPrincipal = env.getProperty("kerberos.hive.kerberosPrincipal");
        String keytabLocation = env.getProperty("kerberos.hive.keytabLocation");

        KerberosTicketConfiguration config = new KerberosTicketConfiguration();
        config.setKerberosEnabled("true".equalsIgnoreCase(kerberosEnabled) ? true : false);
        config.setHadoopConfigurationResources(hadoopConfigurationResources);
        config.setKerberosPrincipal(kerberosPrincipal);
        config.setKeytabLocation(keytabLocation);

        if (config.isKerberosEnabled() && (StringUtils.isBlank(config.getHadoopConfigurationResources())
                                           || StringUtils.isBlank(config.getKeytabLocation()) || StringUtils.isBlank(config.getKerberosPrincipal()))) {
            throw new RuntimeException("Kerberos is enabled. hadoopConfigurationResources, kerberosPrincipal, and keytabLocation are required");
        }
        return config;
    }

}
