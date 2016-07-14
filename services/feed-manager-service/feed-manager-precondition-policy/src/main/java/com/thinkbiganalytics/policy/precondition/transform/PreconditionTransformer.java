package com.thinkbiganalytics.policy.precondition.transform;

import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

/**
 * Created by sr186054 on 4/21/16.
 */
public interface PreconditionTransformer {

    PreconditionRule toUIModel(Precondition standardizationRule);

    Precondition fromUiModel(PreconditionRule rule)
        throws PolicyTransformException;


}
