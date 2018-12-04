package com.thinkbiganalytics.metadata.modeshape.template;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.template.ChangeComment;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.joda.time.DateTime;

import javax.jcr.Node;

public class JcrChangeComment extends JcrObject implements ChangeComment {

    public static final String NODE_TYPE = "tba:ChangeComment";

    public static final String CREATED_TIME = "jcr:created";

    public static final String CREATE_DATE_TIME = "tba:createDateTime";

    public static final String CREATED_BY = "jcr:createdBy";

    public static final String COMMENT = "tba:comment";

    /**
     *
     * @param node
     */
    public JcrChangeComment(Node node, String comment) {
        super(node);
        JcrPropertyUtil.setProperty(getNode(), COMMENT, comment);
    }

    public JcrChangeComment(Node node, String comment, DateTime dateTime) {
        super(node);
        JcrPropertyUtil.setProperty(getNode(), COMMENT, comment);
        JcrPropertyUtil.setProperty(getNode(), CREATE_DATE_TIME, dateTime);
    }

    public JcrChangeComment(Node node) {
        super(node);
    }

//    @Override
//    public DateTime getCreatedTime() {
//        return JcrPropertyUtil.getProperty(getNode(), CREATED_TIME);
//    }

    @Override
    public DateTime getCreateDateTime() {
        if(JcrPropertyUtil.hasProperty(getNode(), CREATE_DATE_TIME))
            return DateTime.parse(JcrPropertyUtil.getProperty(getNode(), CREATE_DATE_TIME));

        return JcrPropertyUtil.getProperty(getNode(), CREATED_TIME);
    }

    @Override
    public String getComment() {
        return JcrPropertyUtil.getProperty(getNode(), COMMENT);
    }

    @Override
    public UsernamePrincipal getUser() {
        return new UsernamePrincipal(JcrPropertyUtil.getProperty(getNode(), CREATED_BY));
    }
}
