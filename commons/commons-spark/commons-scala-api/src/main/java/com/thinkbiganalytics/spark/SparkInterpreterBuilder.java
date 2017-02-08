package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-commons-scala-api
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

import org.springframework.stereotype.Component;

import java.io.PrintWriter;

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.IMain;

/**
 * Builds a Spark interpreter.
 */
@Component
public interface SparkInterpreterBuilder {

    /**
     * Sets the settings for the Spark interpreter.
     *
     * @param param the settings
     * @return this builder
     */
    SparkInterpreterBuilder withSettings(Settings param);

    /**
     * Sets the print writer for the Spark interpreter.
     *
     * @param param the print writer
     * @return this builder
     */
    SparkInterpreterBuilder withPrintWriter(PrintWriter param);

    /**
     * Sets the class loader for the Spark interpreter.
     *
     * @param param the class loader
     * @return this builder
     */
    SparkInterpreterBuilder withClassLoader(ClassLoader param);

    /**
     * Builds a new Spark interpreter.
     *
     * @return a Spark interpreter
     */
    IMain newInstance();
}
