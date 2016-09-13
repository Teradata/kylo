package com.thinkbiganalytics.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

/**
 * Created by Jeremy Merrifield on 8/23/16.
 *
 * Currently not used. We may not need this after all since it looks like the login method in KerberosTicketGenerator does auto renewal
 */
@Component
public class KerberosTicketRenewer implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(KerberosTicketRenewer.class);

    private AtomicBoolean applicationStarted = new AtomicBoolean(true);
    private Timer checkAndRenewKerberosTicket;

    @PostConstruct
    public void KerberosTicketTimer() {
        /*log.info("Initializing the Kerberos Ticket Renewer Class");
        try {
            final String user = "thinkbig";
            final String keyPath = "/etc/security/keytabs/thinkbig.headless.keytab";
            KerberosTicketGenerator t = new KerberosTicketGenerator();
            t.generateKerberosTicket(user, keyPath);
        } catch (Exception e) {
            log.error("Error with kerberos authentication jeremy");

            throw new RuntimeException(e);
        }
        log.info("Kerbers ticket granted for thinkbig user jeremy");
        checkAndRenewKerberosTicket = new Timer();
        checkAndRenewKerberosTicket.schedule(new CheckAndRenewKerberosTicket(), 0, 360 * 1000);
        */
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        applicationStarted.set(true);
    }

    class CheckAndRenewKerberosTicket extends TimerTask {

        private final KerberosTicketRenewerRunnable secRunnable = new KerberosTicketRenewerRunnable();

        @Override
        public void run() {
            this.secRunnable.run();
        }
    }

}
