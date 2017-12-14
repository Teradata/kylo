package com.thinkbiganalytics.auth.jwt;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

/*-
 * #%L
 * thinkbig-security-auth
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.thinkbiganalytics.auth.config.JwtProperties;
import com.thinkbiganalytics.security.BasePrincipal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.joda.time.DateTimeUtils;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.jaas.JaasGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.Key;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Identifies previously remembered users by a JSON Web Token.
 *
 * <p>The token contains the user's names and groups. It is stored as a cookie in the user's browser to authenticate the user in subsequent requests.</p>
 */
public class JwtRememberMeServices extends AbstractRememberMeServices {

    private static final Logger log = LoggerFactory.getLogger(JwtRememberMeServices.class);
    
    /**
     * Key of the name of the claim containing principals metadata.
     */
    private static final String PRINCIPALS = "principals";
    
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Identifies the signature algorithm
     */
    @Nonnull
    private final String algorithmIdentifier;

    /**
     * Secret key for signature
     */
    @Nullable
    private Key secretKey;

    /**
     * Constructs a {@code JwtRememberMeServices} with the specified configuration.
     *
     * @param properties the JWT configuration
     */
    public JwtRememberMeServices(@Nonnull final JwtProperties properties) {
        super(properties.getKey(), username -> null);
        this.algorithmIdentifier = properties.getAlgorithm();
    }

    /**
     * Decodes the specified JWT cookie into tokens.
     *
     * <p>The first element of the return value with be the JWT subject. The remaining element (should be 1) is the principals JSON token.</p>
     *
     * @param cookie the JWT cookie
     * @return an array with the username and group names
     * @throws IllegalStateException  if the secret key is invalid
     * @throws InvalidCookieException if the cookie cannot be decoded
     */
    @Nonnull
    @Override
    protected String[] decodeCookie(@Nonnull final String cookie) throws InvalidCookieException {
        // Build the JWT parser
        final JwtConsumer consumer = new JwtConsumerBuilder()
            .setEvaluationTime(NumericDate.fromMilliseconds(DateTimeUtils.currentTimeMillis()))
            .setVerificationKey(getSecretKey())
            .build();

        // Parse the cookie
        final String user;
        final List<String> principalsClaim;

        try {
            final JwtClaims claims = consumer.processToClaims(cookie);
            user = claims.getSubject();
            principalsClaim = claims.getStringListClaimValue(PRINCIPALS);
        } catch (final InvalidJwtException e) {
            log.debug("JWT cookie is invalid: ", e);
            throw new InvalidCookieException("JWT cookie is invalid: " + e);
        } catch (final MalformedClaimException e) {
            log.debug("JWT cookie is malformed: ", e);
            throw new InvalidCookieException("JWT cookie is malformed: " + cookie);
        }

        if (StringUtils.isBlank(user)) {
            throw new InvalidCookieException("Missing user in JWT cookie: " + cookie);
        }

        // Build the token array
        final Stream<String> userStream = Stream.of(user);
        final Stream<String> groupStream = principalsClaim.stream();
        return Stream.concat(userStream, groupStream).toArray(String[]::new);
    }

    /**
     * Encodes the specified tokens into a JWT cookie.
     *
     * <p>The first element of {@code tokens} should be the user's principal. The remaining elements are the groups assigned to the user.</p>
     *
     * @param tokens an array with the username and group names
     * @return a JWT cookie
     * @throws IllegalStateException if the secret key is invalid
     */
    @Nonnull
    @Override
    protected String encodeCookie(@Nonnull final String[] tokens) {
        // Determine expiration time
        final NumericDate expireTime = NumericDate.fromMilliseconds(DateTimeUtils.currentTimeMillis());
        expireTime.addSeconds(getExpirationTimeSeconds());

        // Build the JSON Web Token
        final JwtClaims claims = new JwtClaims();
        claims.setExpirationTime(expireTime);
        claims.setSubject(tokens[0]);
        claims.setStringListClaim(PRINCIPALS, Arrays.asList(tokens).subList(1, tokens.length));

        // Generate a signature
        final JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(algorithmIdentifier);
        jws.setKey(getSecretKey());
        jws.setKeyIdHeaderValue(getSecretKey().getAlgorithm());
        jws.setPayload(claims.toJson());

        // Serialize the cookie
        try {
            return jws.getCompactSerialization();
        } catch (final JoseException e) {
            log.error("Unable to encode cookie: ", e);
            throw new IllegalStateException("Unable to encode cookie: ", e);
        }
    }

    /**
     * Sets a JWT cookie when the user has successfully logged in.
     *
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the user
     */
    @Override
    protected void onLoginSuccess(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response, @Nonnull final Authentication authentication) {
        final Stream<String> user = Stream.of(authentication.getPrincipal().toString());
        final Stream<String> token = Stream.of(generatePrincipalsToken(authentication.getAuthorities()));
        final String[] tokens = Stream.concat(user, token).toArray(String[]::new);

        setCookie(tokens, getTokenValiditySeconds(), request, response);
    }

