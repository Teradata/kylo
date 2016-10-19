/**
 * Return a custom Property input template for a given Processor
 */
angular.module(MODULE_FEED_MGR).factory('FeedDetailsProcessorRenderingHelper', function () {

    var data = {
        GET_TABLE_DATA_PROCESSOR_TYPE: "com.thinkbiganalytics.nifi.GetTableData",
        GET_TABLE_DATA_PROCESSOR_TYPE2: "com.thinkbiganalytics.nifi.v2.ingest.GetTableData",
        WATERMARK_PROCESSOR: 'com.thinkbiganalytics.nifi.v2.core.watermark.LoadHighWaterMarkProcessor',

        isGetTableDataProcessor: function (processor) {
            return data.GET_TABLE_DATA_PROCESSOR_TYPE == processor.type || data.GET_TABLE_DATA_PROCESSOR_TYPE2 == processor.type;
        },
        isWatermarkProcessor: function (processor) {
            return data.WATERMARK_PROCESSOR == processor.type;
        },
        isRenderProcessorGetTableDataProcessor: function (inputProcessor) {
            var render = false;
            //if the processor to check is GetTable Data it should be rendered only if it is the input, or if the input is watermark
            if (data.isGetTableDataProcessor(inputProcessor) || data.isWatermarkProcessor(inputProcessor)) {
                render = true;
            }

            return render;
        },
        updateGetTableDataRendering: function (inputProcessor, nonInputProcessors) {
            var renderGetData = data.isRenderProcessorGetTableDataProcessor(inputProcessor);
            var getTableDataProcessors = _.filter(nonInputProcessors, function (processor) {
                return data.isGetTableDataProcessor(processor);
            })
            _.each(getTableDataProcessors, function (processor) {
                processor.userEditable = renderGetData;
            });
            return renderGetData;
        }
    };
    return data;

});