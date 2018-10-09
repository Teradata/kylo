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
import CommonRestUrlService from "./CommonRestUrlService";
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

@Injectable()
export default class SearchService {

    private searchQuery: string = "";
    searchQuerySubject = new BehaviorSubject<string>(this.searchQuery);

    constructor(private http: HttpClient,
                private CommonRestUrlService: CommonRestUrlService) {}

    setSearchQuery(searchQuery: string) {
        this.searchQuery = searchQuery;
        this.searchQuerySubject.next(searchQuery);
    }

    getSearchQuery() {
        return this.searchQuery;
    }

    search(rows: any, start: any) {
        return this.performSearch(rows, start);
    }

    performSearch(rowsPerPage: any, start: any){
        return this.http.get(this.CommonRestUrlService.SEARCH_URL, {params: {q: this.searchQuery, rows: rowsPerPage, start: start}})
            .toPromise().then(function (response: any) {
                return response;
            });
    }
}
