package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils;

/*-
 * #%L
 * nifi-teradata-tdch-core
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchProcessResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.logging.ComponentLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods to use with TDCH processors
 */
public class TdchUtils {

    public final static String TDCH_EXPORT_HIVE_TO_TERADATA_INPUT_RECORDS_IDENTIFIER = "Map input records=";
    public final static String TDCH_EXPORT_HIVE_TO_TERADATA_OUTPUT_RECORDS_IDENTIFIER = "Map output records=";
    public final static String TDCH_EXPORT_HIVE_TO_TERADATA_EXIT_CODE_IDENTIFIER = "INFO tool.ConnectorExportTool: job completed with exit code";
    public final static String TDCH_EXPORT_HIVE_TO_TERADATA_TIME_TAKEN_IDENTIFIER = "INFO tool.ConnectorExportTool: ConnectorExportTool time is";
    public final static String MASK = "*****";

    private Map<String, String> getTdchCredentialsToMask() {
        final String SPACE_STRING = " ";
        final String PASSWORD_IDENTIFIER = TdchBuilder.getCommonTeradataPasswordLabel();    //-password

        Map<String, String> credentialsToMask = new HashMap<>();
        credentialsToMask.put(PASSWORD_IDENTIFIER, SPACE_STRING);

        return credentialsToMask;
    }

    public String maskTdchCredentials(String tdchCommand) {
        return maskTdchCredentials(tdchCommand, getTdchCredentialsToMask());
    }

    private String maskTdchCredentials(String tdchCommand, Map<String, String> credentialIdentifiers) {

        for (Map.Entry<String, String> credentialIdentifier : credentialIdentifiers.entrySet()) {
            if (!tdchCommand.contains(credentialIdentifier.getKey())) {
                continue;
            }
            int startPosCredentialIdentifier = tdchCommand.indexOf(credentialIdentifier.getKey());
            int startPosValue = tdchCommand.indexOf(credentialIdentifier.getValue(), startPosCredentialIdentifier);
            int endPosValue = tdchCommand.indexOf(" ", startPosValue + 1);
            tdchCommand = tdchCommand.substring(0, startPosValue + 1) + MASK + tdchCommand.substring(endPosValue, tdchCommand.length());
        }
        return tdchCommand;
    }

    public long getExportHiveToTeradataInputRecordsCount(TdchProcessResult tdchProcessResult, ComponentLog logger) {
        String[] logLines = tdchProcessResult.getLogLines();

        if ((tdchProcessResult.getExitValue() != 0)
            || (StringUtils.isEmpty(logLines[0]))
            || (StringUtils.length(logLines[0]) <= StringUtils.length(TDCH_EXPORT_HIVE_TO_TERADATA_INPUT_RECORDS_IDENTIFIER))) {
            logger.warn("Skipping attempt to retrieve number of input records");
            return -1;
        }

        String inputRecordsCountLogLine = logLines[0];
        final String START_INPUT_RECORD_COUNT_IDENTIFIER = TDCH_EXPORT_HIVE_TO_TERADATA_INPUT_RECORDS_IDENTIFIER;
        int start = inputRecordsCountLogLine.indexOf(START_INPUT_RECORD_COUNT_IDENTIFIER);
        int end = inputRecordsCountLogLine.length();
        String numberString = inputRecordsCountLogLine.substring(start + START_INPUT_RECORD_COUNT_IDENTIFIER.length(), end).trim();
        try {
            return Long.parseLong(numberString);
        } catch (Exception e) {
            logger.warn("Unable to parse number of input records processed. {}", new Object[]{e.getMessage()});
            return -1;
        }
    }

    public long getExportHiveToTeradataOutputRecordsCount(TdchProcessResult tdchProcessResult, ComponentLog logger) {
        String[] logLines = tdchProcessResult.getLogLines();

        if ((tdchProcessResult.getExitValue() != 0)
            || (StringUtils.isEmpty(logLines[1]))
            || (StringUtils.length(logLines[1]) <= StringUtils.length(TDCH_EXPORT_HIVE_TO_TERADATA_OUTPUT_RECORDS_IDENTIFIER))) {
            logger.warn("Skipping attempt to retrieve number of output records");
            return -1;
        }

        String outputRecordsCountLogLine = logLines[1];
        final String START_OUTPUT_RECORD_COUNT_IDENTIFIER = TDCH_EXPORT_HIVE_TO_TERADATA_OUTPUT_RECORDS_IDENTIFIER;
        int start = outputRecordsCountLogLine.indexOf(START_OUTPUT_RECORD_COUNT_IDENTIFIER);
        int end = outputRecordsCountLogLine.length();
        String numberString = outputRecordsCountLogLine.substring(start + START_OUTPUT_RECORD_COUNT_IDENTIFIER.length(), end).trim();
        try {
            return Long.parseLong(numberString);
        } catch (Exception e) {
            logger.warn("Unable to parse number of output records processed. {}", new Object[]{e.getMessage()});
            return -1;
        }
    }

    public long getExportHiveToTeradataJobExitCode(TdchProcessResult tdchProcessResult, ComponentLog logger) {
        String[] logLines = tdchProcessResult.getLogLines();

        if ((tdchProcessResult.getExitValue() != 0)
            || (StringUtils.isEmpty(logLines[2]))
            || (StringUtils.length(logLines[2]) <= StringUtils.length(TDCH_EXPORT_HIVE_TO_TERADATA_EXIT_CODE_IDENTIFIER))) {
            logger.warn("Skipping attempt to retrieve TDCH exit code");
            return -1;
        }

        String tdchExitCodeLogLine = logLines[2];
        final String START_EXIT_CODE_IDENTIFIER = TDCH_EXPORT_HIVE_TO_TERADATA_EXIT_CODE_IDENTIFIER;
        int start = tdchExitCodeLogLine.indexOf(START_EXIT_CODE_IDENTIFIER);
        int end = tdchExitCodeLogLine.length();
        String numberString = tdchExitCodeLogLine.substring(start + START_EXIT_CODE_IDENTIFIER.length(), end).trim();

        try {
            return Long.parseLong(numberString);
        } catch (Exception e) {
            logger.warn("Unable to parse TDCH exit code. {}", new Object[]{e.getMessage()});
            return -1;
        }
    }

    public String getExportHiveToTeradataJobTimeTaken(TdchProcessResult tdchProcessResult, ComponentLog logger) {
        String[] logLines = tdchProcessResult.getLogLines();

        if ((tdchProcessResult.getExitValue() != 0)
            || (logLines[3] == null)
            || (StringUtils.length(logLines[3]) <= StringUtils.length(TDCH_EXPORT_HIVE_TO_TERADATA_TIME_TAKEN_IDENTIFIER))) {
            logger.warn("Skipping attempt to retrieve time taken by TDCH job");
            return "Unable to determine time taken";
        }

        String tdchExitCodeLogLine = logLines[3];
        final String START_TIME_TAKEN_IDENTIFIER = TDCH_EXPORT_HIVE_TO_TERADATA_TIME_TAKEN_IDENTIFIER;
        int start = tdchExitCodeLogLine.indexOf(START_TIME_TAKEN_IDENTIFIER);
        int end = tdchExitCodeLogLine.length();
        String timeTaken = tdchExitCodeLogLine.substring(start + START_TIME_TAKEN_IDENTIFIER.length(), end).trim();

        if (StringUtils.endsWith(timeTaken, "s")) {
            return timeTaken;
        } else {
            logger.warn("Unable to get valid value for time taken by TDCH job");
            return "Unable to determine time taken";
        }
    }
}
