/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.op;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="DATASET")
public class JpaDataset<D extends Datasource, C extends ChangeSet> implements Dataset<D, C> {

    private static final long serialVersionUID = 2087060566515660478L;

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(mappedBy="dataset")
    private JpaDataOperation dataOperation;
    
    @ManyToOne(targetEntity=JpaDatasource.class)
    private D datasource;
    
    @OneToMany(targetEntity=JpaChangeSet.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<C> changes = new HashSet<>();
    
    private DateTime time;
    private ChangeType type;

    public JpaDataset() {
    }
    
    public JpaDataset(D dataset, C content) {
        this.time = new DateTime();
        this.type = ChangeType.UPDATE;
        this.dataOperation = null;  // TODO
        this.datasource = dataset;
        this.changes.add(content); 
    }

    public DateTime getTime() {
        return time;
    }

    public ChangeType getType() {
        return type;
    }

    public D getDatasource() {
        return datasource;
    }

    public DataOperation getDataOperation() {
        return dataOperation;
    }

    public Set<C> getChanges() {
        return changes;
    }

}
