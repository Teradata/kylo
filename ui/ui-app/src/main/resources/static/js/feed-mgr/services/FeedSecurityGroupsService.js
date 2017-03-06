define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('FeedSecurityGroups', ["$http","$q","RestUrlService",function ($http, $q, RestUrlService) {

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
                var groups = self.loadAvailableGroups(query);
                return groups;
            },
            loadAvailableGroups: function (query) {

                var securityGroups = $http.get(RestUrlService.HADOOP_SECURITY_GROUPS)
                    .then(function (dataResult) {
                            lowerGroups = dataResult.data.map(function (tag) {
                                tag._lowername = tag.name.toLowerCase();
                                return tag;
                            });
                            var results = query ? lowerGroups.filter(createFilterFor(query)) : [];
                            return results;
                        },
                        function (error) {
                            console.log('Error retrieving hadoop authorization groups');
                        });
                return securityGroups;
            },
            isEnabled: function () {
                var isEnabled = $http.get(RestUrlService.HADOOP_AUTHORIZATATION_BASE_URL + "/enabled")
                    .then(function (dataResult) {
                            return dataResult.data[0].enabled;
                        },
                        function (error) {
                            console.log('Error retrieving hadoop authorization groups');
                        });
                return isEnabled;
            }
        };
        return data;

    }]);
});
