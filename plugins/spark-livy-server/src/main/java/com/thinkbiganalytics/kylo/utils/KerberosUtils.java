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


import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class KerberosUtils {

    @Resource
    private ProcessRunner processRunner;

    /**
     * Returns the resolved path of kinit using a search heuristic
     *
     * @param kinit the absolute path we should search first, if not found here it will begin searching for kinit
     * @return the resolved and validated path or 'null' if none can be found.  If the provided path is valid, returned path will match
     */
    public File getKinitPath(File kinit) {
        if (kinit != null) {
            if (FileUtils.canExecute(kinit.toString())) {
                return kinit;
            } else {
                throw new IllegalArgumentException("The kinit path could not be found or is not executable");
            }
        }

        // on systems that support which command we will attempt to find kinit in the path
        if( processRunner.runScript("which", Arrays.asList("kinit") ) ) {
            String pathFromWhich = StringUtils.chomp(processRunner.getOutputFromLastCommand());
            return new File(pathFromWhich);
        }

        // check the default location for most systems
        kinit = new File("/usr/bin/kinit");
        if (FileUtils.canExecute(kinit.toString())) {
            return kinit;
        }

        return null;
    }
}
