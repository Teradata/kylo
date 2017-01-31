package com.thinkbiganalytics.auth.config;

import org.jose4j.jwa.AlgorithmFactoryFactory;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Configuration properties for the JSON Web Token Remember Me Service.
 */
@ConfigurationProperties("security.jwt")
public class JwtProperties {

    /** Identifies the signature algorithm */
    private String algorithm = AlgorithmIdentifiers.HMAC_SHA256;

    /** Secret key for signature */
    private String key;

    @Nonnull
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the algorithm to use for the JWT signature.
     *
     * @param algorithm the algorithm identifier
     * @throws IllegalArgumentException if the algorithm is not supported
     */
    public void setAlgorithm(@Nonnull final String algorithm) {
        if (AlgorithmFactoryFactory.getInstance().getJwsAlgorithmFactory().isAvailable(algorithm) && algorithm.startsWith("HS")) {
            this.algorithm = algorithm;
        } else {
            throw new IllegalArgumentException("Not a supported JWT algorithm: " + algorithm);
        }
    }

    @Nonnull
    public String getKey() {
        return Objects.requireNonNull(key, "The security.jwt.key property must be set.");
    }

    public void setKey(@Nonnull final String key) {
        this.key = key;
    }
}
