package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Stores SLA Actions and their associated Cron Schedule Created by sr186054 on 7/21/16.
 */
public interface ServiceLevelAgreementCheck {

    interface ID extends Serializable {

    }

    /**
     * @return the unique ID of this SLACheck
     */
    ID getId();

    /**
     * @return the name of this SLACheck
     */
    String getName();

    /**
     * @return the time when this SLACheck was created
     */
    DateTime getCreatedTime();

    /**
     * @return a description of this SLACheck
     */
    String getDescription();


    ServiceLevelAgreement getServiceLevelAgreement();

    List<? extends ServiceLevelAgreementActionConfiguration> getActionConfigurations();

    String getCronSchedule();

}
