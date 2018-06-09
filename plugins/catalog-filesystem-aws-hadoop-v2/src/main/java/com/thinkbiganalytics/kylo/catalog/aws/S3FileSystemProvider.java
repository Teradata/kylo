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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.annotations.VisibleForTesting;
import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.spi.FileSystemProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3a.BasicAWSCredentialsProvider;
import org.apache.hadoop.fs.s3a.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Provides access for listing buckets of S3 file systems.
 */
@Component
public class S3FileSystemProvider implements FileSystemProvider {

    private static final Logger log = LoggerFactory.getLogger(S3FileSystemProvider.class);

    @Nonnull
    @Override
    public List<DataSetFile> listFiles(@Nonnull final Path path, @Nonnull final Configuration conf) {
        final AmazonS3 s3 = createS3Client(path.toUri(), conf);
        try {
            return s3.listBuckets().stream()
                .map(bucket -> {
                    final DataSetFile file = new DataSetFile();
                    file.setName(bucket.getName());
                    file.setDirectory(true);
                    file.setModificationTime(bucket.getCreationDate().getTime());
                    file.setPath(path.toUri().getScheme() + "://" + bucket.getName() + "/");
                    return file;
                })
                .collect(Collectors.toList());
        } finally {
            if (s3 instanceof AmazonS3Client) {
                ((AmazonS3Client) s3).shutdown();
            }
        }
    }

    @Override
    public boolean supportsPath(@Nonnull final Path path) {
        final URI uri = path.toUri();
        return (uri.getScheme() != null && (uri.getScheme().startsWith("s3") || uri.getScheme().startsWith("S3")) && path.toUri().getHost() == null);
    }

    /**
     * Creates an S3 client with the standard credential providers.
     */
    @VisibleForTesting
    protected AmazonS3 createS3Client(@Nonnull final URI uri, @Nonnull final Configuration conf) {
        // Create list of credential providers
        final List<AWSCredentialsProvider> credentials = new ArrayList<>();
        getBasicCredentialsProvider(uri, conf).ifPresent(credentials::add);
        credentials.add(new InstanceProfileCredentialsProvider());

        // Create client
        final AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(credentials.toArray(new AWSCredentialsProvider[0]));
        chain.setReuseLastProvider(true);
        return new AmazonS3Client(chain);
    }

    /**
     * Gets a basic credentials provider from the specified Hadoop configuration.
     */
    @Nonnull
    @VisibleForTesting
    Optional<AWSCredentialsProvider> getBasicCredentialsProvider(@Nonnull final URI uri, @Nonnull final Configuration conf) {
        // Read credentials from configuration
        String scheme = uri.getScheme();

        final String accessKeyProperty = "s3a".equalsIgnoreCase(scheme) ? Constants.ACCESS_KEY : "fs." + scheme + ".awsAccessKeyId";
        final String accessKey = conf.get(accessKeyProperty);

        final String secretKeyProperty = "s3a".equalsIgnoreCase(scheme) ? Constants.SECRET_KEY : "fs." + scheme + ".awsSecretAccessKey";
        final String secretKey = conf.get(secretKeyProperty);

        // Return credentials provider
        if (accessKey != null && secretKey != null) {
            return Optional.of(new BasicAWSCredentialsProvider(accessKey, secretKey));
        } else if (secretKey != null) {
            throw new CatalogException("catalog.fs.s3.missingAccessKeyProperty", accessKeyProperty);
        } else if (accessKey != null) {
            throw new CatalogException("catalog.fs.s3.missingSecretAccessKeyProperty", secretKeyProperty);
        } else {
            log.info("AWS Access Key ID and Secret Access Key must be specified by setting the {} and {} properties (respectively).", accessKeyProperty, secretKeyProperty);
            return Optional.empty();
        }
    }
}
