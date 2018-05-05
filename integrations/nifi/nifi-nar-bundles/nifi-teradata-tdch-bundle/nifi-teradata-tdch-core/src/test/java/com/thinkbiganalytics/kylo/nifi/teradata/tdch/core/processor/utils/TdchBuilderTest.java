package com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.utils;

/*-
 * #%L
 * kylo-nifi-teradata-tdch-core
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchOperationType;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TestAbstractTdchProcessor;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.utils.TdchBuilder;

import org.apache.nifi.util.LogMessage;
import org.apache.nifi.util.MockComponentLog;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests for class {@link TdchBuilder}
 */
public class TdchBuilderTest {

    private static final String SPACE = " ";

    private static TdchBuilder getBaseTdchBuilder() {
        TdchBuilder tdchBuilder = new TdchBuilder();
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        return tdchBuilder
            .setLogger(componentLog)
            .setTdchJarEnvironmentVariable("USERLIBTDCH")
            .setTdchLibraryJarsVariable("LIBJARS")
            .setTdchHadoopClassPathVariable("HADOOP_CLASSPATH")
            .setTdchOperationType(TdchOperationType.TDCH_EXPORT)
            .setCommonExportToolJobType("hive");
    }

    private static String getBaseTdchCommand1() {
        return "hadoop jar $USERLIBTDCH com.teradata.connector.common.tool.ConnectorExportTool -libjars $LIBJARS";
    }

    private static String getBaseTdchCommand2() {
        return "-jobtype \"hive\"";
    }

    @Test
    public void testSetting_CommonTeradataUrl() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTeradataUrl("jdbc:teradata://localhost", "tera_db.tera_table")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + "-url \"jdbc:teradata://localhost/database=tera_db\""
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_setCommonTeradataClassname() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTeradataClassname("com.teradata.jdbc.TeraDriver")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + "-classname \"com.teradata.jdbc.TeraDriver\""
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonTeradataUsername() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTeradataUsername("td_user")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + "-username \"td_user\""
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonTeradataPassword() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTeradataPassword("td_password")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + "-password \"td_password\""
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonExportToolMethod() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonExportToolMethod("batch.insert")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + "-method \"batch.insert\""
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonExportToolFileFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonExportToolFileFormat("textfile")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-fileformat \"textfile\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonNumMappers() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonNumMappers(15)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-nummappers \"15\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonThrottleMappers() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonThrottleMappers(true)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-throttlemappers \"true\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonMinMappers_NotConsidered() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonMinMappers(3)
            .build();

        //Need ThrottleMappers to be true also
        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonMinMappers() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonThrottleMappers(true)
            .setCommonMinMappers(3)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-throttlemappers \"true\""
                                 + SPACE
                                 + "-minmappers \"3\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonSourceDateFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonSourceDateFormat("yyyy-MM-dd")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcedateformat \"yyyy-MM-dd\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonSourceTimeFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonSourceTimeFormat("HH:mm:ss")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcetimeformat \"HH:mm:ss\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonSourceTimestampFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonSourceTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcetimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonSourceTimezoneId() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonSourceTimezoneId("GMT")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcetimezoneid \"GMT\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    //
    @Test
    public void testSetting_CommonTargetDateFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTargetDateFormat("yyyy-MM-dd")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-targetdateformat \"yyyy-MM-dd\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonTargetTimeFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTargetTimeFormat("HH:mm:ss")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-targettimeformat \"HH:mm:ss\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonTargetTimestampFormat() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTargetTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-targettimestampformat \"yyyy-MM-dd HH:mm:ss.SSS\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonTargetTimezoneId() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonTargetTimezoneId("PST")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-targettimezoneid \"PST\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_CommonStringTruncate() {
        String actualCommand = getBaseTdchBuilder()
            .setCommonStringTruncate(true)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-stringtruncate \"true\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_SourceHiveConfigurationFileHdfsPath() {
        String actualCommand = getBaseTdchBuilder()
            .setSourceHiveConfigurationFileHdfsPath("/hdfs/path/to/hive.xml")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-hiveconf \"/hdfs/path/to/hive.xml\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_SourceHiveSourceDatabase() {
        String actualCommand = getBaseTdchBuilder()
            .setSourceHiveSourceDatabase("hive_source_db")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcedatabase \"hive_source_db\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_SourceHiveSourceTable() {
        String actualCommand = getBaseTdchBuilder()
            .setSourceHiveSourceTable("hive_source_table")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcetable \"hive_source_table\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_SourceHiveSourceFieldNames() {
        String actualCommand = getBaseTdchBuilder()
            .setSourceHiveSourceFieldNames("hive_field1, hive_field2, hive_field3")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-sourcefieldnames \"hive_field1,hive_field2,hive_field3\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_SourceHiveFieldSeparator() {
        String actualCommand = getBaseTdchBuilder()
            .setSourceHiveFieldSeparator("|")
            .build();

