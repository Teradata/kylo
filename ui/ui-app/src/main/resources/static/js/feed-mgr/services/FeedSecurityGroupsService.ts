import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');


// export class FeedSecurityGroups {
    function FeedSecurityGroups ($http:any, $q:any, RestUrlService:any) {

        /**
         * Create filter function for a query string
         */
        function createFilterFor(query:any) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(tag:any) {
                return (tag._lowername.indexOf(lowercaseQuery) === 0);
            };
        }

        var data = {
            querySearch: function (query:any) {
                var self = this;
                var groups = self.loadAvailableGroups(query);
                return groups;
            },
            loadAvailableGroups: function (query:any) {

                var securityGroups = $http.get(RestUrlService.HADOOP_SECURITY_GROUPS)
                    .then(function (dataResult:any) {
                            let lowerGroups = dataResult.data.map(function (tag:any) {
                                tag._lowername = tag.name.toLowerCase();
                                return tag;
                            });
                            var results = query ? lowerGroups.filter(createFilterFor(query)) : [];
                            return results;
                        },
                        function (error:any) {
                            console.log('Error retrieving hadoop authorization groups');
                        });
                return securityGroups;
            },
            isEnabled: function () {
                var isEnabled = $http.get(RestUrlService.HADOOP_AUTHORIZATATION_BASE_URL + "/enabled")
                    .then(function (dataResult:any) {
                            return dataResult.data[0].enabled;
                        },
                        function (error:any) {
                            console.log('Error retrieving hadoop authorization groups');
                        });
                return isEnabled;
            }
        };
       
        return data;

    }
// }
angular.module(moduleName).factory('FeedSecurityGroups', ["$http","$q","RestUrlService", FeedSecurityGroups]);