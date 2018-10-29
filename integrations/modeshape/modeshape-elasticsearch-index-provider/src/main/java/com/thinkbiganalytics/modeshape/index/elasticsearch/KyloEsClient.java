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
import org.apache.http.client.ClientProtocolException;
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
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

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
    private static final String USR_PROPERTIES_PREFIX = JcrMetadataAccess.USR_PREFIX + ":";
    private static final String USR_PROPERTIES_PREFIX_WITH_WILDCARD = USR_PROPERTIES_PREFIX + "*";
    public static final String USR_PROPERTIES_DELIMITER = "\t";
    private static final String USR_PROPERTIES_PROPERTY = "usr:properties";
    private static final String FEED_ID_PROPERTY = "meta:feedId";

    public KyloEsClient(String host, int port) {
        super(host, port);
        this.kyloHost = host;
        this.kyloPort = port;
        log.debug("Initialized with host {} and port {}", this.kyloHost, this.kyloPort);
    }

    @Override
    public boolean deleteDocument(String name, String type, String id) {
        if (isEntityACategory(name)) {
            String workspace = getMetadataActiveWorkspace();
            KyloEsNodeDetails kyloEsNodeDetails = new KyloEsNodeDetails();
            if (workspace != null) {
                kyloEsNodeDetails.setExtendedId(id, true);
                kyloEsNodeDetails.setWorkspaceName(workspace);
                return deleteIndexedCategoryMetadata(kyloEsNodeDetails, type);
            } else {
                log.warn("[category] Unable to delete indexed metadata for category id: {}", kyloEsNodeDetails.getId());
                return false;
            }
        } else if (isEntityAFeed(name)) {
            String workspace = getMetadataActiveWorkspace();
            KyloEsNodeDetails kyloEsNodeDetails = new KyloEsNodeDetails();
            if (workspace != null) {
                kyloEsNodeDetails.setExtendedId(id, true);
                kyloEsNodeDetails.setWorkspaceName(workspace);
                return deleteIndexedFeedMetadata(kyloEsNodeDetails, type);
            } else {
                log.warn("[feed] Unable to delete indexed feed metadata for feed (using node id: {})", kyloEsNodeDetails.getId());
                return false;
            }
        }
        return false;
    }

    private String getMetadataActiveWorkspace() {
        return metadataAccess.read(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            if (session != null) {
                return session.getWorkspace().getName();
            } else {
                log.warn("[category/feed] Could not get active session to metadata.");
                return null;
            }
        }, MetadataAccess.SERVICE);
    }

    @Override
    public boolean storeDocument(String name, String type, String id,
                                 EsRequest doc) {

        if (isEntityACategory(name)) {
            return indexCategoryMetadata(name, type, id);
        } else if (isEntityAFeed(name)) {
            return indexFeedMetadata(name, type, id);
        }

        return false;
    }


    private boolean isEntityACategory(String indexName) {
        return (indexName.contains("kylo-categories"));
    }

    private boolean isEntityAFeed(String indexName) {
        return (indexName.contains("kylo-feeds") || indexName.contains("kylo-internal-fd1"));
    }

    private boolean indexCategoryMetadata(String name, String type, String id) {
        KyloEsNodeDetails kyloEsNodeDetails = getKyloEsNodeDetailsForCategory(id);

        if (kyloEsNodeDetails != null && kyloEsNodeDetails.isValid()) {
            String indexIndicator = kyloEsNodeDetails.getAllowIndexing();
            if ((indexIndicator != null) && (indexIndicator.equals(INDEX_INDICATOR_FOR_NO))) {
                return deleteIndexedCategoryMetadata(kyloEsNodeDetails, type);
            }

            JSONObject fullDocumentJsonObject = constructCategoryMetadataToIndex(name, type, id, kyloEsNodeDetails);
            if (fullDocumentJsonObject == null) {
                log.warn("[category] Unable to index metadata for node id: {}, category id: {}", id, kyloEsNodeDetails.getId());
                return false;
            }

            return submitCategoryMetadataToIndex(fullDocumentJsonObject, type, kyloEsNodeDetails);
        } else {
            log.warn("[category] Unable to index metadata for node id: {}", id);
        }
        return false;
    }

    private boolean submitCategoryMetadataToIndex(JSONObject fullDocumentJsonObject, String type, KyloEsNodeDetails kyloEsNodeDetails) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost method = new HttpPost(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, "kylo-categories-" + kyloEsNodeDetails.getWorkspaceName(), type, kyloEsNodeDetails.getId()));

        try {
            StringEntity requestEntity;
            requestEntity = new StringEntity(fullDocumentJsonObject.toString(), ContentType.APPLICATION_JSON);
            method.setEntity(requestEntity);
            CloseableHttpResponse resp = client.execute(method);
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {
                log.info("[category] Indexed metadata for category id: {}", kyloEsNodeDetails.getId());
                return true;
            } else {
                log.warn("[category] Indexing metadata failed for category id: {}", kyloEsNodeDetails.getId());
                return false;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }

        return false;
    }

    private JSONObject constructCategoryMetadataToIndex(String name, String type, String id, KyloEsNodeDetails kyloEsNodeDetails) {
        JSONObject fullDocumentJsonObject;
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
                            || (property.toString().contains("jcr:description")) || (property.toString().contains("jcr:lastModified")) || (property.toString().contains("jcr:created"))) {
                            try {
                                String propertyName = property.getName();
                                String propertyValue = getPropertyValue(property);
                                fullDocumentJsonObjectInternal.put(propertyName, propertyValue);
                                fullDocumentJsonObjectInternal.put("lowercase_" + propertyName, propertyValue.toLowerCase());
                                fullDocumentJsonObjectInternal.put("uppercase_" + propertyName, propertyValue.toUpperCase());
                                fullDocumentJsonObjectInternal.put("length_" + propertyName, propertyValue.length());
                            } catch (JSONException e) {
                                log.warn("[category] Unable to construct JSON payload for indexing category metadata, category id = {}: {}", kyloEsNodeDetails.getId(), e.getMessage());
                                e.printStackTrace();
                                return null;
                            } catch (RepositoryException e) {
                                log.warn("[category] Repository error for indexing category metadata, category id = {}: {}", kyloEsNodeDetails.getId(), e.getMessage());
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }
                    fullDocumentJsonObjectInternal = augmentCategoryMetadataDocWithUserProperties(name, type, id, fullDocumentJsonObjectInternal.toString());
                } else {
                    log.warn("[category] Null node found in metadata for category. Node id: {}", id);
                    return null;
                }
            } else {
                log.warn("[category] Could not get active session to metadata. Node id: {}", id);
                return null;
            }
            return fullDocumentJsonObjectInternal;
        }, MetadataAccess.SERVICE);

        return fullDocumentJsonObject;
    }

    private boolean deleteIndexedCategoryMetadata(KyloEsNodeDetails kyloEsNodeDetails, String type) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete method = new HttpDelete(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, "kylo-categories-" + kyloEsNodeDetails.getWorkspaceName(), type, kyloEsNodeDetails.getId()));
        boolean retVal = false;
        try {
            CloseableHttpResponse resp = client.execute(method);
            int statusCode = resp.getStatusLine().getStatusCode();
            retVal = (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_FOUND);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        if (retVal) {
            log.info("[category] Deleted indexed metadata for category id: {}", kyloEsNodeDetails.getId());
        } else {
            log.warn("[category] Unable to delete indexed metadata for category id: {}", kyloEsNodeDetails.getId());
        }
        return retVal;
    }

    private KyloEsNodeDetails getKyloEsNodeDetailsForCategory(String id) {
        return metadataAccess.read(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            KyloEsNodeDetails kyloEsNodeDetailsInternal = new KyloEsNodeDetails();
            if (session != null) {
                Node node = session.getNodeByIdentifier(id);
                if (node != null) {
                    kyloEsNodeDetailsInternal.setAllowIndexing(node.getProperty(ALLOW_INDEXING_PROPERTY).getValue().getString());
                    kyloEsNodeDetailsInternal.setExtendedId(id, true);
                    kyloEsNodeDetailsInternal.setWorkspaceName(session.getWorkspace().getName());
                    kyloEsNodeDetailsInternal.setValid(true);
                } else {
                    log.warn("[category] Null node found in metadata for category. Node id: {}", id);
                }
            } else {
                log.warn("[category] Could not get active session to metadata. Node id: {}", id);
            }
            return kyloEsNodeDetailsInternal;
        }, MetadataAccess.SERVICE);
    }

    private boolean indexFeedMetadata(String name, String type, String id) {
        KyloEsClientFeedIds kyloEsClientFeedIds = populateKyloEsClientFeedIds(name, id);

        if (!kyloEsClientFeedIds.areFeedIdsAvailable()) {
            log.warn("[feed] Unable to index metadata for node id: {}, type: {}", id, type);
            return false;
        }

        KyloEsNodeDetails kyloEsNodeDetails = getKyloEsNodeDetailsForFeed(kyloEsClientFeedIds);

        if (kyloEsNodeDetails != null && kyloEsNodeDetails.isValid()) {
            String indexIndicator = kyloEsNodeDetails.getAllowIndexing();
            if ((indexIndicator != null) && (indexIndicator.equals(INDEX_INDICATOR_FOR_NO))) {
                return deleteIndexedFeedMetadata(kyloEsNodeDetails, type);
            }

            JSONObject fullDocumentJsonObject = constructFeedMetadataToIndex(name, type, kyloEsClientFeedIds);
            if (fullDocumentJsonObject == null) {
                log.warn("[feed] Unable to index metadata for node id: {}, feed id: {}", id, kyloEsClientFeedIds.getTbaFeedId());
                return false;
            }

            return submitFeedMetadataToIndex(fullDocumentJsonObject, type, kyloEsNodeDetails, kyloEsClientFeedIds);
        } else {
            log.warn("[feed] Unable to index metadata for node id: {}", id);
        }
        return false;
    }

    private boolean submitFeedMetadataToIndex(JSONObject fullDocumentJsonObject, String type, KyloEsNodeDetails kyloEsNodeDetails, KyloEsClientFeedIds kyloEsClientFeedIds) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost method = new HttpPost(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, "kylo-feeds-" + kyloEsNodeDetails.getWorkspaceName(), type, kyloEsClientFeedIds.getTbaFeedSummaryId()));

        try {
            StringEntity requestEntity;
            requestEntity = new StringEntity(fullDocumentJsonObject.toString(), ContentType.APPLICATION_JSON);
            method.setEntity(requestEntity);
            CloseableHttpResponse resp = client.execute(method);
            int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_OK) {
                log.info("[feed] Indexed metadata for feed id: {}", kyloEsClientFeedIds.getTbaFeedId());
                return true;
            } else {
                log.warn("[feed] Indexing metadata failed for feed id: {}", kyloEsClientFeedIds.getTbaFeedId());
                return false;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }

        return false;
    }

    private JSONObject constructFeedMetadataToIndex(String name, String type, KyloEsClientFeedIds kyloEsClientFeedIds) {
        JSONObject fullDocumentJsonObject;
        fullDocumentJsonObject = metadataAccess.read(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            JSONObject fullDocumentJsonObjectInternal = new JSONObject();
            if (session != null) {
                Node node = session.getNodeByIdentifier(kyloEsClientFeedIds.getTbaFeedSummaryId());
                if (node != null) {
                    PropertyIterator propertyIterator = node.getProperties();
                    while (propertyIterator.hasNext()) {
                        Property property = (Property) propertyIterator.next();
                        log.debug(property.toString());
                        if ((property.toString().contains("jcr:title")) || (property.toString().contains("tba:allowIndexing")) || (property.toString().contains("tba:systemName"))
                            || (property.toString().contains("jcr:description")) || (property.toString().contains("tba:category")) || (property.toString().contains("tba:tags"))) {
                            try {
                                String propertyName = property.getName();
                                String propertyValue = getPropertyValue(property);
                                fullDocumentJsonObjectInternal.put(propertyName, propertyValue);
                                fullDocumentJsonObjectInternal.put("lowercase_" + propertyName, propertyValue.toLowerCase());
                                fullDocumentJsonObjectInternal.put("uppercase_" + propertyName, propertyValue.toUpperCase());
                                fullDocumentJsonObjectInternal.put("length_" + propertyName, propertyValue.length());
                            } catch (JSONException e) {
                                log.warn("[feed] Unable to construct JSON payload for indexing feed metadata, feed id = {}: {}", kyloEsClientFeedIds.getTbaFeedId(), e.getMessage());
                                e.printStackTrace();
                                return null;
                            } catch (RepositoryException e) {
                                log.warn("[feed] Repository error for indexing feed metadata, feed id = {}: {}", kyloEsClientFeedIds.getTbaFeedId(), e.getMessage());
                                e.printStackTrace();
                                return null;
                            }
                        }
                    }
                    fullDocumentJsonObjectInternal.put(FEED_ID_PROPERTY, kyloEsClientFeedIds.getTbaFeedId());
                    fullDocumentJsonObjectInternal = augmentFeedMetadataDocWithUserProperties(name, type, kyloEsClientFeedIds, fullDocumentJsonObjectInternal.toString());
                } else {
                    log.warn("[feed] Null node found in metadata for feed (summary). Node id: {}, Feed id: {}", kyloEsClientFeedIds.getTbaFeedSummaryId(), kyloEsClientFeedIds.getTbaFeedId());
                    return null;
                }
            } else {
                log.warn("[feed] Could not get active session to metadata. Node id: {}, Feed id: {}", kyloEsClientFeedIds.getTbaFeedSummaryId(), kyloEsClientFeedIds.getTbaFeedId());
                return null;
            }
            return fullDocumentJsonObjectInternal;
        }, MetadataAccess.SERVICE);

        return fullDocumentJsonObject;
    }

    private String getPropertyValue(Property property) throws RepositoryException {
        if (property.isMultiple()) {
            return StringUtils.join(property.getValues(), " ");
        } else {
            return property.getString();
        }
    }

    private boolean deleteIndexedFeedMetadata(KyloEsNodeDetails kyloEsNodeDetails, String type) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete method = new HttpDelete(String.format("http://%s:%d/%s/%s/%s", kyloHost, kyloPort, "kylo-feeds-" + kyloEsNodeDetails.getWorkspaceName(), type, kyloEsNodeDetails.getId()));
        boolean retVal = false;
        try {
            CloseableHttpResponse resp = client.execute(method);
            int statusCode = resp.getStatusLine().getStatusCode();
            retVal = (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_FOUND);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        if (retVal) {
            log.info("[feed] Deleted indexed metadata for feed (using node id: {})", kyloEsNodeDetails.getId());
        } else {
            log.warn("[feed] Unable to delete indexed metadata for feed (using node id: {})", kyloEsNodeDetails.getId());
        }
        return retVal;
    }

    private KyloEsNodeDetails getKyloEsNodeDetailsForFeed(KyloEsClientFeedIds kyloEsClientFeedIds) {
        return metadataAccess.read(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            KyloEsNodeDetails kyloEsNodeDetailsInternal = new KyloEsNodeDetails();
            if (session != null) {
                Node node = session.getNodeByIdentifier(kyloEsClientFeedIds.getTbaFeedSummaryId());
                if (node != null) {
                    kyloEsNodeDetailsInternal.setAllowIndexing(node.getProperty(ALLOW_INDEXING_PROPERTY).getValue().getString());
                    kyloEsNodeDetailsInternal.setExtendedId(kyloEsClientFeedIds.getTbaFeedSummaryId(), false);
                    kyloEsNodeDetailsInternal.setWorkspaceName(session.getWorkspace().getName());
                    kyloEsNodeDetailsInternal.setValid(true);
                } else {
                    log.warn("[feed] Null node found in metadata for feed (summary). Node id: {}", kyloEsClientFeedIds.getTbaFeedSummaryId());
                }
            } else {
                log.warn("[feed] Could not get active session to metadata. Node id: {}", kyloEsClientFeedIds.getTbaFeedSummaryId());
            }
            return kyloEsNodeDetailsInternal;
        }, MetadataAccess.SERVICE);
    }

    private KyloEsClientFeedIds populateKyloEsClientFeedIds(String indexName, String id) {
        final KyloEsClientFeedIds kyloEsClientFeedIds = new KyloEsClientFeedIds();

        if (indexName.contains("kylo-feeds")) {
            metadataAccess.read(() -> {
                Session session = JcrMetadataAccess.getActiveSession();
                if (session != null) {
                    Node tbaFeedSummaryNode = session.getNodeByIdentifier(id);
                    kyloEsClientFeedIds.setTbaFeedSummaryId(tbaFeedSummaryNode.getIdentifier());
                    kyloEsClientFeedIds.setTbaFeedId(tbaFeedSummaryNode.getParent().getIdentifier());
                }
                return null;
            }, MetadataAccess.SERVICE);
        }

        if (indexName.contains("kylo-internal-fd1")) {
            metadataAccess.read(() -> {
                Session session = JcrMetadataAccess.getActiveSession();
                if (session != null) {
                    Node tbaFeedDetailsNode = session.getNodeByIdentifier(id);
                    Node tbaFeedNode = tbaFeedDetailsNode.getParent().getParent(); //tba:feed
                    kyloEsClientFeedIds.setTbaFeedId(tbaFeedNode.getIdentifier());
                    kyloEsClientFeedIds.setTbaFeedSummaryId(tbaFeedNode.getNode("tba:summary").getIdentifier());
                }
                return null;
            }, MetadataAccess.SERVICE);
        }

        return kyloEsClientFeedIds;
    }

    private JSONObject augmentCategoryMetadataDocWithUserProperties(String name, String type, String id,
                                                                    String docAsString) throws JSONException {

        JSONObject jsonObjectIncomingEsDoc = new JSONObject(docAsString);

        return metadataAccess.read(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            if (session != null) {
                Node node = session.getNodeByIdentifier(id);
                if (node != null) {
                    //Get tba:details child node
                    PropertyIterator userDefinedProperties = node.getNode("tba:details").getProperties(USR_PROPERTIES_PREFIX_WITH_WILDCARD);
                    List<String> userDefinedPropertiesList = new ArrayList<>();
                    while (userDefinedProperties.hasNext()) {
                        String propertyKeyValuePair = URLDecoder.decode(userDefinedProperties
                                                                            .next()
                                                                            .toString()
                                                                            .substring(USR_PROPERTIES_PREFIX.length()), JcrPropertyUtil.USER_PROPERTY_ENCODING);
                        userDefinedPropertiesList.add(propertyKeyValuePair);
                    }
                    String userDefinedPropertiesAsDelimitedString = StringUtils.join(userDefinedPropertiesList.toArray(), USR_PROPERTIES_DELIMITER);
                    jsonObjectIncomingEsDoc.put(USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString);
                    jsonObjectIncomingEsDoc.put("lowercase_" + USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString.toLowerCase());
                    jsonObjectIncomingEsDoc.put("uppercase_" + USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString.toUpperCase());
                    jsonObjectIncomingEsDoc.put("length_" + USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString.length());
                }
            }
            return jsonObjectIncomingEsDoc;

        }, MetadataAccess.SERVICE);
    }


    private JSONObject augmentFeedMetadataDocWithUserProperties(String name, String type, KyloEsClientFeedIds kyloEsClientFeedIds,
                                                                String docAsString) throws JSONException {
        JSONObject jsonObjectIncomingEsDoc = new JSONObject(docAsString);

        return metadataAccess.read(() -> {
            Session session = JcrMetadataAccess.getActiveSession();
            if (session != null) {
                Node node1 = session.getNodeByIdentifier(kyloEsClientFeedIds.getTbaFeedId());
                Node node = null;
                if (node1 != null) {
                     node = node1.getNode("tba:summary/tba:details");
                }
                if (node != null) {
                    PropertyIterator userDefinedProperties = node.getProperties(USR_PROPERTIES_PREFIX_WITH_WILDCARD);
                    List<String> userDefinedPropertiesList = new ArrayList<>();
                    while (userDefinedProperties.hasNext()) {
                        String propertyKeyValuePair = URLDecoder.decode(userDefinedProperties
                                                                            .next()
                                                                            .toString()
                                                                            .substring(USR_PROPERTIES_PREFIX.length()), JcrPropertyUtil.USER_PROPERTY_ENCODING);
                        userDefinedPropertiesList.add(propertyKeyValuePair);
                    }
                    String userDefinedPropertiesAsDelimitedString = StringUtils.join(userDefinedPropertiesList.toArray(), USR_PROPERTIES_DELIMITER);
                    jsonObjectIncomingEsDoc.put(USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString);
                    jsonObjectIncomingEsDoc.put("lowercase_" + USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString.toLowerCase());
                    jsonObjectIncomingEsDoc.put("uppercase_" + USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString.toUpperCase());
                    jsonObjectIncomingEsDoc.put("length_" + USR_PROPERTIES_PROPERTY, userDefinedPropertiesAsDelimitedString.length());
                }
            }

            return jsonObjectIncomingEsDoc;
        }, MetadataAccess.SERVICE);
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
}
