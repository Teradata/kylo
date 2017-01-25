/*-
 * #%L
 * thinkbig-spark-job-profiler-app
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
def myFunc =  [ call: { val -> return val.substring(1,5) }] as org.apache.spark.sql.api.java.UDF1

        //hiveContext.udf().register("myFunc", new UDF1() { (x: Integer) => x + 1})

def df = hiveContext.sql("select * from employees.drug_costs")
df = df.filter("market = 'United States'")
df = df.withColumn("salary2", myFunc(df("salary")))
df.saveAsTable("groovy2")
