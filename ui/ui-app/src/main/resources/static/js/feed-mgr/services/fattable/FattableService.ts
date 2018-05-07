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
import {VisualQueryPainterService} from '../../visual-query/transform-data/visual-query-table/visual-query-painter.service';

const moduleName = require('feed-mgr/module-name');

function FattableService($window: any) {
    const self = this;

    const ATTR_DATA_COLUMN_ID = "data-column-id";

    const optionDefaults: any = {
        tableContainerId: "",
        headers: [],
        rows: [],
        minColumnWidth: 50,
        maxColumnWidth: 300,
        rowHeight: 27,
        headerHeight: 40,
        headerPadding: 5,
        padding: 50,
        firstColumnPaddingLeft: 24,
        headerFontFamily: "Roboto, \"Helvetica Neue\", sans-serif",
        headerFontSize: "13px",
        headerFontWeight: "500",
        rowFontFamily: "sans-serif",
        rowFontSize: "13px",
        rowFontWeight: "normal",
        setupRefreshDebounce: 300,
        headerText: function (header: any) {
            return header.displayName;
        },
        cellText: function (row: any, column: any) {
            return row[column.displayName];
        },
        fillCell: function (cellDiv: any, data: any) {
            cellDiv.innerHTML = _.escape(data.value);
        },
        getCellSync: function (i: any, j: any) {
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
        fillHeader: function (headerDiv: any, header: any) {
            headerDiv.innerHTML = _.escape(header.value);
        },
        getHeaderSync: function (j: any) {
            return this.headers[j].displayName;
        }
    };

    self.setupTable = function (options: any) {
        // console.log('setupTable');
        let scrollXY: number[] = [];

        const optionsCopy = _.clone(options);
        const settings = _.defaults(optionsCopy, optionDefaults);

        const tableData: any = new fattable.SyncTableModel();
        const painter = new fattable.Painter();

        const headers = settings.headers;
        const rows = settings.rows;

        function get2dContext(font: any) {
            const canvas = document.createElement("canvas");
            document.createDocumentFragment().appendChild(canvas);
            const context = canvas.getContext("2d");
            context.font = font;
            return context;
        }

        const headerContext = get2dContext(settings.headerFontWeight + " " + settings.headerFontSize + " " + settings.headerFontFamily);
        const rowContext = get2dContext(settings.rowFontWeight + " " + settings.rowFontSize + " " + settings.rowFontFamily);

        tableData.columnHeaders = [];
        const columnWidths: number[] = [];
        _.each(headers, function (column) {
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

        painter.setupHeader = function (div) {
            // console.log("setupHeader");
            const separator = angular.element('<div class="header-separator"></div>');
            separator.on("mousedown", event => mousedown(separator, event));

            const heading = angular.element('<div class="header-value ui-grid-header-cell-title"></div>');

            const headerDiv = angular.element(div);

            headerDiv.append(heading).append(separator);
        };

        painter.fillCell = function (div:any, data:any) {
            if (data === undefined) {
                return;
            }
            if (data.columnId === 0) {
                div.className = " first-column ";
            }
            div.style.fontSize = settings.rowFontSize;
            div.style.fontFamily = settings.rowFontFamily;
            div.className += "layout-column layout-align-center-start ";
            if (data["rowId"] % 2 === 0) {
                div.className += " even ";
            }
            else {
                div.className += " odd ";
            }
            settings.fillCell(div, data);
        };

        painter.fillHeader = function (div: any, header: any) {
            // console.log('fill header', header);
            div.style.fontSize = settings.headerFontSize;
            div.style.fontFamily = settings.headerFontFamily;
            div.style.fontWeight = settings.headerFontWeight;
            const children = angular.element(div).children();

            setColumnId(children.last(), header.id);

            const valueDiv = children.first();
            valueDiv.css("width", (self.table.columnWidths[header.id] - settings.headerPadding - 2) + "px"); //leave 2 pixels for column separator
            const valueSpan = valueDiv.get(0);
            settings.fillHeader(valueSpan, header);
        };

        tableData.getCellSync = function (i: any, j: any) {
            const data = settings.getCellSync(i, j);
            if (data !== undefined) {
                //add row id so that we can add odd/even classes to rows
                data.rowId = i;
                data.columnId = j;
            }
            return data;
        };

        tableData.getHeaderSync = function (j: any) {
            const header = settings.getHeaderSync(j);
            return {
                value: header,
                id: j
            };
        };

        const selector = "#" + settings.tableContainerId;
        const parameters = {
            "container": selector,
            "model": tableData,
            "nbRows": rows.length,
            "rowHeight": settings.rowHeight,
            "headerHeight": settings.headerHeight,
            "painter": painter,
            "columnWidths": columnWidths
        };

        function onScroll(x: number, y: number) {
            scrollXY[0] = x;
            scrollXY[1] = y;
        }

        self.table = fattable(parameters);
        self.table.onScroll = onScroll;
        self.table.setup();

        function getColumnId(separatorSpan: any) {
            return separatorSpan.attr(ATTR_DATA_COLUMN_ID);
        }

        function setColumnId(separatorSpan: any, id: any) {
            separatorSpan.attr(ATTR_DATA_COLUMN_ID, id);
        }

        function mousedown(separator: any, e: any) {
            e.preventDefault(); //prevent default action of selecting text

            const columnId = getColumnId(separator);
            e = e || window.event;
            let start = 0, diff = 0, newWidth = settings.minColumnWidth;
            if (e.pageX) {
                start = e.pageX;
            } else if (e.clientX) {
                start = e.clientX;
            }

            const headerDiv = separator.parent();
            headerDiv.css("z-index", "1"); //to hide other columns behind the one being resized
            const headerElem = headerDiv.get(0);
            const initialHeaderWidth = headerElem.offsetWidth;
            document.body.style.cursor = "col-resize";
            document.body.onmousemove = function (e: any) {
                e = e || window.event;
                let end = 0;
                if (e.pageX) {
                    end = e.pageX;
                } else if (e.clientX) {
                    end = e.clientX;
                }

                diff = end - start;
                const width = initialHeaderWidth + diff;
                newWidth = width < settings.minColumnWidth ? settings.minColumnWidth : width;
                headerElem.style.width = newWidth + "px";
            };
            document.body.onmouseup = function () {
                document.body.onmousemove = document.body.onmouseup = null;
                headerDiv.css("z-index", "unset");
                document.body.style.cursor = null;
                resizeColumn(columnId, newWidth);
            };
        }

        function resizeColumn(columnId: number, columnWidth: number) {
            const x = scrollXY[0];
            const y = scrollXY[1];
            // console.log('resize to new width', columnWidth);
            self.table.columnWidths[columnId] = columnWidth;
            const columnOffset = _.reduce((self.table.columnWidths as number[]), function (memo, width) {
                memo.push(memo[memo.length - 1] + width);
                return memo;
            }, [0]);
            // console.log('columnWidths, columnOffset', columnWidths, columnOffset);
            self.table.columnOffset = columnOffset;
            self.table.W = columnOffset[columnOffset.length - 1];
            self.table.setup();

            // console.log('displaying cells', scrolledCellX, scrolledCellY);
            self.table.scroll.setScrollXY(x, y); //table.setup() scrolls to 0,0, here we scroll back to were we were while resizing column
        }

        const eventId = "resize.fattable." + settings.tableContainerId;
        angular.element($window).unbind(eventId);
        const debounced = _.debounce(self.setupTable, settings.setupRefreshDebounce);
        angular.element($window).on(eventId, function () {
            debounced(settings);
        });

        angular.element(selector).on('$destroy', function () {
            angular.element($window).unbind(eventId);
        });
    }

}

angular.module(moduleName).service('FattableService', ["$window", FattableService]);