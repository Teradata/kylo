package com.thinkbiganalytics.feedmgr.rest.controller;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.thinkbiganalytics.common.velocity.model.VelocityEmailTemplate;
import com.thinkbiganalytics.common.velocity.service.VelocityTemplateProvider;
import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementActionConfigTransformer;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementActionUiConfigurationItem;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementGroup;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementMetricTransformerHelper;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementModelTransform;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementRule;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementService;
import com.thinkbiganalytics.feedmgr.sla.SimpleServiceLevelAgreementDescription;
import com.thinkbiganalytics.feedmgr.sla.TestSlaVelocityEmail;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.api.sla.ServiceLevelAgreementActionTemplateProvider;
import com.thinkbiganalytics.metadata.api.sla.TestMetric;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAssessmentAlertUtil;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementEmailTemplate;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.SimpleServiceLevelAssessor;
import com.thinkbiganalytics.rest.model.LabelValue;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.beanvalidation.UUID;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.spring.SpringApplicationContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

import static com.thinkbiganalytics.feedmgr.rest.controller.ServiceLevelAgreementRestController.V1_FEEDMGR_SLA;

@Api(tags = "Feed Manager - SLA", produces = "application/json")
@Path(V1_FEEDMGR_SLA)
@Component
@SwaggerDefinition(tags = @Tag(name = "Feed Manager - SLA", description = "service level agreements"))
public class ServiceLevelAgreementRestController {

    private static final Logger log = LoggerFactory.getLogger(ServiceLevelAgreementRestController.class);
    public static final String V1_FEEDMGR_SLA = "/v1/feedmgr/sla";

    @Inject
    ServiceLevelAgreementService serviceLevelAgreementService;
    @Inject
    ServiceLevelAssessor assessor;
    @Inject
    private ServiceLevelAgreementProvider provider;
    @Inject
    private FeedServiceLevelAgreementProvider feedSlaProvider;
    @Inject
    private JcrMetadataAccess metadata;
    @Inject
    private MetadataAccess metadataAccess;
    @Inject
    private ServiceLevelAgreementModelTransform serviceLevelAgreementTransform;

    @Inject
    private AccessController accessController;

    @Inject
    private VelocityTemplateProvider velocityTemplateProvider;

    @Inject
    private ServiceLevelAgreementActionTemplateProvider serviceLevelAgreementActionTemplateProvider;

