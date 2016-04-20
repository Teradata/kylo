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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
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
    @JoinColumn(name="dataset_id")
    private Set<C> changes = new HashSet<>();
    
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime time;
    private ChangeType type;

    public JpaDataset() {
    }
    
    public JpaDataset(D dataset, C content) {
        setTime(DateTime.now());
        setType(ChangeType.UPDATE);
//        setDataOperation(dataOperation);
        setDatasource(dataset);
        getChanges().add(content);
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDataOperation(JpaDataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }

    public void setDatasource(D datasource) {
        this.datasource = datasource;
    }

    public void setChanges(Set<C> changes) {
        this.changes = changes;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

}
