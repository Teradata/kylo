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

        this.model = FeedService.editFeedModel;
//        this.leftModel = FeedService.editFeedModel;
//        this.rightModel = FeedService.editFeedModel;
        this.leftModel = {};
        this.rightModel = {};
        this.leftVersions = [];
        this.rightVersions = [];
        this.leftSelection;
        this.rightSelection;

        this.initVersions = function() {
        		var feedVersions = FeedService.getFeedVersions(this.model.feedId);
        		// TODO populate pull downs
        		self.leftVersions = feedVersions.versions;
        		self.rightVersions = feedVersions.versions;
        		// TODO remove below
        		self.leftSelection = feedVersions.versions[0];
        		self.rightSelection = feedVersions.versions[feedVersions.versions.length - 1];
        }
        
        this.initDiff = function() {
        		if (self.leftSelection != undefined && self.rightSelection != undefined) {
        			var diff = FeedService.diffFeedVersions(this.model.feedId, leftSelection, rightSelection);
        			self.leftModel = diff.toVersion.entity;
        			// TODO derive right model instead from the patch
        			self.rightModel = diff.toVersion.entity;
        		}
        }
        

        // Initialize this instance
        self.initVersions();
        self.initDiff();
    };

    angular.module(moduleName).controller('FeedVersionsCompareController', ["$scope", "$http", "$q", "RestUrlService", "FeedService", controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedVersionsCompare', directive);

});
