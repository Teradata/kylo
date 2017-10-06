/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;
import com.thinkbiganalytics.metadata.api.security.HadoopSecurityGroup;
import com.thinkbiganalytics.metadata.api.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertiesEntity;
import com.thinkbiganalytics.metadata.modeshape.security.JcrHadoopSecurityGroup;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 */
public class FeedData extends JcrPropertiesEntity {

    public static final String NODE_TYPE = "tba:feedData";

    public static final String HIGH_WATER_MARKS = "tba:highWaterMarks";
    public static final String WATER_MARKS_TYPE = "tba:waterMarks";

    public static final String INITIALIZATION_TYPE = "tba:initialization";
    public static final String INIT_STATUS_TYPE = "tba:initStatus";
    public static final String INITIALIZATION = "tba:initialization";
    public static final String INIT_HISTORY = "tba:history";
    public static final int MAX_INIT_HISTORY = 10;
    public static final String INIT_STATE = "tba:state";
    public static final String CURRENT_INIT_STATUS = "tba:currentStatus";

    public static final String STATE = INIT_STATE;

    public static final String SCHEDULE_PERIOD = "tba:schedulingPeriod"; // Cron expression, or Timer Expression
    public static final String SCHEDULE_STRATEGY = "tba:schedulingStrategy"; //CRON_DRIVEN, TIMER_DRIVEN
    public static final String HADOOP_SECURITY_GROUPS = "tba:securityGroups";

    public static final String USR_PREFIX = "usr:";

    
    public FeedData(Node node) {
        super(node);
    }

    public State getState() {
        return getProperty(STATE, Feed.State.ENABLED);
    }

    public void setState(State state) {
        setProperty(STATE, state);
    }

    public boolean isInitialized() {
        // TODO this smells like a bug
        return false;
    }

    public InitializationStatus getCurrentInitStatus() {
        if (JcrUtil.hasNode(getNode(), INITIALIZATION)) {
            Node initNode = JcrUtil.getNode(getNode(), INITIALIZATION);
            Node statusNode = JcrPropertyUtil.getProperty(initNode, CURRENT_INIT_STATUS);
            return createInitializationStatus(statusNode);
        } else {
            return new InitializationStatus(InitializationStatus.State.PENDING);
        }
    }

    public void updateInitStatus(InitializationStatus status) {
        try {
            Node initNode = JcrUtil.getOrCreateNode(getNode(), INITIALIZATION, INITIALIZATION);
            Node statusNode = initNode.addNode(INIT_HISTORY, INIT_STATUS_TYPE);
            statusNode.setProperty(INIT_STATE, status.getState().toString());
            initNode.setProperty(CURRENT_INIT_STATUS, statusNode);

            // Trim the history if necessary
            NodeIterator itr = initNode.getNodes(INIT_HISTORY);
            if (itr.getSize() > MAX_INIT_HISTORY) {
                long excess = itr.getSize() - MAX_INIT_HISTORY;
                for (int cnt = 0; cnt < excess; cnt++) {
                    Node node = itr.nextNode();
                    node.remove();
                }
            }
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access initializations statuses", e);
        }
    }

    public List<InitializationStatus> getInitHistory() {
        Node initNode = JcrUtil.getNode(getNode(), INITIALIZATION);

        if (initNode != null) {
            return JcrUtil.getNodeList(initNode, INIT_HISTORY).stream()
                .map(n -> createInitializationStatus(n))
                .sorted(Comparator.comparing(InitializationStatus::getTimestamp).reversed())
                .limit(MAX_INIT_HISTORY)
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
    
    public Set<String> getWaterMarkNames() {
        if (JcrUtil.hasNode(getNode(), HIGH_WATER_MARKS)) {
            Node wmNode = JcrUtil.getNode(getNode(), HIGH_WATER_MARKS);
            return JcrPropertyUtil.streamProperties(wmNode)
                .map(JcrPropertyUtil::getName)
                .filter(name -> name.startsWith(USR_PREFIX))
                .map(name -> name.replace(USR_PREFIX, ""))
                .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    public Optional<String> getWaterMarkValue(String waterMarkName) {
        if (JcrUtil.hasNode(getNode(), HIGH_WATER_MARKS)) {
            Node wmNode = JcrUtil.getNode(getNode(), HIGH_WATER_MARKS);
            return JcrPropertyUtil.findProperty(wmNode, USR_PREFIX + waterMarkName).map(JcrPropertyUtil::toString);
        } else {
            return Optional.empty();
        }
    }
    
    public void setWaterMarkValue(String waterMarkName, String value) {
        Node wmNode = JcrUtil.getOrCreateNode(getNode(), HIGH_WATER_MARKS, WATER_MARKS_TYPE);
        JcrPropertyUtil.setProperty(wmNode, USR_PREFIX + waterMarkName, value);
    }

    public String getSchedulePeriod() {
        return getProperty(SCHEDULE_PERIOD, String.class);
    }

    public void setSchedulePeriod(String schedulePeriod) {
        setProperty(SCHEDULE_PERIOD, schedulePeriod);
    }

    public String getScheduleStrategy() {
        return getProperty(SCHEDULE_STRATEGY, String.class);
    }

    public void setScheduleStrategy(String scheduleStrategy) {
        setProperty(SCHEDULE_STRATEGY, scheduleStrategy);
    }

    public List<? extends HadoopSecurityGroup> getSecurityGroups() {
        Set<Node> list = JcrPropertyUtil.getReferencedNodeSet(this.node, HADOOP_SECURITY_GROUPS);
        List<HadoopSecurityGroup> hadoopSecurityGroups = new ArrayList<>();
        if (list != null) {
            for (Node n : list) {
                hadoopSecurityGroups.add(JcrUtil.createJcrObject(n, JcrHadoopSecurityGroup.class));
            }
        }
        return hadoopSecurityGroups;
    }

    public void setSecurityGroups(List<? extends HadoopSecurityGroup> hadoopSecurityGroups) {
        JcrPropertyUtil.setProperty(this.node, HADOOP_SECURITY_GROUPS, null);

        for (HadoopSecurityGroup securityGroup : hadoopSecurityGroups) {
            Node securityGroupNode = ((JcrHadoopSecurityGroup) securityGroup).getNode();
            JcrPropertyUtil.addToSetProperty(this.node, HADOOP_SECURITY_GROUPS, securityGroupNode, true);
        }
    }

    private InitializationStatus createInitializationStatus(Node statusNode) {
        InitializationStatus.State state = InitializationStatus.State.valueOf(JcrPropertyUtil.getString(statusNode, INIT_STATE));
        DateTime timestamp = JcrPropertyUtil.getProperty(statusNode, "jcr:created");
        return new InitializationStatus(state, timestamp);
    }

}
