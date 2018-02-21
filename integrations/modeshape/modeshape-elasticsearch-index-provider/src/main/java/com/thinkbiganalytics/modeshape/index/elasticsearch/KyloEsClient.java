package com.thinkbiganalytics.modeshape.index.elasticsearch;

/*-
 * #%L
 * kylo-modeshape-elasticsearch-index-provider
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.modeshape.jcr.index.elasticsearch.client.EsClient;
import org.modeshape.jcr.index.elasticsearch.client.EsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;

import java.io.IOException;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A REST-based client to interact with Elasticsearch. Facilitates integration with modeshape.
 */
@SuppressWarnings("Annotator")
public class KyloEsClient extends EsClient {

    private static final Logger log = LoggerFactory.getLogger(KyloEsClient.class);

    @Inject
    private MetadataAccess metadataAccess;

    private static final String ALLOW_INDEXING_PROPERTY = "tba:allowIndexing";
    private static final String INDEX_INDICATOR_FOR_NO = "N";
    private final String kyloHost;
    private final int kyloPort;

    public KyloEsClient(String host, int port) {
        super(host, port);
        this.kyloHost = host;
        this.kyloPort = port;
        log.debug("KyloEsClient initialized with host {} and port {}", this.kyloHost, this.kyloPort);
    }

