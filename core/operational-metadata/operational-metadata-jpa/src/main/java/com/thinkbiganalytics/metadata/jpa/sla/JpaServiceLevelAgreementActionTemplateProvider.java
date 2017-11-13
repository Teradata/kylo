package com.thinkbiganalytics.metadata.jpa.sla;
/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementActionTemplate;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementActionTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.common.JpaVelocityTemplate;
import com.thinkbiganalytics.metadata.jpa.common.VelocityTemplateRepository;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 10/5/17.
 */
@Service
public class JpaServiceLevelAgreementActionTemplateProvider implements ServiceLevelAgreementActionTemplateProvider {

    private ServiceLevelAgreementActionTemplateRepository serviceLevelAgreementActionTemplateRepository;

    private VelocityTemplateRepository velocityTemplateRepository;

    @Autowired
    public JpaServiceLevelAgreementActionTemplateProvider(VelocityTemplateRepository velocityTemplateRepository,ServiceLevelAgreementActionTemplateRepository serviceLevelAgreementActionTemplateRepository){
        this.velocityTemplateRepository = velocityTemplateRepository;
        this.serviceLevelAgreementActionTemplateRepository = serviceLevelAgreementActionTemplateRepository;
    }


    @Override
    public List<? extends ServiceLevelAgreementActionTemplate> findByVelocityTemplate(VelocityTemplate.ID id){
        return this.serviceLevelAgreementActionTemplateRepository.findByVelocityTemplate(id);
    }

    @Override
    public List<? extends ServiceLevelAgreementActionTemplate> findBySlaId(ServiceLevelAgreement.ID slaId) {
        return serviceLevelAgreementActionTemplateRepository.findByServiceLevelAgreement((ServiceLevelAgreementDescriptionId)slaId);
    }


    public List<? extends ServiceLevelAgreementActionTemplate> deleteForSlaId(ServiceLevelAgreement.ID slaId) {
        List<JpaServiceLevelAgreementActionTemplate> slaTemplates = serviceLevelAgreementActionTemplateRepository.findByServiceLevelAgreement((ServiceLevelAgreementDescriptionId)slaId);
        if (slaTemplates != null) {
            serviceLevelAgreementActionTemplateRepository.delete(slaTemplates);
        }
        return slaTemplates;
    }

    @Override
    public List<JpaServiceLevelAgreementActionTemplate> assignTemplate(ServiceLevelAgreementDescription sla, List<? extends VelocityTemplate> velocityTemplates) {

        List<JpaServiceLevelAgreementActionTemplate> slaTemplates = serviceLevelAgreementActionTemplateRepository.findByServiceLevelAgreement((ServiceLevelAgreementDescriptionId) sla.getSlaId());
        if (slaTemplates != null) {
            serviceLevelAgreementActionTemplateRepository.delete(slaTemplates);
        }
        if (velocityTemplates != null) {
            List<JpaServiceLevelAgreementActionTemplate> newTemplates = velocityTemplates.stream().map(t ->
                                                                                                       {
                                                                                                           JpaServiceLevelAgreementActionTemplate
                                                                                                               template =
                                                                                                               new JpaServiceLevelAgreementActionTemplate();
                                                                                                           template.setId(JpaServiceLevelAgreementActionTemplate.ServiceLevelAgreementActionTemplateId.create());
                                                                                                           template.setServiceLevelAgreementDescription((JpaServiceLevelAgreementDescription) sla);
                                                                                                           template.setVelocityTemplate((JpaVelocityTemplate) t);
                                                                                                           return template;
                                                                                                       }).collect(Collectors.toList());
            newTemplates = serviceLevelAgreementActionTemplateRepository.save(newTemplates);
            return newTemplates;
        }
        return Collections.emptyList();
    }

    public List<JpaServiceLevelAgreementActionTemplate> assignTemplateByIds(ServiceLevelAgreementDescription sla, Set<VelocityTemplate.ID> velocityTemplateIds){
        List<JpaVelocityTemplate> templates = null;
        if(velocityTemplateIds != null && !velocityTemplateIds.isEmpty()){
         templates = velocityTemplateRepository.findForIds(velocityTemplateIds);
        }
        return assignTemplate(sla,templates);

     }


    @Override
    public List<? extends ServiceLevelAgreementActionTemplate> findAll() {
        return serviceLevelAgreementActionTemplateRepository.findAll();
    }
}
