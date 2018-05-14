package com.thinkbiganalytics.kylo.nifi.teradata.tdch;

/*-
 * #%L
 * kylo-nifi-teradata-tdch-core
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

import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.controllerservice.StandardTdchConnectionServiceTest;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.AbstractTdchProcessorTest;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchOperationTypeTest;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.base.TdchProcessResultTest;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.export.TdchExportHiveToTeradataTest;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.utils.TdchBuilderTest;
import com.thinkbiganalytics.kylo.nifi.teradata.tdch.core.processor.utils.TdchUtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for TDCH processors
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
                        StandardTdchConnectionServiceTest.class,
                        AbstractTdchProcessorTest.class,
                        TdchOperationTypeTest.class,
                        TdchProcessResultTest.class,
                        TdchExportHiveToTeradataTest.class,
                        TdchBuilderTest.class,
                        TdchUtilsTest.class
                    })

public class TdchCoreTestSuite {

}
