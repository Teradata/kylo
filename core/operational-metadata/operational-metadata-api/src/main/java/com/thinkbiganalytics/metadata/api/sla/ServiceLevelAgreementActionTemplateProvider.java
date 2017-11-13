package com.thinkbiganalytics.metadata.api.sla;
/*-
 * #%L
 * thinkbig-operational-metadata-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;

import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 10/5/17.
 */
public interface ServiceLevelAgreementActionTemplateProvider {


    List<? extends ServiceLevelAgreementActionTemplate> assignTemplate(ServiceLevelAgreementDescription sla, List<? extends VelocityTemplate> velocityTemplates);

    List<? extends ServiceLevelAgreementActionTemplate> assignTemplateByIds(ServiceLevelAgreementDescription sla, Set<VelocityTemplate.ID> velocityTemplateIds);


    List<? extends ServiceLevelAgreementActionTemplate> findByVelocityTemplate(VelocityTemplate.ID id);

    List<? extends ServiceLevelAgreementActionTemplate> findBySlaId(ServiceLevelAgreement.ID slaId);

    List<? extends ServiceLevelAgreementActionTemplate> deleteForSlaId(ServiceLevelAgreement.ID slaId);

   // ServiceLevelAgreementActionTemplate assignTemplate(String actionClass, VelocityTemplate velocityTemplate);


    List<? extends ServiceLevelAgreementActionTemplate> findAll();
}
