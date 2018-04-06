define(["require", "exports", "angular", "./module-name", "./CommonRestUrlService"], function (require, exports, angular, module_name_1, CommonRestUrlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var SearchService = /** @class */ (function () {
        function SearchService($q, $http, CommonRestUrlService) {
            var _this = this;
            this.$q = $q;
            this.$http = $http;
            this.CommonRestUrlService = CommonRestUrlService;
            this.data = {
                searchQuery: '',
                search: function (query, rows, start) {
                    return this.performSearch(query, rows, start);
                }
            };
            this.performSearch = function (query, rowsPerPage, start) {
                return _this.$http.get(_this.CommonRestUrlService.SEARCH_URL, { params: { q: query, rows: rowsPerPage, start: start } }).then(function (response) {
                    return response.data;
                });
            };
            return this.data;
        }
        return SearchService;
    }());
    exports.default = SearchService;
    angular.module(module_name_1.moduleName)
        .service('CommonRestUrlService', CommonRestUrlService_1.default)
        .factory('SearchService', ["$q", "$http", "CommonRestUrlService", SearchService]);
});
//# sourceMappingURL=SearchService.js.map