package com.thinkbiganalytics.install.inspector.inspection;

/*-
 * #%L
 * kylo-install-inspector
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import static com.thinkbiganalytics.install.inspector.inspection.Configuration.SPRING_PROFILES_INCLUDE;

@Component
public class NifiConnectionInspection extends InspectionBase {

    private static final String NIFI_API_FLOW_ABOUT = "/nifi-api/flow/about";
    private static final String NIFI_V = "nifi-v";

    public static class AboutDTO {
        public String title;
        public String version;
        public String uri;
        public String contentViewerUrl;
        public String timezone;
        public String buildTag;
        public String buildRevision;
        public String buildBranch;
        public String buildTimestamp;

        public AboutDTO() {}

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getContentViewerUrl() {
            return contentViewerUrl;
        }

        public void setContentViewerUrl(String contentViewerUrl) {
            this.contentViewerUrl = contentViewerUrl;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getBuildTag() {
            return buildTag;
        }

        public void setBuildTag(String buildTag) {
            this.buildTag = buildTag;
        }

        public String getBuildRevision() {
            return buildRevision;
        }

        public void setBuildRevision(String buildRevision) {
            this.buildRevision = buildRevision;
        }

        public String getBuildBranch() {
            return buildBranch;
        }

        public void setBuildBranch(String buildBranch) {
            this.buildBranch = buildBranch;
        }

        public String getBuildTimestamp() {
            return buildTimestamp;
        }

        public void setBuildTimestamp(String buildTimestamp) {
            this.buildTimestamp = buildTimestamp;
        }
    }

    public static class About {
        public AboutDTO about;

        public About() {}

        public AboutDTO getAbout() {
            return about;
        }

        public void setAbout(AboutDTO about) {
            this.about = about;
        }
    }

    private Map<String, List<String>> nifiVersionsToProfiles = new HashMap<>();

    @Inject
    private JerseyClientConfig jerseyClientConfig;

    public NifiConnectionInspection() {
        nifiVersionsToProfiles.put("1.0.x", Collections.singletonList("1"));
        nifiVersionsToProfiles.put("1.1.x", Collections.singletonList("1.1"));
        nifiVersionsToProfiles.put("1.2.x", Collections.singletonList("1.2"));
        nifiVersionsToProfiles.put("1.3.x", Arrays.asList("1.2", "1.3"));
        nifiVersionsToProfiles.put("1.4.x", Arrays.asList("1.2", "1.3", "1.4"));
        nifiVersionsToProfiles.put("1.5.x", Arrays.asList("1.2", "1.3", "1.4", "1.5"));

        setDocsUrl("/installation/KyloApplicationProperties.html#nifi-rest");
        setName("Nifi Connection Check");
        setDescription("Checks whether Kylo Services can connect to Nifi");
    }

    @Override
    public InspectionStatus inspect(Configuration configuration) {
        InspectionStatus status = new InspectionStatus(false);

        JerseyRestClient restClient;
        try {
            restClient = new JerseyRestClient(jerseyClientConfig);
        } catch (Exception e) {
            status.addError(String.format("Failed to parse Nifi properties: %s. Check values of configuration properties starting with 'nifi.rest' in '%s'",
                                          e.getMessage(), configuration.getServicesConfigLocation()));
            return status;
        }

        About response;
        try {
            response = restClient.get(NIFI_API_FLOW_ABOUT, About.class);
        } catch (Exception e) {
            status.addError(String.format("Failed to connect to Nifi at '%s': %s. Check values of configuration properties starting with 'nifi.rest' in '%s'",
                                          jerseyClientConfig.getUrl(), e.getMessage(), configuration.getServicesConfigLocation()));
            return status;
        }

        String nifiVersion = response.about.version;

        status.addDescription(String.format("Successfully connected to Nifi version '%s' running at '%s'", nifiVersion, jerseyClientConfig.getUrl()));

        String nifiProfileKey = nifiVersion.substring(0, nifiVersion.lastIndexOf(".")) + ".x";
        List<String> nifiProfiles = nifiVersionsToProfiles.get(nifiProfileKey);
        String expectedNifiProfile = NIFI_V + nifiProfiles.get(nifiProfiles.size() - 1);

        List<String> profiles = configuration.getServicesProfiles();
        List<String> selectedNifiProfiles = profiles.stream().filter(profile -> profile.startsWith(NIFI_V)).collect(Collectors.toList());
        if (selectedNifiProfiles.size() == 0) {
            status.addError(String.format("Nifi profile is not set in '%s'. Add Nifi profile to '%s' property, e.g. '%s=<all-other-profiles>,%s'",
                                          configuration.getServicesConfigLocation(), SPRING_PROFILES_INCLUDE, SPRING_PROFILES_INCLUDE, expectedNifiProfile));
            return status;
        }
        if (selectedNifiProfiles.size() > 1) {
            status.addError(String.format("Found %s Nifi profiles in '%s'. Ensure only one Nifi profile is set, e.g. '%s=<all-other-profiles>,%s'",
                                          selectedNifiProfiles.size(), configuration.getServicesConfigLocation(), SPRING_PROFILES_INCLUDE, expectedNifiProfile));
            return status;
        }


        String selectedNifiProfile = selectedNifiProfiles.get(0);
        boolean isValidProfileSelected = nifiProfiles.stream().anyMatch(profile -> (NIFI_V + profile).equals(selectedNifiProfile));
        if (!isValidProfileSelected) {
            status.addError(String.format("Selected Nifi profile '%s' in '%s' doesn't match Nifi version '%s' running at '%s'. "
                                          + "Replace '%s' with '%s', eg. '%s=<all-other-profiles>,%s'",
                                          selectedNifiProfile, configuration.getServicesConfigLocation(), nifiVersion, jerseyClientConfig.getUrl(),
                                          selectedNifiProfile, expectedNifiProfile, SPRING_PROFILES_INCLUDE, expectedNifiProfile));
            return status;
        }

        status.setValid(true);
        return status;
    }
}
