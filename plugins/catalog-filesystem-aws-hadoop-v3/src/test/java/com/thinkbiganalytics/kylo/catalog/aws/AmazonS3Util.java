package com.thinkbiganalytics.kylo.catalog.aws;

/*-
 * #%L
 * kylo-catalog-filesystem-aws-hadoop-v3
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.amazonaws.services.s3.model.Bucket;

import java.util.Date;

import javax.annotation.Nonnull;

/**
 * Static support methods for Amazon S3.
 */
public class AmazonS3Util {

    /**
     * Creates an S3 bucket with the specified name.
     */
    @Nonnull
    @SuppressWarnings("WeakerAccess")
    public static Bucket createBucket(@Nonnull final String name) {
        final Bucket bucket = new Bucket(name);
        bucket.setCreationDate(new Date());
        return bucket;
    }

    /**
     * Instances of {@code AmazonS3Util} should not be constructed.
     */
    private AmazonS3Util() {
        throw new UnsupportedOperationException();
    }
}
