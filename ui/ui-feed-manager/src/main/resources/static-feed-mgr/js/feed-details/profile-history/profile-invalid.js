(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                processingdttm:'=',
                rowsPerPage:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-details/profile-history/profile-invalid-results.html',
            controller: "FeedProfileInvalidResultsController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

    var controller =  function($scope,$http,$stateParams, FeedService, RestUrlService, HiveService, Utils,BroadcastService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        this.data = [];
        this.loading = false;
       var fieldMappings = {}



        var transformFn = function(row,columns,displayColumns){
            var invalidFields = [];
            var invalidFieldMap = {};
            row.invalidFields = invalidFields;
            row.invalidFieldMap = invalidFieldMap;
            row.isInvalid = function(column){
                return this.invalidFieldMap[column] != undefined;
            }
            row.invalidField = function(column){
                return this.invalidFieldMap[column];
            }
            var _index = _.indexOf(displayColumns,'dlp_reject_reason');
            var rejectReasons = row[columns[_index]];
            if(rejectReasons != null){
                rejectReasons = angular.fromJson(rejectReasons);
            }
            if(rejectReasons != null){
                angular.forEach(rejectReasons,function(rejectReason){
                    if(rejectReason.scope =='field'){
                        var field = rejectReason.field;
                        var copy = angular.copy(rejectReason);
                        _index = _.indexOf(displayColumns,field);
                        copy.fieldValue = row[columns[_index]];
                        invalidFields.push(copy)
                        invalidFieldMap[columns[_index]] = copy;
                    }
                });
            }

        }

        function getProfileValidation(){
            self.loading = true;
            var successFn = function (response) {
                var data = HiveService.transformResults2(response,null,transformFn);
                //remove the reject reasons column
                var _index = _.indexOf(data.displayColumns,'dlp_reject_reason');
                data.displayColumns.splice(_index,1)
                data.columns.splice(_index,1)
                 self.data = data;
                self.loading = false;
                BroadcastService.notify('PROFILE_TAB_DATA_LOADED','invalid');

            }
            var errorFn = function (err) {
                self.loading = false;
            }
            var promise = $http.get(RestUrlService.FEED_PROFILE_INVALID_RESULTS_URL(self.model.id),{params:{'processingdttm':self.processingdttm}});
            promise.then(successFn, errorFn);
            return promise;
        }



        getProfileValidation();
    };


    angular.module(MODULE_FEED_MGR).controller('FeedProfileInvalidResultsController', controller);

    angular.module(MODULE_FEED_MGR)
        .directive('thinkbigFeedProfileInvalid', directive);

})();
