package com.thinkbiganalytics.nifi.feedmgr;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiControllerServicesRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptor;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.nifi.web.api.dto.AllowableValueDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.apache.nifi.web.api.entity.AllowableValueEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class TemplateCreationHelperTest {

    /**
     * Verify preferring enabled controller services over disabled controller services when updating processor properties.
     */
    @Test
    public void updateControllerServiceReferencesWithEnabled() {
        final AtomicReference<NifiProperty> updateProperty = new AtomicReference<>();

        // Mock NiFi client
        final LegacyNifiRestClient restClient = Mockito.mock(LegacyNifiRestClient.class);
        Mockito.when(restClient.getPropertyDescriptorTransform()).thenReturn(new MockNiFiPropertyDescriptorTransform());
        Mockito.doAnswer(invocation -> {
            updateProperty.set(invocation.getArgumentAt(2, NifiProperty.class));
            return null;

        }).when(restClient).updateProcessorProperty(Mockito.isNull(String.class), Mockito.eq("P1"), Mockito.any());

        final ControllerServiceDTO service1 = new ControllerServiceDTO();
        service1.setId("S1");
        service1.setName("Service1");
        service1.setProperties(Collections.emptyMap());
        service1.setState("DISABLED");

        final ControllerServiceDTO service2 = new ControllerServiceDTO();
        service2.setId("S2");
        service2.setName("Service2");
        service2.setProperties(Collections.emptyMap());
        service2.setState("ENABLED");
        Mockito.when(restClient.getControllerServices()).thenReturn(ImmutableSet.of(service1, service2));

        // Mock processors
        final ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setDescriptors(Collections.singletonMap("service", newPropertyDescriptor("service", "com.example.Service", "S1", "S2")));
        config.setProperties(Collections.singletonMap("service", "invalid"));

        final ProcessorDTO processor = new ProcessorDTO();
        processor.setId("P1");
        processor.setName("Processor1");
        processor.setConfig(config);

        // Update processors
        final TemplateCreationHelper helper = new TemplateCreationHelper(restClient);
        helper.snapshotControllerServiceReferences();
        helper.identifyNewlyCreatedControllerServiceReferences();
        helper.updateControllerServiceReferences(Collections.singletonList(processor));

        // Verify new processor properties
        Assert.assertNotNull("Property 'Service' not set on processor 'Processor1'.", updateProperty.get());
        Assert.assertEquals("S2", updateProperty.get().getValue());
    }

    /**
     * Verify recursively enabling controller services when updating processor properties.
     */
    @Test
    public void updateControllerServiceReferencesWithRecursive() {
        final List<ControllerServiceDTO> updatedControllerServices = new ArrayList<>();
        final List<NifiProperty> updatedProperties = new ArrayList<>();

        // Mock NiFi client
        final NiFiControllerServicesRestClient controllerServicesRestClient = Mockito.mock(NiFiControllerServicesRestClient.class);
        Mockito.when(controllerServicesRestClient.update(Mockito.any())).thenAnswer(answer -> {
            final ControllerServiceDTO controllerService = answer.getArgumentAt(0, ControllerServiceDTO.class);
            updatedControllerServices.add(controllerService);
            return controllerService;
        });

        final NiFiRestClient restClient = Mockito.mock(NiFiRestClient.class);
        Mockito.when(restClient.controllerServices()).thenReturn(controllerServicesRestClient);

        // Mock Legacy NiFi client
        final LegacyNifiRestClient legacyRestClient = Mockito.mock(LegacyNifiRestClient.class);
        Mockito.when(legacyRestClient.enableControllerServiceAndSetProperties(Mockito.any(), Mockito.any())).thenReturn(new ControllerServiceDTO());
        Mockito.when(legacyRestClient.getNiFiRestClient()).thenReturn(restClient);
        Mockito.when(legacyRestClient.getPropertyDescriptorTransform()).thenReturn(new MockNiFiPropertyDescriptorTransform());
        Mockito.doAnswer(invocation -> {
            updatedProperties.add(invocation.getArgumentAt(2, NifiProperty.class));
            return null;

        }).when(legacyRestClient).updateProcessorProperty(Mockito.isNull(String.class), Mockito.eq("P1"), Mockito.any());

        final ControllerServiceDTO service1 = new ControllerServiceDTO();
        service1.setDescriptors(Collections.singletonMap("service", newPropertyDescriptor("service", "com.example.Service2", "S2")));
        service1.setId("S1");
        service1.setName("Service1");
        service1.setProperties(newHashMap("service", "invalid"));
        service1.setState("DISABLED");

        final ControllerServiceDTO service2 = new ControllerServiceDTO();
        service2.setId("S2");
        service2.setName("Service2");
        service2.setProperties(new HashMap<>());
        service2.setState("ENABLED");
        Mockito.when(legacyRestClient.getControllerServices()).thenReturn(ImmutableSet.of(service1, service2));

        // Mock processors
        final ProcessorConfigDTO config = new ProcessorConfigDTO();
        config.setDescriptors(Collections.singletonMap("service", newPropertyDescriptor("service", "com.example.Service1", "S1")));
        config.setProperties(Collections.singletonMap("service", "invalid"));

        final ProcessorDTO processor = new ProcessorDTO();
        processor.setId("P1");
        processor.setName("Processor1");
        processor.setConfig(config);

        // Update processors
        final TemplateCreationHelper helper = new TemplateCreationHelper(legacyRestClient);
        helper.snapshotControllerServiceReferences();
        helper.identifyNewlyCreatedControllerServiceReferences();
        helper.updateControllerServiceReferences(Collections.singletonList(processor));

        // Verify updated properties
        Assert.assertEquals("Property 'Service' not set on controller service 'Server1'.", 1, updatedControllerServices.size());
        Assert.assertEquals("S2", updatedControllerServices.get(0).getProperties().get("service"));

        Assert.assertEquals("Property 'Service' not set on processor 'Processor1'.", 1, updatedProperties.size());
        Assert.assertEquals("S1", updatedProperties.get(0).getValue());
    }

    /**
     * Creates a new {@link HashMap} with the specified entry.
     *
     * @param k1 the key for the entry
     * @param v1 the value for the entry
     * @return the new {@code HashMap}
     */
    @Nonnull
    private <K, V> HashMap<K, V> newHashMap(K k1, V v1) {
        final HashMap<K, V> map = new HashMap<K, V>();
        map.put(k1, v1);
        return map;
    }

    /**
     * Creates a new {@link PropertyDescriptorDTO} that identifies the specified controller service.
     *
     * @param name            the name of the property
     * @param type            the type of controller service
     * @param allowableValues the allowable controller service ids
     * @return the new property descriptor
     */
    @Nonnull
    private PropertyDescriptorDTO newPropertyDescriptor(@Nonnull final String name, @Nonnull final String type, @Nonnull final String... allowableValues) {
        // Create the list of allowable values
        final List<AllowableValueEntity> allowableValueEntities = Stream.of(allowableValues)
            .map(value -> {
                final AllowableValueDTO dto = new AllowableValueDTO();
                dto.setValue(value);
                return dto;
            })
            .map(dto -> {
                final AllowableValueEntity entity = new AllowableValueEntity();
                entity.setAllowableValue(dto);
                return entity;
            })
            .collect(Collectors.toList());

        // Create the property descriptor
        final PropertyDescriptorDTO property = new PropertyDescriptorDTO();
        property.setAllowableValues(allowableValueEntities);
        property.setName(name);
        property.setIdentifiesControllerService(type);
        property.setRequired(true);
        return property;
    }

    /**
     * Implementation of {@link NiFiPropertyDescriptorTransform} for testing.
     */
    private class MockNiFiPropertyDescriptorTransform implements NiFiPropertyDescriptorTransform {

        @Override
        public Boolean isSensitive(@Nonnull final PropertyDescriptorDTO propertyDescriptorDTO) {
            return false;
        }

        @Nonnull
        @Override
        public NiFiPropertyDescriptor toNiFiPropertyDescriptor(@Nonnull final PropertyDescriptorDTO dto) {
            final NiFiPropertyDescriptor nifi = new NiFiPropertyDescriptor();
            nifi.setName(dto.getName());
            nifi.setRequired(dto.isRequired());
            nifi.setIdentifiesControllerService(dto.getIdentifiesControllerService());

            if (dto.getAllowableValues() != null) {
                final List<NiFiAllowableValue> allowableValues = dto.getAllowableValues().stream()
                    .map(AllowableValueEntity::getAllowableValue)
                    .map(allowableValueDTO -> {
                        final NiFiAllowableValue nifiAllowableValue = new NiFiAllowableValue();
                        nifiAllowableValue.setValue(allowableValueDTO.getValue());
                        return nifiAllowableValue;
                    })
                    .collect(Collectors.toList());
                nifi.setAllowableValues(allowableValues);
            }

            return nifi;
        }

        @Override
        public String getQueuedCount(ProcessGroupStatusDTO processGroupStatusDTO) {
            return null;
        }

    }
}
