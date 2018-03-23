define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var PivotTableUtilClass = /** @class */ (function () {
        function PivotTableUtilClass() {
            var _this = this;
            this.camelCaseToWords = function (str) {
                return str.match(/^[a-z]+|[A-Z][a-z]*/g).map(function (x) {
                    return x[0].toUpperCase() + x.substr(1).toLowerCase();
                }).join(' ');
            };
            /**
             *
             * @param tableData Array of Objects
             * @param hidColumns // Array of Field Names in the array of objects below that you dont want on the pivot
             * @param pivotNameMap // a map of field name to an object {name:'',fn:function(val){}} that allows you to transform the current data to something else
             *  pivotNameMap = {"startTime":{name:"Start Time", fn:function(val){
                    return new Date(val);
                }},
                    "endTime":{name:"End Time", fn:function(val){
                        return new Date(val);
                    }}
             * @param addedColumns map of "Column Name":function(row){}
             *   {"Duration (sec)":function(row){
                 var duration  = row.runTime || 0;
                 return duration/1000;
                 }
             * @returns {Array}
             */
            this.transformToPivotTable = function (tableData, hideColumns, pivotNameMap, addedColumns) {
                var pivotRows = [];
                $.each(tableData, function (i, row) {
                    var pivotRow = {};
                    $.each(row, function (k, val) {
                        if ($.inArray(k, hideColumns) == -1) {
                            var pivotItem = pivotNameMap[k];
                            var pivotName;
                            var pivotValue;
                            if (pivotItem !== undefined) {
                                pivotName = pivotItem.name;
                                pivotValue = val;
                                if (pivotItem.fn) {
                                    pivotValue = pivotItem.fn(val);
                                }
                            }
                            else {
                                pivotValue = val;
                            }
                            if (pivotName == undefined) {
                                //cavmelcase it
                                pivotName = _this.camelCaseToWords(k);
                            }
                            pivotRow[pivotName] = pivotValue;
                        }
                        if (addedColumns && !$.isEmptyObject(addedColumns)) {
                            $.each(addedColumns, function (key, fn) {
                                pivotRow[key] = fn(row);
                            });
                        }
                    });
                    pivotRows.push(pivotRow);
                });
                return pivotRows;
            };
        }
        return PivotTableUtilClass;
    }());
    var PivotTableUtil = new PivotTableUtilClass();
    exports.default = PivotTableUtil;
});
//# sourceMappingURL=PivotTableUtil.js.map