/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.Dataset;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="DATASOURCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE")
public abstract class JpaDatasource implements Datasource, Serializable {

    private static final long serialVersionUID = -2805184157648437890L;
    
    @EmbeddedId
    private DatasourceId id;
    
    private String name;
    private String description;
    
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime creationTime;
    
    @Transient  // TODO implement
    private List<Dataset<? extends Datasource, ? extends ChangeSet>> datasets = new ArrayList<>();

    public JpaDatasource(String name, String descr) {
        this.id = DatasourceId.create();
        this.creationTime = new DateTime();
        this.name = name;
        this.description = descr;
    }

    public ID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public List<Dataset<? extends Datasource, ? extends ChangeSet>> getDatasets() {
        return datasets;
    }

    
    @Embeddable
    protected static class DatasourceId implements ID {
        
        private static final long serialVersionUID = 241001606640713117L;
        
        private UUID uuid;
        
        public static DatasourceId create() {
            return new DatasourceId(UUID.randomUUID());
        }
        
        public DatasourceId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public DatasourceId(Serializable ser) {
            if (ser instanceof String) {
                this.uuid = UUID.fromString((String) ser);
            } else if (ser instanceof UUID) {
                this.uuid = (UUID) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DatasourceId) {
                DatasourceId that = (DatasourceId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
        
        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }
//
//  
//  @Embeddable
//  protected static class DatasourceId extends BaseId implements ID {
//      
//      private static final long serialVersionUID = 241001606640713117L;
//
//      public static DatasourceId create() {
//          return new DatasourceId(UUID.randomUUID());
//      }
//      
//      public DatasourceId() {
//          super();
//      }
//
//      public DatasourceId(Serializable ser) {
//          super(ser);
//      } 
//  }
//  
}
