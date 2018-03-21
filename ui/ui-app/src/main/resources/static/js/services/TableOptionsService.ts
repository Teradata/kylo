/*-
 * #%L
 * thinkbig-ui-common
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
/**
 *
 */
import * as angular from 'angular';
import {moduleName} from './module-name';
import * as _ from "underscore";
import PaginationDataService from "./PaginationDataService";

export default class TableOptionsService{
sortOptions: any;
newSortOptions: any;
newOption: any;
initializeSortOption: any;
saveSortOption: any;
toggleSort: any;
toSortString: any;
setSortOption: any;
getCurrentSort: any;

constructor (private PaginationDataService: any) {
  this.sortOptions = {};
  this.newSortOptions = function (key: any, labelValueMap: any, defaultValue: any, defaultDirection: any) {
        var sortOptions = Object.keys(labelValueMap).map(function (mapKey: any) {
                var value: any = labelValueMap[mapKey];
                var sortOption : any= {label: mapKey, value: value, direction: '', reverse: false, type: 'sort'}
                if (defaultValue && value == defaultValue) {
                    sortOption['default'] = defaultDirection || 'asc';
                    sortOption['direction'] = defaultDirection || 'asc';
                    sortOption['reverse'] = sortOption['direction'] == 'asc' ? false : true;
                    sortOption['icon'] = sortOption['direction'] == 'asc' ? 'keyboard_arrow_up' : 'keyboard_arrow_down';
                }
                return sortOption;
            });
            this.sortOptions[key] = sortOptions;
            return sortOptions;
        }

        this.newOption = function (label: any, type: any, isHeader: any, disabled: any, icon: any) {
            if (isHeader == undefined) {
                isHeader = false;
            }
            if (disabled == undefined) {
                disabled = false;
            }
            return {label: label, type: type, header: isHeader, icon: icon, disabled: disabled};
        }





        /**
         * Sets the sort option to either the saved value from the PaginationDataService or the default value.
         * @param key
         */
        this.initializeSortOption = function (key: any) {
            var currentOption : any= PaginationDataService.sort(key);
            if (currentOption) {
                this.setSortOption(key, currentOption)
            }
            else {
                this.saveSortOption(key, this.getDefaultSortOption(key))
            }
        }

        this.saveSortOption = function (key: any, sortOption: any) {
            if (sortOption) {
                var val : any= sortOption.value;
                if (sortOption.reverse) {
                    val = '-' + val;
                }
                PaginationDataService.sort(key, val);
            }
        }

        this.toggleSort = function (key: any, option: any) {
            //single column sorting, clear sort if different
            this.clearOtherSorts(key, option)
            var returnedSortOption : any= option;
            if (option.direction == undefined || option.direction == '' || option.direction == 'desc') {
                option.direction = 'asc';
                option.icon = 'keyboard_arrow_up'
                option.reverse = false;
            }
            else if (option.direction == 'asc') {
                option.direction = 'desc';
                option.icon = 'keyboard_arrow_down'
                option.reverse = true;
            }
            // this.saveSortOption(key,returnedSortOption)
            return returnedSortOption;
        }
        this.toSortString = function (option: any) {
            if (option.direction == 'desc') {
                return "-" + option.value;
            }
            else {
                return option.value;
            }
        }

        this.setSortOption = function (key: any, val: any) {
            var dir : any= 'asc'
            var icon: any = 'keyboard_arrow_up';
            var sortColumn : any= val;
            if (val.indexOf('-') == 0) {
                dir = 'desc';
                icon = 'keyboard_arrow_down';
                sortColumn = val.substring(1);
            }
            var sortOptions : any= this.sortOptions[key];
            angular.forEach(sortOptions, function (sortOption: any, i: any) {
                if (sortOption.value == sortColumn) {
                    sortOption.direction = dir
                    sortOption.icon = icon;
                    sortOption.reverse = dir == 'desc' ? true : false;
                }
                else {
                    sortOption.direction = '';
                    sortOption.icon = '';
                    sortOption.reverse = false;
                }

            });
        }

        this.getCurrentSort = function (key: any) {

            var sortOptions: any = this.sortOptions[key];
            var returnedSortOption: any = null;
            if (sortOptions) {
                angular.forEach(sortOptions, function (sortOption: any, i: any) {
                    if (sortOption.direction && sortOption.direction != '') {
                        returnedSortOption = sortOption;
                        return false;
                    }
                });
                if (returnedSortOption == null) {
                    returnedSortOption = this.getDefaultSortOption(key);
                }
            }
            return returnedSortOption;
        }
    }

        getDefaultSortOption=(key: any)=>{
            var sortOptions : any= this.sortOptions[key];
            var defaultSortOption : any= null;
            if (sortOptions) {
                defaultSortOption = _.find(sortOptions, function (opt: any) {
                    return opt.default
                });
            }
            return defaultSortOption;
        }
        clearOtherSorts=(key: any, option: any)=>{
            var sortOptions : any= this.sortOptions[key];
            if (sortOptions) {
                angular.forEach(sortOptions, function (sortOption: any, i: any) {
                    if (sortOption !== option) {
                        sortOption.direction = '';
                        sortOption.icon = '';
                    }
                });
            }
        }
}
angular.module(moduleName)
.service('PaginationDataService',PaginationDataService)
.service('TableOptionsService', ['PaginationDataService', TableOptionsService]);