package com.thinkbiganalytics.spark.rest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class CorsFilterTest {
    /** Verify headers set by CORS filter */
    @Test
    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        // Mock request
        Method method = CorsFilterTest.class.getMethod("test");
        Resource.Builder resource = Resource.builder();
        ResourceMethod getMethod = resource.addMethod(HttpMethod.GET).handledBy(CorsFilterTest.class, method).build();
        ResourceMethod postMethod = resource.addMethod(HttpMethod.POST).handledBy(CorsFilterTest.class, method).build();

        RuntimeResource runtimeResource = Mockito.when(Mockito.mock(RuntimeResource.class).getResourceMethods()).thenReturn(
                ImmutableList.of(getMethod, postMethod)).getMock();
        ExtendedUriInfo uriInfo = (ExtendedUriInfo)Mockito.when(Mockito.mock(ExtendedUriInfo.class).getMatchedRuntimeResources())
                .thenReturn((List)ImmutableList.of(runtimeResource)).getMock();
        ContainerRequest request = Mockito.when(Mockito.mock(ContainerRequest.class).getUriInfo()).thenReturn(uriInfo).getMock();

        // Mock response
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        ContainerResponse response = Mockito.when(Mockito.mock(ContainerResponse.class).getHeaders()).thenReturn(headers)
                .getMock();

        // Test headers
        CorsFilter filter = new CorsFilter();
        filter.filter(request, response);

        Assert.assertEquals(3, headers.size());
        Assert.assertEquals(ImmutableList.of("*"), headers.get("Access-Control-Allow-Origin"));
        Assert.assertEquals(ImmutableList.of("Content-Type"), headers.get("Access-Control-Allow-Headers"));

        List<Object> methodsList = headers.get("Access-Control-Allow-Methods");
        Assert.assertNotNull("Methods header should not be null", methodsList);
        Assert.assertEquals(1, methodsList.size());

        String[] methods = ((String)methodsList.get(0)).split(",");
        Arrays.sort(methods);
        Assert.assertArrayEquals(new String[] {"GET", "OPTIONS", "POST"}, methods);
    }
}
