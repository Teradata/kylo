package com.thinkbiganalytics.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * AuthProvider that delegates to the AuthenticationService
 * <p>
 * Note: AuthServiceLoginModule has replaced direct usage of this provider in most cases except for testing, etc.
 */
public class AuthServiceAuthenticationProvider implements AuthenticationProvider {

    public AuthServiceAuthenticationProvider() {
    }
    
    @Autowired
    @Qualifier("authenticationService")
    private AuthenticationService authenticationService;

        @Override
        public Authentication authenticate(Authentication authentication)
                throws AuthenticationException {
            String name = authentication.getName();
            String password = authentication.getCredentials().toString();

            if (authenticationService.authenticate(name,password)) {
                List<GrantedAuthority> grantedAuths = new ArrayList<>();
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
                return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);
            } else {
                return null;
            }
        }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
        public boolean supports(Class<?> authentication) {
            return authentication.equals(UsernamePasswordAuthenticationToken.class);
        }


}
