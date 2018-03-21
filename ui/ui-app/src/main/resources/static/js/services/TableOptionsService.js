define(["require", "exports", "angular", "./module-name", "underscore", "./PaginationDataService"], function (require, exports, angular, module_name_1, _, PaginationDataService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var TableOptionsService = /** @class */ (function () {
        function TableOptionsService(PaginationDataService) {
            var _this = this;
            this.PaginationDataService = PaginationDataService;
            this.getDefaultSortOption = function (key) {
                var sortOptions = _this.sortOptions[key];
                var defaultSortOption = null;
                if (sortOptions) {
                    defaultSortOption = _.find(sortOptions, function (opt) {
                        return opt.default;
                    });
                }
                return defaultSortOption;
            };
            this.clearOtherSorts = function (key, option) {
                var sortOptions = _this.sortOptions[key];
                if (sortOptions) {
                    angular.forEach(sortOptions, function (sortOption, i) {
                        if (sortOption !== option) {
                            sortOption.direction = '';
                            sortOption.icon = '';
                        }
                    });
                }
            };
            this.sortOptions = {};
            this.newSortOptions = function (key, labelValueMap, defaultValue, defaultDirection) {
                var sortOptions = Object.keys(labelValueMap).map(function (mapKey) {
                    var value = labelValueMap[mapKey];
                    var sortOption = { label: mapKey, value: value, direction: '', reverse: false, type: 'sort' };
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
            };
            this.newOption = function (label, type, isHeader, disabled, icon) {
                if (isHeader == undefined) {
                    isHeader = false;
                }
                if (disabled == undefined) {
                    disabled = false;
                }
                return { label: label, type: type, header: isHeader, icon: icon, disabled: disabled };
            };
            /**
             * Sets the sort option to either the saved value from the PaginationDataService or the default value.
             * @param key
             */
            this.initializeSortOption = function (key) {
                var currentOption = PaginationDataService.sort(key);
                if (currentOption) {
                    this.setSortOption(key, currentOption);
                }
                else {
                    this.saveSortOption(key, this.getDefaultSortOption(key));
                }
            };
            this.saveSortOption = function (key, sortOption) {
                if (sortOption) {
                    var val = sortOption.value;
                    if (sortOption.reverse) {
                        val = '-' + val;
                    }
                    PaginationDataService.sort(key, val);
                }
            };
            this.toggleSort = function (key, option) {
                //single column sorting, clear sort if different
                this.clearOtherSorts(key, option);
                var returnedSortOption = option;
                if (option.direction == undefined || option.direction == '' || option.direction == 'desc') {
                    option.direction = 'asc';
                    option.icon = 'keyboard_arrow_up';
                    option.reverse = false;
                }
                else if (option.direction == 'asc') {
                    option.direction = 'desc';
                    option.icon = 'keyboard_arrow_down';
                    option.reverse = true;
                }
                // this.saveSortOption(key,returnedSortOption)
                return returnedSortOption;
            };
            this.toSortString = function (option) {
                if (option.direction == 'desc') {
                    return "-" + option.value;
                }
                else {
                    return option.value;
                }
            };
            this.setSortOption = function (key, val) {
                var dir = 'asc';
                var icon = 'keyboard_arrow_up';
                var sortColumn = val;
                if (val.indexOf('-') == 0) {
                    dir = 'desc';
                    icon = 'keyboard_arrow_down';
                    sortColumn = val.substring(1);
                }
                var sortOptions = this.sortOptions[key];
                angular.forEach(sortOptions, function (sortOption, i) {
                    if (sortOption.value == sortColumn) {
                        sortOption.direction = dir;
                        sortOption.icon = icon;
                        sortOption.reverse = dir == 'desc' ? true : false;
                    }
                    else {
                        sortOption.direction = '';
                        sortOption.icon = '';
                        sortOption.reverse = false;
                    }
                });
            };
            this.getCurrentSort = function (key) {
                var sortOptions = this.sortOptions[key];
                var returnedSortOption = null;
                if (sortOptions) {
                    angular.forEach(sortOptions, function (sortOption, i) {
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
            };
        }
        return TableOptionsService;
    }());
    exports.default = TableOptionsService;
    angular.module(module_name_1.moduleName)
        .service('PaginationDataService', PaginationDataService_1.default)
        .service('TableOptionsService', ['PaginationDataService', TableOptionsService]);
});
//# sourceMappingURL=TableOptionsService.js.map