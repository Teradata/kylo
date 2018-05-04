package com.thinkbiganalytics.kylo.catalog.spark.sources;

import com.thinkbiganalytics.kylo.catalog.api.MissingOptionException;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

/**
 * Static support methods for interacting with data sets.
 */
@SuppressWarnings("WeakerAccess")
public class DataSetUtil {

    /**
     * Gets the specified option value or throws an exception.
     *
     * @throws MissingOptionException if the option is not defined
     */
    @Nonnull
    public static String getOptionOrThrow(@Nonnull final DataSetOptions options, @Nonnull final String key, @Nullable final String errorMessage) {
        final Option<String> value = options.getOption(key);
        if (value.isDefined()) {
            return value.get();
        } else {
            throw new MissingOptionException((errorMessage == null) ? "Missing required option: " + key : errorMessage);
        }
    }

    /**
     * Instances of {@code DataSetUtil} should not be constructed.
     */
    private DataSetUtil() {
        throw new UnsupportedOperationException();
    }
}
