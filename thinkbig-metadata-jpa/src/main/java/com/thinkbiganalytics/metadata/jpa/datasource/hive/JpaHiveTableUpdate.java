/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
    
//    @OneToMany(targetEntity=JpaHivePartitionUpdate.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @ElementCollection(targetClass=JpaHivePartitionUpdate.class)
    private List<HivePartitionUpdate> partitions = new ArrayList<>();
    
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
        return this.partitions;
    }

}