    @GET
    @Path("/available-metrics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available metrics.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the metrics.", response = ServiceLevelAgreementRule.class, responseContainer = "List")
    )
    public Response getAvailableSLAMetrics() {
        return Response.ok(serviceLevelAgreementService.discoverSlaMetrics()).build();
    }

    @GET
    @Path("/available-responders")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of available responders.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the responders.", response = ServiceLevelAgreementActionUiConfigurationItem.class, responseContainer = "List")
    )
    public Response getAvailableSLAResponders() {
        return Response.ok(serviceLevelAgreementService.discoverActionConfigurations()).build();
    }

    /**
     * Save the General SLA coming in from the UI that is not related to a Feed
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Saves the specified SLA.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the SLA.", response = ServiceLevelAgreementGroup.class),
                      @ApiResponse(code = 500, message = "The SLA could not be saved.", response = RestResponseStatus.class)
                  })
    public Response saveSla(ServiceLevelAgreementGroup sla) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS);
        ServiceLevelAgreement serviceLevelAgreement = serviceLevelAgreementService.saveAndScheduleSla(sla);
        ServiceLevelAgreementMetricTransformerHelper helper = new ServiceLevelAgreementMetricTransformerHelper();
        ServiceLevelAgreementGroup serviceLevelAgreementGroup = helper.toServiceLevelAgreementGroup(serviceLevelAgreement);
        return Response.ok(serviceLevelAgreementGroup).build();
    }

    /**
     * Save an SLA and attach the ref to the Feed
     */
    @POST
    @Path("/feed/{feedId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Saves the SLA and attaches it to the specified feed.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the SLA.", response = ServiceLevelAgreementGroup.class),
                      @ApiResponse(code = 400, message = "The feedId is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The SLA could not be saved.", response = RestResponseStatus.class)
                  })
    public Response saveAndScheduleFeedSla(@UUID @PathParam("feedId") String feedId, ServiceLevelAgreementGroup sla) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS);
        ServiceLevelAgreement serviceLevelAgreement = serviceLevelAgreementService.saveAndScheduleFeedSla(sla, feedId);
        ServiceLevelAgreementMetricTransformerHelper helper = new ServiceLevelAgreementMetricTransformerHelper();
        ServiceLevelAgreementGroup serviceLevelAgreementGroup = helper.toServiceLevelAgreementGroup(serviceLevelAgreement);
        return Response.ok(serviceLevelAgreementGroup).build();
    }


    /**
     * Delete an SLA
     */
    @DELETE
    @Path("/{slaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Deletes the specified SLA.")
    @ApiResponses({
                      @ApiResponse(code = 204, message = "The SLA has been deleted."),
                      @ApiResponse(code = 400, message = "The slaId is not a valid UUID.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The SLA could not be deleted.", response = RestResponseStatus.class)
                  })
    public Response deleteSla(@UUID @PathParam("slaId") String slaId) throws InvalidOperationException {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENTS);
        boolean removed = serviceLevelAgreementService.removeAndUnscheduleAgreement(slaId);
        if (!removed) {
            throw new WebApplicationException("Unable to remove the SLA");
        } else {
            return Response.ok().build();
        }
    }


    @GET
    @Path("/feed")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation("Gets all SLAs related to any feed.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the SLAs.", response = FeedServiceLevelAgreement.class, responseContainer = "List")
    )
    public Response getAllSlas() {
        List<FeedServiceLevelAgreement> agreementList = serviceLevelAgreementService.getServiceLevelAgreements();
        if (agreementList == null) {
            agreementList = new ArrayList<>();
        }
        return Response.ok(agreementList).build();
    }

    @GET
    @Path("/{slaId}/form-object")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the form for editing the specified SLA.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the SLA form.", response = ServiceLevelAgreementGroup.class),
                      @ApiResponse(code = 400, message = "The slaId is not a valid UUID.", response = RestResponseStatus.class)
                  })
    public Response getSlaAsForm(@UUID @PathParam("slaId") String slaId) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
        ServiceLevelAgreementGroup agreement = serviceLevelAgreementService.getServiceLevelAgreementAsFormObject(slaId);

        return Response.ok(agreement).build();
    }


    @GET
    @Path("/action/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Validates the specified action configuration.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the validation.", response = ServiceLevelAgreementActionValidation.class)
    )
    public Response validateAction(@QueryParam("actionConfigClass") String actionConfigClass) {

        List<ServiceLevelAgreementActionValidation> validation = serviceLevelAgreementService.validateAction(actionConfigClass);
        return Response.ok(validation).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets all SLAs.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the SLAs.", response = FeedServiceLevelAgreement.class, responseContainer = "List")
    )
    public List<FeedServiceLevelAgreement> getAgreements() {
        log.debug("GET all SLA's");
        return serviceLevelAgreementService.getServiceLevelAgreements();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified SLA.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the SLA.", response = ServiceLevelAgreement.class),
                      @ApiResponse(code = 404, message = "The SLA could not be found.", response = RestResponseStatus.class)
                  })
    public ServiceLevelAgreement getAgreement(@PathParam("id") String idValue) {
        log.debug("GET SLA by ID: {}", idValue);
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_SERVICE_LEVEL_AGREEMENTS);
        ServiceLevelAgreement serviceLevelAgreement = serviceLevelAgreementService.getServiceLevelAgreement(idValue);
        if (serviceLevelAgreement == null) {
            throw new WebApplicationException("No SLA with the given ID was found", Response.Status.NOT_FOUND);
        } else {
            return serviceLevelAgreement;
        }
    }

    @GET
    @Path("{id}/assessment")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the specified assessment.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the assessment.", response = ServiceLevelAgreement.class),
                      @ApiResponse(code = 400, message = "The assessment could not be found.", response = RestResponseStatus.class)
                  })
    public ServiceLevelAssessment assessAgreement(@PathParam("id") String idValue) {
        log.debug("GET SLA by ID: {}", idValue);

        return this.metadata.read(() -> {
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID id = this.provider.resolve(idValue);
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla = this.provider.getAgreement(id);
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assessment = this.assessor.assess(sla);

            return ServiceLevelAgreementModelTransform.DOMAIN_TO_SLA_ASSMT.apply(assessment);
        });
    }

    @GET
    @Path("/email-template")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of email templates.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the list of email templates.", response = ServiceLevelAgreementEmailTemplate.class, responseContainer = "List")
                  })
    public List<ServiceLevelAgreementEmailTemplate> getServiceLevelAgreementEmailTemplates() {
        return serviceLevelAgreementService.getServiceLevelAgreementEmailTemplates();
    }

    @GET
    @Path("/available-sla-template-actions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the available SLA actions.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the list of SLA actions used when creating/editing an SLA Email Template.", response = LabelValue.class, responseContainer = "List")
                  })
    public Set<LabelValue> getAvailableServiceLevelAgreementTemplateActionClasses() {

        List<ServiceLevelAgreementActionUiConfigurationItem> actionItems = ServiceLevelAgreementActionConfigTransformer.instance().discoverActionConfigurations();
        Set<LabelValue> set = new HashSet<>();
        actionItems.stream().forEach(a -> {
            a.getActionClasses().stream().forEach(c -> {
                LabelValue labelValue = new LabelValue(a.getDisplayName(), c.getName());
                set.add(labelValue);
            });

        });
        return set;
    }

    @GET
    @Path("/email-template-sla-references")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the SLA's related to this email template.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the list of SLA's related to the given templateId.", response = SimpleServiceLevelAgreementDescription.class, responseContainer = "List")
                  })
    public List<SimpleServiceLevelAgreementDescription> getSlaReferencesForVelocityTemplate(@QueryParam("templateId") String velocityTemplateId) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE);
        return serviceLevelAgreementService.getSlaReferencesForVelocityTemplate(velocityTemplateId);
    }


    /**
     * Save the template
     */
    @POST
    @Path("/email-template")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Saves the specified SLA.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Save the SLA email template.", response = ServiceLevelAgreementEmailTemplate.class),
                      @ApiResponse(code = 500, message = "The SLA could not be saved.", response = RestResponseStatus.class)
                  })
    public ServiceLevelAgreementEmailTemplate saveEmailTemplate(ServiceLevelAgreementEmailTemplate emailTemplate) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE);
        try {
            return serviceLevelAgreementService.saveEmailTemplate(emailTemplate);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }

    @POST
    @Path("/test-email-template")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Tests a velocity SLA email template.  This will validate and return the parsed template allowing you to preview it.  If an parse exception occurs it will be indicated in the content of the response.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "The the SLA email template.", response = VelocityEmailTemplate.class)
                  })
    public VelocityEmailTemplate testTemplate(VelocityEmailTemplate template) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE);
        Map<String, Object> map = new HashMap();
        InMemorySLAProvider slaProvider = new InMemorySLAProvider();
        Metric m1 = new SimpleMetric();

        com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla = slaProvider.builder()
            .name("Test SLA")
            .description("SLA Description")
            .obligationBuilder(ObligationGroup.Condition.REQUIRED)
            .metric(m1)
            .build()
            .build();
        map.put("sla", sla);
        SimpleServiceLevelAssessor simpleServiceLevelAssessor = new SimpleServiceLevelAssessor();
        simpleServiceLevelAssessor.registerMetricAssessor(new TestMetricAssessor());

        com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assessment = simpleServiceLevelAssessor.assess(sla);
        map.put("assessment", assessment);
        map.put("assessmentDescription", ServiceLevelAssessmentAlertUtil.getDescription(assessment, "<br/>"));

        String subject = velocityTemplateProvider.testTemplate(template.getSubject(), map);
        String body = velocityTemplateProvider.testTemplate(template.getBody(), map);

        return new VelocityEmailTemplate(subject, body);

    }

    @POST
    @Path("/send-test-email-template")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Test sending the SLA email template.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Test and send the email template to a test address.  If unable to send or an error occurs it will be indicated in the response.", response = VelocityEmailTemplate.class)
                  })
    public VelocityEmailTemplate sendTestTemplate(TestSlaVelocityEmail template) {
        accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE);
        VelocityEmailTemplate parsedTemplate = testTemplate(template);
        String subject = parsedTemplate.getSubject();
        String body = parsedTemplate.getBody();

        TestSlaVelocityEmail testSlaVelocityEmail = new TestSlaVelocityEmail(subject, body, template.getEmailAddress());

        //if we have the plugin then send it
        try {
            Object emailService = SpringApplicationContext.getBean("slaEmailService");
            if (emailService != null) {
                MethodUtils.invokeMethod(emailService, "sendMail", template.getEmailAddress(), subject, body);
                testSlaVelocityEmail.setSuccess(true);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            Throwable root = ExceptionUtils.getRootCause(e);
            if (root != null) {
                message = root.getMessage();
            }
            log.error("unable to send preview/test email for SLA template {} ", message, e);
            testSlaVelocityEmail.setExceptionMessage(message);
        }
        return testSlaVelocityEmail;


    }

    private class TestMetricAssessor implements MetricAssessor<SimpleMetric, Serializable> {

        @Override
        public boolean accepts(Metric metric) {
            return metric instanceof SimpleMetric;
        }

        @Override
        public void assess(SimpleMetric metric, MetricAssessmentBuilder<Serializable> builder) {
            builder.metric(metric);
            builder.message("Test Assessment ")
                .result(AssessmentResult.FAILURE);

        }
    }

    private class SimpleMetric extends TestMetric {

        private String description;

        public SimpleMetric() {
            this.description = "this is my metric desc";
        }

        public SimpleMetric(AssessmentResult result) {
            super(result);
        }

        public SimpleMetric(AssessmentResult result, String msg) {
            super(result, msg);
        }

        @Override
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
