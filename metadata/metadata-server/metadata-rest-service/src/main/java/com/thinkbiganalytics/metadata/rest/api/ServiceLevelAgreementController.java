/**
 *
 */
package com.thinkbiganalytics.metadata.rest.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

/**
 *
 * @author Sean Felten
 */
@Component
@Path("/sla")
public class ServiceLevelAgreementController {

    private static final Logger log = LoggerFactory.getLogger(ServiceLevelAgreementController.class);

    @Inject
    private ServiceLevelAgreementProvider provider;

    @Inject ServiceLevelAssessor assessor;

    @Inject
    private MetadataAccess metadata;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceLevelAgreement createAgreement(ServiceLevelAgreement agreement) {
        log.debug("POST Create SLA {}", agreement);

        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement domainSla
                = Model.generateDomain(agreement, this.provider);

            return Model.DOMAIN_TO_SLA.apply(domainSla);
        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceLevelAgreement> getAgreements() {
        log.debug("GET all SLA's");

        return this.metadata.commit(() -> {
            List<com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement> list = this.provider.getAgreements();

            return Lists.transform(list, Model.DOMAIN_TO_SLA);
        });
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceLevelAgreement getAgreement(@PathParam("id") String idValue) {
        log.debug("GET SLA by ID: {}", idValue);

        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID id = this.provider.resolve(idValue);
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla = this.provider.getAgreement(id);

            if (sla != null) {
                return Model.DOMAIN_TO_SLA.apply(sla);
            } else {
                throw new WebApplicationException("No SLA with the given ID was found", Response.Status.NOT_FOUND);
            }
        });
    }

    @GET
    @Path("{id}/assessment")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceLevelAssessment assessAgreement(@PathParam("id") String idValue) {
        log.debug("GET SLA by ID: {}", idValue);

        return this.metadata.commit(() -> {
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID id = this.provider.resolve(idValue);
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement sla = this.provider.getAgreement(id);
            com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment assessment = this.assessor.assess(sla);

            return Model.DOMAIN_TO_SLA_ASSMT.apply(assessment);
        });
    }


}
