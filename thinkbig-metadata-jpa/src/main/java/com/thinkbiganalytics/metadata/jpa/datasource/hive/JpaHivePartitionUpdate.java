/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource.hive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate;

/**
 *
 * @author Sean Felten
 */
@Embeddable
@Table(name="CHANGE_SET_HIVE_TABLE_PART")
public class JpaHivePartitionUpdate implements HivePartitionUpdate {

    private static final long serialVersionUID = 8505876587865483987L;

//    @Id
//    @GeneratedValue
//    private Long id;

    private String columnName;
    
//    @OneToMany(targetEntity=JpaHivePartitionValue.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @ElementCollection
    @CollectionTable(name="CHANGE_SET_HIVE_TABLE_PART_VALUE", joinColumns=@JoinColumn(name="change_set_hive_table_part_id"))
    @Column(name="value")
    private List<String> partitionValues;
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate#getColumnName()
     */
    public String getColumnName() {
        return this.columnName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.datasource.hive.HivePartitionUpdate#getValuses()
     */
    public List<Serializable> getValues() {
        List<Serializable> list = new ArrayList<>();
        for (String value : getPartitionValues()) {
            list.add(value);
        }
        return list;
    }

    public List<String> getPartitionValues() {
        return partitionValues;
    }

    public void setPartitionValues(List<String> partitionValues) {
        this.partitionValues = partitionValues;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

}
