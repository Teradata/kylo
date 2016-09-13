package com.thinkbiganalytics.datalake.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HadoopAuthorizationDemo class used to invoke Ranger/Sentry authorization Service.
 *
 * @author sv186013
 */

public class HadoopAuthorizationDemo {

    private static final Logger log = LoggerFactory.getLogger(HadoopAuthorizationDemo.class);

    //static HadoopAuthorizationFactory hadoopAuthorizationFactory;
    //static HadoopAuthorizationService hadoopAuthorizationServiceObj;


    public static void main(String args[]) throws IOException, Exception {

       /* // Initialize Sentry Authorization Service
        hadoopAuthorizationFactory = new HadoopAuthorizationFactory();
        hadoopAuthorizationServiceObj = hadoopAuthorizationFactory.authorizationType("sentry");

        if (hadoopAuthorizationServiceObj == null) {
            System.out.println("No proper authorization service provided");
        } else {
            hadoopAuthorizationServiceObj.initiateAuthorizationService();
        }
*/
    }
}


