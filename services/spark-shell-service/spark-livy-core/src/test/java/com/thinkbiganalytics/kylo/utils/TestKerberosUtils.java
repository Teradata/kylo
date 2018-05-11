package com.thinkbiganalytics.kylo.utils;

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

import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestKerberosUtils.Config.class},
        loader = AnnotationConfigContextLoader.class)
@TestPropertySource("classpath:kerberos-client.properties")
public class TestKerberosUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestKerberosUtils.class);

    private static final File DEFAULT_KINIT_PATH = new File("/usr/bin/kinit");

    @Resource
    private KerberosUtils kerberosUtils;

    @Resource
    private KerberosSparkProperties kerberosSparkProperties;

    @Before
    public void beforeMethod() {
        // only run tests if this system contains kinit in the expected location
        org.junit.Assume.assumeTrue(DEFAULT_KINIT_PATH.exists());
    }

    @Test
    public void testKinitPathFind() {
        assertThat(kerberosSparkProperties.isKerberosEnabled()).as("this test requires kerberos properties set kerberos enabled").isTrue();

        File foundScript = kerberosUtils.getKinitPath(DEFAULT_KINIT_PATH);
        assertThat(foundScript).isEqualTo(DEFAULT_KINIT_PATH);
        logger.info("'/usr/bin/kinit' found by getKinitPath");

        foundScript = kerberosUtils.getKinitPath(null);
        assertThat(foundScript).isEqualTo(DEFAULT_KINIT_PATH);
        logger.info("kinit was found using which");

        // our config file will have the kerberos.spark.kinitPath property missing, ensure /usr/bin/kinit comes back from scanning system path
        assertThat(kerberosSparkProperties.getKinitPath()).isEqualTo(DEFAULT_KINIT_PATH);
    }


    @Configuration
    @EnableAutoConfiguration
    static class Config {
        @Bean
        @ConfigurationProperties("kerberos.spark")
        public KerberosSparkProperties kerberosSparkProperties() {
            KerberosSparkProperties ksp = new KerberosSparkProperties();
             /*if( ksp.isKerberosEnabled() ) {
                 // if configuration doesn't specify a path for kinit, let's try to set a default.
                 Path actualKinitPath = kerberosUtils().getKinitPath(ksp.getKinitPath().toPath());
                 ksp.setKinitPath(actualKinitPath.toFile());
             }*/
            return ksp;
        }

        @Bean
        public KerberosUtils kerberosUtils() {
            return new KerberosUtils();
        }

        @Bean
        public ProcessRunner processRunner() {
            return new ProcessRunner();
        }

        @PostConstruct
        public void postConstruct() {
            KerberosSparkProperties ksp = kerberosSparkProperties();
            File actualKinitPath = kerberosUtils().getKinitPath(ksp.getKinitPath());
            ksp.setKinitPath(actualKinitPath);
            logger.debug("{}", ksp);
        }
    }
}
