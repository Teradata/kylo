package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 7/20/16.
 */

public interface ServiceLevelAgreementActionConfiguration extends Serializable {

    void setActionClasses(List<Class<? extends ServiceLevelAgreementAction>> actionClasses);

    List<Class<? extends ServiceLevelAgreementAction>> getActionClasses();


}
