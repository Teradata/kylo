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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.spi.FileSystemProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3a.BasicAWSCredentialsProvider;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.hadoop.fs.s3a.S3AUtils;
import org.apache.hadoop.fs.s3a.S3ClientFactory;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
        // Determine the credentials
        final AmazonS3 s3;
        final URI uri = path.toUri();

        if ("s3".equalsIgnoreCase(uri.getScheme()) || "s3bfs".equalsIgnoreCase(uri.getScheme()) || "s3n".equalsIgnoreCase(uri.getScheme())) {
            s3 = createS3Client(uri, conf);
        } else if ("s3a".equalsIgnoreCase(uri.getScheme())) {
            final Class<? extends S3ClientFactory> s3ClientFactoryClass = conf.getClass(Constants.S3_CLIENT_FACTORY_IMPL, Constants.DEFAULT_S3_CLIENT_FACTORY_IMPL, S3ClientFactory.class);
            try {
                s3 = ReflectionUtils.newInstance(s3ClientFactoryClass, conf).createS3Client(uri);
            } catch (final IOException e) {
                throw new IllegalArgumentException("Unable to create S3 client: " + e, e);
            }
        } else {
            log.debug("Scheme {} not supported for S3 path: {}", uri.getScheme(), path);
            throw new CatalogException("catalog.fs.s3.invalidScheme", uri.getScheme());
        }

        // Fetch the list of buckets
        try {
            return s3.listBuckets().stream()
                .map(bucket -> {
                    final DataSetFile file = new DataSetFile();
                    file.setName(bucket.getName());
                    file.setDirectory(true);
                    file.setModificationTime(bucket.getCreationDate().getTime());
                    file.setPath(uri.getScheme() + "://" + bucket.getName() + "/");
                    return file;
                })
                .collect(Collectors.toList());
        } finally {
            s3.shutdown();
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
        getCustomCredentialsProvider(uri, conf).ifPresent(credentials::add);
        getBasicCredentialsProvider(uri, conf).ifPresent(credentials::add);
        credentials.add(InstanceProfileCredentialsProvider.getInstance());

        // Create client
        final AWSCredentialsProviderChain chain = new AWSCredentialsProviderChain(credentials);
        chain.setReuseLastProvider(true);
        return AmazonS3ClientBuilder.standard().withCredentials(chain).build();
    }

    /**
     * Gets a basic credentials provider from the specified Hadoop configuration.
     */
    @Nonnull
    @VisibleForTesting
    Optional<AWSCredentialsProvider> getBasicCredentialsProvider(@Nonnull final URI uri, @Nonnull final Configuration conf) {
        // Read credentials from configuration
        String scheme = uri.getScheme();

        final String accessKeyProperty = "fs." + scheme + ".awsAccessKeyId";
        final String accessKey = conf.get(accessKeyProperty);

        final String secretKeyProperty = "fs." + scheme + ".awsSecretAccessKey";
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

    /**
     * Gets a custom credentials provider from the specified Hadoop configuration.
     */
    @Nonnull
    @VisibleForTesting
    Optional<AWSCredentialsProvider> getCustomCredentialsProvider(@Nonnull final URI uri, @Nonnull final Configuration conf) {
        return Optional.ofNullable(conf.getClass("fs.s3.customAWSCredentialsProvider", null))
            .map(providerClass -> {
                try {
                    return S3AUtils.createAWSCredentialProvider(conf, providerClass, uri);
                } catch (final IOException e) {
                    throw new IllegalArgumentException("Unable to create S3 client: " + e, e);
                }
            });
    }
}
