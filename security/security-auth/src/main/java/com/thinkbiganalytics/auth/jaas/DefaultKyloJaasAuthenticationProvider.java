package com.thinkbiganalytics.auth.jaas;

import com.thinkbiganalytics.logging.LoggingUtil;
import com.thinkbiganalytics.logging.LoggingUtil.LogLevel;
import com.thinkbiganalytics.security.UsernamePrincipal;

/*-
 * #%L
 * kylo-security-auth
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider;
import org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler;
import org.springframework.security.authentication.jaas.JaasAuthenticationToken;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.acl.Group;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 *
 */
public class DefaultKyloJaasAuthenticationProvider extends DefaultJaasAuthenticationProvider {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultKyloJaasAuthenticationProvider.class);

//    public enum LogFields { PERM, ENTITY, RESULT, USER, GROUPS, IP_ADDRESS };
    public enum LogFields { RESULT, USER, GROUPS };

    @org.springframework.beans.factory.annotation.Value("${security.log.auth:false}")
    private boolean logAuthentication;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.auth.level:DEBUG}")
    private String logLevelValue;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.auth.login.format:Authentication attempt: {RESULT}, user: {USER}}")
    private String loginFormat;
    
    @org.springframework.beans.factory.annotation.Value("${security.log.auth.logout.format:Logout attempt: {RESULT}, user: {USER}}")
    private String logoutFormat;
    
    private LoggingUtil.LogLevel logLevel;
    private List<LogFields> loginFields;
    private String loginMessage;
    
    private String loginContextName;
    private JaasAuthenticationCallbackHandler[] callbackHandlers;
    
    @PostConstruct
    protected void initLogging() {
        this.logLevel = LogLevel.level(logLevelValue);
        this.loginFields = LoggingUtil.extractTokens(LogFields.class, loginFormat);
        this.loginMessage = LoggingUtil.toLogMessage(loginFormat);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider#setCallbackHandlers(org.springframework.security.authentication.jaas.JaasAuthenticationCallbackHandler[])
     */
    @Override
    public void setCallbackHandlers(JaasAuthenticationCallbackHandler[] callbackHandlers) {
        super.setCallbackHandlers(callbackHandlers);
        this.callbackHandlers = callbackHandlers;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider#setLoginContextName(java.lang.String)
     */
    @Override
    public void setLoginContextName(String loginContextName) {
        super.setLoginContextName(loginContextName);
        this.loginContextName = loginContextName;
    }
    
    protected String getContextName() {
        return loginContextName;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider#handleLogout(org.springframework.security.core.session.SessionDestroyedEvent)
     */
    @Override
    protected void handleLogout(SessionDestroyedEvent event) {
        List<SecurityContext> contexts = event.getSecurityContexts();

        if (contexts.isEmpty()) {
            log.debug("The destroyed session has no SecurityContexts");
            return;
        }

        for (SecurityContext context : contexts) {
            Authentication auth = context.getAuthentication();

            if (auth != null) {
                try {
                    getLoginContext(auth)
                        .ifPresent(loginContext -> { 
                            JaasAuthenticationToken jaasToken = (JaasAuthenticationToken) auth;
                            
                            try {
                                loginContext.logout();
                                LoggingUtil.log(log, this.logLevel, LogFields.class, this.logoutFormat, deriveSuccessTokenValues(jaasToken));
                            } catch (LoginException e) {
                                LoggingUtil.log(log, this.logLevel, LogFields.class, this.logoutFormat, deriveFailedTokenValues(e, jaasToken));
                                log.debug("Error logging out of LoginContext", e);
                            }
                        });
                } catch (LoginException e) {
                    log.warn("Error obtaining LoginContext", e);
                }
            }
        }
    }
    
    protected LoginContext createLoginContext(Subject subject, CallbackHandler handler) throws LoginException {
        return new LoginContext(getContextName(), subject, handler, getConfiguration());
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider#publishSuccessEvent(org.springframework.security.authentication.UsernamePasswordAuthenticationToken)
     */
    @Override
    protected void publishSuccessEvent(UsernamePasswordAuthenticationToken token) {
        JaasAuthenticationToken jaasToken = (JaasAuthenticationToken) token;
        Object[] args = LoggingUtil.deriveArguments(this.loginFields, deriveSuccessTokenValues(jaasToken));
        LoggingUtil.log(log, this.logLevel, this.loginMessage, args);
        super.publishSuccessEvent(token);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.authentication.jaas.AbstractJaasAuthenticationProvider#publishFailureEvent(org.springframework.security.authentication.UsernamePasswordAuthenticationToken, org.springframework.security.core.AuthenticationException)
     */
    @Override
    protected void publishFailureEvent(UsernamePasswordAuthenticationToken token, AuthenticationException ase) {
        Object[] args = LoggingUtil.deriveArguments(this.loginFields, deriveFailedTokenValues(ase, token));
        LoggingUtil.log(log, this.logLevel, this.loginMessage, args);
        super.publishFailureEvent(token, ase);
    }
    
    private Function<LogFields, Object> deriveSuccessTokenValues(JaasAuthenticationToken token) {
        return (field) -> {
            switch (field) {
                case RESULT:
                    return "success";
                case USER:
                    return token.getName();
                case GROUPS:
                    return token.getAuthorities().stream()
                                    .map(JaasGrantedAuthority.class::cast)
                                    .map(JaasGrantedAuthority::getPrincipal)
                                    .filter(Group.class::isInstance)
                                    .map(Principal::getName)
                                    .collect(Collectors.joining(", "));
//                  case IP_ADDRESS:
//                  return "";
                default:
                    return "";
            }
        };
    }

    private Function<LogFields, Object> deriveFailedTokenValues(Exception exception, UsernamePasswordAuthenticationToken token) {
        return (field) -> {
            switch (field) {
                case RESULT:
                    return "failed - " + exception.getMessage();
                case USER:
                    return token.getName();
                case GROUPS:
                    return token.getAuthorities().stream()
                            .filter(JaasGrantedAuthority.class::isInstance)
                            .map(JaasGrantedAuthority.class::cast)
                            .map(JaasGrantedAuthority::getPrincipal)
                            .filter(Group.class::isInstance)
                            .map(Principal::getName)
                            .collect(Collectors.joining(", "));
//                case IP_ADDRESS:
//                    return "";
                default:
                    return "";
            }
        };
    }

    private Optional<LoginContext> getLoginContext(Authentication auth) throws LoginException {
        LoginContext loginContext;
        
        if (auth instanceof JaasAuthenticationToken) {
            JaasAuthenticationToken token = (JaasAuthenticationToken) auth;
            loginContext = token.getLoginContext();
            
            if (loginContext == null) {
                loginContext = createLoginContext(createSubject(auth), new InternalCallbackHandler(auth));
                log.debug("Created LoginContext for auth: {}", auth);
            } else {
                log.debug("Using LoginContext from token: {}", token);
            }
        } else {
            loginContext = createLoginContext(createSubject(auth), new InternalCallbackHandler(auth));
            log.debug("Created LoginContext for auth: {}", auth);
        }

        return Optional.ofNullable(loginContext);
    }
    

    private Subject createSubject(Authentication auth) {
        Set<Principal> principals = auth.getAuthorities().stream()
            .filter(grant -> grant instanceof JaasGrantedAuthority)
            .map(JaasGrantedAuthority.class::cast)
            .map(jga -> jga.getPrincipal())
            .collect(Collectors.toCollection(HashSet::new));
            
        principals.add(new UsernamePrincipal(auth.getName()));
        
        Subject subject = Subject.getSubject(AccessController.getContext());
        if (subject == null) {
            return new Subject(false, principals, new HashSet<>(), new HashSet<>());
        } else {
            subject.getPrincipals().addAll(principals);
            return subject;
        }
    }


    /**
     * Wrapper class for JAASAuthenticationCallbackHandlers
     */
    private class InternalCallbackHandler implements CallbackHandler {
        private final Authentication authentication;

        public InternalCallbackHandler(Authentication authentication) {
            this.authentication = authentication;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (JaasAuthenticationCallbackHandler handler : DefaultKyloJaasAuthenticationProvider.this.callbackHandlers) {
                for (Callback callback : callbacks) {
                    handler.handle(callback, this.authentication);
                }
            }
        }
    }

}
