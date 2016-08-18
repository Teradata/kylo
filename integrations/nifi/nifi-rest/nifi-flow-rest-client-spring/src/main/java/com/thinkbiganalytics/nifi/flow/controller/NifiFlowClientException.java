/**
 *
 */
package com.thinkbiganalytics.nifi.flow.controller;

import org.springframework.http.ResponseEntity;

/**
 * @author Sean Felten
 */
public class NifiFlowClientException extends RuntimeException {


    private final ResponseEntity<Void> response;

    public NifiFlowClientException(ResponseEntity<Void> resp) {
        this.response = resp;
    }

    public ResponseEntity<Void> getResponse() {
        return response;
    }
}
