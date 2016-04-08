/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate;
import com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.jpa.op.JpaChangeSet;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="CHANGE_SET_HIVE_TABLE")
public class JpaHiveTableUpdate extends JpaChangeSet implements HiveTableUpdate {

    private static final long serialVersionUID = -521636184533464566L;

    private int recourdCount;
    
    @ElementCollection
    @CollectionTable(name="CHANGE_SET_HIVE_TABLE_PART_VALUE", joinColumns=@JoinColumn(name="change_set_hive_table_id"))
    private List<HivePartitionValue> partitionValues = new ArrayList<>();
    
//    @ElementCollection(targetClass=HivePartitionUpdateImpl.class)
//    @CollectionTable(name="CHANGE_SET_HIVE_TABLE_PART", joinColumns=@JoinColumn(name="change_set_hive_table_id"))
//    @Column(name="part")
//    private List<HivePartitionUpdate> partitions = new ArrayList<>();

    
    public JpaHiveTableUpdate(int count) {
        this.recourdCount = count;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate#getRecordCount()
     */
    public int getRecordCount() {
        return this.recourdCount;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HiveTableUpdate#getPartitions()
     */
    public List<HivePartitionUpdate> getPartitions() {
        Map<String, HivePartitionUpdateImpl> map = new HashMap<>();
        
        for (HivePartitionValue part : getPartitionValues()) {
            HivePartitionUpdateImpl update = map.get(part.getName());
            
            if (update == null) {
                update = new HivePartitionUpdateImpl(part.getName());
            }
            
            update.getPartitionValues().add(part.getValue());
        }
        
        return new ArrayList<HivePartitionUpdate>(map.values());
    }

    public int getRecourdCount() {
        return recourdCount;
    }

    public void setRecourdCount(int recourdCount) {
        this.recourdCount = recourdCount;
    }

    public List<HivePartitionValue> getPartitionValues() {
        return partitionValues;
    }

    public void setPartitionValues(List<HivePartitionValue> partitionValues) {
        this.partitionValues = partitionValues;
    }

}
