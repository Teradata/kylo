package com.thinkbiganalytics.nifi.provenance;

/*-
 * #%L
 * thinkbig-nifi-provenance-constants
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

/**
 * Provenance constants needed for Kylo to check create controller services, reporting task, and check its status in NiFi
 */
public interface NiFiProvenanceConstants {


    String NiFiMetadataServiceName = "Kylo Metadata Service";

    String NiFiMetadataControllerServiceType = "com.thinkbiganalytics.nifi.v2.core.metadata.MetadataProviderSelectorService";

    String NiFiKyloProvenanceEventReportingTaskType = "com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask";

}
