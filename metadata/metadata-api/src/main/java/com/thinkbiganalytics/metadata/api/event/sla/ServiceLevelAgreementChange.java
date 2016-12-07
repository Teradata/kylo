/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.sla;

import java.util.Objects;

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChange;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public class ServiceLevelAgreementChange extends MetadataChange {

    private static final long serialVersionUID = 1L;
    
    private final ServiceLevelAgreement.ID id;
    private final String name;
    
    public ServiceLevelAgreementChange(ChangeType change, ServiceLevelAgreement.ID id, String name) {
        this(change, "", id, name);
    }

    public ServiceLevelAgreementChange(ChangeType change, String descr, ServiceLevelAgreement.ID id, String name) {
        super(change, descr);
        this.id = id;
        this.name = name;
    }

    public ServiceLevelAgreement.ID getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.id, this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceLevelAgreementChange) {
            ServiceLevelAgreementChange that = (ServiceLevelAgreementChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.id, that.id) &&
                   Objects.equals(this.name, that.name);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SLA change ");
        return sb
                        .append("(").append(getChange()).append(") - ")
                        .append("ID: ").append(this.id)
                        .append(" name: ").append(this.name)
                        .toString();
    }

}
