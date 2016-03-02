/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.PreconditonTrigger;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;

/**
 *
 * @author Sean Felten
 */
public class MetadataClient extends JerseyClient {
    
    private URI baseUri;
    private WebTarget baseTarget;
    
    public MetadataClient(URI base) {
        super();
        this.baseUri = base;
        this.baseTarget = target(base);
        
        register(JacksonFeature.class);
    }

    public FeedBuilder buildFeed(String name) {
        return new FeedBuilderImpl(name);
    }

    private class FeedBuilderImpl implements FeedBuilder {
        private String feedName;
        private String systemName;
        private String description;
        private String owner;
        private List<Metric> preconditionMetrics = new ArrayList<Metric>();

        public FeedBuilderImpl(String name) {
            this.feedName = name;
        }

        @Override
        public FeedBuilder systemName(String name) {
            this.systemName = name;
            return this;
        }

        @Override
        public FeedBuilder description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public FeedBuilder owner(String owner) {
            this.owner = owner;
            return this;
        }

        @Override
        public FeedBuilder preconditionMetric(Metric... metrics) {
            for (Metric m : metrics) {
                this.preconditionMetrics.add(m);
            }
            return this;
        }

        @Override
        public Feed build() {
            Feed feed = new Feed();
            feed.setDisplayName(this.feedName);
            feed.setSystemName(this.systemName);
            feed.setOwner(this.owner);
            feed.setTrigger(createTrigger(this.preconditionMetrics));
            
            return feed;
        }
        
        @Override
        public Feed post() {
            Feed feed = build();
            return postFeed(feed);
        }

    }

    private PreconditonTrigger createTrigger(List<Metric> metrics) {
        PreconditonTrigger trigger = new PreconditonTrigger();
        trigger.setMetrics(metrics);
        return null;
    }

    private Feed postFeed(Feed feed) {
        URI uri = this.baseUri.resolve("feed");
        return post(uri, feed, Feed.class);
    }
    
    private <R> R post(URI uri, Object body, Class<R> resultType) {
        WebTarget target = target(uri);
        R result = target.request()
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .buildPost(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE))
            .invoke(resultType);
        return result;
    }
}
