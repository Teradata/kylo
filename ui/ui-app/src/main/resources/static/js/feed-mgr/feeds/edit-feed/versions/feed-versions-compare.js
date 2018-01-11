define(['angular','feed-mgr/feeds/edit-feed/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/versions/feed-versions-compare.html',
            controller: "FeedVersionsCompareController",
            link: function ($scope, element, attrs, controller) {

            }

        };
    }

//    var controller =  function($scope, $http, $q, $transition$, AccessControlService, EntityAccessControlService, FeedService) {
    var controller =  function($scope, $http, $q, RestUrlService, FeedService) {

        var self = this;

//        /**
//         * Indicates if the feed definitions may be edited.
//         * @type {boolean}
//         */
//        self.allowEdit = false;

        this.model = FeedService.editFeedModel;
//        this.editableSection = false;

        $scope.$watch(function(){
            return FeedService.editFeedModel;
        },function(newVal) {
            //only update the model if it is not set yet
            if(self.model == null) {
                self.model = angular.copy(FeedService.editFeedModel);
            }
        })


//        self.editModel = {};

        
        self.init = function () {
            var successFn = function (response) {
                self.model = response.data;
                self.model.loaded = true;
           };
            var errorFn = function () {
                var alert = $mdDialog.alert()
                    .parent($("body"))
                    .clickOutsideToClose(true)
                    .title("Unable to load feed version")
                    .textContent("Unable to load feed version.")
                    .ariaLabel("Unable to load feed version")
                    .ok("Got it!");
                $mdDialog.show(alert);
            };

            $http.get(RestUrlService.GET_FEEDS_URL + "/" + self.model.feedId).then(successFn, errorFn);
//            $http.get(RestUrlService.FEED_VERSION_ID_URL(self.model.feedId, self.version.id)).then(successFn, errorFn);
        }
        

        // Initialize this instance
//        self.init();
        

//        this.onEdit = function(){
//            //copy the model
//            var copy = FeedService.editFeedModel;
//            self.editModel= {};
//            self.editModel.feedName = copy.feedName;
//            self.editModel.systemFeedName = copy.systemFeedName;
//            self.editModel.description = copy.description;
//            self.editModel.templateId = copy.templateId;
//        }
//
//        this.onCancel = function() {
//
//        }
//
//        this.onSave = function (ev) {
//            //save changes to the model
//            FeedService.showFeedSavingDialog(ev, "Saving...", self.model.feedName);
//            var copy = angular.copy(FeedService.editFeedModel);
//
//            copy.feedName = self.editModel.feedName;
//            copy.systemFeedName = self.editModel.systemFeedName;
//            copy.description = self.editModel.description;
//            copy.templateId = self.editModel.templateId;
//            copy.userProperties = null;
//
//            FeedService.saveFeedModel(copy).then(function (response) {
//                FeedService.hideFeedSavingDialog();
//                self.editableSection = false;
//                //save the changes back to the model
//                self.model.feedName = self.editModel.feedName;
//                self.model.systemFeedName = self.editModel.systemFeedName;
//                self.model.description = self.editModel.description;
//                self.model.templateId = self.editModel.templateId;
//            }, function (response) {
//                FeedService.hideFeedSavingDialog();
//                FeedService.buildErrorData(self.model.feedName, response);
//                FeedService.showFeedErrorsDialog();
//                //make it editable
//                self.editableSection = true;
//            });
//        };
//
//        //Apply the entity access permissions
//        $q.when(AccessControlService.hasPermission(AccessControlService.FEEDS_EDIT,self.model,AccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS)).then(function(access) {
//            self.allowEdit = access && !self.model.view.generalInfo.disabled;
//        });
    };

    angular.module(moduleName).controller('FeedVersionsCompareController', ["$scope", "$http", "$q", "RestUrlService", "FeedService", controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedVersionsCompare', directive);

});
