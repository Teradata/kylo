package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * kylo-sla-email
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

import org.junit.Test;
import org.mockito.Mockito;

public class EmailServiceLevelAgreementActionTest {

    @Test
    public void testSendMultiple() throws Exception {
        EmailServiceLevelAgreementAction action = new EmailServiceLevelAgreementAction();
        SlaEmailService service = Mockito.mock(SlaEmailService.class);
        action.setEmailService(service);
        action.sendToAddresses("desc", "sla name", "a@a.com, b@b.com,c@c.com");

        Mockito.verify(service).sendMail("a@a.com", "SLA Violated: sla name", "desc");
        Mockito.verify(service).sendMail("b@b.com", "SLA Violated: sla name", "desc");
        Mockito.verify(service).sendMail("c@c.com", "SLA Violated: sla name", "desc");

    }

    @Test
    public void testSendSingle() throws Exception {
        EmailServiceLevelAgreementAction action = new EmailServiceLevelAgreementAction();
        SlaEmailService service = Mockito.mock(SlaEmailService.class);
        action.setEmailService(service);
        action.sendToAddresses("desc", "sla name", "a@a.com");

        Mockito.verify(service).sendMail("a@a.com", "SLA Violated: sla name", "desc");

    }

}
