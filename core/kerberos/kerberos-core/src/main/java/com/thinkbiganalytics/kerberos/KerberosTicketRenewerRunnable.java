package com.thinkbiganalytics.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jeremy Merrifield on 8/24/16.
 *
 * Currently not used. We may not need this after all since it looks like the login method in KerberosTicketGenerator does auto renewal
 */
public class KerberosTicketRenewerRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(KerberosTicketRenewerRunnable.class);

    public KerberosTicketRenewerRunnable() {

    }

    @Override
    public void run() {
        log.info("I am in the run class jeremy");
    }

}
