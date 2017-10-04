/**
 *
 */
package org.springframework.security.ldap.authentication.ad;

/*-
 * #%L
 * thinkbig-security-auth-ldap
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

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.thinkbiganalytics.security.UsernamePrincipal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;

/**
 * An Active Directory provider that looks up a user and groups, optionally using a separate service account.  
 * In the latter case, it will only validate that the user exists in AD; no user password validation is performed.  
 * If the user does exist (and authenticated when necessary) then the user's groups may be loaded if configured
 * to do so.
 * <p>
 * Much of this implementation had to be copied from {@link ActiveDirectoryLdapAuthenticationProvider} 
 * since that class is final and the methods needed to override its behavior are private. 
 */
public class ActiveDirectoryAuthenticationProvider extends AbstractLdapAuthenticationProvider {
    
    private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");
    
    // Error codes
    private static final int USERNAME_NOT_FOUND = 0x525;
    private static final int INVALID_PASSWORD = 0x52e;
    private static final int NOT_PERMITTED = 0x530;
    private static final int PASSWORD_EXPIRED = 0x532;
    private static final int ACCOUNT_DISABLED = 0x533;
    private static final int ACCOUNT_EXPIRED = 0x701;
    private static final int PASSWORD_NEEDS_RESET = 0x773;
    private static final int ACCOUNT_LOCKED = 0x775;
    
    private final UsernamePasswordAuthenticationToken serviceToken;
    private final boolean groupsEnabled;

    private final String domain;
    private final String rootDn;
    private final String url;
    private boolean convertSubErrorCodesToExceptions;
    private String searchFilter = "(&(objectClass=user)(userPrincipalName={0}))";
    
    /**
     * @param domain the domain name (may be null or empty)
     * @param url an LDAP url (or multiple URLs)
     */
    public ActiveDirectoryAuthenticationProvider(String domain, 
                                                       String url, 
                                                       boolean enableGroups, 
                                                       String serviceUser, 
                                                       String servicePassword) {
        Assert.isTrue(StringUtils.hasText(url), "Url cannot be empty");
        
        this.domain = StringUtils.hasText(domain) ? domain.toLowerCase() : null;
        this.url = url;
        this.groupsEnabled = enableGroups;
        this.rootDn = this.domain == null ? null : rootDnFromDomain(this.domain);
        
        if (serviceUser != null) {
            Assert.isTrue(StringUtils.hasText(servicePassword), "service password cannot be empty");
            this.serviceToken = new UsernamePasswordAuthenticationToken(new UsernamePrincipal(serviceUser), servicePassword);
        } else {
            this.serviceToken = null;
        }
        
    }
    
