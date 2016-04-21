package com.thinkbiganalytics.spark.dataprofiler.topn;

import java.io.Serializable;
import java.util.Arrays;
import java.util.PriorityQueue;

import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerConfiguration;

/**
 * Class to store top-N items<br>
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class TopNDataList implements Serializable {

	PriorityQueue<TopNDataItem> topNDataItemsForColumn;
	
	
	/**
	 * No-argument constructor initialize an empty list
	 */
	public TopNDataList() {
		
		topNDataItemsForColumn = new PriorityQueue<TopNDataItem>();
	}
	
	
	/**
	 * Get items in reverse order. Also refer to printTopNItems method.
	 * @return priority queue of items (ordered in reverse from smallest count to highest count)
	 */
	public PriorityQueue<TopNDataItem> getTopNDataItemsForColumnInReverse() {
		return topNDataItemsForColumn;
	}


	/**
	 * Add an item for inclusion in top-N list
	 * @param newValue value
	 * @param newCount count/frequency
	 */
	public void add(Object newValue, Long newCount) {
		topNDataItemsForColumn.add(new TopNDataItem(newValue, newCount));
	}
	
	
	/**
	 * Print the top-N items as a string. This will give Top-N items in generally expected format (highest count first, lowest count last)
	 * @param n Number of items to get
	 * @return String of top-N items with configured delimiters within and between entries (Refer to configuration parameters in ProfilerConfiguration class)
	 */
	public String printTopNItems(int n) {
		Object[] topNDataItemsReverseOrder = topNDataItemsForColumn.toArray();
		Arrays.sort(topNDataItemsReverseOrder);
		
		StringBuilder sb = new StringBuilder();
		int requestedCount = n;
		int i = 0;
		
		for (i = topNDataItemsReverseOrder.length-1; ( (i>=0) && (n-- > 0)) ; i--) {
			TopNDataItem dataItem = (TopNDataItem) topNDataItemsReverseOrder[i];
			
			sb.append((topNDataItemsReverseOrder.length - i) 
					+ ProfilerConfiguration.TOP_N_VALUES_INTERNAL_DELIMITER + dataItem.getValue() 
					+ ProfilerConfiguration.TOP_N_VALUES_INTERNAL_DELIMITER + dataItem.getCount() 
					+ ProfilerConfiguration.TOP_N_VALUES_RECORD_DELIMITER);
			
		}
		
		if (n > 0) {
			System.out.println("[PROFILER-INFO] Only " + topNDataItemsReverseOrder.length 
					+ " items exist. Top " + requestedCount + " requested." 
					+ "\nPrinted all items.");
		}
		
		return sb.toString();
	}
	
	
	
	/**
	 * Print all the items ordered from highest count to lowest count
	 * String will have configured delimiters within and between entries (Refer to configuration parameters in ProfilerConfiguration class)
	 */
	@Override
	public String toString() {
		return printTopNItems(topNDataItemsForColumn.size());
	}
	
}
