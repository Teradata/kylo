/*-
 * #%L
 * thinkbig-ui-feed-manager
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
 * Return a custom Property input template for a given Processor
 */
define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('FeedInputProcessorOptionsFactory', function () {

        var data = {

            templateForProcessor: function (processor, mode) {
                if (processor.type == "com.thinkbiganalytics.nifi.GetTableData" || processor.type == "com.thinkbiganalytics.nifi.v2.ingest.GetTableData") {
                    if (mode == 'create') {
                        return 'js/feed-mgr/feeds/get-table-data-properties/get-table-data-create.html'
                    }
                    else {
                        return 'js/feed-mgr/feeds/get-table-data-properties/get-table-data-edit.html'
                    }
                }
                if (processor.type == "com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop") {
                    if (mode == 'create') {
                        return 'js/feed-mgr/feeds/get-table-data-properties/import-sqoop-create.html'
                    }
                    else {
                        return 'js/feed-mgr/feeds/get-table-data-properties/import-sqoop-edit.html'
                    }
                }
                return null;
            }

        };
        return data;

    });
});