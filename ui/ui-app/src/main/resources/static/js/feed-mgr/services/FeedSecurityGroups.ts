import * as angular from 'angular';
import * as _ from "underscore";
import { RestUrlService } from './RestUrlService';
import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class FeedSecurityGroups {

    constructor(private restUrlService:RestUrlService,
                private http: HttpClient) {
    }

    querySearch (query:any) {
        return this.loadAvailableGroups(query);
    }
    loadAvailableGroups(query:any) {

        var securityGroups = this.http.get(this.restUrlService.HADOOP_SECURITY_GROUPS)
            .toPromise().then((dataResult:any) => {
                    let lowerGroups = dataResult.map((tag:any) => {
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
    }
    isEnabled() {
        var isEnabled = this.http.get(this.restUrlService.HADOOP_AUTHORIZATATION_BASE_URL + "/enabled")
            .toPromise().then((dataResult:any) => {
                    return dataResult[0].enabled;
                },
                (error:any) => {
                    console.log('Error retrieving hadoop authorization groups');
                });
        return isEnabled;
    }

    /**
     * Create filter function for a query string
     */
    createFilterFor(query:any) {
        var lowercaseQuery = angular.lowercase(query);
        return function filterFn(tag:any) {
            return (tag._lowername.indexOf(lowercaseQuery) === 0);
        };
    }
}