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
define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    // export class FattableService {
    function FattableService($window) {
        var self = this;
        var FONT_FAMILY = "Roboto, \"Helvetica Neue\", sans-serif";
        var ATTR_DATA_COLUMN_ID = "data-column-id";
        var optionDefaults = {
            tableContainerId: "",
            headers: [],
            rows: [],
            minColumnWidth: 50,
            maxColumnWidth: 300,
            rowHeight: 53,
            headerHeight: 40,
            padding: 50,
            headerFontFamily: FONT_FAMILY,
            headerFontSize: "12px",
            headerFontWeight: "bold",
            rowFontFamily: FONT_FAMILY,
            rowFontSize: "14px",
            rowFontWeight: "normal",
            setupRefreshDebounce: 300,
            headerText: function (header) {
                return header.displayName;
            },
            cellText: function (row, column) {
                return row[column.displayName];
            },
            fillCell: function (cellDiv, data) {
                cellDiv.innerHTML = _.escape(data.value);
            },
            getCellSync: function (i, j) {
                var displayName = this.headers[j].displayName;
                var row = this.rows[i];
                if (row === undefined) {
                    //occurs when filtering table
                    return undefined;
                }
                return {
                    "value": row[displayName]
                };
            },
            fillHeader: function (headerDiv, header) {
                headerDiv.innerHTML = _.escape(header.value);
            },
            getHeaderSync: function (j) {
                return this.headers[j].displayName;
            }
        };
        self.setupTable = function (options) {
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
            _.each(headers, function (column) {
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
            painter.setupHeader = function (div) {
                console.log("setupHeader");
                var separator = angular.element('<span class="separator" draggable="true"></span>');
                separator
                    .on("dragstart", function (event) { return dragstart(separator, event); })
                    .on("drag", function (event) { return drag(separator, event); })
                    .on("dragend", function (event) { return dragend(separator, event); });
                var heading = angular.element('<span class="value"></span>');
                var headerDiv = angular.element(div);
                headerDiv.css('display', 'flex');
                headerDiv.css('justify-content', 'space-between');
                headerDiv.append(heading).append(separator);
            };
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
            painter.fillHeader = function (div, header) {
                console.log('fill header', header);
                div.style.fontSize = settings.headerFontSize;
                div.style.fontFamily = settings.headerFontFamily;
                div.style.fontWeight = "bold";
                var children = angular.element(div).children();
                setColumnId(children.last(), header.id);
                var valueSpan = children.first().get(0);
                settings.fillHeader(valueSpan, header);
            };
            tableData.getCellSync = function (i, j) {
                var data = settings.getCellSync(i, j);
                if (data !== undefined) {
                    //add row id so that we can add odd/even classes to rows
                    data.rowId = i;
                }
                return data;
            };
            tableData.getHeaderSync = function (j) {
                var header = settings.getHeaderSync(j);
                return {
                    value: header,
                    id: j
                };
            };
            var selector = "#" + settings.tableContainerId;
            var parameters = {
                "container": selector,
                "model": tableData,
                "nbRows": rows.length,
                "rowHeight": settings.rowHeight,
                "headerHeight": settings.headerHeight,
                "painter": painter,
                "columnWidths": columnWidths
            };
            // let x = 0;
            // let y = 0;
            // function onScroll(scrollX: number, scrollY: number) {
            //     console.log("scrolling to x,y", x, y);
            //     x = scrollX;
            //     y = scrollY;
            // }
            self.table = fattable(parameters);
            self.table.setup();
            // self.table.onScroll = onScroll;
            // window.setInterval(changeWidth, 5000, table);
            function changeWidth(table) {
                // console.log('widening first column');
                // columnWidths[0] = columnWidths[0] + 10;
                // table.cleanUp();
                // table = fattable(parameters);
                // table.setup();
                // table.onScroll = onScroll;
                // console.log('resetting to x,y', x, y);
                // const cells = table.leftTopCornerFromXY(x, y);
                // console.log('resetting to cells', cells);
                // table.goTo(cells[0],cells[1]);
                // table.refreshAllContent(true);
                // table.setup();
            }
            function getColumnId(separatorSpan) {
                return separatorSpan.attr(ATTR_DATA_COLUMN_ID);
            }
            function setColumnId(separatorSpan, id) {
                separatorSpan.attr(ATTR_DATA_COLUMN_ID, id);
            }
            function dragstart(separator, event) {
                var columnId = getColumnId(separator);
                console.log('dragstart header, event/columnId', event, columnId);
                self.dragstartX = event.originalEvent.x;
                self.dragstartColumnWidth = self.table.columnWidths[columnId];
            }
            function drag(separator, event) {
                var columnId = getColumnId(separator);
                console.log('drag header, event/columnId', event, columnId);
                var dragX = event.originalEvent.x;
                //todo use for visual feedback
            }
            function dragend(separator, event) {
                var columnId = getColumnId(separator);
                console.log('dragend header, event/columnId', event, columnId);
                var dragendX = event.originalEvent.x;
                var newWidth = self.dragstartColumnWidth + (dragendX - self.dragstartX);
                resizeColumn(columnId, newWidth < settings.minColumnWidth ? settings.minColumnWidth : newWidth);
            }
            function resizeColumn(columnId, columnWidth) {
                console.log('resize to new width', columnWidth);
                self.table.columnWidths[columnId] = columnWidth;
                var columnOffset = _.reduce(self.table.columnWidths, function (memo, width) {
                    memo.push(memo[memo.length - 1] + width);
                    return memo;
                }, [0]);
                self.table.columnOffset = columnOffset;
                self.table.W = columnOffset[columnOffset.length - 1];
                self.table.setup();
            }
            var eventId = "resize.fattable." + settings.tableContainerId;
            angular.element($window).unbind(eventId);
            var debounced = _.debounce(self.setupTable, settings.setupRefreshDebounce);
            angular.element($window).on(eventId, function () {
                debounced(settings);
            });
            angular.element(selector).on('$destroy', function () {
                angular.element($window).unbind(eventId);
            });
        };
    }
    // }
    angular.module(moduleName).service('FattableService', ["$window", FattableService]);
});
//# sourceMappingURL=FattableService.js.map