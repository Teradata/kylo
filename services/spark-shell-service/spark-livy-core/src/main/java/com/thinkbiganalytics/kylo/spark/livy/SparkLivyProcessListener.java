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

import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellProcessListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class SparkLivyProcessListener implements SparkShellProcessListener {
    private static final Logger logger = LoggerFactory.getLogger(SparkLivyProcessManager.class);

    @Override
    public void processReady(@Nonnull SparkShellProcess process) {
        logger.debug("processReady() called");
        throw new UnsupportedOperationException();
    }

    @Override
    public void processStarted(@Nonnull SparkShellProcess process) {
        logger.debug("processStarted() called");
        throw new UnsupportedOperationException();
    }

    @Override
    public void processStopped(@Nonnull SparkShellProcess process) {
        logger.debug("processStopped() called");
        throw new UnsupportedOperationException();
    }
}
