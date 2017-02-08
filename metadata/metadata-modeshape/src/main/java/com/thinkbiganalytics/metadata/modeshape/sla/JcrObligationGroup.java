/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.sla;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class JcrObligationGroup extends JcrObject implements ObligationGroup, Serializable {

    public static final String OBLIGATIONS = "tba:obligations";
    public static final String CONDITION = "tba:condition";
    public static final String OBLIGATION_TYPE = "tba:obligation";
    private static final long serialVersionUID = 3948150775928992180L;
    private JcrServiceLevelAgreement agreement;

//    public static JcrObligationGroup createGroup(JcrServiceLevelAgreement sla, Condition cond) {
//        Node slaNode = sla.getNode();
//        return JcrUtil.addJcrObject(slaNode, "tba:groups", "tba:obligationGroup", JcrObligationGroup.class, sla);
//    }

    public JcrObligationGroup(Node node, JcrServiceLevelAgreement sla) {
        super(node);
        this.agreement = sla;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationGroup#getCondition()
     */
    @Override
    public Condition getCondition() {
        return JcrPropertyUtil.getEnum(this.node, "tba:condition", Condition.class, Condition.REQUIRED);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationGroup#getObligations()
     */
    @Override
    public List<Obligation> getObligations() {
        try {
            @SuppressWarnings("unchecked")
            Iterator<Node> itr = (Iterator<Node>) this.node.getNodes("tba:obligations");

            return Lists.newArrayList(Iterators.transform(itr, (obNode) -> {
                return JcrUtil.createJcrObject(obNode, JcrObligation.class, JcrObligationGroup.this);
            }));
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the obligation nodes", e);
        }

    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationGroup#getAgreement()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        return this.agreement;
    }
}
