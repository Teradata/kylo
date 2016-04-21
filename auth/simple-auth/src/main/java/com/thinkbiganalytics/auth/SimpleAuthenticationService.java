package com.thinkbiganalytics.auth;

import java.util.Arrays;

/**
 * Simple Auth to compare passed in Username and Password
 * username and passwords are passed in via application.properties
 */
public class SimpleAuthenticationService implements AuthenticationService {


    protected String username;

    protected char[] password;

    public  boolean authenticate(String username, String password) {
        if(username.equalsIgnoreCase(this.username) && Arrays.equals(password.toCharArray(), this.password)){
            return true;
        }
        return false;
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
