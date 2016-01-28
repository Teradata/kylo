/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.kafka.KafkaDataset;
import com.thinkbiganalytics.metadata.api.dataset.kafka.KeyRange;

/**
 *
 * @author Sean Felten
 */
public interface KafkaChangeEvent extends ChangeEvent<KafkaDataset, KeyRange> {

}