    @Override
    public boolean storeDocument(String name, String type, String id,
                                 EsRequest doc) throws IOException {
        log.debug("KyloEsClient - store document in ES: name={}, type={}, id={}, doc={}", name, type, id, doc.toString());

        if (name.contains("kylo-categories")) {
            // Categories start
            log.debug("Indexing a category's metadata now");
            KyloEsNodeDetails kyloEsNodeDetails = metadataAccess.read(() -> {
                Session session = JcrMetadataAccess.getActiveSession();
                KyloEsNodeDetails kyloEsNodeDetailsInternal = new KyloEsNodeDetails();
                if (session != null) {
                    Node node = session.getNodeByIdentifier(id);
                    if (node != null) {
                        kyloEsNodeDetailsInternal.setAllowIndexing(node.getProperty(ALLOW_INDEXING_PROPERTY).getValue().getString());
                        kyloEsNodeDetailsInternal.setExtendedId(id);
                        log.debug("KyloEsClient - (category metadata) - indexing={}, id={}",
                                  kyloEsNodeDetailsInternal.getAllowIndexing(), kyloEsNodeDetailsInternal.getExtendedId());
                    } else {
                        log.warn("KyloEsClient - (category metadata) null node retrieved from metadata for id {}", id);
                    }
                } else {
                    log.warn("KyloEsClient - (category metadata) store document in ES: Could not get active session to metadata");
                }
                log.debug("KyloEsClient - (category metadata) node details: allow indexing = {}, extended id = {}, id = {}",
                          kyloEsNodeDetailsInternal.getAllowIndexing(), kyloEsNodeDetailsInternal.getExtendedId(), kyloEsNodeDetailsInternal.getId());

                return kyloEsNodeDetailsInternal;
            }, MetadataAccess.SERVICE);

            if (kyloEsNodeDetails != null) {
                String indexIndicator = kyloEsNodeDetails.getAllowIndexing();

                //N indicates do-not-index, otherwise index
                if ((indexIndicator != null) && (indexIndicator.equals(INDEX_INDICATOR_FOR_NO))) {
                    log.debug("KyloEsClient - Category metadata indexing not allowed for extended id= {}, id = {}", kyloEsNodeDetails.getExtendedId(), kyloEsNodeDetails.getId());
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpDelete method = new HttpDelete(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, name, type, kyloEsNodeDetails.getExtendedId()));
                    try {
                        log.debug("KyloEsClient - Doc id to be deleted from Elasticsearch: {}. (The modeshape identifier is {})", kyloEsNodeDetails.getExtendedId(), kyloEsNodeDetails.getId());
                        CloseableHttpResponse resp = client.execute(method);
                        int statusCode = resp.getStatusLine().getStatusCode();
                        log.debug("KyloEsClient - Deletion result (Expected 200/404): {}", statusCode);
                        return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_FOUND;
                    } finally {
                        method.releaseConnection();
                    }
                } else {
                    log.debug("KyloEsClient - Category metadata indexing allowed for extended id= {}, id = {}", kyloEsNodeDetails.getExtendedId(), kyloEsNodeDetails.getId());
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost method = new HttpPost(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, name, type, id));

                    boolean hasIndexingBeenTurnedOn = checkIfIndexingHasBeenTurnedOnForCategories(doc);
                    log.debug("Check if indexing has been turned on reveals (for category metadata): {}", hasIndexingBeenTurnedOn);

                    JSONObject fullDocumentJsonObject = new JSONObject();
                    if (hasIndexingBeenTurnedOn) {
                        fullDocumentJsonObject = metadataAccess.read(() -> {
                            Session session = JcrMetadataAccess.getActiveSession();
                            JSONObject fullDocumentJsonObjectInternal = new JSONObject();
                            if (session != null) {
                                Node node = session.getNodeByIdentifier(id);
                                if (node != null) {
                                    PropertyIterator propertyIterator = node.getProperties();
                                    while (propertyIterator.hasNext()) {
                                        Property property = (Property) propertyIterator.next();
                                        log.debug(property.toString());
                                        if ((property.toString().contains("jcr:title")) || (property.toString().contains("tba:allowIndexing")) || (property.toString().contains("tba:systemName"))
                                            || (property.toString().contains("jcr:description"))) {
                                            try {
                                                String propertyName = property.getName();
                                                String propertyValue = property.getString();
                                                fullDocumentJsonObjectInternal.put(propertyName, propertyValue);
                                                fullDocumentJsonObjectInternal.put("lowercase_" + propertyName, propertyValue.toLowerCase());
                                                fullDocumentJsonObjectInternal.put("uppercase_" + propertyName, propertyValue.toUpperCase());
                                                fullDocumentJsonObjectInternal.put("length_" + propertyName, propertyValue.length());
                                            } catch (JSONException e) {
                                                log.error("Error encountered when constructing JSON payload for category metadata to index, category id = {}: {}", id, e.getMessage());
                                                e.printStackTrace();
                                            } catch (RepositoryException e) {
                                                log.error("Error encountered related to repository when trying to index category metadata, category id = {}: {}", id, e.getMessage());
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                } else {
                                    log.warn("KyloEsClient - (category metadata) null node retrieved from metadata for category id {}", id);
                                }
                            } else {
                                log.warn("KyloEsClient - (category metadata) store document in ES: Could not get active session to metadata");
                            }
                            return fullDocumentJsonObjectInternal;
                        }, MetadataAccess.SERVICE);

                        log.debug("Full document JSON for category metadata to index: {}", fullDocumentJsonObject);
                    }

                    try {
                        StringEntity requestEntity;
                        if (hasIndexingBeenTurnedOn) {
                            requestEntity = new StringEntity(fullDocumentJsonObject.toString(), ContentType.APPLICATION_JSON);
                        } else {
                            requestEntity = new StringEntity(doc.toString(), ContentType.APPLICATION_JSON);
                        }
                        method.setEntity(requestEntity);
                        CloseableHttpResponse resp = client.execute(method);
                        int statusCode = resp.getStatusLine().getStatusCode();
                        log.info("Indexing result for category id {} (Expected 200/201): {}", id, statusCode);
                        return statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK;
                    } finally {
                        method.releaseConnection();
                    }
                }
            }
            // Categories end
        } else if (name.contains("kylo-feeds")) {
            // Feeds start
            log.debug("Indexing a feed's metadata now");
            KyloEsNodeDetails kyloEsNodeDetails = metadataAccess.read(() -> {
                Session session = JcrMetadataAccess.getActiveSession();
                KyloEsNodeDetails kyloEsNodeDetailsInternal = new KyloEsNodeDetails();
                if (session != null) {
                    Node node = session.getNodeByIdentifier(id);
                    if (node != null) {
                        kyloEsNodeDetailsInternal.setAllowIndexing(node.getProperty(ALLOW_INDEXING_PROPERTY).getValue().getString());
                        kyloEsNodeDetailsInternal.setExtendedId(id);
                        log.debug("KyloEsClient - (feed metadata) - indexing={}, id={}",
                                  kyloEsNodeDetailsInternal.getAllowIndexing(), kyloEsNodeDetailsInternal.getExtendedId());
                    } else {
                        log.warn("KyloEsClient - (feed metadata) null node retrieved from metadata for id {}", id);
                    }
                } else {
                    log.warn("KyloEsClient - (feed metadata) store document in ES: Could not get active session to metadata");
                }
                log.debug("KyloEsClient - (feed metadata) node details: allow indexing = {}, extended id = {}, id = {}",
                          kyloEsNodeDetailsInternal.getAllowIndexing(), kyloEsNodeDetailsInternal.getExtendedId(), kyloEsNodeDetailsInternal.getId());

                return kyloEsNodeDetailsInternal;
            }, MetadataAccess.SERVICE);

            if (kyloEsNodeDetails != null) {
                String indexIndicator = kyloEsNodeDetails.getAllowIndexing();

                //N indicates do-not-index, otherwise index
                if ((indexIndicator != null) && (indexIndicator.equals(INDEX_INDICATOR_FOR_NO))) {
                    log.debug("KyloEsClient - Feed metadata indexing not allowed for extended id= {}, id = {}", kyloEsNodeDetails.getExtendedId(), kyloEsNodeDetails.getId());
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpDelete method = new HttpDelete(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, name, type, kyloEsNodeDetails.getExtendedId()));
                    try {
                        log.debug("KyloEsClient - Doc id to be deleted from Elasticsearch: {}. (The modeshape identifier is {})", kyloEsNodeDetails.getExtendedId(), kyloEsNodeDetails.getId());
                        CloseableHttpResponse resp = client.execute(method);
                        int statusCode = resp.getStatusLine().getStatusCode();
                        log.debug("KyloEsClient - Deletion result (Expected 200/404): {}", statusCode);
                        return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_FOUND;
                    } finally {
                        method.releaseConnection();
                    }
                } else {
                    log.debug("KyloEsClient - Feed metadata indexing allowed for extended id= {}, id = {}", kyloEsNodeDetails.getExtendedId(), kyloEsNodeDetails.getId());
                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost method = new HttpPost(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, name, type, id));

                    boolean hasIndexingBeenTurnedOn = checkIfIndexingHasBeenTurnedOnForFeeds(doc);
                    log.debug("Check if indexing has been turned on reveals (for feed metadata): {}", hasIndexingBeenTurnedOn);

                    JSONObject fullDocumentJsonObject = new JSONObject();
                    if (hasIndexingBeenTurnedOn) {
                        fullDocumentJsonObject = metadataAccess.read(() -> {
                            Session session = JcrMetadataAccess.getActiveSession();
                            JSONObject fullDocumentJsonObjectInternal = new JSONObject();
                            if (session != null) {
                                Node node = session.getNodeByIdentifier(id);
                                if (node != null) {
                                    PropertyIterator propertyIterator = node.getProperties();
                                    while (propertyIterator.hasNext()) {
                                        Property property = (Property) propertyIterator.next();
                                        log.debug(property.toString());
                                        if ((property.toString().contains("jcr:title")) || (property.toString().contains("tba:allowIndexing")) || (property.toString().contains("tba:systemName"))
                                            || (property.toString().contains("jcr:description")) || (property.toString().contains("tba:category")) || (property.toString().contains("tba:tags"))) {
                                            try {
                                                String propertyName = property.getName();
                                                String propertyValue;
                                                if (property.isMultiple()) {
                                                    propertyValue = StringUtils.join(property.getValues(), " ");
                                                } else {
                                                    propertyValue = property.getString();
                                                }

                                                fullDocumentJsonObjectInternal.put(propertyName, propertyValue);
                                                fullDocumentJsonObjectInternal.put("lowercase_" + propertyName, propertyValue.toLowerCase());
                                                fullDocumentJsonObjectInternal.put("uppercase_" + propertyName, propertyValue.toUpperCase());
                                                fullDocumentJsonObjectInternal.put("length_" + propertyName, propertyValue.length());
                                            } catch (JSONException e) {
                                                log.error("Error encountered when constructing JSON payload for feed metadata to index, feed id = {}: {}", id, e.getMessage());
                                                e.printStackTrace();
                                            } catch (RepositoryException e) {
                                                log.error("Error encountered related to repository when trying to index feed metadata: feed id = {}: {}", id, e.getMessage());
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                } else {
                                    log.warn("KyloEsClient - (feed metadata) null node retrieved from metadata for feed id {}", id);
                                }
                            } else {
                                log.warn("KyloEsClient - store document in ES: Could not get active session to metadata");
                            }
                            return fullDocumentJsonObjectInternal;
                        }, MetadataAccess.SERVICE);

                        log.debug("Full document JSON for feed metadata to index: {}", fullDocumentJsonObject);
                    }

                    try {
                        StringEntity requestEntity;
                        if (hasIndexingBeenTurnedOn) {
                            requestEntity = new StringEntity(fullDocumentJsonObject.toString(), ContentType.APPLICATION_JSON);
                        } else {
                            requestEntity = new StringEntity(doc.toString(), ContentType.APPLICATION_JSON);
                        }
                        method.setEntity(requestEntity);
                        CloseableHttpResponse resp = client.execute(method);
                        int statusCode = resp.getStatusLine().getStatusCode();
                        log.info("Indexing result for feed id {} (Expected 200/201): {}", id, statusCode);
                        return statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK;
                    } finally {
                        method.releaseConnection();
                    }
                }
            }
        }

        return false;
    }

    private boolean checkIfIndexingHasBeenTurnedOnForFeeds(EsRequest doc) {
        String descriptionCheck = (String) doc.get("jcr:description");
        String titleCheck = (String) doc.get("jcr:title");
        String categoryCheck = (String) doc.get("tba:category");
        String systemNameCheck = (String) doc.get("tba:systemName");
        String tagsCheck = (String) doc.get("tba:tags");
        String allowIndexingCheck = (String) doc.get("tba:allowIndexing");

        return ((descriptionCheck == null) && (titleCheck == null) && (categoryCheck == null) && (systemNameCheck == null) && (tagsCheck == null) && (!allowIndexingCheck.equals("N")));
    }

    private boolean checkIfIndexingHasBeenTurnedOnForCategories(EsRequest doc) {
        String titleCheck = (String) doc.get("jcr:title");
        String descriptionCheck = (String) doc.get("jcr:description");
        String systemNameCheck = (String) doc.get("tba:systemName");
        String allowIndexingCheck = (String) doc.get("tba:allowIndexing");

        return ((titleCheck == null) && (descriptionCheck == null) && (systemNameCheck == null) && (!allowIndexingCheck.equals("N")));
    }
}
