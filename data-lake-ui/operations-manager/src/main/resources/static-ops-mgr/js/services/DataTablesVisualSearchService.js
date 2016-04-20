/**
 * Service that helps build a customizable search bar above a dataTable for filtering on any column in the table.
 */
angular.module(MODULE_OPERATIONS).service('DataTablesVisualSearchService', function () {

    function DataTablesVisualSearchServiceTag() {
    }

    this.__tag = new DataTablesVisualSearchServiceTag();

    var self = this;
    this.searchSettings = {},

        this.getPipelineDistinctColumnValues = function (tableKey, pipelineDataType, facet, operator, value, callback) {
            var settings = self.searchSettings[tableKey];
            var columnsArr = settings.allColumnsArray;
            var index = $.inArray(facet, columnsArr);
            if (index >= 0) {

                var name = settings.dataTable.column(index).dataSrc();
                var filter = settings.dataTable.ajax.params();

                if (value !== undefined) {
                    //If modifying an existing filter value, remove the current filter from the list of filters
                    var selectedFilter = {key: facet, value: value, operator: operator};
                    if (filter.visualSearchFilter) {
                        var allFilters = $.parseJSON(filter.visualSearchFilter);
                        var removeIndex = -1;
                        $.each(allFilters, function (i, _filter) {
                            if (_filter.key == facet && _filter.operator == operator && _filter.value == value) {
                                removeIndex = i;
                                return false;
                            }
                        });

                        if (removeIndex >= 0) {
                            allFilters.splice(removeIndex, 1);
                            var newFilterString = JSON.stringify(allFilters);
                            filter.visualSearchFilter = newFilterString;
                        }
                    }
                }
                var data = $.extend({columnName: name}, filter);
                var xhr = $.ajax({
                    url: pipelineDataType + "/dataTableDistinctValues",
                    data: data,
                    dataType: 'json'
                });
                $.when(xhr).done(function (data) {
                    data = jQuery.grep(data, function (n, i) {
                        return (n !== "" && n != null && n !== "null");
                    });
                    callback(data);
                }).fail(function () {
                    self.getDistinctValuesForColumn(tableKey, facet, callback);
                });
            }
            else {
                self.getDistinctValuesForColumn(tableKey, facet, callback);
            }
        },

        this.getDistinctValuesForColumn = function (tableKey, facet, callback) {
            var settings = self.searchSettings[tableKey];
            var columnsArr = settings.allColumnsArray;
            var index = $.inArray(facet, columnsArr);
            if (index >= 0) {
                // Get Unique and Sorted values.
                var unique = [];
                settings.dataTable.column(index, {search: 'applied'}).data().unique().sort().each(function (value, index) {
                    unique.push(value.toString());
                });

                callback(unique);

            }
        }


    this.getSearchSettings = function (tableKey) {
        return self.searchSettings[tableKey];
    }
    this.newVisualSearchFilter = function (key, operator, value) {
        return {"key": key, "operator": operator, "value": value};
    }
    this.setCurrentVisualSearchString = function (tableKey, currentSearchString) {
        if (self.searchSettings[tableKey] == undefined) {
            self.searchSettings[tableKey] = {};
        }
        self.searchSettings[tableKey].currentVisualSearchFilter = currentSearchString;
    }
    this.getCurrentVisualSearchJSONString = function (tableKey) {
        if (self.searchSettings[tableKey] == undefined) {
            return undefined;
        }
        return self.searchSettings[tableKey].currentVisualSearchFilter;
    }
    this.initVisualSearch = function (tableKey, visualSearchInputId, serverSide, dataTableInstance, searchOptions, data) {

        var dataTable = dataTableInstance.DataTable;
        $.fn.dataTable.Api.register('column().title()', function () {
            var colheader = this.header();
            var coltitle = $(colheader).text().trim();
            return coltitle;
        });


        var allColumnsArr = [];
        var visualSearchColumnsArr = [];
        var visualSearchDataTableColumnMap = {};
        var params = [];
        var currentVisualSearchFilter = undefined;

        var savedSettings = this.getSearchSettings(tableKey);
        if (savedSettings && savedSettings.allColumnsArray) {
            allColumnsArr = savedSettings.allColumnsArray;
            visualSearchColumnsArr = savedSettings.visualSearchColumns;
            visualSearchDataTableColumnMap = savedSettings.visualSearchDataTableColumnMap;
            params = savedSettings.params;
            currentVisualSearchFilter = savedSettings.currentVisualSearchFilter || '[]';
            savedSettings.dataTable = dataTable;
            savedSettings.dataTableInstance = dataTableInstance;
        }
        else {
            var currentVisualSearchFilter = undefined;
            if (savedSettings && savedSettings.currentVisualSearchFilter) {
                currentVisualSearchFilter = savedSettings.currentVisualSearchFilter;
            }

            // Find all those columns marked as 'visualSearch' and add them to the visualSearch Array
            dataTable.columns().each(function (value, index) {
                $.each(value, function (i, val) {
                    var settings = dataTable.column(val).settings();
                    var title = dataTable.column(val).title();
                    var colDef = settings[0].aoColumns[val];
                    var searchable = colDef.visualSearch;
                    var uiInputType = colDef.uiInputType;
                    if (searchable !== undefined && searchable == true) {
                        $.extend(colDef, {title: title});
                        var len = visualSearchColumnsArr.length;
                        var dtLen = allColumnsArr.length;
                        visualSearchDataTableColumnMap[title] = colDef;
                        visualSearchColumnsArr.push(colDef);
                    }
                    allColumnsArr.push(dataTable.column(val).title());
                });
            });

            $.each(visualSearchColumnsArr, function (i, columnDef) {

                var operators = ['=', '!=', 'in', 'not in', 'like', 'not like'];
                var numbersArr = ["long", "integer", "float", "double"];

                if ($.inArray(columnDef.dataType, numbersArr) >= 0) {
                    operators = ["=", "!=", "<", ">", "<=", ">=", 'in', 'not in']
                }
                else if (columnDef.dataType == "date") {
                    operators = ["=", "!=", "<", ">", "<=", ">="]
                }
                var inputType = 'text';

                var placeholder = "";
                if (columnDef.visualSearchPlaceholder !== undefined) {
                    placeholder = columnDef.visualSearchPlaceholder;
                }
                var min = columnDef.min || null;
                var max = columnDef.max || null;

                if (columnDef.useFacetSelectedCallback == undefined) {
                    useFacetSelectedCallback = true;
                }
                params.push({
                    key: columnDef.title,
                    operators: operators,
                    values: [],
                    placeholder: placeholder,
                    type: inputType,
                    max: max,
                    min: min,
                    selected: false,
                    editing: false,
                    maxlength: null,
                    uiInputType: columnDef.uiInputType,
                    dataType: columnDef.dataType || 'string',
                    useFacetSelectedCallback: columnDef.useFacetSelectedCallback
                });

            });
            //save the settings
            self.searchSettings[tableKey] = {
                params: params,
                dataTable: dataTable,
                dataTableInstance: dataTableInstance,
                currentVisualSearchFilter: currentVisualSearchFilter,
                allColumnsArray: allColumnsArr,
                visualSearchColumns: visualSearchColumnsArr,
                visualSearchDataTableColumnMap: visualSearchDataTableColumnMap
            };
        }

        var defaultSearchOptions = {
            tableKey: tableKey,
            el: $('#' + visualSearchInputId),
            placeholder: "Search the table below",
            strict: true,
            facetSelected: function (facetKey, operator, value, callback) {
                self.getDistinctValuesForColumn(tableKey, facetKey, callback);
            },
            search: function (json, model) {
                var currentSettings = self.getSearchSettings(tableKey);
                var currentVisualSearchFilter = json;
                currentSettings.currentVisualSearchFilter = json;
                var searchParams = $.parseJSON(json);
                if (searchParams.length == 0) {
                    //clear it out
                    dataTable.search('').columns().search('');
                }
                else {
                    $.each(searchParams, function (i, param) {
                        var key = param.key;
                        var value = param.value;
                        //set the dataTable object values
                        var index = currentSettings.visualSearchDataTableColumnMap[key];
                        if (index != undefined) {
                            dataTable.column(index).search(value);//.draw();
                        }
                    })
                }
                if (currentSettings.dataTableInstance.reloadData) {
                    currentSettings.dataTableInstance.reloadData(null, true);
                } else {
                    currentSettings.dataTableInstance.DataTable.ajax.reload(null, true);
                }

            },
            parameters: params
        }

        var defaultQuery = self.searchSettings[tableKey].currentVisualSearchFilter;
        if (defaultQuery) {
            searchOptions.defaultquery =
                defaultSearchOptions.defaultquery = $.parseJSON(defaultQuery)
        }
        if (searchOptions.placeholder) {
            defaultSearchOptions.placeholder = searchOptions.placeholder;
        }
        if (searchOptions.facetSelected) {
            defaultSearchOptions.facetSelected = searchOptions.facetSelected;
        }

        var visualSearch = new VisualSearch(defaultSearchOptions)

        return visualSearch;


// this.visualSearch = VS.init(searchOptions);
    }


});