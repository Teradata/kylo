package com.thinkbiganalytics.spark.dataprofiler.accum;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.AccumulatorParam;

/**
 * Custom accumulator to store a map: (value, count)<br>
 * Can be used to count frequency of arbitrary values encountered during processing of an RDD
 * 
 * @author jagrut sharma
 */
@SuppressWarnings("serial")
public class MapAccumulator implements AccumulatorParam<Map<String, Long>>{

	/**
	 * Return identity/zero value for accumulator (an empty map)
	 */
	@Override
	public Map<String, Long> zero(Map<String, Long> m) {
		return new HashMap<>();
	}
	
	
	/**
	 * Add additional data to the accumulator
	 */
	@Override
	public Map<String, Long> addAccumulator(Map<String, Long> m1, Map<String, Long> m2) {
		return mergeMap(m1, m2);
	}
	
	
	/**
	 * Add two accumulators 
	 */
	@Override
	public Map<String, Long> addInPlace(Map<String, Long> m1, Map<String, Long> m2) {
		return mergeMap(m1, m2);
	}

	
	/*
	 * Helper method to merge two maps
	 */
	private Map<String, Long> mergeMap( Map<String, Long> map1, Map<String, Long> map2) {
        Map<String, Long> result = new HashMap<>(map1);
                
        for (String key: map2.keySet()) {
 
        	Long value = map2.get(key);
        	        	
        	if (result.containsKey(key)) {
        		result.put(key, result.get(key) + value);        		
        	}
        	else {
        		result.put(key, value);
        	}
        }
        
        return result;
    }

}
