/**
 * Service which sets up fattable.
 * In the simplest form, create a div on a page with an id and then initialise table in javascript providing div selector and arrays of headers and rows, e.g.
 *
 * HTML:
 *  <div id="table-id">
 *
 * JS:
 *   FattableService.setupTable({
 *      tableContainerId:"table-id",
 *      headers: self.headers,
 *      rows: self.rows
 *  });
 *
 *  Default implementation expects each header to have "displayName" property and each row to have a property matching display name, e.g.
 *  var headers = [{displayName: column1}, {displayName: column2}, ... ]
 *  var rows = [{column1: value1, column2: value2}, ... ]
 *
 *  Default behaviour can be overridden by implementing headerText, cellText, fillCell, getCellSync, fillHeader, getHeaderSync methods on options passed to setupTable method, e.g.
 *   FattableService.setupTable({
 *      tableContainerId:"table-id",
 *      headers: self.headers,
 *      rows: self.rows,
 *      headerText: function(header) {...},
 *      cellText: function(row, column) {...}
 *      ...
 *  });
 *
 */
define(['angular','feed-mgr/module-name','fattable'], function (angular,moduleName) {
    angular.module(moduleName).service('FattableService', ["$window", function ($window) {

        var self = this;

        var FONT_FAMILY = "Roboto, \"Helvetica Neue\", sans-serif";

        var optionDefaults = {
            tableContainerId: "",
            headers: [],
            rows: [],
            minColumnWidth: 50,
            maxColumnWidth: 300,
            rowHeight: 53,
            headerHeight: 40,
            padding: 40,
            headerFontFamily: FONT_FAMILY,
            headerFontSize: "12px",
            headerFontWeight: "bold",
            rowFontFamily: FONT_FAMILY,
            rowFontSize: "14px",
            rowFontWeight: "normal",
            setupRefreshDebounce: 300,
            headerText: function(header) {
                return header.displayName;
            },
            cellText: function(row, column) {
                return row[column.displayName];
            },
            fillCell: function(cellDiv, data) {
                cellDiv.innerHTML = data.value;
            },
            getCellSync: function(i, j) {
                var displayName = this.headers[j].displayName;
                var row = this.rows[i];
                if (row === undefined) {
                    //occurs when filtering table
                    return undefined;
                }
                return {
                    "value": row[displayName]
                }
            },
            fillHeader: function(headerDiv, header) {
                headerDiv.innerHTML = '<div>' + header + '</div>';
            },
            getHeaderSync: function(j) {
                return this.headers[j].displayName;
            }
        };

        self.setupTable = function(options) {
            var optionsCopy = _.clone(options);
            var settings = _.defaults(optionsCopy, optionDefaults);

            var tableData = new fattable.SyncTableModel();
            var painter = new fattable.Painter();

            var headers = settings.headers;
            var rows = settings.rows;

            function get2dContext(font) {
                var canvas = document.createElement("canvas");
                document.createDocumentFragment().appendChild(canvas);
                var context = canvas.getContext("2d");
                context.font = font;
                return context;
            }

            var headerContext = get2dContext(settings.headerFontWeight + " " + settings.headerFontSize + " " + settings.headerFontFamily);
            var rowContext = get2dContext(settings.rowFontWeight + " " + settings.rowFontSize + " " + settings.rowFontFamily);

            tableData.columnHeaders = [];
            var columnWidths = [];
            _.each(headers, function(column) {
                var headerText = settings.headerText(column);
                var headerTextWidth = headerContext.measureText(headerText).width;
                var longestColumnText = _.reduce(rows, function (previousMax, row) {
                    var cellText = settings.cellText(row, column);
                    var cellTextLength = cellText === undefined || cellText === null ? 0 : cellText.length;
                    return previousMax.length < cellTextLength ? cellText : previousMax;
                }, "");

                var columnTextWidth = rowContext.measureText(longestColumnText).width;
                columnWidths.push(Math.min(settings.maxColumnWidth, Math.max(settings.minColumnWidth, headerTextWidth, columnTextWidth)) + settings.padding);
                tableData.columnHeaders.push(headerText);
            });

            painter.fillCell = function (div, data) {
                if (data === undefined) {
                    return;
                }
                div.style.fontSize = settings.rowFontSize;
                div.style.fontFamily = settings.rowFontFamily;
                div.className = "layout-column layout-align-center-start ";
                if (data["rowId"] % 2 === 0) {
                    div.className += "even";
                }
                else {
                    div.className += "odd";
                }
                settings.fillCell(div, data);
            };

            painter.fillHeader = function(div, header) {
                div.style.fontSize = settings.headerFontSize;
                div.style.fontFamily = settings.headerFontFamily;
                div.style.fontWeight = "bold";
                settings.fillHeader(div, header);
            };

            tableData.getCellSync = function (i, j) {
                var data = settings.getCellSync(i, j);
                if (data !== undefined) {
                    //add row id so that we can add odd/even classes to rows
                    data.rowId = i;
                }
                return data;
            };

            tableData.getHeaderSync = function(j) {
                return settings.getHeaderSync(j);
            };

            var selector = "#" + settings.tableContainerId;
            var table = fattable({
                "container": selector,
                "model": tableData,
                "nbRows": rows.length,
                "rowHeight": settings.rowHeight,
                "headerHeight": settings.headerHeight,
                "painter": painter,
                "columnWidths": columnWidths
            });

            table.setup();


            var eventId = "resize.fattable." + settings.tableContainerId;
            angular.element($window).unbind(eventId);
            var debounced = _.debounce(self.setupTable, settings.setupRefreshDebounce);
            angular.element($window).on(eventId, function() {
                debounced(settings);
            });

            angular.element(selector).on('$destroy', function() {
                angular.element($window).unbind(eventId);
            });
        }

    }]);
});