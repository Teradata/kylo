package com.thinkbiganalytics.nifi.rest.client;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import java.net.ConnectException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;

/**
 * Created by sr186054 on 6/19/16.
 */
public class NifiRestClientExceptionTranslator {


    public static Throwable translateException(Throwable e) {

        //Return if its already translated to a NifiClientRuntimeException
        if (e instanceof NifiClientRuntimeException) {
            return e;
        }
        if (e instanceof NotFoundException) {
            return new NifiComponentNotFoundException(e.getMessage());
        } else if (e instanceof NullPointerException) {
            return new NifiConnectionException("Verify NiFi is running and try again", e);
        } else if (e instanceof ProcessingException) {
            int throwables = ExceptionUtils.getThrowableCount(e);
            if (throwables > 1) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof NoHttpResponseException || rootCause instanceof HttpHostConnectException || rootCause instanceof ConnectException) {
                    //connection error
                    return new NifiConnectionException(e.getMessage(), e);

                }
            }
        }
        return new NifiClientRuntimeException(e);
    }

}


