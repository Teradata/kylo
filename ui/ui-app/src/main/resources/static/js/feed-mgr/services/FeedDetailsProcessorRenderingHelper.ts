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
import { Templates } from "./TemplateTypes";

export class FeedDetailsProcessorRenderingHelper {

            GET_TABLE_DATA_PROCESSOR_TYPE: "com.thinkbiganalytics.nifi.GetTableData";
            GET_TABLE_DATA_PROCESSOR_TYPE2: "com.thinkbiganalytics.nifi.v2.ingest.GetTableData";
            WATERMARK_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMark';
            SQOOP_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop';

            isSqoopProcessor(processor:Templates.Processor) {
                return this.SQOOP_PROCESSOR == processor.type;
            }

            isGetTableDataProcessor(processor:Templates.Processor) {
                return this.GET_TABLE_DATA_PROCESSOR_TYPE == processor.type || this.GET_TABLE_DATA_PROCESSOR_TYPE2 == processor.type;
            }
            isWatermarkProcessor(processor:Templates.Processor) {
                return this.WATERMARK_PROCESSOR == processor.type;
            }
            isRenderProcessorGetTableDataProcessor(inputProcessor:any) {
                var render = false;
                //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
                if (inputProcessor != undefined && (this.isGetTableDataProcessor(inputProcessor) || this.isWatermarkProcessor(inputProcessor))) {
                    render = true;
                }

                return render;
            }
            isRenderSqoopProcessor(inputProcessor:Templates.Processor) {
                var render = false;
                //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
                if (inputProcessor != undefined && (this.isSqoopProcessor(inputProcessor) || this.isWatermarkProcessor(inputProcessor))) {
                    render = true;
                }

                return render;
            }
            updateGetTableDataRendering(inputProcessor:Templates.Processor, nonInputProcessors:Templates.Processor[]) {
                var renderGetData = this.isRenderProcessorGetTableDataProcessor(inputProcessor);
                var getTableDataProcessors = _.filter(nonInputProcessors,  (processor:Templates.Processor)=> {
                    return this.isGetTableDataProcessor(processor);
                })
                _.each(getTableDataProcessors, function (processor:any) {
                    processor.userEditable = renderGetData;
                });
                //if the flow starts with the watermark and doesnt have a downstream getTableData then dont render the table
                if( renderGetData && this.isWatermarkProcessor(inputProcessor) && getTableDataProcessors.length ==0){
                    renderGetData = false;
                }
                return renderGetData;
            }
            updateSqoopProcessorRendering(inputProcessor:Templates.Processor, nonInputProcessors:Templates.Processor[]) {
                var render = this.isRenderSqoopProcessor(inputProcessor);
                var sqoopProcessors = _.filter(nonInputProcessors,  (processor:Templates.Processor)=> {
                    return this.isRenderSqoopProcessor(processor);
                })
                _.each(sqoopProcessors, function (processor:any) {
                    processor.userEditable = render;
                });
                return render;
            }

       

    }

