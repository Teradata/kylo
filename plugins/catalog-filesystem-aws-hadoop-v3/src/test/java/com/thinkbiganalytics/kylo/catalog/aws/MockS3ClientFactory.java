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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;

import org.apache.hadoop.fs.s3a.S3ClientFactory;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Arrays;

/**
 * Mock S3 client factory for testing.
 */
public class MockS3ClientFactory implements S3ClientFactory {

    static final Bucket BUCKET1 = AmazonS3Util.createBucket("bucket1");

    static final Bucket BUCKET2 = AmazonS3Util.createBucket("bucket2");

    @Override
    public AmazonS3 createS3Client(final URI uri) {
        return Mockito.when(Mockito.mock(AmazonS3.class).listBuckets()).thenReturn(Arrays.asList(BUCKET1, BUCKET2)).getMock();
    }
}
