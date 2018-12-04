package com.thinkbiganalytics.kylo.spark.util;

/*-
 * #%L
 * spark-file-metadata-spark-v1
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.apache.spark.sql.DataFrame;

import java.util.List;

import scala.collection.JavaConverters;
import scala.collection.Map;

public class SparkUtil {

    public static <A, B> Map<A, B> toScalaMap(java.util.Map<A, B> m) {
        return JavaConverters.mapAsScalaMapConverter(m).asScala();
    }

    public static DataFrame unionAll(List<DataFrame> dataFrameList) {
        DataFrame unionDf = null;
        for (DataFrame df : dataFrameList) {
            if (unionDf == null) {
                unionDf = df;
            } else {
                unionDf = unionDf.unionAll(df);
            }
        }
        return unionDf;
    }

}
