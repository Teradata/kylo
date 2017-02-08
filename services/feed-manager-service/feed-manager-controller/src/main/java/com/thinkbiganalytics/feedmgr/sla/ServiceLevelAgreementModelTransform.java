package com.thinkbiganalytics.feedmgr.sla;

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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transforms to/from  Domain/Rest
 */
public class ServiceLevelAgreementModelTransform {


    public static final Function<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> DOMAIN_TO_FEED_SLA
        = new Function<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement>() {
        @Override
        public com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement apply(com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement domain) {
            return toModel(domain, true);
        }
    };

    public static final Function<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> DOMAIN_TO_FEED_SLA_SHALLOW
        = new Function<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement>() {
        @Override
        public com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement apply(com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement domain) {
            return toModel(domain, false);
        }
    };
    public static final Function<ServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement> DOMAIN_TO_SLA
        = new Function<ServiceLevelAgreement, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement>() {
        @Override
        public com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement apply(ServiceLevelAgreement domain) {
            return toModel(domain, true);
        }
    };
    public static final Function<ServiceLevelAssessment, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment> DOMAIN_TO_SLA_ASSMT
        = new Function<ServiceLevelAssessment, com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment>() {
        @Override
        public com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment apply(ServiceLevelAssessment domain) {
            com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement sla = toModel(domain.getAgreement(), false);

            com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment slAssmt
                = new com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment(sla,
                                                                                           domain.getTime(),
//                                                                                               null,
                                                                                           domain.getMessage(),
                                                                                           com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment.Result
                                                                                               .valueOf(domain.getResult().name()));
            for (ObligationAssessment domainObAssmt : domain.getObligationAssessments()) {
                com.thinkbiganalytics.metadata.rest.model.sla.ObligationAssessment obAssmt
                    = new com.thinkbiganalytics.metadata.rest.model.sla.ObligationAssessment(toModel(domainObAssmt.getObligation(), false),
                                                                                             com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment.Result
                                                                                                 .valueOf(domain.getResult().name()),
                                                                                             domainObAssmt.getMessage());
                for (MetricAssessment<?> domainMetAssmt : domainObAssmt.getMetricAssessments()) {
                    com.thinkbiganalytics.metadata.rest.model.sla.MetricAssessment metricAssmnt
                        = new com.thinkbiganalytics.metadata.rest.model.sla.MetricAssessment(domainMetAssmt.getMetric(),
                                                                                             com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment.Result
                                                                                                 .valueOf(domain.getResult().name()),
                                                                                             domainMetAssmt.getMessage());
                    obAssmt.addMetricAssessment(metricAssmnt);
                }

                slAssmt.addObligationAssessment(obAssmt);
            }

            return slAssmt;
        }
    };

    public static final List<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> transformFeedServiceLevelAgreements(
        List<com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement> slaList) {
        Collection<com.thinkbiganalytics.metadata.rest.model.sla.FeedServiceLevelAgreement> list = null;
        if (slaList != null) {
            list = Collections2.transform(slaList, DOMAIN_TO_FEED_SLA_SHALLOW);
            return new ArrayList<>(list);
        }
        return null;
    }

    public static ServiceLevelAgreement generateDomain(com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement model,
                                                       ServiceLevelAgreementProvider provider) {
        ServiceLevelAgreementBuilder slaBldr = provider.builder()
            .name(model.getName())
            .description(model.getDescription());

        for (com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup grp : model.getGroups()) {
            ObligationGroupBuilder grpBldr = slaBldr.obligationGroupBuilder(ObligationGroup.Condition.valueOf(grp.getCondition()));

            for (com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob : grp.getObligations()) {
                ObligationBuilder<?> obBldr = grpBldr.obligationBuilder().description(ob.getDescription());

                for (com.thinkbiganalytics.metadata.sla.api.Metric metric : ob.getMetrics()) {
                    obBldr.metric(metric);
                }

                obBldr.build();
            }

            grpBldr.build();
        }

        return slaBldr.build();
    }


    public static com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement toModel(ServiceLevelAgreement domain, boolean deep) {

        com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement sla
            = new com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement(domain.getId().toString(),
                                                                                      domain.getName(),
                                                                                      domain.getDescription());
        if (domain.getSlaChecks() != null) {
            List<ServiceLevelAgreementCheck> checks = new ArrayList<>();
            sla.setSlaChecks(checks);
            for (com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck check : domain.getSlaChecks()) {
                ServiceLevelAgreementCheck restModel = new ServiceLevelAgreementCheck();
                restModel.setCronSchedule(check.getCronSchedule());
                if (deep) {
                    try {
                        restModel.setActionConfigurations(check.getActionConfigurations());
                    } catch (Exception e) {
                        if (ExceptionUtils.getRootCause(e) instanceof ClassNotFoundException) {
                            String msg = ExceptionUtils.getRootCauseMessage(e);
                            //get just the simpleClassName stripping the package info
                            msg = StringUtils.substringAfterLast(msg, ".");
                            sla.addSlaCheckError("Unable to find the SLA Action Configurations of type: " + msg
                                                 + ". Check with an administrator to ensure the correct plugin is installed with this SLA configuration. ");
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                }
                checks.add(restModel);
            }
        }

        if (deep) {
            if (domain.getObligationGroups().size() == 1 && domain.getObligationGroups().get(0).getCondition() == ObligationGroup.Condition.REQUIRED) {
                for (Obligation domainOb : domain.getObligations()) {
                    com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob = toModel(domainOb, true);
                    sla.addObligation(ob);
                }
            } else {
                for (ObligationGroup domainGroup : domain.getObligationGroups()) {
                    com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup group
                        = new com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup(domainGroup.getCondition().toString());
                    for (Obligation domainOb : domainGroup.getObligations()) {
                        com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob = toModel(domainOb, true);
                        group.addObligation(ob);
                    }

                    sla.addGroup(group);
                }
            }
        }

        return sla;
    }


    public static FeedServiceLevelAgreement toModel(com.thinkbiganalytics.metadata.api.sla.FeedServiceLevelAgreement domain, boolean deep) {
        return toModel(domain, (Set<com.thinkbiganalytics.metadata.api.feed.Feed>) domain.getFeeds(), deep);
    }

    public static FeedServiceLevelAgreement toModel(ServiceLevelAgreement domain, Set<com.thinkbiganalytics.metadata.api.feed.Feed> feeds, boolean deep) {
        com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement slaModel = toModel(domain, deep);
        FeedServiceLevelAgreement feedServiceLevelAgreement = new FeedServiceLevelAgreement(slaModel);
        if (feeds != null && !feeds.isEmpty()) {
            final Set<Feed> feedModels = feeds.stream()
                .filter(feed -> feed != null)
                .map(Model.DOMAIN_TO_FEED::apply)
                .collect(Collectors.toSet());
            feedServiceLevelAgreement.setFeeds(feedModels);
        }
        return feedServiceLevelAgreement;
    }

    public static com.thinkbiganalytics.metadata.rest.model.sla.Obligation toModel(Obligation domainOb, boolean deep) {
        com.thinkbiganalytics.metadata.rest.model.sla.Obligation ob
            = new com.thinkbiganalytics.metadata.rest.model.sla.Obligation();
        ob.setDescription(domainOb.getDescription());
        if (deep) {
            ob.setMetrics(Lists.newArrayList(domainOb.getMetrics()));
        }
        return ob;
    }


}
