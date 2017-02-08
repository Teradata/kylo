/**
 *
 */
package com.thinkbiganalytics.security.auth.kerberos;

/*-
 * #%L
 * kylo-security-auth-kerberos
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.auth.UsernameAuthenticationToken;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Performs SPNEGO validation of the Kerberos ticket and, if valid, performs further authentication
 * of the user name using a UsernamePasswordAuthenticationToken with an empty password.
 */
public class SpnegoValidationUserAuthenticationFilter extends GenericFilterBean {

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private AuthenticationManager authenticationManager;
    private RememberMeServices rememberMeServices = new NullRememberMeServices();
    private boolean skipIfAlreadyAuthenticated = true;


    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        if (skipIfAlreadyAuthenticated) {
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

            if (existingAuth != null && existingAuth.isAuthenticated()
                && (existingAuth instanceof AnonymousAuthenticationToken) == false) {
                chain.doFilter(httpReq, httpResp);
                return;
            }
        }

        String header = httpReq.getHeader("Authorization");

        if (isTicketHeader(header)) {
            KerberosServiceRequestToken token = extractTicketToken(header, httpReq);
            Authentication kerberosAuth;

            try {
                kerberosAuth = authenticationManager.authenticate(token);
            } catch (AuthenticationException e) {
                // Most likely a wrong configuration on the server side, such as the wrong service principal is configured or  
                // it is not present in the keytab.
                logger.warn("Failed to validate negotiate header: " + header, e);

                SecurityContextHolder.clearContext();
                httpResp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpResp.flushBuffer();
                return;
            }

            // If the ticket has been authenticated then attempt to authenticate the username.
            try {
                UsernameAuthenticationToken userToken = extractUsernameToken(kerberosAuth);
                Authentication usernameAuth = authenticationManager.authenticate(userToken);

                SecurityContextHolder.getContext().setAuthentication(usernameAuth);
                this.rememberMeServices.loginSuccess(httpReq, httpResp, usernameAuth);
            } catch (AuthenticationException e) {
                this.rememberMeServices.loginFail(httpReq, httpResp);

//                httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + e.getMessage());
            }
        }

        chain.doFilter(httpReq, httpResp);
    }

    /**
     * The authentication manager for validating the ticket.
     *
     * @param authenticationManager the authentication manager
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * Should Kerberos authentication be skipped if a user is already authenticated
     * for this request (e.g. in the HTTP session).
     *
     * @param skipIfAlreadyAuthenticated default is true
     */
    public void setSkipIfAlreadyAuthenticated(boolean skipIfAlreadyAuthenticated) {
        this.skipIfAlreadyAuthenticated = skipIfAlreadyAuthenticated;
    }

    /**
     * Sets the authentication details source.
     *
     * @param authenticationDetailsSource the authentication details source
     */
    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * Sets the remember-me service to invoke upon successful for failed authentication.
     *
     * @param rememberMeServices the service
     */
    public void setRememberMeServices(RememberMeServices rememberMeServices) {
        Assert.notNull(rememberMeServices, "rememberMeServices cannot be null");
        this.rememberMeServices = rememberMeServices;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified");
        Assert.notNull(rememberMeServices, "rememberMeServices must be specified");
    }

    protected KerberosServiceRequestToken extractTicketToken(String header, HttpServletRequest request) throws UnsupportedEncodingException {
        if (logger.isDebugEnabled()) {
            logger.debug("Received Negotiate Header for request " + request.getRequestURL() + ": " + header);
        }

        byte[] base64Token = header.substring(header.indexOf(" ") + 1).getBytes("UTF-8");
        byte[] kerberosTicket = Base64.decode(base64Token);
        KerberosServiceRequestToken token = new KerberosServiceRequestToken(kerberosTicket);
        token.setDetails(authenticationDetailsSource.buildDetails(request));
        return token;
    }

    protected boolean isTicketHeader(String header) {
        return header != null && (header.startsWith("Negotiate ") || header.startsWith("Kerberos "));
    }

    protected UsernameAuthenticationToken extractUsernameToken(Authentication kerberosAuth) {
        String krbUser = ((User) kerberosAuth.getPrincipal()).getUsername();
        int realmStart = krbUser.lastIndexOf('@');
        String username = krbUser.substring(0, realmStart == -1 ? krbUser.length() : realmStart);
        return new UsernameAuthenticationToken(username);
    }

}
