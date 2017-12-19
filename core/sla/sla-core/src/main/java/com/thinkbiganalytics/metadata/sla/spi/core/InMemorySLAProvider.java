/**
 *
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-core
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

import com.thinkbiganalytics.metadata.sla.api.DuplicateAgreementNameException;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheckBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 */
public class InMemorySLAProvider implements ServiceLevelAgreementProvider {

    private Map<SLAID, ServiceLevelAgreement> slas;
    private Map<String, ID> nameToSlas;

    /**
     *
     */
    public InMemorySLAProvider() {
        this.slas = Collections.synchronizedMap(new HashMap<SLAID, ServiceLevelAgreement>());
        this.nameToSlas = Collections.synchronizedMap(new HashMap<String, ID>());
    }

    @Override
    public ID resolve(Serializable ser) {
        return resolveImpl(ser);
    }

    @Override
    public List<ServiceLevelAgreement> getAgreements() {
        synchronized (this.slas) {
            return new ArrayList<ServiceLevelAgreement>(this.slas.values());
        }
    }

    @Override
    public ServiceLevelAgreement getAgreement(ID id) {
        return this.slas.get(id);
    }

    @Override
    public ServiceLevelAgreement findAgreementByName(String slaName) {
        ID id = this.nameToSlas.get(slaName);

        if (id != null) {
            return this.slas.get(id);
        } else {
            return null;
        }
    }

