package com.thinkbiganalytics.feedmgr.sla;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.policy.PolicyTransformException;

/**
 * Created by sr186054 on 7/18/16.
 */
public interface ServiceLevelAgreementTransformer {

    ServiceLevelAgreementRule toUIModel(Metric rule);

    Metric fromUiModel(ServiceLevelAgreementRule rule)
        throws PolicyTransformException;


}
