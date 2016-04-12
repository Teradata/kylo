/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.op;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.Period;

import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
//@MappedSuperclass
@Entity
@Table(name="CHANGE_SET")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class JpaChangeSet implements ChangeSet {

    private static final long serialVersionUID = -5427878851793245525L;
    
    @Id
    @GeneratedValue
    private UUID id;

    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime intrinsicTime;
    @Type(type="org.joda.time.contrib.hibernate.PersistentPeriod")
    private Period intrinsicPeriod;
    private int completenessFactor;

    @Override
    public DateTime getIntrinsicTime() {
        return this.intrinsicTime;
    }

    @Override
    public Period getIntrinsicPeriod() {
        return this.intrinsicPeriod;
    }

    @Override
    public int getCompletenessFactor() {
        return this.completenessFactor;
    }

}
