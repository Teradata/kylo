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
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

// export class FeedDetailsProcessorRenderingHelper {
    function FeedDetailsProcessorRenderingHelper () {

        var data = {
            GET_TABLE_DATA_PROCESSOR_TYPE: "com.thinkbiganalytics.nifi.GetTableData",
            GET_TABLE_DATA_PROCESSOR_TYPE2: "com.thinkbiganalytics.nifi.v2.ingest.GetTableData",
            WATERMARK_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMark',
            SQOOP_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop',

            isSqoopProcessor: function (processor:any) {
                return data.SQOOP_PROCESSOR == processor.type;
            },

            isGetTableDataProcessor: function (processor:any) {
                return data.GET_TABLE_DATA_PROCESSOR_TYPE == processor.type || data.GET_TABLE_DATA_PROCESSOR_TYPE2 == processor.type;
            },
            isWatermarkProcessor: function (processor:any) {
                return data.WATERMARK_PROCESSOR == processor.type;
            },
            isRenderProcessorGetTableDataProcessor: function (inputProcessor:any) {
                var render = false;
                //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
                if (inputProcessor != undefined && (data.isGetTableDataProcessor(inputProcessor) || data.isWatermarkProcessor(inputProcessor))) {
                    render = true;
                }

                return render;
            },
            isRenderSqoopProcessor: function (inputProcessor:any) {
                var render = false;
                //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
                if (inputProcessor != undefined && (data.isSqoopProcessor(inputProcessor) || data.isWatermarkProcessor(inputProcessor))) {
                    render = true;
                }

                return render;
            },
            updateGetTableDataRendering: function (inputProcessor:any, nonInputProcessors:any) {
                var renderGetData = data.isRenderProcessorGetTableDataProcessor(inputProcessor);
                var getTableDataProcessors = _.filter(nonInputProcessors, function (processor) {
                    return data.isGetTableDataProcessor(processor);
                })
                _.each(getTableDataProcessors, function (processor:any) {
                    processor.userEditable = renderGetData;
                });
                //if the flow starts with the watermark and doesnt have a downstream getTableData then dont render the table
                if( renderGetData && data.isWatermarkProcessor(inputProcessor) && getTableDataProcessors.length ==0){
                    renderGetData = false;
                }
                return renderGetData;
            },
            updateSqoopProcessorRendering: function (inputProcessor:any, nonInputProcessors:any) {
                var render = data.isRenderSqoopProcessor(inputProcessor);
                var sqoopProcessors = _.filter(nonInputProcessors, function (processor) {
                    return data.isRenderSqoopProcessor(processor);
                })
                _.each(sqoopProcessors, function (processor:any) {
                    processor.userEditable = render;
                });
                return render;
            }

        };
        return data;

    }
// }
angular.module(moduleName).factory('FeedDetailsProcessorRenderingHelper', FeedDetailsProcessorRenderingHelper);
