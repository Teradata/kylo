/*-
 * #%L
 * thinkbig-ui-operations-manager
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
 * Service to help manage common Datatable routines
 */
angular.module(MODULE_OPERATIONS).service('DataTablesService', function (Utils, $compile, $timeout) {

    function DataTablesServiceTag() {
    }

    this.__tag = new DataTablesServiceTag();

    this.useAngularTimer = true; //Flag to use the interactive Timer inside the DataTable
    var self = this;
    this.searchSettings = {},
        this.selectedRows = {};

    this.createStaticTimer = function (startMs, textMs) {
        var textString = Utils.formatTimeMinSec(textMs);
        return this.createStaticTimerWithTextString(startMs, textString);
    }

    this.createStaticTimerWithTextString = function (startMs, textString) {
        return '<span class="timer" data-ms="' + startMs + '">' + textString + '</span>';
    }
    this.toggleAngularTimer = function () {
        if (this.useAngularTimer == true) {
            this.useAngularTimer = false;
        }
        else {
            this.useAngularTimer = true;
        }
    }

    this.preDrawTableCleanup = function (tableId) {
        this.removeTimersForTable(tableId);
        $('#' + tableId + ' tbody').off('click', 'td');
        $('#' + tableId).find("*").addBack().off();
    }

    /**
     * removes the timer from the table.
     * Used in the DataTables preDraw event the remove any existing timers for memory cleanup
     * @param tableId
     */
    this.removeTimersForTable = function (tableId) {
        var items = $('#' + tableId).find('timer');
        // items.remove();
        angular.forEach(items, function (timer, i) {
            var $timer = $(timer);
            var txt = $timer.text();
            var $parent = $timer.parent();
            $timer.remove();
            $parent.append(txt);
            //$timer.hide().parent().append(txt);
        })
    }
    /**
     * Replace the static text with an angular-timer directive that counts
     * @param row
     * @param $scope
     */
    this.replaceStaticTimer = function (row, $scope) {
        if (this.useAngularTimer) {
            angular.forEach($(row).find('.timer'), function (timer, i) {
                //using the timer directive for contiuous counting
                var $timer = angular.element(timer);
                var ms = $timer.data('ms');
                $timer.removeData();

                var html = "<timer max-time-unit=\"'minute'\"start-ms=\"" + ms + "\">{{minutes}} min {{seconds}} sec</timer>";

                var compiled = $compile(html)($scope);
                //set a delay to make smoother render.  without it
                // the page would flash quickly first with the template {{minutes}} min {{seconds}} sec,
                // and then it would start the timer
                //the delay fixes this.
                var timeoutId = $timeout(function ($theTimer, theCompiledCode) {
                    $theTimer.empty();
                    $theTimer.replaceWith(theCompiledCode);
                    theCompiledCode = null;
                    clearTimeout(timeoutId);
                }, 100, false, $timer, compiled);

            });
        }
    }

    this.saveSelectedRow = function (tableKey, row, uniqueIdKey) {
        this.selectedRows[tableKey] = {row: row, key: uniqueIdKey};
    }
    this.getSavedSelectedRow = function (tableKey) {
        return this.selectedRows[tableKey];
    }
    this.isSavedSelectedRow = function (tableKey, currentRow, uniqueIdKey) {
        var saved = this.getSavedSelectedRow(tableKey);
        if (saved) {
            return saved.row[uniqueIdKey] == currentRow[uniqueIdKey];
        }
        return false;
    }
    this.styleSavedSelectedRow = function (rowElement, tableKey, currentRow, uniqueIdKey) {
        var isSaved = this.isSavedSelectedRow(tableKey, currentRow, uniqueIdKey);
        if (isSaved) {
            $(rowElement).addClass('info');
        }
    }

    this.saveSearchSettings = function (tableKey, dataTableData) {
        delete this.searchSettings[tableKey];
        var order = dataTableData.order;
        var start = dataTableData.start;
        var length = dataTableData.length;
        var data = {order: order, start: start, length: length};
        this.searchSettings[tableKey] = data;
    }
    this.getSearchSettings = function (tableKey) {
        return this.searchSettings[tableKey] || {};
    }
    this.applySavedSettings = function (tableKey, defaultOrder, dtOptions, reset) {

        var savedDataTableSettings = this.getSearchSettings(tableKey);

        var order = defaultOrder;
        var pageLength = 10;
        var displayStart = 0;
        if ((reset == undefined || reset == false) && savedDataTableSettings.order) {
            var order = [];
            angular.forEach(savedDataTableSettings.order, function (item, i) {
                order.push([item.column, item.dir]);
            });
            pageLength = savedDataTableSettings.length;
            displayStart = savedDataTableSettings.start;
        }
        if (dtOptions.withOptions) {
            dtOptions
                .withOption("order", order)  //default sort by start time descending
                .withOption("pageLength", pageLength)
                .withOption("displayStart", displayStart)
        }
        else {
            dtOptions["order"] = order  //default sort by start time descending
            dtOptions["pageLength"] = pageLength;
            dtOptions["displayStart"] = displayStart;
        }
    }

});
