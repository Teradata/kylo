import * as angular from 'angular';
import * as _ from "underscore";
import { RestUrlService } from './RestUrlService';
import { Inject, Injectable } from '@angular/core';
const moduleName = require('feed-mgr/module-name');

@Injectable()
export class FeedSecurityGroups {
    // function FeedSecurityGroups ($http:any, $q:any, ) {

    data = {
        querySearch: function (query:any) {
            var self = this;
            var groups = self.loadAvailableGroups(query);
            return groups;
        },
        loadAvailableGroups: (query:any) => {

            var securityGroups = this.$injector.get("$http").get(this.RestUrlService.HADOOP_SECURITY_GROUPS)
                .then((dataResult:any) => {
                        let lowerGroups = dataResult.data.map((tag:any) => {
                            tag._lowername = tag.name.toLowerCase();
                            return tag;
                        });
                        var results = query ? lowerGroups.filter(this.createFilterFor(query)) : [];
                        return results;
                    },
                    (error:any) => {
                        console.log('Error retrieving hadoop authorization groups');
                    });
            return securityGroups;
        },
        isEnabled: () => {
            var isEnabled = this.$injector.get("$http").get(this.RestUrlService.HADOOP_AUTHORIZATATION_BASE_URL + "/enabled")
                .then((dataResult:any) => {
                        return dataResult.data[0].enabled;
                    },
                    (error:any) => {
                        console.log('Error retrieving hadoop authorization groups');
                    });
            return isEnabled;
        }
    };
    constructor(private RestUrlService:RestUrlService,
        @Inject("$injector") private $injector: any) {
            this.init();
    }

    init() {
        return this.data;
    }
    /**
     * Create filter function for a query string
     */
    createFilterFor = (query:any) => {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(tag:any) {
            return (tag._lowername.indexOf(lowercaseQuery) === 0);
        };
    }

}
// }
// angular.module(moduleName).factory('FeedSecurityGroups', ["$http","$q","RestUrlService", FeedSecurityGroups]);