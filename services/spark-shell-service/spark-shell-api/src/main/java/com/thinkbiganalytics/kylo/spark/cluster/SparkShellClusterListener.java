package com.thinkbiganalytics.kylo.spark.cluster;

/*-
 * #%L
 * Spark Shell Service API
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


import com.thinkbiganalytics.cluster.ClusterServiceListener;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.spark.shell.SparkShellProcessListener;

/**
 * Coordinates Spark Shell processes between Kylo nodes in a cluster.
 */
public interface SparkShellClusterListener extends ClusterServiceListener, ClusterServiceMessageReceiver, SparkShellProcessListener {
}
