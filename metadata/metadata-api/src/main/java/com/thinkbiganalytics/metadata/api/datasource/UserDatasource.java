package com.thinkbiganalytics.metadata.api.datasource;

import com.thinkbiganalytics.security.AccessControlled;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Defines a data source managed through Kylo by a user.
 */
public interface UserDatasource extends Datasource, AccessControlled {

    /**
     * Gets the additional properties for this data source.
     *
     * @return the details
     */
    @Nonnull
    Optional<? extends DatasourceDetails> getDetails();

    /**
     * Sets the name of this data source.
     *
     * @param name the name
     */
    void setName(@Nonnull String name);

    /**
     * Gets the type of this data source.
     *
     * @return the type
     */
    @Nonnull
    String getType();

    /**
     * Sets the type of this data source.
     *
     * @param type the type
     */
    void setType(@Nonnull String type);

}
