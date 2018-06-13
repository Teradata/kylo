package com.thinkbiganalytics.spark.multiexec;

/*-
 * #%L
 * kylo-spark-multi-exec-app
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.spark.SparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

public class MultiSparkExecApp {

    private static final Logger log = LoggerFactory.getLogger(MultiSparkExecApp.class);

    public static void main(String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Running Spark MultiSparkExecApp with the following command line args (comma separated):{}", StringUtils.join(args, ","));
        }
        new MultiSparkExecApp().run(System.out, args);
    }

    private void run(@Nonnull final PrintStream out, @Nonnull final String... args) {
        log.info("MultiSparkExecApp running...");
        final SparkContext sparkContext = SparkContext.getOrCreate();
        
        try {
            final MultiSparkExecArguments sparkExecArgs = new MultiSparkExecArguments(args);
            final List<SparkApplicationCommand> commands = sparkExecArgs.getCommands();
            final List<Class<?>> appClasses = new ArrayList<>(sparkExecArgs.getCommands().size());
            
            // Get the list of all app classes; verifying each have main() methods.
            for (SparkApplicationCommand cmd : sparkExecArgs.getCommands()) {
                appClasses.add(getApplicationClasses(cmd));
            }
            log.debug("Preparing to execute apps: {}", appClasses);

            for (int idx = 0; idx < appClasses.size(); idx++) {
                Class<?> appClass = appClasses.get(idx);
                SparkApplicationCommand cmd = commands.get(idx);
                
                System.out.println(">>> Beginning: " + cmd.getName() + " *****************************************************");
                executeApp(appClass, cmd);
                System.out.println("<<< Completed: " + cmd.getName() + " *****************************************************");
                
                // TODO Generate provenance events.
            }
            
            log.info("MultiSparkExecApp finished");
        } catch (Exception e) {
            log.error("Execution failed", e);
            throw e;
        } finally {
            sparkContext.stop();
        }
    }

    private void executeApp(Class<?> appClass, SparkApplicationCommand cmd) {
        String[] args = cmd.asCommandLineArgs();
        
        try {
            log.debug("Executing app: {} wih arguments: {}", appClass, Arrays.toString(args));
            invokeMain(appClass, args);
        } catch (Exception e) {
            log.error("Exception executing app: {} {}", appClass, Arrays.toString(args), e);
            throw new SparkAppExecException("Exception executing app: " + appClass + " " + Arrays.toString(args), e);
        }
    }
    
    private void invokeMain(Class<?> appClass, String[] args) {
        try {
            Method main = MethodUtils.getAccessibleMethod(appClass, "main", String[].class);
            main.invoke(null, (Object) args);
        } catch (IllegalAccessException e) {
            // Shouldn't happen as an accessible main() is checked for when the app class is looked up.
            log.error("The specified Spark application's main() method is inaccessible: {}", appClass, e.getCause());
            throw new IllegalStateException("The specified Spark application's main() method is inaccessible: " + appClass);
        } catch (InvocationTargetException e) {
            log.error("Failed to execute the main() method of Spark application: {}", appClass, e.getCause());
            throw new SparkAppExecException("Failed to execute the main() method of Spark application: " + appClass, e.getCause());
        }
    }

    private Class<?> getApplicationClasses(SparkApplicationCommand cmd) {
        try {
            Class<?> cls = Class.forName(cmd.getClassName());
            if (MethodUtils.getAccessibleMethod(cls, "main", String[].class) != null) {
                return cls;
            } else {
                log.error("The specified Spark application class does not have a main() method: {}", cmd.getClassName());
                throw new SparkAppExecException("The specified Spark application class does not have a main() method: " + cmd.getClassName());
            }
        } catch (ClassNotFoundException e) {
            log.error("The specified Spark application class does not exist: {}", cmd.getClassName());
            throw new SparkAppExecException("The specified Spark application class does not exist: " + cmd.getClassName());
        }
    }
}