    /**
     * Specifies whether this provider has be configured to authenticate to AD using 
     * a the credentials of a service account username/password.
     * @return if this provider is configured with service credentials
     */
    public boolean isUsingServiceCredentials() {
        return this.serviceToken != null;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider#authenticate(org.springframework.security.core.Authentication)
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws org.springframework.security.core.AuthenticationException {
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, 
                            authentication,
                            this.messages.getMessage("LdapAuthenticationProvider.onlySupports", "Only UsernamePasswordAuthenticationToken is supported"));

        final UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) authentication;
        final UsernamePasswordAuthenticationToken authToken = this.serviceToken != null ? this.serviceToken : userToken;

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Processing authentication request for user: " + userToken.getName());
        }

        if (!StringUtils.hasLength(userToken.getName())) {
            throw new BadCredentialsException(this.messages.getMessage("LdapAuthenticationProvider.emptyUsername", "Empty Username"));
        }

        if (!StringUtils.hasLength((String) authToken.getCredentials())) {
            throw new BadCredentialsException(this.messages.getMessage("AbstractLdapAuthenticationProvider.emptyPassword", "Empty Password"));
        }

        DirContextOperations userData = doAuthentication(userToken);
        Collection<? extends GrantedAuthority> authorities = loadUserAuthorities(userData, 
                                                                                 authToken.getName(), 
                                                                                 (String) authToken.getCredentials());
        UserDetails user = this.userDetailsContextMapper.mapUserFromContext(userData, userToken.getName(), authorities);

        return createSuccessfulAuthentication(userToken, user);
    }
    
    @Override
    protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken userToken) {
        final UsernamePasswordAuthenticationToken authToken = this.serviceToken != null ? this.serviceToken : userToken;
        DirContext ctx = bindAsUser(authToken.getName(), (String) authToken.getCredentials());
    
        try {
            return searchForUser(ctx, userToken.getName());
        } catch (NamingException e) {
            logger.error("Failed to locate directory entry for authenticated user: " + userToken.getName(), e);
            throw badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
    }
    
    /**
     * Creates the user authority list from the values of the {@code memberOf} attribute
     * obtained from the user's Active Directory entry.
     */
    @Override
    protected Collection<? extends GrantedAuthority> loadUserAuthorities(DirContextOperations userData, String username, String password) {
        if (this.groupsEnabled) {
            String[] groups = userData.getStringAttributes("memberOf");
            
            if (groups == null) {
                logger.debug("No values for 'memberOf' attribute.");
                
                return AuthorityUtils.NO_AUTHORITIES;
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("'memberOf' attribute values: " + Arrays.asList(groups));
            }
            
            return Arrays.stream(groups)
                            .map(g -> new SimpleGrantedAuthority(new DistinguishedName(g).removeLast().getValue()))
                            .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
    
    private DirContext bindAsUser(String username, String password) {
        // TODO. add DNS lookup based on domain
        final String bindUrl = url;
    
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        String bindPrincipal = createBindPrincipal(username);
        env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
        env.put(Context.PROVIDER_URL, bindUrl);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());
    
        try {
            return new InitialLdapContext(env, null);
        } catch (NamingException e) {
            if ((e instanceof AuthenticationException) || (e instanceof OperationNotSupportedException)) {
                handleBindException(bindPrincipal, e);
                throw badCredentials(e);
            } else {
                throw LdapUtils.convertLdapException(e);
            }
        }
    }
    
    private void handleBindException(String bindPrincipal, NamingException exception) {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication for " + bindPrincipal + " failed:" + exception);
        }
    
        int subErrorCode = parseSubErrorCode(exception.getMessage());
    
        if (subErrorCode <= 0) {
            logger.debug("Failed to locate AD-specific sub-error code in message");
            return;
        }
    
        logger.info("Active Directory authentication failed: " + subCodeToLogMessage(subErrorCode));
    
        if (convertSubErrorCodesToExceptions) {
            raiseExceptionForErrorCode(subErrorCode, exception);
        }
    }
    
    private int parseSubErrorCode(String message) {
        Matcher m = SUB_ERROR_CODE.matcher(message);
    
        if (m.matches()) {
            return Integer.parseInt(m.group(1), 16);
        }
    
        return -1;
    }
    
    private void raiseExceptionForErrorCode(int code, NamingException exception) {
        String hexString = Integer.toHexString(code);
        Throwable cause = new ActiveDirectoryAuthenticationException(hexString, exception.getMessage(), exception);
        
        switch (code) {
            case PASSWORD_EXPIRED:
                throw new CredentialsExpiredException(messages.getMessage("LdapAuthenticationProvider.credentialsExpired", "User credentials have expired"), cause);
            case ACCOUNT_DISABLED:
                throw new DisabledException(messages.getMessage("LdapAuthenticationProvider.disabled", "User is disabled"), cause);
            case ACCOUNT_EXPIRED:
                throw new AccountExpiredException(messages.getMessage("LdapAuthenticationProvider.expired", "User account has expired"), cause);
            case ACCOUNT_LOCKED:
                throw new LockedException(messages.getMessage("LdapAuthenticationProvider.locked", "User account is locked"), cause);
            default:
                throw badCredentials(cause);
        }
    }
    
    private String subCodeToLogMessage(int code) {
        switch (code) {
            case USERNAME_NOT_FOUND:
                return "User was not found in directory";
            case INVALID_PASSWORD:
                return "Supplied password was invalid";
            case NOT_PERMITTED:
                return "User not permitted to logon at this time";
            case PASSWORD_EXPIRED:
                return "Password has expired";
            case ACCOUNT_DISABLED:
                return "Account is disabled";
            case ACCOUNT_EXPIRED:
                return "Account expired";
            case PASSWORD_NEEDS_RESET:
                return "User must reset password";
            case ACCOUNT_LOCKED:
                return "Account locked";
        }
    
        return "Unknown (error code " + Integer.toHexString(code) + ")";
    }
    
    private BadCredentialsException badCredentials() {
        return new BadCredentialsException(messages.getMessage("LdapAuthenticationProvider.badCredentials", "Bad credentials"));
    }
    
    private BadCredentialsException badCredentials(Throwable cause) {
        return (BadCredentialsException) badCredentials().initCause(cause);
    }
    
    private DirContextOperations searchForUser(DirContext context, String username) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    
        String bindPrincipal = createBindPrincipal(username);
        String searchRoot = rootDn != null ? rootDn : searchRootFromPrincipal(bindPrincipal);
    
        try {
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(context,
                                                                           searchControls, 
                                                                           searchRoot, 
                                                                           searchFilter,
                                                                           new Object[] { bindPrincipal, username });
        } catch (IncorrectResultSizeDataAccessException incorrectResults) {
            // Search should never return multiple results if properly configured - just rethrow
            if (incorrectResults.getActualSize() != 0) {
                throw incorrectResults;
            }
            // If we found no results, then the username/password did not match
            UsernameNotFoundException userNameNotFoundException = new UsernameNotFoundException("User " + username + " not found in directory.", incorrectResults);
            throw badCredentials(userNameNotFoundException);
        }
    }
    
    private String searchRootFromPrincipal(String bindPrincipal) {
        int atChar = bindPrincipal.lastIndexOf('@');
    
        if (atChar < 0) {
            logger.debug("User principal '" + bindPrincipal + "' does not contain the domain, and no domain has been configured");
            throw badCredentials();
        }
    
        return rootDnFromDomain(bindPrincipal.substring(atChar + 1, bindPrincipal.length()));
    }
    
    private String rootDnFromDomain(String domain) {
        String[] tokens = StringUtils.tokenizeToStringArray(domain, ".");
        StringBuilder root = new StringBuilder();
    
        for (String token : tokens) {
            if (root.length() > 0) {
                root.append(',');
            }
            root.append("dc=").append(token);
        }
    
        return root.toString();
    }
    
    String createBindPrincipal(String username) {
        if (domain == null || username.toLowerCase().endsWith(domain)) {
            return username;
        }
    
        return username + "@" + domain;
    }
    
    /**
     * By default, a failed authentication (LDAP error 49) will result in a
     * {@code BadCredentialsException}.
     * <p>
     * If this property is set to {@code true}, the exception message from a failed bind
     * attempt will be parsed for the AD-specific error code and a
     * {@link CredentialsExpiredException}, {@link DisabledException},
     * {@link AccountExpiredException} or {@link LockedException} will be thrown for the
     * corresponding codes. All other codes will result in the default
     * {@code BadCredentialsException}.
     *
     * @param convertSubErrorCodesToExceptions {@code true} to raise an exception based on
     * the AD error code.
     */
    public void setConvertSubErrorCodesToExceptions(boolean convertSubErrorCodesToExceptions) {
        this.convertSubErrorCodesToExceptions = convertSubErrorCodesToExceptions;
    }
    
    /**
     * The LDAP filter string to search for the user being authenticated. Occurrences of
     * {0} are replaced with the bind principal (user@domain), and {1} replaced with the username.
     * <p>
     * Defaults to: {@code (&(objectClass=user)(userPrincipalName={0}))}
     * </p>
     * <p>
     * Common alternative: {@code (&(objectClass=user)(sAMAccountName={1}))}
     * </p>
     *
     * @param searchFilter the filter string
     *
     * @since 3.2.6
     */
    public void setSearchFilter(String searchFilter) {
        Assert.hasText(searchFilter, "searchFilter must have text");
        this.searchFilter = searchFilter;
    }
}
