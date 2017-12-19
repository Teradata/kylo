/**
 * Service which sets up fattable.
 * In the simplest form, create a div on a page with an id and then initialise table in javascript providing div selector and arrays of headers and rows, e.g.
 *
 * HTML:
 *  <div id="table-id">
 *
 * JS:
 *   FattableService.setupTable({
 *      tableContainerId:"#table-id",
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
 *      tableContainerId:"#table-id",
 *      headers: self.headers,
 *      rows: self.rows,
 *      headerText: function(header) {...},
 *      cellText: function(row, column) {...}
 *      ...
 *  });
 *
 */
define(['angular','feed-mgr/module-name','fattable'], function (angular,moduleName) {
    angular.module(moduleName).service('FattableService', function () {

        var self = this;

        var MIN_COLUMN_WIDTH = 50;
        var MAX_COLUMN_WIDTH = 300;
        var ROW_HEIGHT = 53;
        var HEADER_HEIGHT = 40;
        var PADDING = 40;
        var HEADER_FONT = "bold 12px Roboto, \"Helvetica Neue\", sans-serif";
        var ROW_FONT = "14px Roboto, \"Helvetica Neue\", sans-serif";

        var optionDefaults = {
            tableContainerId: "",
            headers: [],
            rows: [],
            minColumnWidth: MIN_COLUMN_WIDTH,
            maxColumnWidth: MAX_COLUMN_WIDTH,
            rowHeight: ROW_HEIGHT,
            headerHeight: HEADER_HEIGHT,
            padding: PADDING,
            headerFont: HEADER_FONT,
            rowFont: ROW_FONT,
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

            var headerContext = get2dContext(settings.headerFont);
            var rowContext = get2dContext(settings.rowFont);

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

            painter.fillCell = function (cellDiv, data) {
                if (data === undefined) {
                    return;
                }
                cellDiv.className = "layout-column layout-align-center-start ";
                if (data["rowId"] % 2 === 0) {
                    cellDiv.className += "even";
                }
                else {
                    cellDiv.className += "odd";
                }
                settings.fillCell(cellDiv, data);
            };

            painter.fillHeader = function(headerDiv, header) {
                settings.fillHeader(headerDiv, header);
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

            var table = fattable({
                "container": settings.tableContainerId,
                "model": tableData,
                "nbRows": rows.length,
                "rowHeight": settings.rowHeight,
                "headerHeight": settings.headerHeight,
                "painter": painter,
                "columnWidths": columnWidths
            });

            // angular.element($window).on('resize.invalidTable', function() {
            //     console.log("resizing");
            //     table.setup();
            // });
            // $scope.$on('destroy', function() {
            //     console.log('on destroy'); //todo this is not being called
            //     angular.element($window).unbind('resize.invalidTable');
            // });
            table.setup();
        }

    });
});