/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import org.springframework.http.ResponseEntity;

/**
 *
 * @author Sean Felten
 */
public class WebResponseException extends RuntimeException {
    
    private static final long serialVersionUID = -7888612717699269696L;
    
    private final ResponseEntity<Void> response;

    public WebResponseException(ResponseEntity<Void> resp) {
        this.response = resp;
    }

    public ResponseEntity<Void> getResponse() {
        return response;
    }
}
