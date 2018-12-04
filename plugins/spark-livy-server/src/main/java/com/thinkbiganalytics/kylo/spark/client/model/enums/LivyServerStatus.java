package com.thinkbiganalytics.kylo.spark.client.model.enums;

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


// @formatter:off
/**
 * The server status provides additional insight for a heartbeat thread
 *
 * LivyServerStatus
 *
 * Value	 Description
 * alive         Livy service answering HTTP calls
 * http_error    Livy is throwing HTTP errors, sometimes from bugs in Livy
 * not_found     Livy service stopped, or restarting.  Could be temporary and state may move to recovering at some point
 */
// @formatter:on
public enum LivyServerStatus {
    alive,
    http_error,    // HTTP error > 500 response from Livy, not necessarily fatal but likely so..  queries may be retried if configured so
    not_found
}
