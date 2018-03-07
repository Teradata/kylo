define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    // export class FeedTagService {
    function FeedTagService() {
        /**
         * Create filter function for a query string
         */
        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag) {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        }
        var data = {
            querySearch: function (query) {
                var self = this;
                var tags = self.loadAvailableTags();
                var results = query ? tags.filter(createFilterFor(query)) : [];
                return results;
            },
            loadAvailableTags: function () {
                var data = [];
                return data.map(function (tag) {
                    tag._lowername = tag.name.toLowerCase();
                    return tag;
                });
            }
        };
        return data;
    }
    // }
    angular.module(moduleName).factory('FeedTagService', FeedTagService);
});
//# sourceMappingURL=FeedTagService.js.map