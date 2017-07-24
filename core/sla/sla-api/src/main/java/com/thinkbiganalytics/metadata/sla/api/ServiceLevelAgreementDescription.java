package com.thinkbiganalytics.metadata.sla.api;

/**
 * Description information about the SLA
 *
 */
public interface ServiceLevelAgreementDescription {

    ServiceLevelAgreement.ID getSlaId();

    String getName();

    String getDescription();

}
