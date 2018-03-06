/**
 * Used to store temporary state of the Edit Feed Nifi Properties
 * when a user clicks the Edit link for the Feed Details so the object can be passed to the template factory
 *
 */
define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var EditFeedNifiPropertiesService = /** @class */ (function () {
        function EditFeedNifiPropertiesService() {
            var self = this;
            this.editFeedModel = {};
        }
        return EditFeedNifiPropertiesService;
    }());
    exports.EditFeedNifiPropertiesService = EditFeedNifiPropertiesService;
    angular.module(moduleName).service('EditFeedNifiPropertiesService', EditFeedNifiPropertiesService);
});
//# sourceMappingURL=EditFeedNifiPropertiesService.js.map