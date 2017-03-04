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
define(['angular','services/module-name'], function (angular,moduleName) {
    return  angular.module(moduleName).service('TableOptionsService', ['PaginationDataService', function (PaginationDataService) {
        this.sortOptions = {};
        var self = this;

        this.newSortOptions = function (key, labelValueMap, defaultValue, defaultDirection) {

            var sortOptions = Object.keys(labelValueMap).map(function (mapKey) {
                var value = labelValueMap[mapKey];
                var sortOption = {label: mapKey, value: value, direction: '', reverse: false, type: 'sort'}
                if (defaultValue && value == defaultValue) {
                    sortOption['default'] = defaultDirection || 'asc';
                    sortOption['direction'] = defaultDirection || 'asc';
                    sortOption['reverse'] = sortOption['direction'] == 'asc' ? false : true;
                    sortOption['icon'] = sortOption['direction'] == 'asc' ? 'keyboard_arrow_up' : 'keyboard_arrow_down';
                }
                return sortOption;
            });
            self.sortOptions[key] = sortOptions;
            return sortOptions;
        }

        this.newOption = function (label, type, isHeader, disabled, icon) {
            if (isHeader == undefined) {
                isHeader = false;
            }
            if (disabled == undefined) {
                disabled = false;
            }
            return {label: label, type: type, header: isHeader, icon: icon, disabled: disabled};
        }

        function clearOtherSorts(key, option) {
            var sortOptions = self.sortOptions[key];
            if (sortOptions) {
                angular.forEach(sortOptions, function (sortOption, i) {
                    if (sortOption !== option) {
                        sortOption.direction = '';
                        sortOption.icon = '';
                    }
                });
            }
        }

        function getDefaultSortOption(key) {
            var sortOptions = self.sortOptions[key];
            var defaultSortOption = null;
            if (sortOptions) {
                defaultSortOption = _.find(sortOptions, function (opt) {
                    return opt.default
                });
            }
            return defaultSortOption;
        }

        /**
         * Sets the sort option to either the saved value from the PaginationDataService or the default value.
         * @param key
         */
        this.initializeSortOption = function (key) {
            var currentOption = PaginationDataService.sort(key);
            if (currentOption) {
                self.setSortOption(key, currentOption)
            }
            else {
                self.saveSortOption(key, getDefaultSortOption(key))
            }
        }

        this.saveSortOption = function (key, sortOption) {
            if (sortOption) {
                var val = sortOption.value;
                if (sortOption.reverse) {
                    val = '-' + val;
                }
                PaginationDataService.sort(key, val);
            }
        }

        this.toggleSort = function (key, option) {
            //single column sorting, clear sort if different
            clearOtherSorts(key, option)
            var returnedSortOption = option;
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
            // self.saveSortOption(key,returnedSortOption)
            return returnedSortOption;
        }
        this.toSortString = function (option) {
            if (option.direction == 'desc') {
                return "-" + option.value;
            }
            else {
                return option.value;
            }
        }

        this.setSortOption = function (key, val) {
            var dir = 'asc'
            var icon = 'keyboard_arrow_up';
            var sortColumn = val;
            if (val.indexOf('-') == 0) {
                dir = 'desc';
                icon = 'keyboard_arrow_down';
                sortColumn = val.substring(1);
            }
            var sortOptions = self.sortOptions[key];
            angular.forEach(sortOptions, function (sortOption, i) {
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

        this.getCurrentSort = function (key) {

            var sortOptions = self.sortOptions[key];
            var returnedSortOption = null;
            if (sortOptions) {
                angular.forEach(sortOptions, function (sortOption, i) {
                    if (sortOption.direction && sortOption.direction != '') {
                        returnedSortOption = sortOption;
                        return false;
                    }
                });
                if (returnedSortOption == null) {
                    returnedSortOption = getDefaultSortOption(key);
                }
            }
            return returnedSortOption;
        }

    }]);
});