package com.thinkbiganalytics.auth;


/**
 * Simple Auth to allow a plugin to provide auth via a username/password
 * SimpleAuthenticationService is an example impl
 * @see SimpleAuthenticationService
 */
public interface AuthenticationService {

    public boolean authenticate(String username, String password);
}
