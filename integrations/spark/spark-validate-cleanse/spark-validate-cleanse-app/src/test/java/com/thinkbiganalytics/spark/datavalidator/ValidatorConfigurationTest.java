package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * kylo-spark-validate-cleanse-app
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

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValidatorConfigurationTest {

    @Test
    public void testParseRemainingParameters() {
        String[] args = {"-h", "hive.setting.1=value.1", "--hiveConf", "hive.setting.2=value.2"};
        ValidatorConfiguration params = new ValidatorConfiguration(new String[]{"targetDatabase", "entity", "partition", "path-to-policy-file"});
        new JCommander(params).parse(args);
        List<Param> hiveParams = params.getHiveParams();

        Param first = hiveParams.get(0);
        assertEquals("hive.setting.1", first.getName());
        assertEquals("value.1", first.getValue());

        Param second = hiveParams.get(1);
        assertEquals("hive.setting.2", second.getName());
        assertEquals("value.2", second.getValue());
    }

    @Test
    public void testParseRemainingParametersStorageLevel() {
        String[] args = {"--storageLevel", "MEMORY_ONLY"};
        ValidatorConfiguration params = new ValidatorConfiguration(new String[]{"targetDatabase", "entity", "partition", "path-to-policy-file"});
        new JCommander(params).parse(args);
        String storageLevel = params.getStorageLevel();
        assertEquals("MEMORY_ONLY", storageLevel);
    }

    @Test
    public void testDefaultStorageLevel() {
        String[] args = {};
        ValidatorConfiguration params = new ValidatorConfiguration(new String[]{"targetDatabase", "entity", "partition", "path-to-policy-file"});
        new JCommander(params).parse(args);
        String defaultStorageLevel = params.getStorageLevel();
        assertEquals("MEMORY_AND_DISK", defaultStorageLevel);
    }

    @Test
    public void testParseRemainingParametersNumPartitions() {
        String[] args = {"--storageLevel", "MEMORY_ONLY", "--numPartitions", "10"};
        ValidatorConfiguration params = new ValidatorConfiguration(new String[]{"targetDatabase", "entity", "partition", "path-to-policy-file"});
        new JCommander(params).parse(args);
        Integer numRDDPartitions = params.getNumPartitions();
        assertEquals("10", String.valueOf(numRDDPartitions));
    }

    @Test
    public void testDefaultNumPartitions() {
        String[] args = {};
        ValidatorConfiguration params = new ValidatorConfiguration(new String[]{"targetDatabase", "entity", "partition", "path-to-policy-file"});
        new JCommander(params).parse(args);
        Integer defaultRDDPartitions = params.getNumPartitions();
        assertEquals("-1", String.valueOf(defaultRDDPartitions));
    }

    @Test
    public void testParseRemainingParameters_missingParameters() {
        String[] args = {};
        ValidatorConfiguration params = new ValidatorConfiguration(new String[]{"targetDatabase", "entity", "partition", "path-to-policy-file"});
        new JCommander(params).parse(args);
        List<Param> hiveParams = params.getHiveParams();

        assertTrue(hiveParams.isEmpty());
    }
}
