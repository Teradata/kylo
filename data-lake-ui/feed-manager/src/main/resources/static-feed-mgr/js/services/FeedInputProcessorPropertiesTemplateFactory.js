/**
 * Return a custom Property input template for a given Processor
 */
angular.module(MODULE_FEED_MGR).factory('FeedInputProcessorOptionsFactory', function () {

    var data = {

        templateForProcessor:function(processor,mode){
               if(processor.type == "com.thinkbiganalytics.nifi.GetTableData") {
                   if(mode =='create') {
                        return 'js/define-feed/get-table-data-properties/get-table-data-create.html'
                   }
                   else {
                       return 'js/define-feed/get-table-data-properties/get-table-data-edit.html'
                   }
                    }
                return null;
            }

    };
    return data;



});