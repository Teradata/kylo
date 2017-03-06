/*-
 * #%L
 * thinkbig-ui-common
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
var PivotTableUtil = (function () {
    function PivotTableUtil() {
    }


    PivotTableUtil.camelCaseToWords = function (str) {
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
    PivotTableUtil.transformToPivotTable = function (tableData, hideColumns, pivotNameMap, addedColumns) {
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
                        pivotName = PivotTableUtil.camelCaseToWords(k);
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
    }


    return PivotTableUtil;
})();

