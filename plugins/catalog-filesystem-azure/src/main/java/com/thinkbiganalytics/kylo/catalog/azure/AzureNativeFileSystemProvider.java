package com.thinkbiganalytics.kylo.catalog.azure;

/*-
 * #%L
 * kylo-catalog-filesystem-azure
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

import com.google.common.annotations.VisibleForTesting;
import com.microsoft.azure.storage.RetryExponentialRetry;
import com.microsoft.azure.storage.RetryPolicyFactory;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;
import com.thinkbiganalytics.kylo.catalog.spi.FileSystemProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.azure.AzureNativeFileSystemStore;
import org.apache.hadoop.fs.azure.KeyProviderException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public class AzureNativeFileSystemProvider implements FileSystemProvider {

    @Nonnull
    @Override
    @SuppressWarnings("squid:S1075")
    public List<DataSetFile> listFiles(@Nonnull final Path path, @Nonnull final Configuration conf) {
        // Create Azure Blob client
        final URI uri = path.toUri();
        final CloudBlobClient client = createBlobClient(uri, conf);

        // List containers as data set files
        final String pathPrefix = uri.getScheme() + "://";
        final String pathSuffix = "@" + uri.getHost() + (uri.getPort() > -1 ? ":" + uri.getPort() : "") + "/";
        return StreamSupport.stream(listContainers(client).spliterator(), false)
            .map(container -> {
                final DataSetFile file = new DataSetFile();
                file.setName(container.getName());
                file.setDirectory(true);
                file.setModificationTime(container.getProperties().getLastModified().getTime());
                file.setPath(pathPrefix + container.getName() + pathSuffix);
                return file;
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean supportsPath(@Nonnull Path path) {
        return StringUtils.equalsAnyIgnoreCase(path.toUri().getScheme(), "asv", "asvs", "wasb", "wasbs")
               && StringUtils.isEmpty(path.toUri().getUserInfo())
               && StringUtils.isNotEmpty(path.toUri().getHost())
               && StringUtils.equalsAny(path.toUri().getPath(), null, "", "/");
    }

    @Nonnull
    private CloudBlobClient createBlobClient(@Nonnull final URI uri, @Nonnull final Configuration conf) {
        // Determine endpoint
        final String httpScheme = StringUtils.equalsAnyIgnoreCase(uri.getScheme(), "asvs", "wasbs") ? "https" : "http";
        final URI blobEndPoint = URI.create(httpScheme + "://" + uri.getRawAuthority());

        // Create client
        final CloudBlobClient client = new CloudBlobClient(blobEndPoint, getCredentials(uri, conf));

        final RetryPolicyFactory retryPolicyFactory = new RetryExponentialRetry(conf.getInt("fs.azure.io.retry.min.backoff.interval", 3 * 1000 /* 1s */),
                                                                                conf.getInt("fs.azure.io.retry.backoff.interval", 3 * 1000 /* 1s */),
                                                                                conf.getInt("fs.azure.io.retry.max.backoff.interval", 30 * 1000 /* 30s */),
                                                                                conf.getInt("fs.azure.io.retry.max.retries", 30));
        client.getDefaultRequestOptions().setRetryPolicyFactory(retryPolicyFactory);

        final int storageConnectionTimeout = conf.getInt("fs.azure.storage.timeout", 0);
        if (storageConnectionTimeout > 0) {
            client.getDefaultRequestOptions().setTimeoutIntervalInMs(storageConnectionTimeout * 1000);
        }

        return client;
    }

    @Nullable
    private StorageCredentials getCredentials(@Nonnull final URI uri, @Nonnull final Configuration conf) {
        // Find account name
        final String accountName = uri.getHost();
        if (StringUtils.isEmpty(accountName)) {
            throw new CatalogException("catalog.fs.azure.missingAccountName");
        }

        // Find account key
        final String accountKey;
        try {
            accountKey = AzureNativeFileSystemStore.getAccountKeyFromConfiguration(accountName, conf);
        } catch (final KeyProviderException e) {
            throw new CatalogException("catalog.fs.azure.invalidAccountKey");
        }

        // Create credentials
        if (StringUtils.isNotEmpty(accountKey)) {
            final String rawAccountName = accountName.split("\\.")[0];
            return new StorageCredentialsAccountAndKey(rawAccountName, accountKey);
        } else {
            return null;
        }
    }

    @Nonnull
    @VisibleForTesting
    protected Iterable<CloudBlobContainer> listContainers(@Nonnull final CloudBlobClient client) {
        return client.listContainers();
    }
}
