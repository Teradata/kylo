/**
 * 
 */
package com.thinkbiganalytics.kylo.catalog.credential.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Thrown when there is an error involved with Data Source credentials.
 */
public class DataSourceCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final Set<String> credentalNames;
    
    public DataSourceCredentialsException(String message, String... credNames) {
        super(message);
        this.credentalNames = Collections.unmodifiableSet(Arrays.stream(credNames).collect(Collectors.toSet()));
    }
    
    public DataSourceCredentialsException(String message, Collection<String> credNames) {
        super(message);
        this.credentalNames = Collections.unmodifiableSet(new HashSet<>(credNames));
    }

    public DataSourceCredentialsException(String message, Throwable cause) {
        super(message, cause);
        this.credentalNames = Collections.emptySet();
    }
    
    /**
     * @return the credentalNames names involved with the error (if any.)
     */
    public Set<String> getCredentalNames() {
        return credentalNames;
    }

}