    /**
     * Reconstructs the user from the specified tokens.
     *
     * @param tokens   the tokens from the JWT cookie
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the user
     */
    @Override
    protected UserDetails processAutoLoginCookie(@Nonnull final String[] tokens, @Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response) {
        final Collection<? extends GrantedAuthority> authorities = generateAuthorities(tokens[1]);
        return new User(tokens[0], "", authorities);
    }

    /**
     * Gets the JWT expiration time in seconds. This is usually the same as the max age for the cookie.
     *
     * @return the expiration time in seconds
     */
    private int getExpirationTimeSeconds() {
        final int tokenValiditySeconds = getTokenValiditySeconds();
        return (tokenValiditySeconds >= 0) ? tokenValiditySeconds : TWO_WEEKS_S;
    }

    /**
     * Gets the secret key for the JWT signature.
     *
     * <p>The key is constructed based on which the configured signature algorithm.</p>
     *
     * @return the secret key
     * @throws IllegalStateException if the algorithm is not supported
     */
    private Key getSecretKey() {
        if (secretKey == null) {
            switch (algorithmIdentifier.substring(0, 2)) {
                case "HS":
                    secretKey = new HmacKey(getKey().getBytes());
                    break;

                default:
                    throw new IllegalStateException("Not a supported JWT algorithm: " + algorithmIdentifier);
            }
        }
        return secretKey;
    }
    
    /**
     * Serializes principal information derived from the supplied authorities as a JSON string 
     * that can be deserialized back into a set of authorities containing those Principal objects.
     * @param collection the authorities produced after login
     * @return a JSON string describing the principals derived from the authorities
     */
    protected String generatePrincipalsToken(Collection<? extends GrantedAuthority> authorities) {
        PrincipalsToken token = new PrincipalsToken();
        ObjectWriter writer = mapper.writer().forType(PrincipalsToken.class);
        
        for (GrantedAuthority authority : authorities) {
            token.add(authority);
        }
        
        try {
            return writer.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            // Shouldn't really happen
            throw new IllegalStateException("Unable to serialize principals for JWT token", e);
        }
    }

    /**
     * Deserializes the JSON of the principals token back into JaasGrantedAuthrities containing
     * principals of the same types as when they were serialized.
     * @param jsonString a JSON string describing the principals derived from the authorities
     * @return the original authorities produced after login
     */
    protected Collection<? extends GrantedAuthority> generateAuthorities(String jsonString) {
        ObjectReader reader = mapper.reader().forType(PrincipalsToken.class);
        
        try {
            PrincipalsToken token = reader.readValue(jsonString);
            return token.deriveAuthorities();
        } catch (IOException e) {
            // Shouldn't really happen
            throw new IllegalStateException("Unable to deserialize principals for JWT token", e);
        }
    }

    protected static class PrincipalsToken {
        private static final String UNKNOWN = "*";
        
        @JsonIgnore
        private Map<String, Set<String>> principals = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Set<String>> getPrincipals() {
            return principals;
        }

        @JsonAnySetter
        public void setPrincipals(String principalType, Set<String> principalNames) {
            this.principals.put(principalType, principalNames);
        }
        
        public Collection<? extends GrantedAuthority> deriveAuthorities() {
            return this.principals.entrySet().stream()
                            .flatMap(entry -> {
                                Class<? extends Principal> type = derivePrincipalType(entry.getKey());
                                return entry.getValue().stream().map(name -> createPrincipal(type, name));
                             })
                            .map(principal -> new JaasGrantedAuthority(principal.getName(), principal))
                            .collect(Collectors.toList());
        }
        
        public void add(GrantedAuthority authority) {
            if (authority instanceof JaasGrantedAuthority) {
                JaasGrantedAuthority jaas = (JaasGrantedAuthority) authority;
                add(jaas.getPrincipal());
            } else {
                add(authority.getAuthority());
            }
        }
        
        public void add(Principal principal) {
            add(principal.getClass().getName(), principal.getName());
        }
        
        public void add(String principalName) {
            add(UNKNOWN, principalName);
        }
        
        private void add(String typeName, String principalName) {
            Set<String> set = this.principals.computeIfAbsent(typeName, (k) -> new HashSet<String>());
            set.add(principalName);
        }
        
        private Class<? extends Principal> derivePrincipalType(String typeName){
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Principal> type = typeName.equals(PrincipalsToken.UNKNOWN) 
                    ? SimplePrincipal.class 
                    : (Class<? extends Principal>) Class.forName(typeName);
                return type;
            } catch (ClassNotFoundException e) {
                log.error("Unsupported principal type: {}", typeName, e);
                throw new IllegalStateException("Unsupported principal type: " + typeName);
            }
        }

        private Principal createPrincipal(Class<? extends Principal> type, String name){
            try {
                return ConstructorUtils.invokeConstructor(type, name);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                log.error("Could not instanciate principal type: ", type.getName(), e);
                throw new IllegalStateException("Could not instanciate principal type: " + type.getName(), e);
            }
        }
    }
    
    protected static class SimplePrincipal extends BasePrincipal {
        private static final long serialVersionUID = 1L;
        
        public SimplePrincipal(String name) {
            super(name);
        }
    }
}
