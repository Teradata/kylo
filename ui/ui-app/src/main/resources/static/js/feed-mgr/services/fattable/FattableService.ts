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

 
import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/module-name');

// export class FattableService {

    
    function FattableService ($window:any){
        const self = this;

        const FONT_FAMILY = "Roboto, \"Helvetica Neue\", sans-serif";

        const optionDefaults:any = {
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
            headerText: function(header:any) {
                return header.displayName;
            },
            cellText: function(row:any, column:any) {
                return row[column.displayName];
            },
            fillCell: function(cellDiv:any, data:any) {
                cellDiv.innerHTML = _.escape(data.value);
            },
            getCellSync: function(i:any, j:any) {
                const displayName = this.headers[j].displayName;
                const row = this.rows[i];
                if (row === undefined) {
                    //occurs when filtering table
                    return undefined;
                }
                return {
                    "value": row[displayName]
                }
            },
            fillHeader: function(headerDiv:any, header:any) {
                headerDiv.innerHTML = '<div>' + _.escape(header) + '</div>';
            },
            getHeaderSync: function(j:any) {
                return this.headers[j].displayName;
            }
        };

        self.setupTable = function(options:any) {
            const optionsCopy = _.clone(options);
            const settings = _.defaults(optionsCopy, optionDefaults);

            const tableData:any = new fattable.SyncTableModel();
            const painter = new fattable.Painter();

            const headers = settings.headers;
            const rows = settings.rows;

            function get2dContext(font:any) {
                const canvas = document.createElement("canvas");
                document.createDocumentFragment().appendChild(canvas);
                const context = canvas.getContext("2d");
                context.font = font;
                return context;
            }

            const headerContext = get2dContext(settings.headerFontWeight + " " + settings.headerFontSize + " " + settings.headerFontFamily);
            const rowContext = get2dContext(settings.rowFontWeight + " " + settings.rowFontSize + " " + settings.rowFontFamily);

            tableData.columnHeaders = [];
            const columnWidths:any = [];
            _.each(headers, function(column) {
                const headerText = settings.headerText(column);
                const headerTextWidth = headerContext.measureText(headerText).width;
                const longestColumnText = _.reduce(rows, function (previousMax, row) {
                    const cellText = settings.cellText(row, column);
                    const cellTextLength = cellText === undefined || cellText === null ? 0 : cellText.length;
                    return previousMax.length < cellTextLength ? cellText : previousMax;
                }, "");

                const columnTextWidth = rowContext.measureText(longestColumnText).width;
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

            tableData.getCellSync = function (i:any, j:any) {
                const data = settings.getCellSync(i, j);
                if (data !== undefined) {
                    //add row id so that we can add odd/even classes to rows
                    data.rowId = i;
                }
                return data;
            };

            tableData.getHeaderSync = function(j:any) {
                return settings.getHeaderSync(j);
            };

            const selector = "#" + settings.tableContainerId;
            const table = fattable({
                "container": selector,
                "model": tableData,
                "nbRows": rows.length,
                "rowHeight": settings.rowHeight,
                "headerHeight": settings.headerHeight,
                "painter": painter,
                "columnWidths": columnWidths
            });

            table.setup();


            const eventId = "resize.fattable." + settings.tableContainerId;
            angular.element($window).unbind(eventId);
            const debounced = _.debounce(self.setupTable, settings.setupRefreshDebounce);
            angular.element($window).on(eventId, function() {
                debounced(settings);
            });

            angular.element(selector).on('$destroy', function() {
                angular.element($window).unbind(eventId);
            });
        }

    }
// }

angular.module(moduleName).service('FattableService', ["$window",FattableService]);