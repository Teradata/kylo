package com.thinkbiganalytics.nifi.v2.sqoop.process;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

/**
 * Class to store the result of sqoop command execution
 */
public class SqoopProcessResult {

    private int exitValue = -1;
    private String[] logLines = null;

    /**
     * Constructor
     *
     * @param exitValue exit value of sqoop command execution
     * @param logLines  log information with number of records, next high watermark
     */
    public SqoopProcessResult(int exitValue, String[] logLines) {
        this.exitValue = exitValue;
        this.logLines = logLines.clone();
    }

    /**
     * Get exit value of sqoop command execution (0 is success, any other value indicates failure)
     *
     * @return exit value
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * Get log information with number of records, next high watermark
     *
     * @return log information as array of strings
     */
    public String[] getLogLines() {
        if (logLines != null) {
            return logLines.clone();
        } else {
            return new String[]{};
        }
    }
}
