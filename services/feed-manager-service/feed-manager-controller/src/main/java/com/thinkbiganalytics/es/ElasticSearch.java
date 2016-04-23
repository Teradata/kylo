package com.thinkbiganalytics.es;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.collect.Lists;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 2/10/16.
 */
public class ElasticSearch {

    private static class LazyHolder {
        static final ElasticSearch INSTANCE = new ElasticSearch();
    }

    public static ElasticSearch getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Client client;
    public Client getClient(){
        if(this.client == null) {
            Client client = null;
            try {
                String hostName = "localhost";
                Settings settings = Settings.settingsBuilder()
                        .put("cluster.name", "demo-cluster").build();
                client = TransportClient.builder().settings(settings).build()
                        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), 9300));
                this.client = client;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }
        return client;
    }

    public SearchResponse search(String query, int size){
        SearchRequestBuilder srb1 = getClient()
                .prepareSearch().setQuery(QueryBuilders.queryStringQuery(query)).setSize(size);
        SearchResponse response = srb1.execute().actionGet();
        return response;
    }

    public SearchResult search(String query, int size, int start){
        SearchRequestBuilder srb1 = getClient()
                .prepareSearch().setQuery(QueryBuilders.queryStringQuery(query)).setFrom(start).setSize(size);
        SearchResponse response = srb1.execute().actionGet();
        SearchResult searchResult = new SearchResult();
        searchResult.setTotalHits(response.getHits().getTotalHits());

        searchResult.setFrom(new Long(start+1));
        searchResult.setTo(new Long(start + size));

        if(searchResult.getTotalHits() < (start + size)) {
            searchResult.setTo(searchResult.getTotalHits());
        }

        if(searchResult.getTotalHits() == 0){
            searchResult.setFrom(0L);
        }

        searchResult.setTookInMillis(response.getTookInMillis());
        searchResult.setSearchHits(Lists.newArrayList(response.getHits().getHits()));
        return searchResult;
    }

    public SearchResult searchTable(String query, int size, int start){

        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(query);

        SearchRequestBuilder srb1 = getClient()
                .prepareSearch().setQuery(queryStringQueryBuilder).setFrom(start).setSize(size);
        SearchResponse response = srb1.execute().actionGet();
        SearchResult searchResult = new SearchResult();
        searchResult.setTotalHits(response.getHits().getTotalHits());

        searchResult.setFrom(new Long(start+1));
        searchResult.setTo(new Long(start + size));

        if(searchResult.getTotalHits() < (start + size)) {
            searchResult.setTo(searchResult.getTotalHits());
        }

        if(searchResult.getTotalHits() == 0){
            searchResult.setFrom(0L);
        }

        searchResult.setTookInMillis(response.getTookInMillis());
        searchResult.setSearchHits(Lists.newArrayList(response.getHits().getHits()));
        return searchResult;
    }


    public SearchResponse search2(String query, int size, List<String> indicies){
        SearchRequestBuilder srb1 = getClient()
                .prepareSearch().setIndices(indicies.toArray(new String[indicies.size()])).setQuery(QueryBuilders.queryStringQuery(query)).setSize(size);
        SearchResponse response = srb1.execute().actionGet();
        return response;
    }

    /**
     * get a list of indexes, list of types  -> list of fields for each type
     * @return
     */
    public  List<IndexMappingDTO> getIndexMapping(){
        List<IndexMappingDTO> list = new ArrayList<>();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getClient().admin().indices().getMappings(new GetMappingsRequest()).actionGet().getMappings();
        Object[] indexList = mappings.keys().toArray();
        for (Object indexObj : indexList) {
            String index = indexObj.toString();
            IndexMappingDTO dto = new IndexMappingDTO();
            dto.setIndex(index);
            list.add(dto);
            ImmutableOpenMap<String, MappingMetaData> mapping = mappings.get(index);
            for (ObjectObjectCursor<String, MappingMetaData> c : mapping) {
                TypeMappingDTO typeMappingDTO = new TypeMappingDTO();
                typeMappingDTO.setType(c.key);
                dto.addType(typeMappingDTO);
                try {
                    Map m =    c.value.getSourceAsMap();
                    typeMappingDTO.setFields(getFieldList("", m));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }



    private  List<String> getFieldList(String fieldName, Map<String, Object> mapProperties) {
        List<String> fieldList = new ArrayList<String>();
        Map<String, Object> map = (Map<String, Object>) mapProperties.get("properties");
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (((Map<String, Object>) map.get(key)).containsKey("type")) {
                fieldList.add(fieldName + "" + key);
            } else {
                List<String> tempList = getFieldList(fieldName + "" + key + ".", (Map<String, Object>) map.get(key));
                fieldList.addAll(tempList);
            }
        }
        return fieldList;
    }


}
