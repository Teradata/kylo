define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var SearchService = /** @class */ (function () {
        function SearchService($q, $http, CommonRestUrlService) {
            // return this.data;
            this.$q = $q;
            this.$http = $http;
            this.CommonRestUrlService = CommonRestUrlService;
            this.searchQuery = "";
            this.search = function (query, rows, start) {
                return this.performSearch(query, rows, start);
            };
            this.performSearch = function (query, rowsPerPage, start) {
                return this.$http.get(this.CommonRestUrlService.SEARCH_URL, { params: { q: query, rows: rowsPerPage, start: start } }).then(function (response) {
                    return response.data;
                });
            };
        }
        return SearchService;
    }());
    exports.default = SearchService;
    angular.module(module_name_1.moduleName)
        .service('SearchService', ["$q", "$http", "CommonRestUrlService", SearchService]);
});
//# sourceMappingURL=SearchService.js.map