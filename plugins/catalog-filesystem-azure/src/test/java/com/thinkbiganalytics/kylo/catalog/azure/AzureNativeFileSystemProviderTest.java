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

import com.google.common.base.Throwables;
import com.microsoft.azure.storage.blob.BlobContainerProperties;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetFile;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

public class AzureNativeFileSystemProviderTest {

    /**
     * Scheme for WASB paths
     */
    private static final URI WASB = URI.create("wasb://example.blob.core.windows.net/");

    /**
     * Verify listing containers using the wasb scheme.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void listFiles() {
        // Test listing containers
        final AzureNativeFileSystemProvider provider = new AzureNativeFileSystemProvider() {
            @Nonnull
            @Override
            protected Iterable<CloudBlobContainer> listContainers(@Nonnull final CloudBlobClient client) {
                return Arrays.asList(createContainer("container1", client), createContainer("container2", client));
            }
        };

        final List<DataSetFile> files = provider.listFiles(new Path(WASB), new Configuration(false));
        Assert.assertThat(files, CoreMatchers.hasItems(isDataSetFile("container1"), isDataSetFile("container2")));
        Assert.assertEquals(2, files.size());
    }

    /**
     * Verifies that WASB paths are supported.
     */
    @Test
    public void supportsPath() {
        final AzureNativeFileSystemProvider provider = new AzureNativeFileSystemProvider();
        Assert.assertTrue("Expected to use provider for wasb://host/", provider.supportsPath(new Path("wasb://host/")));
        Assert.assertTrue("Expected to use provider for wasbs://host/", provider.supportsPath(new Path("wasbs://host/")));
        Assert.assertFalse("Expected to use Hadoop for wasb://container@host/", provider.supportsPath(new Path("wasb://container@host/")));
    }

    /**
     * Creates an Azure Storage container with the specified name.
     */
    @Nonnull
    private CloudBlobContainer createContainer(@Nonnull final String name, @Nonnull final CloudBlobClient client) {
        try {
            final CloudBlobContainer container = new CloudBlobContainer(name, client);
            final Method setLastModified = ReflectionUtils.findMethod(BlobContainerProperties.class, "setLastModified", Date.class);
            ReflectionUtils.makeAccessible(setLastModified);
            ReflectionUtils.invokeMethod(setLastModified, container.getProperties(), new Date());
            return container;
        } catch (final Exception e) {
            Assert.fail("Unable to create container: " + name + ": " + e);
            throw Throwables.propagate(e);
        }
    }

    /**
     * Matches data set files that contain the specified name.
     */
    @Nonnull
    private Matcher<DataSetFile> isDataSetFile(@Nonnull final String name) {
        return new CustomMatcher<DataSetFile>("data set file with name:" + name) {
            @Override
            public boolean matches(final Object item) {
                return (item instanceof DataSetFile)
                       && Objects.equals(name, ((DataSetFile) item).getName())
                       && Objects.equals(Boolean.TRUE, ((DataSetFile) item).isDirectory())
                       && Objects.equals("wasb://" + ((DataSetFile) item).getName() + "@" + WASB.getHost() + "/", ((DataSetFile) item).getPath());
            }
        };
    }
}