    @Override
    public boolean removeAgreement(ID id) {
        synchronized (this.slas) {
            ServiceLevelAgreement sla = this.slas.remove(id);

            if (sla != null) {
                this.nameToSlas.remove(sla.getName());
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public ServiceLevelAgreementBuilder builder() {
        return new SLABuilderImpl();
    }

    @Override
    public ServiceLevelAgreementBuilder builder(ID id) {
        return new SLABuilderImpl(resolveImpl(id));
    }


    private SLAImpl addSLA(SLAImpl sla) {
        synchronized (this.slas) {
            SLAID id = new SLAID();

            if (this.nameToSlas.containsKey(sla.getName())) {
                throw new DuplicateAgreementNameException(sla.getName());
            } else {
                sla.setId(id);
                this.slas.put(id, sla);
                this.nameToSlas.put(sla.getName(), id);
                return sla;
            }
        }
    }

    private SLAImpl replaceSLA(SLAID id, SLAImpl sla) {
        ServiceLevelAgreement oldSla = this.slas.get(id);
        ServiceLevelAgreement.ID namedId = this.nameToSlas.get(sla.getName());

        // Make sure the name of the new SLA does not match that of another SLA
        // besides the one being replaced.
        if (namedId == null || oldSla.getId().equals(namedId)) {
            sla.setId(id);
            this.slas.put(id, sla);
            return sla;
        } else {
            throw new DuplicateAgreementNameException(sla.getName());
        }
    }

    private SLAID resolveImpl(Serializable ser) {
        // TODO: throw unknown ID exception?
        if (ser instanceof String) {
            return new SLAID((String) ser);
        } else if (ser instanceof UUID) {
            return new SLAID((UUID) ser);
        } else if (ser instanceof SLAID) {
            return (SLAID) ser;
        } else {
            throw new IllegalArgumentException("Invalid ID source format: " + ser.getClass());
        }
    }

    @Override
    public ServiceLevelAgreementCheckBuilder slaCheckBuilder(ID slaId) {
        return null;
    }

    private static class ObligationBuilderImpl<B> implements ObligationBuilder<B> {

        private SLABuilderImpl slaBuilder;
        private ObligationGroupBuilderImpl groupBuilder;
        private ObligationGroupImpl group;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();

        public ObligationBuilderImpl(ObligationGroupImpl group, SLABuilderImpl bldr) {
            this.slaBuilder = bldr;
            this.group = group;
        }

        public ObligationBuilderImpl(ObligationGroupImpl group, ObligationGroupBuilderImpl bldr) {
            this.groupBuilder = bldr;
            this.group = group;
        }

        @Override
        public ObligationBuilder<B> description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public ObligationBuilder<B> metric(Metric metric, Metric... more) {
            this.metrics.add(metric);
            for (Metric another : more) {
                this.metrics.add(another);
            }
            return this;
        }

        @Override
        public ObligationBuilder<B> metric(Collection<Metric> metrics) {
            this.metrics.addAll(metrics);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public B build() {
            ObligationImpl ob = new ObligationImpl();
            ob.description = this.description;
            ob.metrics = this.metrics;
            ob.group = this.group;
            this.group.getObligations().add(ob);

            if (this.groupBuilder != null) {
                return (B) this.groupBuilder;
            } else {
                return (B) this.slaBuilder;
            }
        }
    }

    private static class ObligationGroupBuilderImpl implements ObligationGroupBuilder {

        private SLABuilderImpl slaBuilder;
        private ObligationGroupImpl group;

        public ObligationGroupBuilderImpl(SLABuilderImpl slaBuilder, Condition cond) {
            this.slaBuilder = slaBuilder;
            this.group = new ObligationGroupImpl(this.slaBuilder.sla, cond);
        }

        @Override
        public ObligationGroupBuilder obligation(Obligation obligation) {
            this.group.getObligations().add(obligation);
            return this;
        }

        @Override
        public ObligationBuilder<ObligationGroupBuilder> obligationBuilder() {
            return new ObligationBuilderImpl<ObligationGroupBuilder>(this.group, this);
        }

        @Override
        public ServiceLevelAgreementBuilder build() {
            this.slaBuilder.sla.obligationGroups.add(this.group);
            return this.slaBuilder;
        }
    }

    private static class SLAID implements ServiceLevelAgreement.ID {

        private static final long serialVersionUID = 8914036758972637669L;

        private final UUID uuid;

        public SLAID() {
            this(UUID.randomUUID());
        }

        public SLAID(String str) {
            this(UUID.fromString(str));
        }

        public SLAID(UUID id) {
            this.uuid = id;
        }

        @Override
        public String toString() {
            return this.uuid.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!this.getClass().equals(obj.getClass())) {
                return false;
            }

            return Objects.equals(this.uuid, ((SLAID) obj).uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }

    }

    private static class SLAImpl implements ServiceLevelAgreement {

        boolean enabled = true;
        private ServiceLevelAgreement.ID id;
        private String name;
        private DateTime creationTime = DateTime.now();
        private String description;
        private ObligationGroupImpl defaultGroup;
        private List<ObligationGroup> obligationGroups;

        public SLAImpl() {
            this.defaultGroup = new ObligationGroupImpl(this, Condition.REQUIRED);
            this.obligationGroups = new ArrayList<ObligationGroup>();
        }

        public ServiceLevelAgreement.ID getId() {
            return id;
        }

        protected void setId(ServiceLevelAgreement.ID id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        protected void setName(String name) {
            this.name = name;
        }

        @Override
        public DateTime getCreatedTime() {
            return this.creationTime;
        }

        @Override
        public String getDescription() {
            return description;
        }

        protected void setDescription(String description) {
            this.description = description;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public List<ObligationGroup> getObligationGroups() {
            if (this.defaultGroup.getObligations().isEmpty()) {
                return this.obligationGroups;
            } else {
                ArrayList<ObligationGroup> list = new ArrayList<>();
                list.add(this.defaultGroup);
                list.addAll(this.obligationGroups);
                return list;
            }
        }

        @Override
        public List<Obligation> getObligations() {
            List<Obligation> list = new ArrayList<>();

            list.addAll(this.defaultGroup.getObligations());
            for (ObligationGroup group : this.obligationGroups) {
                list.addAll(group.getObligations());
            }

            return list;
        }
        @Override
        public Set<Metric> getAllMetrics() {
            return this.getObligationGroups().stream()
                .flatMap(obligationGroup -> obligationGroup.getObligations().stream())
                .flatMap(obligation -> obligation.getMetrics().stream())
                .collect(Collectors.toSet());
        }

        @Override
        public List<ServiceLevelAgreementCheck> getSlaChecks() {
            return null;
        }
    }


    private static class ObligationId implements Obligation.ID {

        private static final long serialVersionUID = 8914036758972637669L;

        private final UUID uuid;

        public ObligationId() {
            this(UUID.randomUUID());
        }

        public ObligationId(String str) {
            this(UUID.fromString(str));
        }

        public ObligationId(UUID id) {
            this.uuid = id;
        }

        @Override
        public String toString() {
            return this.uuid.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!this.getClass().equals(obj.getClass())) {
                return false;
            }

            return Objects.equals(this.uuid, ((SLAID) obj).uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }

    }

    private static class ObligationImpl implements Obligation {

        private ObligationGroupImpl group;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();
        private Obligation.ID id;

        public ObligationImpl() {
            this.id = new ObligationId();
        }


        @Override
        public ID getId() {
            return id;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public ServiceLevelAgreement getAgreement() {
            return this.group.getAgreement();
        }

        @Override
        public ObligationGroup getGroup() {
            return this.group;
        }

        @Override
        public Set<Metric> getMetrics() {
            return Collections.unmodifiableSet(this.metrics);
        }
    }

    private static class ObligationGroupImpl implements ObligationGroup {

        private SLAImpl sla;
        private Condition condition;
        private List<Obligation> obligations = new ArrayList<>();

        public ObligationGroupImpl(SLAImpl sla, Condition condition) {
            this.sla = sla;
            this.condition = condition;
        }

        @Override
        public ServiceLevelAgreement getAgreement() {
            return this.sla;
        }

        @Override
        public Condition getCondition() {
            return this.condition;
        }

        @Override
        public List<Obligation> getObligations() {
            return this.obligations;
        }
    }

    private class SLABuilderImpl implements ServiceLevelAgreementBuilder {

        private SLAID id;
        private String name;
        private String descrtion;
        private SLAImpl sla = new SLAImpl();

        public SLABuilderImpl() {
            this(null);
        }

        public SLABuilderImpl(SLAID id) {
            this.id = id;
        }

        @Override
        public ServiceLevelAgreementBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ServiceLevelAgreementBuilder description(String description) {
            this.descrtion = description;
            return this;
        }

        @Override
        public ServiceLevelAgreementBuilder obligation(Obligation obligation) {
            this.sla.getObligations().add(obligation);
            return this;
        }

        @Override
        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder() {
            return new ObligationBuilderImpl<ServiceLevelAgreementBuilder>(this.sla.defaultGroup, this);
        }

        @Override
        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(Condition condition) {
            ObligationGroupImpl group = new ObligationGroupImpl(this.sla, condition);
            this.sla.obligationGroups.add(group);
            return new ObligationBuilderImpl<ServiceLevelAgreementBuilder>(group, this);
        }

        @Override
        public ObligationGroupBuilder obligationGroupBuilder(Condition condition) {
            return new ObligationGroupBuilderImpl(this, condition);
        }

        @Override
        public ServiceLevelAgreement build() {
            this.sla.setName(this.name);
            this.sla.setDescription(this.descrtion);

            if (this.id == null) {
                return addSLA(sla);
            } else {
                return replaceSLA(this.id, sla);
            }
        }

        @Override
        public ServiceLevelAgreementBuilder actionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations) {
            return null;
        }
    }
}
