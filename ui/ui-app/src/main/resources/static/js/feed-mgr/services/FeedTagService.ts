import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

// export class FeedTagService {

    function FeedTagService() {

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
                var tags = self.loadAvailableTags();
                var results = query ? tags.filter(createFilterFor(query)) : [];
                return results;
            },
            loadAvailableTags: function () {

                var data:any = [];
                return data.map(function (tag:any) {
                    tag._lowername = tag.name.toLowerCase();
                    return tag;
                })
            }
        };
        return data;

    }
// }

angular.module(moduleName).factory('FeedTagService', FeedTagService);
