define(['angular','feed-mgr/feeds/edit-feed/module-name', 'pascalprecht.translate'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
            },
            scope: {
                policy: '=?',
                policyIdx: '=?'
            },
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-data-policies-row-validators-standardisers.html',
            controller: "FeedDataPoliciesRowValidatorsStandardisers",
            link: function ($scope, element, attrs, controller) {
                // console.log('link vals standardisers');
            }

        };
    }

    var controller =  function($scope, $q, AccessControlService,EntityAccessControlService, FeedService, FeedFieldPolicyRuleService) {

        var self = this;

        self.policy = $scope.policy;
        self.policyIdx = $scope.policyIdx;
        self.versionFeedModel = {};

        this.diff = function(path) {
            return FeedService.diffOperation(path);
        };

        this.getAllFieldPolicies = function (field) {
            return FeedFieldPolicyRuleService.getAllPolicyRules(field);
        };

        this.findVersionedPolicy = function(versionFeedModel, policyIndex) {
            if (versionFeedModel && versionFeedModel.table && versionFeedModel.table.fieldPolicies) {
                return versionFeedModel.table.fieldPolicies[policyIndex];
            }
            return '';
        };

        if (self.policy === undefined) {
            self.policy = self.findVersionedPolicy(self.policyIdx);

            $scope.$watch(function(){
                return FeedService.versionFeedModel;
            },function(newVal) {
                self.versionFeedModel = FeedService.versionFeedModel;
                self.policy = self.findVersionedPolicy(self.versionFeedModel, self.policyIdx);
            });
        }
    };


    angular.module(moduleName).controller('FeedDataPoliciesRowValidatorsStandardisers', ["$scope","$q","AccessControlService","EntityAccessControlService","FeedService", "FeedFieldPolicyRuleService",controller]);

    angular.module(moduleName)
        .directive('thinkbigFeedDataPoliciesValidatorsStandardisers', directive);

});
