package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * kylo-nifi-rest-client-v1
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

import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.glassfish.jersey.media.multipart.MultiPart;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static com.thinkbiganalytics.nifi.v1.rest.util.CatchNifiConnectionExceptionsUtil.catchConnectionExceptions;

public class NiFiJerseyRestClient extends JerseyRestClient {

    public NiFiJerseyRestClient(JerseyClientConfig config) {
        super(config);
    }

    @Override
    public <T> Future<T> getAsync(String path, Map<String, Object> params, Class<T> clazz) {
        return catchConnectionExceptions(super::getAsync, path, params, clazz);
    }

    @Override
    public <T> Future<T> getAsync(String path, Map<String, Object> params, GenericType<T> type) {
        return catchConnectionExceptions(super::getAsync, path, params, type);
    }

    @Override
    public <T> T get(Invocation.Builder builder, Class<T> clazz) {
        return catchConnectionExceptions(super::get, builder, clazz);
    }

    @Override
    public <T> T get(WebTarget target, Class<T> clazz) {
        return catchConnectionExceptions(super::get, target, clazz);
    }

    @Override
    public <T> T get(Invocation.Builder builder, Class<T> clazz, boolean logError) {
        return catchConnectionExceptions(super::get, builder, clazz, logError);
    }

    @Override
    public <T> T get(WebTarget target, Class<T> clazz, boolean logError) {
        return catchConnectionExceptions(super::get, target, clazz, logError);
    }

    @Override
    public <T> T get(String path, Map<String, Object> params, Class<T> clazz) {
        return catchConnectionExceptions(super::get, path, params, clazz);
    }

    @Override
    public <T> T get(String path, Map<String, Object> params, Class<T> clazz, boolean logError) {
        return catchConnectionExceptions(super::get, path, params, clazz, logError);
    }

    @Override
    public <T> T getWithHeaders(String path, MultivaluedMap<String, Object> headers, Map<String, Object> params, Class<T> clazz) {
        return catchConnectionExceptions(super::getWithHeaders, path, headers, params, clazz);
    }

    @Override
    public <T> T getWithoutErrorLogging(String path, Map<String, Object> params, Class<T> clazz) {
        return catchConnectionExceptions(super::getWithoutErrorLogging, path, params, clazz);
    }

    @Override
    public <T> T getFromPathString(String path, Class<T> clazz) {
        return catchConnectionExceptions(super::getFromPathString, path, clazz);
    }

    @Override
    public <T> T get(String path, Class<T> clazz) {
        return catchConnectionExceptions(super::get, path, clazz);
    }

    @Override
    public <T> T get(String path, Map<String, Object> params, GenericType<T> type) {
        return catchConnectionExceptions(super::get, path, params, type);
    }

    @Override
    public Response post(String path, Object o) {
        return catchConnectionExceptions(super::post, path, o);
    }

    @Override
    public <T> T postMultiPart(String path, MultiPart object, Class<T> returnType) {
        return catchConnectionExceptions(super::postMultiPart, path, object, returnType);
    }

    @Override
    public <T> T postMultiPartStream(String path, String name, String fileName, InputStream stream, Class<T> returnType) {
        return catchConnectionExceptions(super::postMultiPartStream, path, name, fileName, stream, returnType);
    }

    @Override
    public <T> T post(String path, Object object, Class<T> returnType) {
        return catchConnectionExceptions(super::post, path, object, returnType);
    }

    @Override
    public <T> T put(String path, Object object, Class<T> returnType) {
        return catchConnectionExceptions(super::put, path, object, returnType);
    }

    @Override
    public <T> T delete(String path, Map<String, Object> params, Class<T> returnType) {
        return catchConnectionExceptions(super::delete, path, params, returnType);
    }

    @Override
    public <T> T deleteWithHeaders(String path, MultivaluedMap<String, Object> headers, Map<String, Object> params, Class<T> clazz) {
        return catchConnectionExceptions(super::deleteWithHeaders, path, headers, params, clazz);
    }

    @Override
    public <T> T postForm(String path, Form form, Class<T> returnType) {
        return catchConnectionExceptions(super::postForm, path, form, returnType);
    }

    @Override
    public <T> Future<T> postAsync(String path, Object object, Class<T> returnType) {
        return catchConnectionExceptions(super::postAsync, path, object, returnType);
    }

}
