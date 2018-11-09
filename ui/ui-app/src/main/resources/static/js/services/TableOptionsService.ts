import {ListTableView} from "./ListTableViewTypes";
import * as angular from 'angular';
import * as _ from "underscore";
import TableOption = ListTableView.TableOption;
import SortOption = ListTableView.SortOption;
import {moduleName} from './module-name';
import {DefaultPaginationDataService} from "./PaginationDataService";
import {Common} from '../../lib/common/CommonTypes';

export class DefaultTableOptionsService implements ListTableView.TableOptionService{
    sortOptions:Common.Map<SortOption[]> = {};
    static $inject = ["PaginationDataService"]
    constructor(private paginationDataService:DefaultPaginationDataService) {}
   
    newSortOptions(key:string, labelValueMap:Common.Map<string>, defaultValue:string, defaultDirection:string):SortOption[]{
        var sortOptions = Object.keys(labelValueMap).map((mapKey:string) => {
            var value = labelValueMap[mapKey];
            var sortOption = {label: mapKey, value: value, direction: '', reverse: false, type: 'sort'}
            if (defaultValue && value == defaultValue) {
                sortOption['default'] = defaultValue;
                sortOption['direction'] = defaultDirection || 'asc';
                sortOption['reverse'] = sortOption['direction'] == 'asc' ? false : true;
                sortOption['icon'] = sortOption['direction'] == 'asc' ? 'keyboard_arrow_up' : 'keyboard_arrow_down';
            }
            return sortOption;
        });
        this.sortOptions[key] = sortOptions;
        return sortOptions;
    }

    newOption(label:string, type:string, isHeader:boolean, disabled:boolean, icon?:string):TableOption {
        if (isHeader == undefined) {
            isHeader = false;
        }
        if (disabled == undefined) {
            disabled = false;
        }
        return {label: label, type: type, header: isHeader, icon: icon, disabled: disabled};
    }

    private clearOtherSorts(key:string, option:SortOption) {
        var sortOptions = this.sortOptions[key];
        if (sortOptions) {
            angular.forEach(sortOptions, (sortOption:SortOption, i) => {
                if (sortOption !== option) {
                    sortOption.direction = '';
                    sortOption.icon = '';
                }
            });
        }
    }

    private getDefaultSortOption(key:string) :SortOption {
        var sortOptions = this.sortOptions[key];
        var defaultSortOption = null;
        if (sortOptions) {
            defaultSortOption = _.find(sortOptions, (opt) => {
                return opt.default
            });
        }
        return defaultSortOption;
    }

    /**
     * Sets the sort option to either the saved value from the PaginationDataService or the default value.
     * @param key
     */
    initializeSortOption(key:string) {
        var currentOption = this.paginationDataService.sort(key);
        if (currentOption) {
            this.setSortOption(key, currentOption)
        }
        else {
            this.saveSortOption(key, this.getDefaultSortOption(key))
        }
    }

    /**
     * Save the sort option back to the store
     * @param {string} key
     * @param {ListTableView.SortOption} sortOption
     */
    saveSortOption(key:string, sortOption:SortOption) {
        if (sortOption) {
            var val = sortOption.value;
            if (sortOption.reverse) {
                val = '-' + val;
            }
            this.paginationDataService.sort(key, val);
        }
    }

    toggleSort(key:string, option:SortOption) {
        //single column sorting, clear sort if different
        this.clearOtherSorts(key, option)
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
        // this.saveSortOption(key,returnedSortOption)
        return returnedSortOption;
    }

    toSortString(option:SortOption) :string{
        if (option.direction == 'desc') {
            return "-" + option.value;
        }
        else {
            return option.value;
        }
    }

    setSortOption(key:string, val:string) {
        var dir = 'asc'
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

    getCurrentSort(key:string):SortOption {
        var sortOptions = this.sortOptions[key];
        var returnedSortOption = null;
        if (sortOptions) {
            angular.forEach(sortOptions, (sortOption, i) => {
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
angular.module(moduleName).service('TableOptionsService', DefaultTableOptionsService);
