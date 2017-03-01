package com.thinkbiganalytics.spark.dataquality.util;

/*-
 * #%L
 * kylo-spark-job-dataquality-app
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

/**
 * Constants used by Data Quality Checker
 */
public class DataQualityConstants {
   
	/**
	 *  Hive Attributes
	 */
    public static final String PROCESSING_DTTM_COLUMN = "processing_dttm";
    public static final String VALID_TABLE_SUFFIX = "_valid";
    public static final String INVALID_TABLE_SUFFIX = "_invalid";
    public static final String FEED_TABLE_SUFFIX = "_feed";
    public static final String DQ_TABLE_SUFFIX = "_dataquality";

    /**
     *  Nifi Attributes coming from FlowFile
     */
	public static String CATEGORY_ATTRIBUTE = "category";
	public static String FEED_ATTRIBUTE = "feed";
	public static String PROCESSING_DTTM_ATTRIBUTE = "feedts";
	public static String SOURCE_ROW_COUNT_ATTRIBUTE = "source.row.count";
	public static String SQOOP_ROW_COUNT_ATTRIBUTE = "sqoop.record.count";
	

	/**
	 *  Derived Attributes used by DQ code
	 */
	public static String DQ_FEED_ROW_COUNT_ATTRIBUTE = "dq.feed.count";
	public static String DQ_VALID_ROW_COUNT_ATTRIBUTE = "dq.valid.count";
	public static String DQ_INVALID_ROW_COUNT_ATTRIBUTE = "dq.invalid.count";
	public static String DQ_INVALID_ALLOWED_COUNT_ATTRIBUTE = "dq.invalid.allowcnt";
	public static String DQ_INVALID_ALLOWED_PERCENT_ATTRIBUTE = "dq.invalid.pctcnt";
	
	/**
	 * Default Values used by DQ code
	 */
    public static final long DEFAULT_INVALID_ALLOWED_COUNT_VALUE = 0;
    public static final long DEFAULT_INVALID_ALLOWED_PERCENT_VALUE = 0;
    

}
