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
import * as angular from 'angular';
import {moduleName} from './module-name';
import CommonRestUrlService from "./CommonRestUrlService";

export default class SearchService{
searchQuery: string="";
search= function (query: any, rows: any, start: any) {
    return this.performSearch(query, rows, start);
    }

constructor (private $q: any,
            private $http: any,
            private CommonRestUrlService: any) {
          
      // return this.data;

}
 performSearch=function(query: any, rowsPerPage: any, start: any){
            return this.$http.get(this.CommonRestUrlService.SEARCH_URL, {params: {q: query, rows: rowsPerPage, start: start}}).then(function (response: any) {
                return response.data;

            });
        }
}
 angular.module(moduleName)
// .service('CommonRestUrlService',CommonRestUrlService)
 .service('SearchService',["$q", "$http", "CommonRestUrlService",SearchService]);
  
