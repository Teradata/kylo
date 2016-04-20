var GoogleChartsUtil = (function () {
    function GoogleChartsUtil() {
    }

    GoogleChartsUtil.getPieChartDataTable = function (tableData, xField, xFieldLabelFn, xColumnHeader, aggregrateColumnHeader, aggregrateFn) {
        var data = {}
        var labelMap = {};
        $.each(tableData, function (i, row) {
            var value = row[xField];
            if (data[value] === undefined) {
                data[value] = [1];
                if (xFieldLabelFn) {
                    var label = xFieldLabelFn(value, data);
                    labelMap[value] = label;
                }
            }
            else {
                data[value].push(1);
            }
        });
        var totals = [[xColumnHeader, aggregrateColumnHeader]];
        var keys = Object.keys(data);
        var len = keys.length;
        keys.sort();
        for (var i = 0; i < len; i++) {
            var key = keys[i];
            var values = data[key];
            var aggregrateValue = ArrayUtils.aggregrate(values, aggregrateFn) || null;
            totals[(i + 1)] = [labelMap[key], aggregrateValue];
        }


        return google.visualization.arrayToDataTable(totals);


    }


    /**
     *
     * @param xField  {label:'',fieldName:'',type:''}
     * @param yField  {label:'',fieldName:'',type:''}
     * @param columnField  // the fieldName to use for the Columns
     * @param aggregrateFn // defaults to 'sum'
     * @param xValueCallbackFn // callback if needing to convert the xvalue to something fn(xValue,row,columnNames)
     * @param tooltipCallbackFn fn(xValue,row,columnNames)
     * @returns google.visualization.DataTable
     */
    GoogleChartsUtil.getGoogleChartsDataTable = function (tableData, xField, yField, columnField, aggregrateFn, xValueCallbackFn, yValueCallbackFn, tooltipCallbackFn) {

        var xyMap = {};
        var xValueMap = {};
        var distinctXValues = [];

        //get unique set of columns and assign their Column Index for the Google DataTable
        var columnNames = $.map(tableData, function (row, i) {
            return row[columnField];
        });
        columnNames = $.unique(columnNames).sort();

        var offset = 0;
        var arrLength = columnNames.length + offset//add in the xValue and tooltip
        var dataTable = new google.visualization.DataTable();
        dataTable.addColumn(xField.type, xField.label);
        $.each(columnNames, function (i, columnName) {
            dataTable.addColumn(yField.type, columnName); //duration
            dataTable.addColumn({'type': 'string', 'role': 'tooltip', 'p': {'html': true}});
        })

        var tableMap = {};
        $.each(tableData, function (i, row) {
            var rowXValue = row[xField.fieldName];
            if (rowXValue !== undefined) {
                if (xValueMap[rowXValue] === undefined) {
                    var rowArray = [];   //[0=[],1=[],2=[],3=[]];
                    xValueMap[rowXValue] = rowArray;
                    for (var i = 0; i < arrLength; i++) {
                        rowArray[i] = {};
                    }
                    distinctXValues.push(rowXValue);
                }
                var rowArray = xValueMap[rowXValue];
                //add the row to the xValue placement
                var columnIndex = $.inArray(row[columnField], columnNames);
                columnIndex += offset;
                var currentValue = rowArray[columnIndex];
                if ($.isEmptyObject(currentValue)) {
                    currentValue = {objects: [], values: []};
                    rowArray[columnIndex] = currentValue;
                }
                var yValue = row[yField.fieldName];
                if (yValue != null && yValue !== undefined) {
                    currentValue.values.push(yValue);
                    currentValue.objects.push(row);
                }
            }
            else {
                //What to do if X field is null??
            }

        });
        var googleDataTableRows = [];

        //  {xValue = [[1,2,3],[2,3,4],[]]}
        //xValueMap is a map of Xvalue with array of columns.  each column is an array of values
        $.each(xValueMap, function (xValue, row) {
            var tableRow = [];
            //first is the xvalue
            var tableXVal = xValue;
            if (xValueCallbackFn) {
                tableXVal = xValueCallbackFn(xValue, row, columnNames);
            }
            tableRow.push(tableXVal);

            $.each(row, function (i, columnValues) {
                if (aggregrateFn == undefined) {
                    aggregrateFn = 'sum';
                }
                var objects = columnValues.objects;
                var values = columnValues.values;

                var yValue = ArrayUtils.aggregrate(values, aggregrateFn) || null;
                if (yValueCallbackFn) {
                    yValue = yValueCallbackFn(yValue, objects, values, columnNames);
                }
                tableRow.push(yValue);
                var toolTipValue = '';
                if (tooltipCallbackFn) {
                    toolTipValue = tooltipCallbackFn(yValue, objects, values, columnNames)
                }
                tableRow.push(toolTipValue);
            });
            googleDataTableRows.push(tableRow);
        });

        dataTable.addRows(googleDataTableRows);
        return dataTable;
    }


    /*

     this.asColumnChart = function(tableData){

     var x ={label:'Start Time',fieldName:'startTime',type:'datetime'}
     var y = {label:'Duration',fieldName:'runTime',type:'number'}
     var tooltipCallback = function(aggregrateValue,objects,values,columnNames){
     return '';
     }
     var yValueCallbackFn = function(aggregrateValue,objects,values,columnNames){
     if(aggregrateValue){
     return aggregrateValue / 1000 / 60;
     }
     else {
     return null;
     }
     }
     var xValueCallbackFn = function(xValue,row,columnNames) {
     return new Date(parseInt(xValue));
     };
     var googleDataTable = self.getGoogleChartsDataTable(tableData,x,y,'jobName','sum',xValueCallbackFn,yValueCallbackFn,tooltipCallback);

     var options = {
     legend: {position: 'right'},
     height:500,
     width:'100%',
     tooltip: {isHtml: true},
     vAxis: {title: "Run Time (min)"},
     hAxis: {title: "Start Time"},
     'chartArea':{ left: '8%', top: '8%', width: "70%", height: "70%" }
     };

     if(self.durationChart === undefined) {
     self.durationChart = new google.visualization.ColumnChart(document.getElementById("duration_chart"));
     //Click Handler to go to the detailed Job Page on click of the chart
     var selectHandler = function (e) {
     var selection = self.durationChart.getSelection();
     if (selection && selection.length > 0) {
     var row = selection[0].row;

     /*
     var instanceId = self.jobRowColumnData[row][selection[0].column].instanceId;
     $location.path('/jobs/details');
     $location.search({'instanceId': instanceId});
     $scope.$apply();


     }

     }
     google.visualization.events.addListener(self.durationChart, 'select', selectHandler);
     }

     self.durationChart.draw(googleDataTable, options);
     }


     */

    return GoogleChartsUtil;
})();