        //no quotes around value
        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-separator |"
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_SourceHiveLineSeparator() {
        String actualCommand = getBaseTdchBuilder()
            .setSourceHiveLineSeparator("\n")
            .build();

        //no quotes around value
        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-lineseparator \\n"
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataDatabaseTable() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataDatabaseTable("teradata_target_db.teradata_target_table")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-targettable \"teradata_target_db.teradata_target_table\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataTargetFieldNames() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataTargetFieldNames("teradata_field1, teradata_field2, teradata_field3")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-targetfieldnames \"teradata_field1,teradata_field2,teradata_field3\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataTruncateTable() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataTruncateTable(true)
            .build();

        //position matters for -D args
        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + "-Dtdch.output.teradata.truncate=true"
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataUseXviews() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataUseXviews(false)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-usexviews \"false\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataQueryBand() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataQueryBand("org=Finance;")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-queryband \"org=Finance;\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataBatchSize() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataBatchSize(5000)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-batchsize \"5000\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataStagingDatabase() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataStagingDatabase("teradata_stage_db")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-stagedatabase \"teradata_stage_db\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataStagingTableName() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataStagingTableName("teradata_stage_table")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-stagetablename \"teradata_stage_table\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataForceStage() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataForceStage(false)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-forcestage \"false\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataKeepStageTable() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataKeepStageTable(false)
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-keepstagetable \"false\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataFastLoadErrorTableDatabase() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataFastLoadErrorTableDatabase("teradata_error_db")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-errortabledatabase \"teradata_error_db\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_TargetTeradataFastLoadErrorTableName() {
        String actualCommand = getBaseTdchBuilder()
            .setTargetTeradataFastLoadErrorTableName("teradata_error_table")
            .build();

        String expectedCommand = getBaseTdchCommand1()
                                 + SPACE
                                 + getBaseTdchCommand2()
                                 + SPACE
                                 + "-errortablename \"teradata_error_table\""
                                 + SPACE;
        Assert.assertEquals(expectedCommand, actualCommand);
    }

    @Test
    public void testSetting_Tdch_Import_OperationType() {
        TdchBuilder tdchBuilder = new TdchBuilder();
        TestRunner runner = TestRunners.newTestRunner(TestAbstractTdchProcessor.class);
        MockComponentLog componentLog = runner.getLogger();
        tdchBuilder
            .setLogger(componentLog)
            .setTdchJarEnvironmentVariable("USERLIBTDCH")
            .setTdchLibraryJarsVariable("LIBJARS")
            .setTdchHadoopClassPathVariable("HADOOP_CLASSPATH")
            .setTdchOperationType(TdchOperationType.TDCH_IMPORT)
            .setCommonExportToolJobType("hive");

        String command = tdchBuilder.build();
        Assert.assertEquals("", command);

        List<LogMessage> warnMessages = componentLog.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().endsWith("TDCH Import not yet implemented"));
    }

    @Test
    public void testGetting_PasswordLabel() {
        Assert.assertEquals("-password", TdchBuilder.getCommonTeradataPasswordLabel());
    }
}
