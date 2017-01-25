/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
angular.module(MODULE_FEED_MGR).factory('FeedSecurityGroups', function ($http, $q, RestUrlService) {

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
        isEnabled: function() {
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

});
