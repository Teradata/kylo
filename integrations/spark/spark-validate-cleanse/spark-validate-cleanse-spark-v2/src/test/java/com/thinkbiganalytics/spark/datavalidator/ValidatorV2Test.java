package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * kylo-spark-validate-cleanse-spark-v2
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

import com.thinkbiganalytics.spark.SparkContextService;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class ValidatorV2Test {

    @Test
    public void testCleansedRowResultsValidationCountsV2() {
        CleansedRowResult cleansedRowResult1 = new CleansedRowResult(Mockito.mock(Row.class), new boolean[]{true, true, true, true, true}, true);
        CleansedRowResult cleansedRowResult2 = new CleansedRowResult(Mockito.mock(Row.class), new boolean[]{true, false, true, true, false}, false);
        CleansedRowResult cleansedRowResult3 = new CleansedRowResult(Mockito.mock(Row.class), new boolean[]{false, false, true, true, false}, false);
        List<CleansedRowResult> cleansedRowResultsList = Arrays.asList(cleansedRowResult1, cleansedRowResult1, cleansedRowResult1,
                                                                       cleansedRowResult1, cleansedRowResult1, cleansedRowResult1,
                                                                       cleansedRowResult1, cleansedRowResult2, cleansedRowResult3);

        SparkConf conf = new SparkConf();
        conf.setMaster("local[*]");
        conf.setAppName("Validator Test - Spark 2");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<CleansedRowResult> inputRDD = sc.parallelize(cleansedRowResultsList, 4);

        StandardDataValidator validator = new StandardDataValidator(new ValidatorStrategyV2(), Mockito.mock(SparkContextService.class));

        long[] output = validator.cleansedRowResultsValidationCounts(inputRDD, 5);
        long[] expectedOutput = {1L, 2L, 0L, 0L, 2L, 7L, 2L};

        assertArrayEquals(expectedOutput, output);
    }
}
