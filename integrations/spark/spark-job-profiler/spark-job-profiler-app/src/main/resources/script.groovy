/*
 * Copyright (c) 2016. Teradata Inc.
 */
def myFunc =  [ call: { val -> return val.substring(1,5) }] as org.apache.spark.sql.api.java.UDF1

        //hiveContext.udf().register("myFunc", new UDF1() { (x: Integer) => x + 1})

def df = hiveContext.sql("select * from employees.drug_costs")
df = df.filter("market = 'United States'")
df = df.withColumn("salary2", myFunc(df("salary")))
df.saveAsTable("groovy2")