package com.thinkbiganalytics.kylo.catalog.credential.spi;

/*-
 * #%L
 * kylo-catalog-credential-api
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

import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTemplate;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;

import org.springframework.util.PropertyPlaceholderHelper;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractDataSourceCredentialProvider implements DataSourceCredentialProvider {

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_POSTFIX = "}";

    private final PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_POSTFIX, null, false);

    public AbstractDataSourceCredentialProvider() {}

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#accepts(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public abstract boolean accepts(DataSource ds);

    protected abstract Optional<Credentials> doGetCredentials(DataSource ds, Set<Principal> principals);

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#applyCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public DataSource applyCredentials(DataSource ds, Set<Principal> principals) {
        return doGetCredentials(ds, principals)
            .map(creds -> {
                DataSource newDs = new DataSource(ds);

                try {
                    Properties credProps = getCredentialProperties(creds, principals);
                    Properties optionProps = getOptionProperties(creds, principals);
                    Properties placeholderProps = getPlaceholderProperties(creds, principals);
                    DataSetTemplate template = newDs.getTemplate();

                    applyOptions(newDs, optionProps);
                    applyPlaceholders(newDs, placeholderProps);
                    template.getOptions().entrySet().forEach(entry -> entry.setValue(placeholderHelper.replacePlaceholders(entry.getValue(), credProps)));

                    return newDs;
                } catch (IllegalArgumentException e) {
                    // Thrown when a placeholder is found for which there is no corresponding property.
                    throw new IllegalStateException("Missing credential: " + e.getMessage());
                }
            })
            .orElse(ds);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#applyPlaceholders(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource, java.util.Set)
     */
    @Override
    public DataSource applyPlaceholders(DataSource ds, Set<Principal> principals) {
        return doGetCredentials(ds, principals)
            .map(creds -> {
                DataSource newDs = new DataSource(ds);
                Properties optionProps = getOptionProperties(creds, principals);
                Properties placeholderProps = getPlaceholderProperties(creds, principals);

                applyOptions(newDs, optionProps);
                applyPlaceholders(newDs, placeholderProps);
                return newDs;
            })
            .orElse(ds);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.kylo.catalog.credential.spi.DataSourceCredentialProvider#getCredentials(com.thinkbiganalytics.kylo.catalog.rest.model.DataSource)
     */
    @Override
    public Map<String, String> getCredentials(DataSource ds, Set<Principal> principals) {
        return doGetCredentials(ds, principals)
            .map(creds -> {
                Map<String, String> map = new HashMap<>();
                Properties credProps = getCredentialProperties(creds, principals);

                forEachProperty(credProps, (name, value) -> map.put(name, value));
                return map;
            })
            .orElse(Collections.emptyMap());
    }

    private Properties getCredentialProperties(Credentials creds, Set<Principal> principals) {
        return creds.asProperties(principals.stream().filter(UsernamePrincipal.class::isInstance).findFirst(),
                                  principals.stream().filter(GroupPrincipal.class::isInstance).collect(Collectors.toList()),
                                  CredentialEntry::isCredential);
    }

    private Properties getOptionProperties(Credentials creds, Set<Principal> principals) {
        return creds.asProperties(principals.stream().filter(UsernamePrincipal.class::isInstance).findFirst(),
                                  principals.stream().filter(GroupPrincipal.class::isInstance).collect(Collectors.toList()),
                                  e -> ! e.isCredential() && ! e.isPlaceholder());
    }

    private Properties getPlaceholderProperties(Credentials creds, Set<Principal> principals) {
        return creds.asProperties(principals.stream().filter(UsernamePrincipal.class::isInstance).findFirst(),
                                  principals.stream().filter(GroupPrincipal.class::isInstance).collect(Collectors.toList()),
                                  CredentialEntry::isPlaceholder);
    }

    private void applyOptions(DataSource ds, Properties props) {
        forEachProperty(props, (name, value) -> ds.getTemplate().getOptions().put(name, value));
    }

    private void applyPlaceholders(DataSource ds, Properties props) {
        forEachProperty(props, (name, value) -> {
            String placeholder = asPlaceholder(name);
            ds.getTemplate().getOptions().put(name, placeholder);
        });
    }

    private void forEachProperty(Properties props, BiConsumer<String, String> consumer) {
        props.stringPropertyNames().forEach(name -> consumer.accept(name, props.getProperty(name)));
    }

    private String asPlaceholder(String name) {
        return PLACEHOLDER_PREFIX + name + PLACEHOLDER_POSTFIX;
    }

    public static class CredentialEntry {
        private String value;
        private boolean placeholder = true;
        private boolean credential = true;

        public CredentialEntry() {
        }

        public CredentialEntry(String name, String value, boolean placeholder, boolean credential) {
            super();
            this.value = value;
            this.placeholder = placeholder;
            this.credential = credential;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(boolean placeholder) {
            this.placeholder = placeholder;
        }

        public boolean isCredential() {
            return credential;
        }

        public void setCredential(boolean credential) {
            this.credential = credential;
        }
    }

    public static class Credentials {
        // connector -> principal -> cred name -> value
        private Map<String, Map<String, CredentialEntry>> userCredentials;
        private Map<String, Map<String, CredentialEntry>> groupCredentials;
        private Map<String, CredentialEntry> defaultCredentials;

        public Credentials() {
            this.userCredentials = new LinkedHashMap<>();
            this.groupCredentials = new LinkedHashMap<>();
            this.defaultCredentials = new LinkedHashMap<>();
        }

        /**
         * Creates a chain of properties in precedence order starting with the user credentials (highest), followed
         * by any group credentials in precedence of left to right in the list, and finally the default credentials.
         * The entries are filtered by the supplied predicate before being added to the properties.
         */
        public Properties asProperties(Optional<Principal> user, List<Principal> groups, Predicate<CredentialEntry> filter) {
            // The default credentials will be the tail of the chain
            Properties defaults = asProperties(this.defaultCredentials, null, filter);

            // Credential precedence in supplied group order; i.e 1st group creds have precedence over the rest of the groups.
            // Iterate through groups in reverse order so that the latter groups serve as defaults to the earlier ones.
            final Properties groupCreds = IntStream.range(0, groups.size())
                .mapToObj(idx -> groups.get(groups.size() - idx - 1))
                .map(Principal::getName)
                .filter(this.groupCredentials::containsKey)
                .reduce(defaults,
                        (lowerProps, name) -> asProperties(this.groupCredentials.get(name), lowerProps, filter),
                        (p1, p2) -> {
                            p1.putAll(p2);
                            return p1;
                        });

            // User credentials have highest precedence.
            return user
                .map(Principal::getName)
                .filter(this.userCredentials::containsKey)
                .map(this.userCredentials::get)
                .map(userCreds -> asProperties(userCreds, groupCreds, filter))
                .orElse(groupCreds);
        }

        private Properties asProperties(Map<String, CredentialEntry> credEntries, Properties defaults, Predicate<CredentialEntry> filter) {
            Map<String, String> map = credEntries.entrySet().stream()
                .filter(e -> filter.test(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
            Properties props = new Properties(defaults);
            props.putAll(map);
            return props;
        }

        public Map<String, Map<String, CredentialEntry>> getUserCredentials() {
            return userCredentials;
        }

        public void setUserCredentials(Map<String, Map<String, CredentialEntry>> userCredentials) {
            this.userCredentials.putAll(userCredentials);
        }

        public Map<String, Map<String, CredentialEntry>> getGroupCredentials() {
            return groupCredentials;
        }

        public void setGroupCredentials(Map<String, Map<String, CredentialEntry>> groupCredentials) {
            this.groupCredentials.putAll(groupCredentials);
        }

        public Map<String, CredentialEntry> getDefaultCredentials() {
            return defaultCredentials;
        }

        public void setDefaultCredentials(Map<String, CredentialEntry> defaultCredentials) {
            this.defaultCredentials.putAll(defaultCredentials);
        }

        public void addUserCredential(String user, String name, String value, boolean placeholder, boolean credential) {
            CredentialEntry entry = new CredentialEntry(name, value, placeholder, credential);
            this.userCredentials.computeIfAbsent(user, k -> new LinkedHashMap<>()).put(name, entry);
        }

        public void addGroupCredential(String group, String name, String value, boolean placeholder, boolean credential) {
            CredentialEntry entry = new CredentialEntry(name, value, placeholder, credential);
            this.groupCredentials.computeIfAbsent(group, k -> new LinkedHashMap<>()).put(name, entry);
        }

        public void addDefaultCredential(String name, String value, boolean option, boolean credential) {
            CredentialEntry entry = new CredentialEntry(name, value, option, credential);
            this.defaultCredentials.put(name, entry);
        }
    }
}
