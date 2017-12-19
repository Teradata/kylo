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

import com.thinkbiganalytics.common.velocity.config.VelocitySpringConfiguration;
import com.thinkbiganalytics.common.velocity.service.VelocityService;
import com.thinkbiganalytics.metadata.api.sla.TestMetric;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementDescription;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.config.DeveloperEmailConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * Test Email settings.
 * Currently set to ignore as email settings are needed to be configured on the server to ensure connection to the valid email server.
 * Users can use this as a base test and update the {@link TestConfiguration} class with the proper connection information
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, DeveloperEmailConfiguration.class, VelocitySpringConfiguration.class})
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@ActiveProfiles("developer.email")
@Ignore
public class TestEmail {


    @Inject
    EmailServiceLevelAgreementAction emailServiceLevelAgreementAction;

    @Inject
    private VelocityService velocityService;


    private ServiceLevelAssessment serviceLevelAssessment() {
        return new ServiceLevelAssessment() {
            @Override
            public ID getId() {
                return null;
            }

            @Override
            public ServiceLevelAgreement.ID getServiceLevelAgreementId() {
                return null;
            }

            @Override
            public DateTime getTime() {
                return new DateTime();
            }

            @Override
            public ServiceLevelAgreementDescription getServiceLevelAgreementDescription() {
                return new ServiceLevelAgreementDescription() {
                    @Override
                    public ServiceLevelAgreement.ID getSlaId() {
                        return null;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                };
            }

            @Override
            public ServiceLevelAgreement getAgreement() {
                return new ServiceLevelAgreement() {
                    @Override
                    public ID getId() {
                        return null;
                    }

                    @Override
                    public String getName() {
                        return "SLA Name";
                    }

                    @Override
                    public DateTime getCreatedTime() {
                        return new DateTime();
                    }

                    @Override
                    public String getDescription() {
                        return "SLA Desc";
                    }

                    @Override
                    public List<ObligationGroup> getObligationGroups() {
                        return null;
                    }

                    @Override
                    public List<Obligation> getObligations() {
                        return null;
                    }
                    
                    @Override
                    public Set<Metric> getAllMetrics() {
                        return null;
                    }

                    @Override
                    public List<ServiceLevelAgreementCheck> getSlaChecks() {
                        return null;
                    }

                    @Override
                    public boolean isEnabled() {
                        return false;
                    }
                };
            }

            @Override
            public String getMessage() {
                return "Test Assessment";
            }

            @Override
            public AssessmentResult getResult() {
                return AssessmentResult.FAILURE;
            }

            @Override
            public Set<ObligationAssessment> getObligationAssessments() {
                return null;
            }

            @Override
            public int compareTo(ServiceLevelAssessment o) {
                return 0;
            }
        };
    }


    @Test
    public void testSlaEmail() {
        String email = "scott.reisdorf@thinkbiganalytics.com";
        String template = getTemplate();
        velocityService.registerEmailTemplate("test","SLA Violation $sla.name ",template);
        EmailServiceLevelAgreementActionConfiguration emailServiceLevelAgreementActionConfiguration = new EmailServiceLevelAgreementActionConfiguration(email);
        emailServiceLevelAgreementActionConfiguration.setVelocityTemplateId("test");
        emailServiceLevelAgreementAction.respond(emailServiceLevelAgreementActionConfiguration, serviceLevelAssessment(), null);

    }

    private String getTemplate(){

       return
        "<html>\n"
        +" <body>"
        + "<table>\n"
        + "\n"
        + "        <tr>\n"
        + "\n"
        + "               <td align=\"center\" style=\"background-color:rgb(119, 119, 119);\"><img src=\"cid:kylo-logo\"></td>\n"
        + "\n"
        + "        </tr>\n"
        + "\n"
        + "        <tr>\n"
        + "\n"
        + "               <td>\n"
        + "\n"
        + "                   <table>\n"
        + "\n"
        + "                       <tr>\n"
        + "                               <td>$sla.name</td>\n"
        + "                       </tr>"
        + "                       <tr> "
        + "                               <td>$sla.description</td>\n"
        + "                       </tr>\n"
        + "                       <tr><td colspan=\"2\">$description</td></tr> "
        + "\n"
        + "                   </table>\n"
        + "\n"
        + "               </td>\n"
        + "\n"
        + "        </tr>\n"
        + "\n"
        + "</table>"
        + "</body>"
        + "</html>";
    }

    @Test
    public void testVelocityEmail() {
        String email = "thinkbig.tester@gmail.com";
        String template = getTemplate();

        velocityService.registerTemplate("test",template);
        Map<String,Object> map = new HashMap();
        InMemorySLAProvider slaProvider = new InMemorySLAProvider();
        Metric m1 = new SimpleMetric();

        ServiceLevelAgreement sla = slaProvider.builder()
            .name("Test SLA")
            .description("Test SLA desc")
            .obligationBuilder(ObligationGroup.Condition.REQUIRED)
            .metric(m1)
            .build()
            .build();
        map.put("sla",sla);
        String emailBody = velocityService.mergeTemplate("test",map);
        Assert.assertEquals("This is my test Test SLA and description: Test SLA desc. \n"
                            + "            Metric: this is my metric desc \n"
                            + "       ",emailBody);
        int i = 0;
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


