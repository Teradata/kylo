package com.thinkbiganalytics.kylo.catalog.aws;

/*-
 * #%L
 * kylo-catalog-filesystem-aws-hadoop-v2
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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class S3FileSystemProviderTest {

    /**
     * Scheme for S3N paths
     */
    private static final URI S3N = URI.create("s3n:/");

    /**
     * Verify listing buckets using the s3n scheme.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void listFiles() {
        // Mock client
        final AmazonS3 client = Mockito.mock(AmazonS3.class);

        final Bucket bucket1 = createBucket("bucket1");
        final Bucket bucket2 = createBucket("bucket2");
        Mockito.when(client.listBuckets()).thenReturn(Arrays.asList(bucket1, bucket2));

        // Test listing buckets
        final S3FileSystemProvider provider = new S3FileSystemProvider() {
            @Override
            protected AmazonS3 createS3Client(@Nonnull final URI uri, @Nonnull final Configuration conf) {
                return client;
            }
        };

        final List<DataSetFile> files = provider.listFiles(new Path(S3N), new Configuration(false));
        Assert.assertThat(files, CoreMatchers.hasItems(isDataSetFile(bucket1), isDataSetFile(bucket2)));
        Assert.assertEquals(2, files.size());
    }

    /**
     * Verifies that S3 paths without a host are supported.
     */
    @Test
    public void supportsPath() {
        final S3FileSystemProvider provider = new S3FileSystemProvider();
        Assert.assertTrue("Expected to use provider for s3:///", provider.supportsPath(new Path("s3:///")));
        Assert.assertTrue("Expected to use provider for s3n:///", provider.supportsPath(new Path(S3N)));
        Assert.assertFalse("Expected to use Hadoop for s3n://bucket/", provider.supportsPath(new Path(S3N.resolve("//bucket/"))));
    }

    /**
     * Verify reading basic credentials.
     */
    @Test
    @SuppressWarnings("squid:S3655")
    public void getBasicCredentialsProvider() {
        // Test empty configuration
        final S3FileSystemProvider provider = new S3FileSystemProvider();
        Assert.assertFalse("Expected empty credentials", provider.getBasicCredentialsProvider(S3N, new Configuration(false)).isPresent());

        // Test configuration credentials
        final Configuration conf = new Configuration(false);
        conf.set("fs.s3n.awsAccessKeyId", "S3N_ACCESS_KEY");
        conf.set("fs.s3n.awsSecretAccessKey", "S3N_SECRET_KEY");

        final Optional<AWSCredentialsProvider> credentialsProvider = provider.getBasicCredentialsProvider(S3N, conf);
        Assert.assertTrue("Expected URI credentials", credentialsProvider.isPresent());
        final AWSCredentials credentials = credentialsProvider.get().getCredentials();
        Assert.assertEquals("S3N_ACCESS_KEY", credentials.getAWSAccessKeyId());
        Assert.assertEquals("S3N_SECRET_KEY", credentials.getAWSSecretKey());
    }

    /**
     * Creates an S3 bucket with the specified name.
     */
    @Nonnull
    private Bucket createBucket(@Nonnull final String name) {
        final Bucket bucket = new Bucket(name);
        bucket.setCreationDate(new Date());
        return bucket;
    }

    /**
     * Matches data set files that contain the same properties as the specified S3 bucket.
     */
    @Nonnull
    private Matcher<DataSetFile> isDataSetFile(@Nonnull final Bucket bucket) {
        return new BaseMatcher<DataSetFile>() {
            @Override
            public boolean matches(final Object item) {
                if (item instanceof DataSetFile) {
                    final DataSetFile file = (DataSetFile) item;
                    return Objects.equals(bucket.getName(), file.getName())
                           && Objects.equals(Boolean.TRUE, file.isDirectory())
                           && Objects.equals(bucket.getCreationDate().getTime(), file.getModificationTime())
                           && file.getPath().matches("s3[a-z]*://" + Pattern.quote(file.getName()) + "/");
                }
                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("data set file with name:" + bucket.getName() + " modifiedTime:" + bucket.getCreationDate() + ' ');
            }
        };
    }
}
