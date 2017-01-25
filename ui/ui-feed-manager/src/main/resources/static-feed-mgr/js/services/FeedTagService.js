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


angular.module(MODULE_FEED_MGR).factory('FeedTagService', function () {


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

        var data = [
            {
                'name': 'Data Ingest'
            },
            {
                'name': 'People'
            },
            {
                'name': 'HR'
            },
            {
                'name': 'Enterprise'
            }

        ];
        return data.map(function(tag){
            tag._lowername = tag.name.toLowerCase();
            return tag;
        })
    }
};
    return data;



});
