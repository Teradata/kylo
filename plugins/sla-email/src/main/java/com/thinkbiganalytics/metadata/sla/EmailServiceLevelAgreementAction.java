package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-sla-email
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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.classnameregistry.ClassNameChange;
import com.thinkbiganalytics.common.velocity.model.VelocityEmailTemplate;
import com.thinkbiganalytics.common.velocity.model.VelocityTemplate;
import com.thinkbiganalytics.common.velocity.service.VelocityTemplateProvider;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAssessmentAlertUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementEmailTemplate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Email the users specified in the incoming Configuration class about the SLA violation
 */
@ClassNameChange(classNames = {"com.thinkbiganalytics.metadata.sla.alerts.EmailServiceLevelAgreementAction"})
public class EmailServiceLevelAgreementAction implements ServiceLevelAgreementAction<EmailServiceLevelAgreementActionConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceLevelAgreementAction.class);
    @Inject
    private SlaEmailService emailService;

    @Inject
    private VelocityTemplateProvider velocityTemplateProvider;

    public boolean respondx(EmailServiceLevelAgreementActionConfiguration actionConfiguration, ServiceLevelAssessment assessment, Alert a) {
        log.info("Responding to SLA violation.");
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(assessment);
        String slaName = assessment.getAgreement().getName();
        String emails = actionConfiguration.getEmailAddresses();
        sendToAddresses(desc, slaName, emails);
        return true;
    }

    @Override
    public boolean respond(EmailServiceLevelAgreementActionConfiguration actionConfiguration, ServiceLevelAssessment assessment, Alert a) {
        log.info("Responding to SLA violation {}. for alert {} received from: {} ",assessment.getServiceLevelAgreementDescription().getName(),a.getId(), a.getSource());
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(assessment);
        String slaName = assessment.getAgreement().getName();
        String emails = actionConfiguration.getEmailAddresses();
        String[] addresses = emails.split(",");
        String subject = "SLA Violated: " + slaName;
        String body = desc;

        VelocityEmailTemplate emailTemplate = parseVelocityTemplate(actionConfiguration,assessment,a);
        if(emailTemplate == null)             {
            body = desc;
        }
        else {
            subject = emailTemplate.getSubject();
            body = emailTemplate.getBody();
        }
        final String finalSubject = subject;
        final String finalBody = body;
        log.info("sending {}  email to: {}",assessment.getServiceLevelAgreementDescription().getName(),addresses);
        Arrays.stream(addresses).forEach(address ->{
            emailService.sendMail(address.trim(), finalSubject, finalBody);
        });

        return true;
    }

    void sendToAddresses(String desc, String slaName, String emails) {
        String[] addresses = emails.split(",");
        Arrays.stream(addresses).forEach(address -> sendToAddress(desc, slaName, address.trim()));
    }

    private void sendToAddress(String desc, String slaName, String address) {
        log.info("Responding to SLA violation.  About to send an email for SLA {} ", slaName);
        emailService.sendMail(address, "SLA Violated: " + slaName, desc);
    }

    private VelocityEmailTemplate parseVelocityTemplate(EmailServiceLevelAgreementActionConfiguration actionConfiguration, ServiceLevelAssessment assessment, Alert a) {
        Map<String,Object> map = new HashMap();
        map.put("alert",a);
        map.put("assessment",assessment);
        map.put("assessmentDescription",ServiceLevelAssessmentAlertUtil.getDescription(assessment,"<br/>"));
        map.put("slaName",assessment.getAgreement().getName());
        map.put("sla",assessment.getAgreement());
        String template = actionConfiguration.getVelocityTemplateId();
         if(StringUtils.isNotBlank(template)) {
             VelocityTemplate defaultTemplate = velocityTemplateProvider.findDefault(ServiceLevelAgreementEmailTemplate.EMAIL_TEMPLATE_TYPE);
             VelocityEmailTemplate defaultEmailTemplate = null;
             if(defaultTemplate != null){
                 defaultEmailTemplate = new VelocityEmailTemplate(defaultTemplate.getTitle(),defaultTemplate.getTemplate());
             }
             else {
                 defaultEmailTemplate = emailService.getDefaultTemplate();
             }

            return velocityTemplateProvider.mergeEmailTemplate(template, map, defaultEmailTemplate);
         }
         return null;
    }

    /**
     * Validate to ensure there is a configuration setup for the email
     *
     * @return a validation object containing information if the configuration is valid
     */
    public ServiceLevelAgreementActionValidation validateConfiguration() {

        if (emailService.isConfigured()) {
            return ServiceLevelAgreementActionValidation.VALID;
        } else {
            return new ServiceLevelAgreementActionValidation(false, "Email connection information is not setup.  Please contact an administrator to set this up.");
        }
    }

    public void setEmailService(SlaEmailService emailService) {
        this.emailService = emailService;
    }


}
