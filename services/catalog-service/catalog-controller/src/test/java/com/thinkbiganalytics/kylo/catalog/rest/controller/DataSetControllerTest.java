package com.thinkbiganalytics.kylo.catalog.rest.controller;

/*-
 * #%L
 * kylo-catalog-controller
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

import com.thinkbiganalytics.kylo.catalog.CatalogException;
import com.thinkbiganalytics.kylo.catalog.dataset.DataSetProvider;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.StaticMessageSource;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public class DataSetControllerTest {

    /**
     * Verify creating a data set.
     */
    @Test
    public void createDataSet() {
        // Mock data set provider
        final DataSetProvider provider = Mockito.mock(DataSetProvider.class);

        final DataSet dataSet = new DataSet();
        dataSet.setId(UUID.randomUUID().toString());
        Mockito.when(provider.createDataSet(Mockito.any(DataSet.class))).thenReturn(dataSet);

        // Test creating a data set
        final DataSetController controller = newDataSetController();
        controller.dataSetProvider = provider;

        final Response response = controller.createDataSet(new DataSet());
        Assert.assertEquals(dataSet, response.getEntity());
    }

    /**
     * Verify exception for creating an invalid data set.
     */
    @Test(expected = BadRequestException.class)
    public void createDataSetWithInvalid() {
        // Mock data set provider
        final DataSetProvider provider = Mockito.mock(DataSetProvider.class);
        Mockito.when(provider.createDataSet(Mockito.any(DataSet.class))).thenThrow(new CatalogException("invalid"));

        // Test creating data set
        final DataSetController controller = newDataSetController();
        controller.dataSetProvider = provider;
        controller.createDataSet(new DataSet());
    }

    /**
     * Verify retrieving a data set.
     */
    @Test
    public void getDataSet() {
        // Mock data set provider
        final DataSetProvider provider = Mockito.mock(DataSetProvider.class);

        final DataSet dataSet = new DataSet();
        dataSet.setId(UUID.randomUUID().toString());
        Mockito.when(provider.findDataSet(dataSet.getId())).thenReturn(Optional.of(dataSet));

        // Test retrieving data set
        final DataSetController controller = newDataSetController();
        controller.dataSetProvider = provider;

        final Response response = controller.getDataSet(dataSet.getId());
        Assert.assertEquals(dataSet, response.getEntity());
    }

    /**
     * Verify exception for retrieving a missing data set.
     */
    @Test(expected = NotFoundException.class)
    public void getDataSetForMissing() {
        // Mock data set provider
        final DataSetProvider provider = Mockito.mock(DataSetProvider.class);
        Mockito.when(provider.findDataSet(Mockito.anyString())).thenReturn(Optional.empty());

        // Test retrieving data set
        final DataSetController controller = newDataSetController();
        controller.dataSetProvider = provider;
        controller.getDataSet("DT1");
    }

    /**
     * Creates a new data set controller.
     */
    @Nonnull
    private DataSetController newDataSetController() {
        final DataSetController controller = new DataSetController();
        controller.dataSetProvider = Mockito.mock(DataSetProvider.class);
        controller.request = Mockito.mock(HttpServletRequest.class);

        final StaticMessageSource messages = new StaticMessageSource();
        messages.setUseCodeAsDefaultMessage(true);
        controller.messages = messages;

        return controller;
    }
}
