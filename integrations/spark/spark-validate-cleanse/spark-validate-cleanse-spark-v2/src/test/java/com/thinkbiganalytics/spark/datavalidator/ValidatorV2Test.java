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

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class ValidatorV2Test {

    @Test
    public void testCleansedRowResultsValidationCountsV2() {
        CleansedRowResult cleansedRowResult1 = new CleansedRowResult();
        cleansedRowResult1.rowIsValid = true;
        cleansedRowResult1.columnsValid = new boolean[]{true, true, true, true, true};

        CleansedRowResult cleansedRowResult2 = new CleansedRowResult();
        cleansedRowResult2.rowIsValid = false;
        cleansedRowResult2.columnsValid = new boolean[]{true, false, true, true, false};

        CleansedRowResult cleansedRowResult3 = new CleansedRowResult();
        cleansedRowResult3.rowIsValid = false;
        cleansedRowResult3.columnsValid = new boolean[]{false, false, true, true, false};

        List<CleansedRowResult> cleansedRowResultsList = Arrays.asList(cleansedRowResult1, cleansedRowResult1, cleansedRowResult1,
                                                                       cleansedRowResult1, cleansedRowResult1, cleansedRowResult1,
                                                                       cleansedRowResult1, cleansedRowResult2, cleansedRowResult3);

        SparkConf conf = new SparkConf();
        conf.setMaster("local[*]");
        conf.setAppName("Validator Test - Spark 2");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<CleansedRowResult> inputRDD = sc.parallelize(cleansedRowResultsList, 4);

        Validator validator = new Validator();
        validator.setValidatorStrategy(new ValidatorStrategyV2());

        long[] output = validator.cleansedRowResultsValidationCounts(inputRDD, 5);
        long[] expectedOutput = {1L, 2L, 0L, 0L, 2L, 7L, 2L};

        assertArrayEquals(expectedOutput, output);
    }
}
