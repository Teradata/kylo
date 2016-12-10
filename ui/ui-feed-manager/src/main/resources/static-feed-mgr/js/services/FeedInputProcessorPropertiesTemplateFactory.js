/**
 * Return a custom Property input template for a given Processor
 */
angular.module(MODULE_FEED_MGR).factory('FeedInputProcessorOptionsFactory', function () {

    var data = {

        templateForProcessor: function (processor, mode) {
            if (processor.type == "com.thinkbiganalytics.nifi.GetTableData" || processor.type == "com.thinkbiganalytics.nifi.v2.ingest.GetTableData") {
                if (mode == 'create') {
                    return 'js/define-feed/get-table-data-properties/get-table-data-create.html'
                }
                else {
                    return 'js/define-feed/get-table-data-properties/get-table-data-edit.html'
                }
            }
            if (processor.type == "com.thinkbiganalytics.nifi.v2.sqoop.core.ImportSqoop") {
                if (mode == 'create') {
                    return 'js/define-feed/get-table-data-properties/import-sqoop-create.html'
                }
                else {
                    return 'js/define-feed/get-table-data-properties/import-sqoop-edit.html'
                }
            }
            return null;
        }

    };
    return data;

});