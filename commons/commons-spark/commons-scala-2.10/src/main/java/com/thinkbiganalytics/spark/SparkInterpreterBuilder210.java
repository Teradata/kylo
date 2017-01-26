package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-commons-scala-2.10
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
 * Builds a Spark interpreter using Scala 2.10.
 */
@Component
public class SparkInterpreterBuilder210 implements SparkInterpreterBuilder {

    private Settings settings;
    private PrintWriter printWriter;
    private ClassLoader classLoader;

    @Override
    public SparkInterpreterBuilder withSettings(Settings param) {
        this.settings = param;
        return this;
    }

    @Override
    public SparkInterpreterBuilder withPrintWriter(PrintWriter param) {
        this.printWriter = param;
        return this;
    }

    @Override
    public SparkInterpreterBuilder withClassLoader(ClassLoader param) {
        this.classLoader = param;
        return this;
    }

    @Override
    public IMain newInstance() {
        return new IMain(settings, printWriter) {
            @Override
            public ClassLoader parentClassLoader() {
                return classLoader;
            }

            @Override
            public PrintWriter out() {
                return printWriter;
            }
        };
    }
}
