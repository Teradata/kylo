package com.thinkbiganalytics.kylo.utils;

/*-
 * #%L
 * kylo-spark-livy-server
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


import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProcessRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestProcessRunner.class);

    private static String testScript = FileUtils.getFilePathFromResource(TestProcessRunner.class, "/setup/echoTest.sh");

    @Before
    public void beforeMethod() {
        // only run tests if the test script can be run  ;; maven-ant-plugin will have chmod the needed perms, but if not, just skip the test
        org.junit.Assume.assumeTrue(new File(testScript).canExecute());
    }

    @Test
    public void testExecute() {
        ProcessRunner processRunner = new ProcessRunner();

        logger.debug("CWD = {} ", FileUtils.getCwd());

        logger.info("Using script found on classpath: '{}'", testScript);

        List<String> args = Arrays.asList("xyz", "/setup/echoTest.sh");
        boolean succeeded = processRunner.runScript(testScript, args);

        assertThat(succeeded).isTrue();
        assertThat(processRunner.getOutputFromLastCommand()).startsWith(String.join(" ", args));
        assertThat(processRunner.getErrorFromLastCommand()).startsWith("This line goes to standard error");
    }

}
