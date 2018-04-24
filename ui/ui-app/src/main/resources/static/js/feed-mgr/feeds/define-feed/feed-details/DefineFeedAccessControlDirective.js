define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedAccessControlController = /** @class */ (function () {
        function DefineFeedAccessControlController($scope, FeedService, FeedSecurityGroups) {
            /**
                 * Flag to indicate if hadoop groups are enabled or not
                 * @type {boolean}
            */
            this.securityGroupsEnabled = false;
            /**
                 * Hadoop security groups chips model
                 * @type {{}}
            */
            this.securityGroupChips = {};
            this.feedAccessControlForm = {};
            this.model = FeedService.createFeedModel;
            this.feedSecurityGroups = FeedSecurityGroups;
            this.securityGroupChips.selectedItem = null;
            this.securityGroupChips.searchText = null;
            FeedSecurityGroups.isEnabled().then(function (isValid) {
                this.securityGroupsEnabled = isValid;
            });
        }
        DefineFeedAccessControlController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        DefineFeedAccessControlController.prototype.ngOnInit = function () {
            this.totalSteps = this.stepperController.totalSteps;
            this.stepNumber = parseInt(this.stepIndex) + 1;
        };
        DefineFeedAccessControlController.prototype.transformChip = function (chip) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return { name: chip };
        };
        DefineFeedAccessControlController.$inject = ["$scope", "FeedService", "FeedSecurityGroups"];
        return DefineFeedAccessControlController;
    }());
    exports.DefineFeedAccessControlController = DefineFeedAccessControlController;
    angular.module(moduleName).
        component("thinkbigDefineFeedAccessControl", {
        bindings: {
            stepIndex: '@'
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedAccessControlController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-access-control.html',
    });
});
//# sourceMappingURL=DefineFeedAccessControlDirective.js.map