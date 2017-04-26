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

import javax.annotation.Nonnull;

/**
 * Class for an item in Top-N list<br>*
 */
@SuppressWarnings("serial")
public class TopNDataItem implements Serializable, Comparable<TopNDataItem> {

    private Object value;
    private Long count;


    /**
     * Constructor to create an item
     *
     * @param value value of item
     * @param count frequency of item
     */
    public TopNDataItem(Object value, Long count) {

        this.value = value;
        this.count = count;
    }


    /**
     * Get value of item
     *
     * @return value
     */
    public Object getValue() {
        return value;
    }


    /**
     * Set value of item
     *
     * @param value item value
     */
    public void setValue(Object value) {
        this.value = value;
    }


    /**
     * Get frequency of item
     *
     * @return count/frequency
     */
    public Long getCount() {
        return count;
    }


    /**
     * Set frequency of item
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
     * This is used by TreeSet for ordering
     */
    @Override
    public int compareTo(@Nonnull TopNDataItem other) {

        /* if count of this item is lower, it is smaller */
        if (this.count < other.count) {
            return -1;
        }
        /* if count of this item is higher, it is bigger */
        else if (this.count > other.count) {
            return 1;
        }
        /*
        if both items have same count
	- 1. if both items have values as null, they are equal
	- 2. if this item's value is null and other item's value is not null, this item is lower
	- 3. if this item's value is not null and other item's value is null, this item is higher
	- 4. if both items have same non-null values, they are equal.
	- 5. if both items have different non-null values, consider this item as lower. (favors keeping non-null items seen earlier)
	*/
        else if (this.count.equals(other.count)) {
            //1
            if ((this.value == null) && (other.value == null)) {
                return 0;
            }
            //2
            else if (this.value == null) {
                return -1;
            }
            //3
            else if (other.value == null) {
                return 1;
            }
            //4
            else if (this.value.equals(other.value)) {
                return 0;
            }
            //5
            else {
                return -1;
            }
        }

        return -1;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TopNDataItem other = (TopNDataItem) obj;
        if (count == null) {
            if (other.count != null) {
                return false;
            }
        } else if (!count.equals(other.count)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }


}
