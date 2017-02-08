package com.thinkbiganalytics.auth.jwt;

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

import com.thinkbiganalytics.auth.config.JwtProperties;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.security.Key;
import java.util.Arrays;
import java.util.List;
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

    /**
     * Key of the string list containing group names
     */
    private static final String GROUPS = "groups";

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
     * <p>The first element of the return value with be the JWT subject. The remaining elements are the elements in the {@code groups} list.</p>
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
        final List<String> groups;

        try {
            final JwtClaims claims = consumer.processToClaims(cookie);
            user = claims.getSubject();
            groups = claims.getStringListClaimValue(GROUPS);
        } catch (final InvalidJwtException e) {
            throw new InvalidCookieException("JWT cookie is invalid: " + e);
        } catch (final MalformedClaimException e) {
            throw new InvalidCookieException("JWT cookie is malformed: " + cookie);
        }

        if (StringUtils.isBlank(user)) {
            throw new InvalidCookieException("Missing user in JWT cookie: " + cookie);
        }

        // Build the token array
        final Stream<String> userStream = Stream.of(user);
        final Stream<String> groupStream = groups.stream();
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
        claims.setStringListClaim("groups", Arrays.asList(tokens).subList(1, tokens.length));

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
            throw new IllegalStateException("Unable to encode cookie: " + e, e);
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
        final Stream<String> groups = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
        final String[] tokens = Stream.concat(user, groups).toArray(String[]::new);

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
        final List<GrantedAuthority> authorities = Arrays.asList(tokens).subList(1, tokens.length).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
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
}
