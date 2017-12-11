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
            headerFont: HEADER_HEIGHT,
            rowFont: ROW_FONT
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

            var headerContext = get2dContext(HEADER_FONT);
            var rowContext = get2dContext(ROW_FONT);

            tableData.columnHeaders = [];
            var columnWidths = [];
            _.each(headers, function(column) {
                var headerTextWidth = headerContext.measureText(column.displayName).width;
                var rowTextWidth = _.reduce(rows, function (previousMax, row) {
                    var textWidth = rowContext.measureText(row[column.displayName]).width;
                    var validationError = row.invalidFieldMap[column.displayName];
                    var ruleTextWidth = 0;
                    var reasonTextWidth = 0;
                    if (validationError !== undefined) {
                        ruleTextWidth = rowContext.measureText(validationError.rule).width;
                        reasonTextWidth = rowContext.measureText(validationError.reason).width;
                    }
                    return Math.max(previousMax, textWidth, ruleTextWidth, reasonTextWidth);
                }, MIN_COLUMN_WIDTH);

                columnWidths.push(Math.min(MAX_COLUMN_WIDTH, Math.max(headerTextWidth, rowTextWidth)) + PADDING);
                tableData.columnHeaders.push(column.displayName);
            });

            painter.fillCell = function (cellDiv, data) {
                if (data === undefined) {
                    return;
                }
                var classname = "";
                if (data.rowId % 2 === 0) {
                    classname = "even";
                }
                else {
                    classname = "odd";
                }

                var html = data.value;
                if (data.isInvalid) {
                    html += '<br><span class="violation hint">' + data.rule + '</span>';
                    html += '<br><span class="violation hint">' + data.reason + '</span>';
                    classname += " warn";
                }
                cellDiv.innerHTML = html;
                cellDiv.className = classname;
            };

            painter.fillHeader = function(headerDiv, header) {
                headerDiv.innerHTML = '<div>' + header + '</div>';
            };

            tableData.getCellSync = function (i, j) {
                var displayName = headers[j].displayName;
                var row = rows[i];
                if (row === undefined) {
                    //occurs when filtering table
                    return undefined;
                }
                var invalidFieldMap = row.invalidFieldMap[displayName];
                var isInvalid = invalidFieldMap !== undefined;
                var rule = isInvalid ? invalidFieldMap.rule : "";
                var reason = isInvalid ? invalidFieldMap.reason : "";
                return {
                    "value": row[displayName],
                    "isInvalid": isInvalid,
                    "rule": rule,
                    "reason": reason,
                    "rowId": i
                };
            };

            tableData.getHeaderSync = function(j) {
                return tableData.columnHeaders[j];
            };

            var table = fattable({
                "container": options.tableContainerId,
                "model": tableData,
                "nbRows": rows.length,
                "rowHeight": ROW_HEIGHT,
                "headerHeight": HEADER_HEIGHT,
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