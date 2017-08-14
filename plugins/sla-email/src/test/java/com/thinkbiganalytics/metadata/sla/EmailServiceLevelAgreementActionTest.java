package com.thinkbiganalytics.metadata.sla;

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