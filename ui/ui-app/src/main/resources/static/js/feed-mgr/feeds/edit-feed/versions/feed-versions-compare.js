define(['angular', 'feed-mgr/feeds/edit-feed/module-name'], function (angular, moduleName) {

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

    var controller = function ($scope, $http, $q, $filter, RestUrlService, FeedService) {

        var self = this;

        this.model = FeedService.editFeedModel;
        FeedService.versionFeedModel = {};
        FeedService.versionFeedModelDiff = [];
        this.current = $filter('translate')('views.feed-versions-compare.Current');
        this.leftVersion = this.current;
        this.rightVersion = {};
        this.versions = [];
        this.loading = false;


        this.loadVersions = function () {
            FeedService.getFeedVersions(this.model.feedId).then(function(result) {
                self.versions = result.versions;
                self.leftVersion = self.current + " (" + getCurrentVersion().name + ")";
            }, function(err) {

            });
        };

        function getCurrentVersion() {
            return self.versions[0];
        }

        this.changeRightVersion = function() {
            var version = _.find(self.versions, function(v){
                return v.id === self.rightVersion;
            });
            self.loading = true;
            var diff = FeedService.diffFeedVersions(this.model.feedId, self.rightVersion, getCurrentVersion().id).then(function(result) {
                // console.log('diff', result.difference);
                FeedService.versionFeedModelDiff = [];
                _.each(result.difference.patch, function(patch) {
                    FeedService.versionFeedModelDiff[patch.path] = patch;
                });
            }, function (err) {

            });

            var versionedFeed = FeedService.getFeedVersion(this.model.feedId, self.rightVersion).then(function(result) {
                self.rightFeed = result.entity;
                FeedService.versionFeedModel = self.rightFeed;
                FeedService.versionFeedModel.version = version;
            }, function (err) {

            });

            Promise.all([diff, versionedFeed]).then(function(result) {
                self.loading = false;
            }).catch(function(err) {
                self.loading = false;
            });

        };

        self.loadVersions();
    };

    angular.module(moduleName).controller('FeedVersionsCompareController', ["$scope", "$http", "$q", "$filter", "RestUrlService", "FeedService", controller]);

    angular.module(moduleName).directive('thinkbigFeedVersionsCompare', directive);
});
