package com.thinkbiganalytics.spark.dataprofiler.topn;

import java.io.Serializable;

/**
 * Class for an item in Top-N list<br>
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class TopNDataItem implements Serializable, Comparable<TopNDataItem> {

	private Object value;
	private Long count;
	
	
	/**
	 * Constructor to create an item
	 * @param value value of item
	 * @param count frequency of item
	 */
	public TopNDataItem(Object value, Long count) {
		
		this.value = value;
		this.count = count;
	}
	
	
	/**
	 * Get value of item
	 * @return value
	 */
	public Object getValue() {
		return value;
	}


	/**
	 * Set value of item
	 * @param value item value
	 */
	public void setValue(Object value) {
		this.value = value;
	}


	/**
	 * Get frequency of item
	 * @return count/frequency
	 */
	public Long getCount() {
		return count;
	}


	/**
	 * Set frequency of item
	 * @param count/frequency
	 */
	public void setCount(Long count) {
		this.count = count;
	}

	
	/**
	 * Get verbose description of item
	 */
	@Override
	public String toString() {
		return "TopNDataItem [value=" + value + ", count=" + count + "]";
	}


	/**
	 * Compare to another item based upon count/frequency
	 */
	@Override
	public int compareTo(TopNDataItem other) {
		if (this.count < other.count) {
			return -1;
		}
		else if (this.count > other.count) {
			return 1;
		}
		else {
			return 0;
		}
	}


	/**
	 * Generate hashCode for item
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}


	/**
	 * Equality check logic to determine if item is equal to another item
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopNDataItem other = (TopNDataItem) obj;
		if (count == null) {
			if (other.count != null)
				return false;
		} else if (!count.equals(other.count))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
