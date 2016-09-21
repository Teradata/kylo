package com.thinkbiganalytics.metadata.upgrade;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.OperationalMetadataAccess;
import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaOpsManagerFeed;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailability;
import com.thinkbiganalytics.metadata.modeshape.common.ModeShapeAvailabilityListener;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 9/19/16.
 */
@Service
public class UpgradeKyloService implements ModeShapeAvailabilityListener {

    private static final Logger log = LoggerFactory.getLogger(UpgradeKyloService.class);

    @Inject
    private KyloVersionProvider kyloVersionProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    OperationalMetadataAccess operationalMetadataAccess;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    private AccessController accessController;

    @Inject
    private ModeShapeAvailability modeShapeAvailability;


    @PostConstruct
    private void init() {
        modeShapeAvailability.subscribe(this);
    }

    @Override
    public void modeShapeAvailable() {

        upgradeCheck();

    }

    public void upgradeCheck() {

        KyloVersion version = kyloVersionProvider.getKyloVersion();
        if (version == null || version.getMajorVersionNumber().floatValue() < 0.4f) {
            version = upgradeTo0_4_0();
        } else {
            version = operationalMetadataAccess.commit(() -> {
                //ensure/update the version
                KyloVersion kyloVersion = kyloVersionProvider.updateToCurrentVersion();
                return kyloVersion;
            });
        }
        log.info("Upgrade check complete for Kylo {}", version.getVersion());


    }


    public KyloVersion upgradeTo0_4_0() {

        return metadataAccess.read(() -> operationalMetadataAccess.commit(() -> {

            //1 get all feeds defined in feed manager
            List<FeedManagerFeed> domainFeeds = feedManagerFeedProvider.findAll();
            Map<String, FeedManagerFeed> feedManagerFeedMap = new HashMap<>();
            if (domainFeeds != null && !domainFeeds.isEmpty()) {
                List<OpsManagerFeed.ID> opsManagerFeedIds = new ArrayList<OpsManagerFeed.ID>();
                for (FeedManagerFeed feedManagerFeed : domainFeeds) {
                    opsManagerFeedIds.add(opsManagerFeedProvider.resolveId(feedManagerFeed.getId().toString()));
                    feedManagerFeedMap.put(feedManagerFeed.getId().toString(), feedManagerFeed);
                }
                //find those that match
                List<? extends OpsManagerFeed> opsManagerFeeds = opsManagerFeedProvider.findByFeedIds(opsManagerFeedIds);
                if (opsManagerFeeds != null) {
                    for (OpsManagerFeed opsManagerFeed : opsManagerFeeds) {
                        feedManagerFeedMap.remove(opsManagerFeed.getId().toString());
                    }
                }

                List<JpaOpsManagerFeed> feedsToAdd = new ArrayList<JpaOpsManagerFeed>();
                for (FeedManagerFeed feed : feedManagerFeedMap.values()) {
                    String fullName = FeedNameUtil.fullName(feed.getCategory().getName(), feed.getName());
                    OpsManagerFeed.ID opsManagerFeedId = opsManagerFeedProvider.resolveId(feed.getId().toString());
                    JpaOpsManagerFeed opsManagerFeed = new JpaOpsManagerFeed(opsManagerFeedId, fullName);
                    feedsToAdd.add(opsManagerFeed);
                }
                log.info("Synchronizing Feeds from Feed Manager. About to insert {} feed ids/names into Operations Manager", feedsToAdd.size());
                opsManagerFeedProvider.save(feedsToAdd);
            }

            //update the version
            return kyloVersionProvider.updateToCurrentVersion();
        }), MetadataAccess.SERVICE);
    }


}
