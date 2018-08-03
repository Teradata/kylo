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

import com.thinkbiganalytics.kylo.config.SparkLivyConfig;
import com.thinkbiganalytics.kylo.spark.livy.TestLivyClient;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestProcessRunner.Config.class,
        loader = AnnotationConfigContextLoader.class)
public class TestProcessRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);

    @Resource
    private ProcessRunner processRunner;

    @Test
    public void testExecute() {
        logger.debug("CWD = {} ", FileUtils.getCwd());

        String script = FileUtils.getFilePathFromResource(getClass(), "/setup/echoTest.sh");
        logger.info("Using script found on classpath: '{}'", script);

        List<String> args =Arrays.asList("xyz", "/setup/echoTest.sh");
        boolean succeeded = processRunner.runScript(script, args);

        assertThat(succeeded).isTrue();
        assertThat(processRunner.getOutputFromLastCommand()).startsWith( String.join(" ",args));
        assertThat(processRunner.getErrorFromLastCommand()).startsWith( "This line goes to standard error");
    }

    @Configuration
    static class Config {
        @Bean
        public ProcessRunner processRunner() {
            return new ProcessRunner();
        }
    }
}
