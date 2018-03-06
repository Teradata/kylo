define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var FeedDetailsProcessorRenderingHelper = /** @class */ (function () {
        function FeedDetailsProcessorRenderingHelper() {
            var data = {
                GET_TABLE_DATA_PROCESSOR_TYPE: "com.thinkbiganalytics.nifi.GetTableData",
                GET_TABLE_DATA_PROCESSOR_TYPE2: "com.thinkbiganalytics.nifi.v2.ingest.GetTableData",
                WATERMARK_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMark',
                SQOOP_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop',
                isSqoopProcessor: function (processor) {
                    return data.SQOOP_PROCESSOR == processor.type;
                },
                isGetTableDataProcessor: function (processor) {
                    return data.GET_TABLE_DATA_PROCESSOR_TYPE == processor.type || data.GET_TABLE_DATA_PROCESSOR_TYPE2 == processor.type;
                },
                isWatermarkProcessor: function (processor) {
                    return data.WATERMARK_PROCESSOR == processor.type;
                },
                isRenderProcessorGetTableDataProcessor: function (inputProcessor) {
                    var render = false;
                    //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
                    if (inputProcessor != undefined && (data.isGetTableDataProcessor(inputProcessor) || data.isWatermarkProcessor(inputProcessor))) {
                        render = true;
                    }
                    return render;
                },
                isRenderSqoopProcessor: function (inputProcessor) {
                    var render = false;
                    //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
                    if (inputProcessor != undefined && (data.isSqoopProcessor(inputProcessor) || data.isWatermarkProcessor(inputProcessor))) {
                        render = true;
                    }
                    return render;
                },
                updateGetTableDataRendering: function (inputProcessor, nonInputProcessors) {
                    var renderGetData = data.isRenderProcessorGetTableDataProcessor(inputProcessor);
                    var getTableDataProcessors = _.filter(nonInputProcessors, function (processor) {
                        return data.isGetTableDataProcessor(processor);
                    });
                    _.each(getTableDataProcessors, function (processor) {
                        processor.userEditable = renderGetData;
                    });
                    //if the flow starts with the watermark and doesnt have a downstream getTableData then dont render the table
                    if (renderGetData && data.isWatermarkProcessor(inputProcessor) && getTableDataProcessors.length == 0) {
                        renderGetData = false;
                    }
                    return renderGetData;
                },
                updateSqoopProcessorRendering: function (inputProcessor, nonInputProcessors) {
                    var render = data.isRenderSqoopProcessor(inputProcessor);
                    var sqoopProcessors = _.filter(nonInputProcessors, function (processor) {
                        return data.isRenderSqoopProcessor(processor);
                    });
                    _.each(sqoopProcessors, function (processor) {
                        processor.userEditable = render;
                    });
                    return render;
                }
            };
            return data;
        }
        return FeedDetailsProcessorRenderingHelper;
    }());
    exports.FeedDetailsProcessorRenderingHelper = FeedDetailsProcessorRenderingHelper;
    angular.module(moduleName).factory('FeedDetailsProcessorRenderingHelper', FeedDetailsProcessorRenderingHelper);
});
//# sourceMappingURL=FeedDetailsProcessorRenderingHelper.js.map