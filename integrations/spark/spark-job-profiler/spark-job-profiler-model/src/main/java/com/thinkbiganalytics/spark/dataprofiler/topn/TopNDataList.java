package com.thinkbiganalytics.spark.dataprofiler.topn;

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

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Class to store top-N items<br>
 */
@SuppressWarnings("serial")
public class TopNDataList implements Serializable {

    /**
     * Delimiter to use when storing top-N values in result table<br>
     * This delimiter is output between fields of a single top-N entry
     */
    public static final String TOP_N_VALUES_INTERNAL_DELIMITER = "^A";


    /**
     * Delimiter to use when storing top-N values in result table<br>
     * This delimiter is output between top-N entries
     */
    public static final String TOP_N_VALUES_RECORD_DELIMITER = "^B";

    private final TreeSet<TopNDataItem> topNDataItemsForColumn;
    private final int maxSize;
    private Long lowestCountSoFar = Long.MAX_VALUE;


    /**
     * Constructor to set the number of items in top N list
     *
     * @param maxSize N in Top N
     */
    public TopNDataList(int maxSize) {
        this.maxSize = (maxSize > 0) ? maxSize : 3;
        topNDataItemsForColumn = new TreeSet<>();
    }


    /**
     * Add an item for inclusion in top-N list <br>
     * If two items have same count, the item that was first seen will be kept.
     *
     * @param newValue value
     * @param newCount count/frequency
     */
    public void add(Object newValue, Long newCount) {

        if (topNDataItemsForColumn.size() >= maxSize) {
            if (newCount > lowestCountSoFar) {
                topNDataItemsForColumn.pollFirst();
                addAndUpdateLowestCount(newValue, newCount);
            }
        } else {
            addAndUpdateLowestCount(newValue, newCount);
        }
    }

    /**
     * Helper method <br>
     * Add a new item in topN structure <br>
     * Update the lowest count in topN structure
     *
     * @param newValue value
     * @param newCount count/frequency
     */
    private void addAndUpdateLowestCount(Object newValue, Long newCount) {
        topNDataItemsForColumn.add(new TopNDataItem(newValue, newCount));
        lowestCountSoFar = topNDataItemsForColumn.first().getCount();
    }


    /**
     * Print the top-N items as a string. This will give Top-N items in generally expected format (highest count first, lowest count last)<br>
     *
     * @return String of top-N items with configured delimiters within and between entries (Refer to configuration parameters in ProfilerConfiguration class)
     */
    public String printTopNItems() {
        int index = 1;
        StringBuilder sb = new StringBuilder();
        Iterator i = topNDataItemsForColumn.descendingIterator();

        while (i.hasNext()) {
            TopNDataItem item = (TopNDataItem) i.next();
            sb.append(index++).append(TOP_N_VALUES_INTERNAL_DELIMITER)
                .append(item.getValue())
                .append(TOP_N_VALUES_INTERNAL_DELIMITER)
                .append(item.getCount())
                .append(TOP_N_VALUES_RECORD_DELIMITER);
        }

        return sb.toString();
    }


    /**
     * Print all the items ordered from highest count to lowest count <br>
     * String will have configured delimiters within and between entries (Refer to configuration parameters in ProfilerConfiguration class)
     */
    @Override
    public String toString() {
        return printTopNItems();
    }


    /**
     * Get the top-N items as an ordered set (lowest count to highest count)
     *
     * @return Set with top-N items ordered from lowest count to highest count
     */
    public TreeSet<TopNDataItem> getTopNDataItemsForColumn() {
        return this.topNDataItemsForColumn;
    }
}
