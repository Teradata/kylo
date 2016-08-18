package com.thinkbiganalytics.nifi.rest.config;

import com.thinkbiganalytics.nifi.rest.client.NifiRestClientExceptionTranslator;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Created by sr186054 on 6/20/16.
 */
@Aspect
public class NifiRestClientAroundAspect {


    @Around("execution(* com.thinkbiganalytics.nifi.rest.client.NifiRestClient.*(..))")
    public Object NifiRestClientAroundAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object obj = joinPoint.proceed();
            return obj;
        } catch (Throwable ex) {
            ex = NifiRestClientExceptionTranslator.translateException(ex);
            throw ex;
        }
    }

}
