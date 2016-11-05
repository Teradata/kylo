var COMMON_APP_MODULE_NAME = "datalakeui.common";

angular.module(COMMON_APP_MODULE_NAME, [] );

/**
 * Generic Interceptor to handle any Http Request errors and push them to the NotificationService
 */
(function(){
    var httpInterceptor = function ($provide, $httpProvider) {
        $provide.factory('httpInterceptor', function ($q,$location,$window,$injector, Utils) {
            return {
                // optional method
/*
                'request': function(config) {
                    var path = $location.absUrl();
                    var pathArray = path.split('/');
                    var appContext = pathArray[3];
                   console.log('path',path,'pathArray',pathArray,'appContext',appContext,'config.url',config.url)
                    //if(config.url.indexOf("/api/") ==0) {
                    if(appContext.indexOf("#") != 0 && appContext.indexOf("/#") != 0 ) {
                        config.url = "/" + appContext + (config.url.indexOf("/") == 0 ? "" : "/") + config.url;
                    }
                    return config;
                },

                // optional method
                'requestError': function(rejection) {
                    console.log('REQUEST ERROR',rejection)
                    //if (canRecover(rejection)) {
                    //    return responseOrNewPromise
                   // }
                    return $q.reject(rejection);
                },
                */
                response: function (response) {
                    //injected manually to get around circular dependency problem.
                    var NotificationService = $injector.get('NotificationService');

                    if(response.headers() && response.headers()['Location'] &&  response.headers()['Location'].endsWith('login.html')){
                        NotificationService.errorWithGroupKey("Login Required","You are required to Login to view this content.","Login Required");

                    }
                    var data = response.data;
                    if(response && response.data && response.config && !Utils.endsWith(response.config.url,".html") && typeof response.data == 'string'){
                        if(response.data.indexOf('<!-- login.html -->') >=0){
                            NotificationService.errorWithGroupKey("Login Required","You are required to Login to view this content.","Login Required");
                            $window.location.href = '/login.html';
                        }
                    }
                    return response || $q.when(response);
                },
                responseError: function (rejection) {
                    //injected manually to get around circular dependency problem.
                    var NotificationService = $injector.get('NotificationService');

                    if(rejection.data == undefined){
                        rejection.data = {};
                    }
                    if(rejection.status === 401) {
                        // you are not autorized
                        NotificationService.errorWithGroupKey("Unauthorized","You are unauthorized to view this content.","Unauthorized");
                    }
                    else if(rejection.status <=0){
                        //internet is down
                        NotificationService.errorWithGroupKey("Connection Error","Not Connected. Server is down.","Connection Error");
                    }
                    else  if(rejection.status === 400) {
                        // Bad Request
                        var  message = "An unexpected error occurred ";
                        var errorMessage = rejection.data["message"];
                        var groupKey = errorMessage;
                        if(groupKey == undefined || groupKey == '') {
                            groupKey = 'OtherError';
                        }
                        var url = rejection.data["url"];
                        if(url != undefined && url != null && url != ""){
                            message +=" attempting to access: "+url
                        }
                        message +=".";
                        if( rejection.data['handledException'] == undefined || (rejection.data['handledException'] != undefined && rejection.data['handledException'] == false )) {
                            if (rejection.data["url"]) {
                                NotificationService.errorWithGroupKey("Error", message, url, errorMessage);
                            }
                            else {
                                NotificationService.errorWithGroupKey("Error", message, groupKey, errorMessage);
                            }
                        }

                    }
                    else{
                        if( rejection.data['handledException'] == undefined || (rejection.data['handledException'] != undefined && rejection.data['handledException'] == false )) {
                            var message = "An unexpected error occurred ";
                            var rejectionMessage = rejection.data['message'];
                            if (rejectionMessage == undefined || rejectionMessage == '') {
                                rejectionMessage = 'OtherError';
                            }
                            NotificationService.errorWithGroupKey("Error", message, rejectionMessage, rejection.data["message"]);
                        }
                    }
                    return $q.reject(rejection);
                }
            };
        });
        $httpProvider.interceptors.push('httpInterceptor');
    };
    angular.module(COMMON_APP_MODULE_NAME).config(httpInterceptor);
}());


var ArrayUtils = (function () {
    function ArrayUtils() {
    }
    ArrayUtils.sum = function(arr) {
      return arr.reduce(
          function(total, num){ return total + num }
          , 0);
    }
    ArrayUtils.avg = function(arr){
        var sum = ArrayUtils.sum(arr);
        return sum / arr.length;
    }
    ArrayUtils.min = function(arr){
        return Math.min.apply(null,arr);
    }
    ArrayUtils.max = function(arr){
        return Math.max.apply(null,arr);
    }
    ArrayUtils.first = function(arr){
        return arr[0];
    }
    ArrayUtils.last = function(arr){
        return arr[arr.length-1];
    }
    ArrayUtils.aggregrate = function(arr,fn){
        if(arr === undefined){
            arr = [];
        }
         fn = fn.toLowerCase();
        if(fn == 'max'){
            return ArrayUtils.max(arr);
        }
        else  if(fn == 'min'){
            return ArrayUtils.min(arr);
        }
        else if(fn == 'sum'){
            return ArrayUtils.sum(arr);
        }
        else if(fn == 'avg'){
            return ArrayUtils.avg(arr);
        }
        else  if(fn == 'first'){
            return ArrayUtils.first(arr);
        }
        else  if(fn == 'last'){
            return ArrayUtils.last(arr);
        }
        else {
            return undefined;
        }
    }


    return ArrayUtils;
})();
/**
 * Allow different controllers/services to subscribe and notify each other
 *
 * to subscribe include the BroadcastService in your controller and call this method:
 *  - BroadcastService.subscribe($scope, 'SOME_EVENT', someFunction);
 *
 * to notify call this:
 * -  BroadcastService.notify('SOME_EVENT,{optional data object},### optional timeout);
 */
angular.module(COMMON_APP_MODULE_NAME).factory('BroadcastService', function ($rootScope, $timeout) {
    /**
     * map to check if multiple events come in for those that {@code data.notifyAfterTime}
     * to ensure multiple events are not fired.
     * @type {{}}
     */
    var waitingEvents = {};

    var data = {
        /**
         * notify subscribers of this event passing an optional data object and optional wait time (millis)
         * @param event
         * @param data
         * @param waitTime
         */
        notify: function (event, data, waitTime) {
            if (waitTime == undefined) {
                waitTime = 0;
            }
            if (waitingEvents[event] == undefined) {
                waitingEvents[event] = event;
                $timeout(function () {
                    $rootScope.$emit(event, data);
                    delete waitingEvents[event];
                }, waitTime);
            }
        },
        /**
         * Subscribe to some event
         * @param scope
         * @param event
         * @param callback
         */
        subscribe: function(scope, event, callback) {
            var handler = $rootScope.$on(event, callback);
            scope.$on('$destroy', handler);
        }

    }
    return data;
});

var BroadcastConstants = (function () {
    function BroadcastConstants() {
    }

    BroadcastConstants.CONTENT_WINDOW_RESIZED = 'CONTENT_WINDOW_RESIZED';

    return BroadcastConstants;
})();

/**
 * Directive to auto size the container to fill the rest of the screen based upon the height of the browser window
 *
 * attrs:
 *  - browser-height-selector=  some css selector (i.e. #content) this will be used to determine the height of the element instead of the current element
 *  - browser-height-scroll-y=true/false   show the scroll bar on the content
 *  - browser-height-wait-and-calc= true/false  if true it will wait before calculating the height to get the items in the page default=false
 *  - browser-height-scroll-left or browser-height-scroll-x=##
 *  - browser-height-resize-event=binding to on resize of the window
 *  - browser-height-offset=offset to apply to the height of the element after getting the window size
 *
 */
(function () {

    var directive = function ($window, $compile) {
        return {

            link: function ($scope, element, attrs) {
                element.addClass('browser-height');
                /**
                 *
                 */

                var eleSelector = attrs.browserHeightSelector;

                var scrollY = attrs.browserHeightScrollY;

                var browserHeightWaitAndCalc = attrs.browserHeightWaitAndCalc;

                if(browserHeightWaitAndCalc != undefined && browserHeightWaitAndCalc == "true"){
                    browserHeightWaitAndCalc = true;
                }
                else {
                    browserHeightWaitAndCalc = false;
                }

                if(scrollY == undefined){
                    scrollY = true;
                }
                else {
                    if(scrollY == "false"){
                        scrollY = false;
                    }
                    else {
                        scrollY = true;
                    }
                }

                var scrollX= attrs.browserHeightScrollLeft;
                if(scrollX == undefined) {
                    scrollX = attrs.browserHeightScrollX;
                }

                if(scrollX == "true"){
                    scrollX = true;
                }
                else {
                    scrollX = false;
                }


                var bindResize = attrs.browserHeightResizeEvent;
                if(bindResize == undefined){
                    bindResize = true;
                }
                else {
                    if(bindResize == "true"){
                        bindResize = true;
                    }
                    else {
                        bindResize = false;
                    }
                }


                var ele = element
                if(eleSelector!= undefined){
                    ele = element.find(eleSelector);
                }
                var offsetHeight = attrs.browserHeightOffset;
                if (offsetHeight) {
                    offsetHeight = parseInt(offsetHeight);
                }
                else {
                    offsetHeight = 0;
                }
                function calcHeight() {
                    var windowHeight = angular.element($window).height();
                    var newHeight = windowHeight - offsetHeight;

                    ele.css('height',newHeight+'px');
                    if(scrollY) {
                        ele.css('overflow-y', 'scroll');
                    }
                    if(scrollX) {
                        ele.css('overflow-x','scroll');
                    }
                }

                if(browserHeightWaitAndCalc){
                    setTimeout(function() {
                        calcHeight();
                    },1300)
                }

                if(bindResize) {
                    angular.element($window).bind("resize.browserheight", function () {
                        // if(element.is(':visible')) {
                        calcHeight();
                        //  }
                    });
                }
                    $scope.$on('$destroy', function () {
                        //tabsWrapper.css('top', '0px')
                        angular.element($window).unbind("resize.browserheight");
                        //angular.element('#content').unbind("scroll");
                    });
                setTimeout(function(){
                    calcHeight();
                },10);



                //set margin-top = top

            }
        }
    }




    angular.module(COMMON_APP_MODULE_NAME)
        .directive('browserHeight', directive);

})();




angular.module(COMMON_APP_MODULE_NAME).directive("cardLayout", function($compile)  {
    return {
        scope: {headerCss:"@",bodyCss:"@",cardCss:'@'},
        transclude: {
            'header1':'?headerSection',
            'body1':'?bodySection'
        },
        templateUrl:'js/shared/card-layout/card-layout-template.html',
    link: function (scope, iElem, iAttrs, ctrl, transcludeFn) {

    }
};
});angular.module(COMMON_APP_MODULE_NAME).directive("tbaCardFilterHeader", function()  {
    return {
        scope: {},
        bindToController: {
            cardTitle:'=',
            viewType:'=',
            filterModel:'=',
            sortOptions:'=',
            pageName:'@',
            onSelectedOption:'&'
        },
        controllerAs:'$cardFilterHeader',
        templateUrl:'js/shared/card-filter-header/card-filter-header-template.html',
        compile: function() {
            return function postCompile(scope, element, attr) {
                element.parents('.md-toolbar-tools:first').addClass('card-filter-header')
            };
        },
        link: function ($scope, $element, $attributes, ctrl, transcludeFn) {
        },
        controller: function($scope, $element, TableOptionsService, PaginationDataService){
            var self = this;

            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedOption = function(option) {
                if(option.type == 'sort') {
                    var currentSort = TableOptionsService.toggleSort(self.pageName, option);
                     if(self.onSelectedOption){
                        self.onSelectedOption()(option);
                    }
                }
            }
        }
    };
});angular.module(COMMON_APP_MODULE_NAME).directive('stringToNumber', function() {
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function(value) {
                return '' + value;
            });
            ngModel.$formatters.push(function(value) {
                return parseFloat(value, 10);
            });
        }
    };
});

angular.module(COMMON_APP_MODULE_NAME).directive('currentTime', function ($http, $interval, $filter) {
    return {
        restrict: 'EA',
        scope: {
            dateFormat: '@',
            refreshInterval: '@'
        },
        template: "<span>{{currentTimeUtc}}</span>",
        link: function (scope, element, attrs) {

            if (scope.dateFormat == null) {
                scope.dateFormat = 'MMM d, yyyy HH:mm:ss';
            }

            function getTime() {
                $http.get('/proxy/v1/configuration/system-time').then(function (response) {
                    scope.currentTimeUtc = $filter('date')(response.data, scope.dateFormat)
                });
            }

            getTime();
            if (scope.refreshInterval == null) {
                scope.refreshInterval = 5000;
            }
            if (scope.refreshInterval > 1000) {
                $interval(getTime, scope.refreshInterval);
            }
        }
    };
});/*
 * Copyright (c) 2016.
 */

/**
 *
 */
angular.module(COMMON_APP_MODULE_NAME).service('ConfigurationService', function () {

  var self = this;
  this.MODULE_URLS = "/api/v1/configuration/module-urls";
});angular.module(COMMON_APP_MODULE_NAME).directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function () {
                scope.$apply(function () {
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);


function uploadFile($parse) {
    var directive = {
        restrict: 'E',
        template: '<input id="fileInput" type="file" class="ng-hide"> <md-button id="uploadButton" class="md-raised md-primary" aria-label="attach_file">    Choose file </md-button><md-input-container class="condensed-no-float" md-no-float  flex>    <input id="textInput" size="40" ng-model="fileName" type="text" placeholder="No file chosen" ng-readonly="true" style="margin-top: 20px;"></md-input-container>',
        link: function (scope, element, attrs) {
            var input = $(element[0].querySelector('#fileInput'));
            var button = $(element[0].querySelector('#uploadButton'));
            var textInput = $(element[0].querySelector('#textInput'));

            var size = attrs.inputSize;
            if (size != null) {
                try {
                    size = parseInt(size);
                    input.attr("size", size)
                } catch (e) {

                }


            }
            var model = $parse(attrs.uploadFileModel);
            var modelSetter = model.assign;

            if (input.length && button.length && textInput.length) {
                button.click(function (e) {
                    input.click();
                });
                textInput.click(function (e) {
                    input.click();
                });
            }

            input.on('change', function (e) {
                var files = e.target.files;
                if (files[0]) {
                    scope.fileName = files[0].name;
                    button.removeClass("md-primary")
                } else {
                    scope.fileName = null;
                    button.addClass("md-primary")
                }
                scope.$apply(function () {
                    modelSetter(scope, files[0]);
                });
            });
        }
    };
    return directive;
}


angular.module(COMMON_APP_MODULE_NAME).directive('uploadFile', ['$parse', uploadFile]);


angular.module(COMMON_APP_MODULE_NAME).service('FileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function (file, uploadUrl, successFn, errorFn, params) {
        var fd = new FormData();
        fd.append('file', file);
        if (params) {
            angular.forEach(params, function (val, key) {
                fd.append(key, val);
            })
        }
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
            .success(function (data) {
                if (successFn) {
                    successFn(data)
                }
            })
            .error(function (err) {
                if (errorFn) {
                    errorFn(err)
                }
            });
    }
}]);angular.module(COMMON_APP_MODULE_NAME).filter('words', function () {
    return function (input, words) {
        if (isNaN(words)) {
            return input;
        }
        if (words <= 0) {
            return '';
        }
        if (input) {
            var inputWords = input.split(/\s+/);
            if (inputWords.length > words) {
                input = inputWords.slice(0, words).join(' ') + '\u2026';
            }
        }
        return input;
    };
});

//previously nifiTimeUnit

angular.module(COMMON_APP_MODULE_NAME).filter('timeAsSeconds',function(){

    return function(seconds){

        //convert back and forth to seconds
        var SECONDS_PER_YEAR = 31536000;
        var SECONDS_PER_MONTH =2592000;
        var SECONDS_PER_WEEK = 604800;
        var SECONDS_PER_DAY = 86400;
        var SECONDS_PER_HOUR = 3600;
        var SECONDS_PER_MINUTE = 60;
        var timePeriod = '';
        var units = '';
        seconds = seconds.substring(0,seconds.indexOf("sec"));
        if(!isNaN(seconds)){
            seconds = parseInt(seconds);
        }

            var numyears = Math.floor(seconds / SECONDS_PER_YEAR);
            var nummonths = Math.floor((seconds % SECONDS_PER_YEAR) / SECONDS_PER_MONTH);
            var numweeks = Math.floor(((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) / SECONDS_PER_WEEK);
            var numdays = Math.floor((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) / SECONDS_PER_DAY);
            var numhours = Math.floor(((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) / SECONDS_PER_HOUR);
            var numminutes = Math.floor((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR)/ SECONDS_PER_MINUTE);
            var numseconds = ((((((seconds % SECONDS_PER_YEAR) % SECONDS_PER_MONTH) % SECONDS_PER_WEEK) % SECONDS_PER_DAY) % SECONDS_PER_HOUR) % SECONDS_PER_MINUTE);

            if(numyears >0){
                timePeriod = "years";
                units = numyears;
            } else if(nummonths >0){
                timePeriod = "months";
                units = nummonths;
            }else if(numweeks >0){
                timePeriod = "weeks";
                units = numweeks;
            }
            else if(numdays >0){
                timePeriod = "days";
                units = numdays;
            }
            else if(numhours >0){
                timePeriod = "hours";
                units = numhours;
            }
            else if(numminutes >0){
                timePeriod = "minutes";
                units = numminutes;
            } else if(numseconds >0){
                timePeriod = "seconds";
                units = numseconds;
            }
        return units+" "+timePeriod;



    }
});

angular.module(COMMON_APP_MODULE_NAME).filter('characters', function () {
    return function (input, chars) {
        if(input == null || input == undefined) {
            input = '';
        }
        if (isNaN(chars)) {
            return input;
        }
        if (chars) {
            if(input.length > chars){
                input = input.substring(0,chars)+"...";
            }
        }
        return input;
    };
});



angular.module(COMMON_APP_MODULE_NAME).filter('maskProfanity',['Utils', function (Utils) {
    return function (input) {
       return Utils.maskProfanity(input)
    };
}]);

/**
 * Filters out all duplicate items from an array by checking the specified key
 * @param [key] {string} the name of the attribute of each object to compare for uniqueness
 if the key is empty, the entire object will be compared
 if the key === false then no filtering will be performed
 * @return {array}
 */
angular.module(COMMON_APP_MODULE_NAME).filter('unique', function () {

    return function (items, filterOn) {

        if (filterOn === false) {
            return items;
        }

        if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
            var hashCheck = {}, newItems = [];

            var extractValueToCompare = function (item) {
                if (angular.isObject(item) && angular.isString(filterOn)) {
                    return item[filterOn];
                } else {
                    return item;
                }
            };

            angular.forEach(items, function (item) {
                var valueToCheck, isDuplicate = false;

                for (var i = 0; i < newItems.length; i++) {
                    if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    newItems.push(item);
                }

            });
            items = newItems;
        }
        return items;
    };
});

angular
    .module('example', [])
    .filter('trust', [
        '$sce',
        function($sce) {
            return function(value, type) {
                // Defaults to treating trusted text as `html`
                return $sce.trustAs(type || 'html', text);
            }
        }
    ])
;


angular.module(COMMON_APP_MODULE_NAME).filter('highlight', function($sce) {
    return function(text, phrase) {
        if (phrase) text = text.replace(new RegExp('('+phrase+')', 'gi'),
            '<span class="highlighted">$1</span>')

        return $sce.trustAsHtml(text)
    }
});

angular.module(COMMON_APP_MODULE_NAME).filter('join', function() {
    return function(arr, joinChar, childProperty) {

        if(joinChar === undefined){
            joinChar = ',';
        }

        if(angular.isArray(arr)) {
           if(!childProperty) {
               return arr.join(joinChar);
           }
            else {
              return _.map(arr,function(item){
                  return item[childProperty];
               }).join(joinChar);
           }
        }
        return arr;
    }
});

angular.module(COMMON_APP_MODULE_NAME).filter('moment', function() {
    return function(timeInMs) {
        return moment(timeInMs).fromNow();
    };
});


angular.module(COMMON_APP_MODULE_NAME).filter('time', ['Utils',function(Utils) {
    return function(timeInMs) {
        var time = Utils.formatTimeMinSec(timeInMs);
        return time;
    };
}]);
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



/**
 * Service that allows you to create Abortable Requests either from a single Url or from multiple urls
 * and attach callbacks and/or abort if necessary
 *
 * Examples:
 * Simple Get
 * var rqst = HttpService.get("/feed/");
 * rqst.promise.success(successFn);
 * //abort it (usually in the destroy of the controller or directive)
 * rqst.abort();
 *
 *
 */
angular.module(COMMON_APP_MODULE_NAME).service('HttpService', ['$q', '$http', function ($q, $http) {

    var self = this;
    /**
     * Cancel all Pending Requests.
     * This is useful when changing views
     */
    this.cancelPendingHttpRequests = function () {
        angular.forEach($http.pendingRequests, function (request) {

            if (request.cancel && request.timeout) {
                request.cancel.resolve();
            }
        });
    }

    this.getJson = function(url){
        return $http.get(url,{headers:{'Content-type': 'application/json'}});
    }


    this.AbortableRequestBuilder = function (url) {
        var builder = this;

        this.successFn;
        this.errorFn;
        this.finallyFn;
        this.transformFn;
        this.params;
        this.url = url;

        return {
            params: function (getParameters) {
                if (getParameters) {
                    builder.params = getParameters;
                }
                return this;
            },
            transform: function (fn) {
                if (fn) {
                    builder.transformFn = fn;
                }
                return this;
            },
            success: function (fn) {
                if (fn) {
                    builder.successFn = fn;
                }
                return this;
            },
            error: function (fn) {
                if (fn) {
                    builder.errorFn = fn;
                }
                return this;
            },
            finally: function (fn) {
                if (fn) {
                    builder.finallyFn = fn;
                }
                return this;
            },
            build: function () {
                var canceller = {
                    resolve: function () {
                    }
                };//$q.defer();
                var options = {}//timeout: canceller.promise, cancel: canceller};
                if (builder.params) {
                    options.params = builder.params;
                }
                if (builder.transformFn) {
                    // options.transformResponse = self.appendTransform($http.defaults.transformResponse,function(value){
                    //    return transformResponseFn(value);
                    //  })
                    options.transformResponse = builder.transformFn;
                }
                var promise = $http.get(builder.url, options);
                if (builder.successFn) {
                    promise.success(builder.successFn);
                }
                if (builder.errorFn) {
                    promise.error(builder.errorFn);
                }

                promise.finally(function () {
                    if (builder.finallyFn) {
                        builder.finallyFn();
                    }
                });

                return {
                    promise: promise,
                    cancel: canceller,
                    abort: function () {
                        if (this.cancel != null) {
                            this.cancel.resolve('Aborted');
                        }
                        this.cancel = null;
                        this.promise = null;
                    }
                };
            }
        }
    }

    /**
     * Creates an Abortable Request Builder for multiple Urls and allows you to listen for the final
     * success return
     * @param urls
     * @returns {{success: Function, error: Function, finally: Function, build: Function}}
     */
    this.getAllRequestBuilder = function (urls) {
        this.id = IDGenerator.generateId('requestBuilder');
        var builder = this;
        this.urls = urls;
        this.requestBuilders = [];

        this.finalSuccessFn;
        this.finalErrorFn;
        this.finallyFn;

        this.allData = [];

        var successFn = function (data) {
            if (data.length === undefined) {
                data = [data];
            }
            if (data.length > 0) {
                builder.allData.push.apply(builder.allData, data);
            }

        };
        var errorFn = function (data, status, headers, config) {
            if (status && status == 0) {
                //cancelled request
            }
            else {
                console.log("Failed to execute query  ", data, status, headers, config);
            }
        };

        for (var i = 0; i < urls.length; i++) {
            var rqst = new self.AbortableRequestBuilder(urls[i]).success(successFn).error(errorFn);
            builder.requestBuilders.push(rqst);
        }

        return {

            success: function (fn) {
                if (fn) {

                    builder.finalSuccessFn = fn;
                }
                return this;
            },
            error: function (fn) {
                if (fn) {
                    builder.finalErrorFn = fn;
                }
                return this;
            },
            finally: function (fn) {
                if (fn) {
                    builder.finallyFn = fn;
                }
                return this;
            },
            build: function () {
                var deferred = $q.defer();


                var promises = [];
                var requests = [];
                for (var i = 0; i < builder.requestBuilders.length; i++) {
                    var rqst = builder.requestBuilders[i].build();
                    requests.push(rqst);
                    promises.push(rqst.promise);
                }

                deferred.promise.then(function (data) {

                    if (builder.finalSuccessFn) {
                        builder.finalSuccessFn(data);
                    }
                }, function () {
                    if (builder.finalErrorFn) {
                        builder.finalErrorFn();
                    }
                }).finally(function () {
                    if (builder.finallyFn) {
                        builder.finallyFn();
                    }
                });

                $q.all(promises).then(function (returnData) {
                    deferred.resolve(builder.allData);
                }, function (e) {
                    if (e && e.status && e.status == 0) {
                        //cancelled request... dont log
                    } else {
                        console.log("Error occurred", e);
                    }
                });

                return {
                    requests: requests,
                    promise: deferred.promise,
                    abort: function () {
                        if (this.requests) {
                            for (var i = 0; i < this.requests.length; i++) {
                                this.requests[i].abort('Aborted');
                            }
                            ;
                        }
                    }
                }
            }
        }
    }

    /**
     * Return an Abortable Request
     * Usage:
     * var rqst = HttpService.get("/feed/" + feed + "/exitCode/" + exitCode);
     * rqst.promise.success(function(data){ ..})
     *     .error(function() { ...})
     *     .finally(function() { ... });
     * //to abort:
     * rqst.abort();
     *
     * @param url
     * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
     */
    this.get = function (url) {
        return self.newRequestBuilder(url).build();
    }
    /**
     * creates a new AbortableRequestBuilder
     * This needs to call build to execute.
     * Example:
     *    HttpService.newRequestBuilder(url).build();
     * @param url
     * @returns {AbortableRequestBuilder}
     */
    this.newRequestBuilder = function (url) {
        return new self.AbortableRequestBuilder(url);
    }

    this.getAndTransform = function (url, transformFn) {
        return self.newRequestBuilder(url).transform(transformFn).build();
    }


    /**
     * Example Usage:
     * 1. pass in callbacks:
     * var rqst = HttpService.getAll(urls,successFn,errorFn,finallyFn);
     * //to abort
     * rqst.abort()
     * 2. add callbacks
     * var rqst = HttpService.getAll(urls);
     * rqst.promise.then(successFn,errorFn).finally(finallyFn);
     * //to abort
     * rqst.abort();
     *
     * @param urls
     * @param successFunction
     * @param errorFunction
     * @param finallyFunction
     * @returns {*|{promise, cancel, abort}|{requests, promise, abort}}
     */
    this.getAll = function (urls, successFunction, errorFunction, finallyFunction) {
        return new self.getAllRequestBuilder(urls).success(successFunction).error(errorFunction).finally(finallyFunction).build();
    }


    this.appendTransform = function (defaults, transform) {

        // We can't guarantee that the default transformation is an array
        defaults = angular.isArray(defaults) ? defaults : [defaults];

        // Append the new transformation to the defaults
        return defaults.concat(transform);
    }
}]);
var IDGenerator = (function () {
    function IDGenerator() {
    }
    IDGenerator.generateId = function (prefix) {
       IDGenerator.idNumber++;
        if(prefix){
            return prefix+'_'+IDGenerator.idNumber;
        }
        else{
            return IDGenerator.idNumber;
        }
    };
    IDGenerator.idNumber = 0;

    return IDGenerator;
})();



var JobUtils = (function () {
    function JobUtils() {
    }
    JobUtils.filterStatusMap = {'All':'','Active':'STARTED,RUNNING,FAILED','Running':'STARTED','Waiting':'STARTING','Completed':'COMPLETED','Failed':'FAILED','Stopped':'STOPPED','Abandoned':'ABANDONED'};
    JobUtils.statusFilterMap = function() {
        var map = {};
        $.each(JobUtils.filterStatusMap,function(filter,status){
            var arr = status.split(',');
            if(arr.length ==1){
                map[status] = filter;
            }
        });
        return map;
    }
    return JobUtils;
})();
/**
 * get params from url address bar
 * example.com?param1=name&param2=&id=6

 $.urlParam('param1'); // name
 $.urlParam('id');        // 6
 $.urlParam('param2');   // null

 * @param name
 * @returns {*}
 */
(function ($) {
$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null){
        return null;
    }
    else{
        return results[1] || 0;
    }
}
})(jQuery);(function() {
angular.module(COMMON_APP_MODULE_NAME).directive("mdDatepickerContainer", function()  {
    return {
        scope: {label:'@'},
        link: function ($scope, $element, attrs, ctrl) {
        $element.prepend('<label>'+$scope.label+'</label>');

        }
    };
})

}());/*
 * Copyright (c) 2015.
 */

(function() {
    'use strict';


    angular.module( 'ngTextTruncate', [] )


        .directive( "ngTextTruncate", [ "$compile", "ValidationServices", "CharBasedTruncation", "WordBasedTruncation",
            function( $compile, ValidationServices, CharBasedTruncation, WordBasedTruncation ) {
                return {
                    restrict: "A",
                    scope: {
                        text: "=ngTextTruncate",
                        charsThreshould: "@ngTtCharsThreshold",
                        wordsThreshould: "@ngTtWordsThreshold",
                        customMoreLabel: "@ngTtMoreLabel",
                        customLessLabel: "@ngTtLessLabel"
                    },
                    controller: function( $scope, $element, $attrs ) {
                        $scope.toggleShow = function() {
                            $scope.open = !$scope.open;
                        };

                        $scope.useToggling = $attrs.ngTtNoToggling === undefined;
                    },
                    link: function( $scope, $element, $attrs ) {
                        $scope.open = false;

                        ValidationServices.failIfWrongThreshouldConfig( $scope.charsThreshould, $scope.wordsThreshould );

                        var CHARS_THRESHOLD = parseInt( $scope.charsThreshould );
                        var WORDS_THRESHOLD = parseInt( $scope.wordsThreshould );

                        $scope.$watch( "text", function() {
                            $element.empty();

                            if( CHARS_THRESHOLD ) {
                                if( $scope.text && CharBasedTruncation.truncationApplies( $scope, CHARS_THRESHOLD ) ) {
                                    CharBasedTruncation.applyTruncation( CHARS_THRESHOLD, $scope, $element );

                                } else {
                                    $element.append( $scope.text );
                                }

                            } else {

                                if( $scope.text && WordBasedTruncation.truncationApplies( $scope, WORDS_THRESHOLD ) ) {
                                    WordBasedTruncation.applyTruncation( WORDS_THRESHOLD, $scope, $element );

                                } else {
                                    $element.append( $scope.text );
                                }

                            }
                        } );
                    }
                };
            }] )



        .factory( "ValidationServices", function() {
            return {
                failIfWrongThreshouldConfig: function( firstThreshould, secondThreshould ) {
                    if( (! firstThreshould && ! secondThreshould) || (firstThreshould && secondThreshould) ) {
                        throw "You must specify one, and only one, type of threshould (chars or words)";
                    }
                }
            };
        })



        .factory( "CharBasedTruncation", [ "$compile", function( $compile ) {
            return {
                truncationApplies: function( $scope, threshould ) {
                    return $scope.text.length > threshould;
                },

                applyTruncation: function( threshould, $scope, $element ) {
                    if( $scope.useToggling ) {
                        var el = angular.element(    "<span>" +
                            $scope.text.substr( 0, threshould ) +
                            "<span ng-show='!open'>...</span>" +
                            "<span class='btn-link ngTruncateToggleText' " +
                            "ng-click='toggleShow()'" +
                            "ng-show='!open'>" +
                            " " + ($scope.customMoreLabel ? $scope.customMoreLabel : "More") +
                            "</span>" +
                            "<span ng-show='open'>" +
                            $scope.text.substring( threshould ) +
                            "<span class='btn-link ngTruncateToggleText'" +
                            "ng-click='toggleShow()'>" +
                            " " + ($scope.customLessLabel ? $scope.customLessLabel : "Less") +
                            "</span>" +
                            "</span>" +
                            "</span>" );
                        $compile( el )( $scope );
                        $element.append( el );

                    } else {
                        $element.append( $scope.text.substr( 0, threshould ) + "..." );

                    }
                }
            };
        }])



        .factory( "WordBasedTruncation", [ "$compile", function( $compile ) {
            return {
                truncationApplies: function( $scope, threshould ) {
                    return $scope.text.split( " " ).length > threshould;
                },

                applyTruncation: function( threshould, $scope, $element ) {
                    var splitText = $scope.text.split( " " );
                    if( $scope.useToggling ) {
                        var el = angular.element(    "<span>" +
                            splitText.slice( 0, threshould ).join( " " ) + " " +
                            "<span ng-show='!open'>...</span>" +
                            "<span class='btn-link ngTruncateToggleText' " +
                            "ng-click='toggleShow()'" +
                            "ng-show='!open'>" +
                            " " + ($scope.customMoreLabel ? $scope.customMoreLabel : "More") +
                            "</span>" +
                            "<span ng-show='open'>" +
                            splitText.slice( threshould, splitText.length ).join( " " ) +
                            "<span class='btn-link ngTruncateToggleText'" +
                            "ng-click='toggleShow()'>" +
                            " " + ($scope.customLessLabel ? $scope.customLessLabel : "Less") +
                            "</span>" +
                            "</span>" +
                            "</span>" );
                        $compile( el )( $scope );
                        $element.append( el );

                    } else {
                        $element.append( splitText.slice( 0, threshould ).join( " " ) + "..." );
                    }
                }
            };
        }]);

}());
angular.module(COMMON_APP_MODULE_NAME).service('NotificationService', function ($timeout,$mdToast) {

    var self = this;
    this.alerts = {};
    this.CONNECTION_ERRORS_ALERT_THRESHOLD = 5;
    this.connectionErrors = 0;
    this.lastConnectionError = 0;

    this.addAlert = function(errorType,message, detailMsg,type, timeout, groupKey) {
        var id = IDGenerator.generateId("alert");
        var alert = {type:type,msg:message,id:id,detailMsg:detailMsg, errorType:errorType, hasDetail:false};
        if(detailMsg != undefined && detailMsg != ""){
            alert.hasDetail = true;
        }
        this.alerts[id] = alert;
     /*   if(timeout){
            $timeout(function(){
                self.removeAlert(id)
            },timeout);
        }*/
        if(groupKey){
            alert.groupKey = groupKey;
        }
        self.toastAlert(alert,timeout);
        return alert;
    }

    this.toastAlert = function(alert,timeout) {

         var options = {hideDelay:false, msg:alert.msg}
        if(timeout){
            options.hideDelay = timeout;
        }
        if(alert.hasDetail){
            options.msg += " "+alert.detailMsg;
        }

        var alertId = alert.id;
        var toast = $mdToast.simple()
            .textContent(options.msg)
            .action('Ok')
            .highlightAction(true)
            .hideDelay(options.hideDelay)
           // .position(pinTo);
        $mdToast.show(toast).then(function(response) {
            if ( response == 'ok' ) {
                $mdToast.hide();
                self.removeAlert(alertId)
            }
        });

        if(timeout){
            $timeout(function(){
                self.removeAlert(id)
            },timeout);
        }


    }

    this.getAlertWithGroupKey = function(groupKey){
        var returnedAlert = null;
        angular.forEach(this.alerts,function(alert,id){
            if(returnedAlert == null && alert.groupKey && alert.groupKey == groupKey){
                returnedAlert = alert;
            }
        });
        return returnedAlert;
    }

    this.success = function(message, timeout) {
        return this.addAlert("Success",message,undefined,"success", timeout);
    }
    this.error = function(message, timeout) {
      //  console.error("ERROR ",message)
        return this.addAlert("Error",message,undefined,"danger", timeout);
    }
    this.errorWithErrorType = function(errorType,message, timeout) {
       // console.error("ERROR ",message)
        return this.addAlert(errorType,message,undefined,"danger", timeout);
    }

    this.errorWithDetail = function(errorType,message,detailMsg, timeout) {
      //  console.error("ERROR ",message, detailMsg)
        return this.addAlert(errorType,message,undefined,"danger",detailMsg, timeout);
    }

    this.errorWithGroupKey = function(errorType,message,groupKey,detailMsg) {
     //   console.error("ERROR ",message, detailMsg)
        //Only add the error if it doesnt already exist
        if(groupKey != undefined) {
            if (this.getAlertWithGroupKey(groupKey) == null) {
                var alert = false;
                if(groupKey == "Connection Error"){
                    self.connectionErrors++;
                    //reset the connection error check if > 1 min
                    if((new Date().getTime() - self.lastConnectionError) > 60000)
                    {
                        self.connectionErrors = 0;
                    }
                    self.lastConnectionError = new Date().getTime();
                    if(self.connectionErrors > self.CONNECTION_ERRORS_ALERT_THRESHOLD){
                        self.connectionErrors = 0;
                        alert = true;
                    }
                } else {
                    alert = true;
                }
                if(alert) {
                    return this.addAlert(errorType, message, detailMsg, "danger", undefined, groupKey);
                }
                else {
                    return {};
                }

            }
        }
        else {
            this.error(message,undefined);
        }
    }
    this.removeAlert = function(id){
        delete this.alerts[id];
    }

    this.getAlerts = function(){
        return this.alerts;
    }



});/*
 * Copyright (c) 2015.
 */

(function () {

    var directive = function ($mdDialog, PaginationDataService) {
        return {
            restrict: "E",
            scope: {
                sortOptions: "=",
                selectedOption: "&",
                openedMenu:"&",
                menuIcon: "@",
                menuKey:"@",
                tabs:'=',
                rowsPerPageOptions:"=",
                showViewType:'=',
                showPagination:'='
            },
            templateUrl: 'js/shared/options-menu/options-menu-template.html',
            link: function ($scope, element, attrs) {
               if($scope.showViewType) {
                   $scope.viewType = {label:'List View', icon:'list', value:'list', type:'viewType'};
               }

                $scope.getPaginationId = function(tab){
                    return PaginationDataService.paginationId($scope.menuKey,tab.title);
                }

                $scope.getCurrentPage = function(tab){
                    return PaginationDataService.currentPage($scope.menuKey, tab.title);
                }


                function setViewTypeOption(toggle){
                    $scope.viewType.value =  PaginationDataService.viewType($scope.menuKey);

                    if(toggle == true) {
                        $scope.viewType.value =  $scope.viewType.value  == 'list' ? 'table' : 'list';
                    }
                    if( $scope.viewType.value == 'list'){
                        $scope.viewType.label = 'List View';
                        $scope.viewType.icon = 'list';
                    }
                    else {
                        $scope.viewType.label = 'Table View';
                        $scope.viewType.icon = 'grid_on';
                    }
                }
                if($scope.showViewType) {
                    //toggle the view Type so its opposite the current view type
                    setViewTypeOption(true);
                }




                $scope.rowsPerPage = 5;
                $scope.paginationData = PaginationDataService.paginationData($scope.menuKey);
                var originatorEv;
                $scope.openMenu = function($mdOpenMenu, ev) {

                    originatorEv = ev;
                    if($scope.openedMenu) {
                        $scope.openedMenu();
                    }
                    if($scope.showPagination) {
                        var tabData = PaginationDataService.getActiveTabData($scope.menuKey);
                        $scope.currentPage = tabData.currentPage;
                        $scope.paginationId = tabData.paginationId;
                    }
                    $mdOpenMenu(ev);
                };

                $scope.selectOption = function(item) {

                    var itemCopy = {};
                    angular.extend(itemCopy,item);
                    if(item.type == 'viewType'){
                        PaginationDataService.toggleViewType($scope.menuKey);
                        setViewTypeOption(true);
                    }

                    if($scope.selectedOption) {
                        $scope.selectedOption()(itemCopy);
                    }

                       originatorEv = null;
                }

                $scope.$on('$destroy', function () {

                });






            }

        }
    };


    angular.module(COMMON_APP_MODULE_NAME)
        .directive('tbaOptionsMenu', ['$mdDialog','PaginationDataService',directive]);

}());
/*
 * Service used to get/set Pagination Data, Sorting Data, and view Type on the tables
 */

angular.module(COMMON_APP_MODULE_NAME).service('PaginationDataService',function() {

    var self = this;
    this.data = {};

    this.paginationData = function(pageName, tabName){
        if(self.data[pageName] === undefined) {
            self.data[pageName] = {rowsPerPage: '5', tabs: {},filter:'', sort:'', sortDesc:false, viewType:'list', activeTab:tabName}
        }
        if(tabName == undefined){
            tabName = pageName;
        }

        if(tabName && self.data[pageName].tabs[tabName] == undefined){
             self.data[pageName].tabs[tabName] = {paginationId:pageName+'_'+tabName, pageInfo:{}};
        }
        if(tabName && self.data[pageName].tabs[tabName].currentPage === undefined ){
            self.data[pageName].tabs[tabName].currentPage = 1;
        }
        return self.data[pageName];
    }

    /**
     * Save the Options for choosing the rows per page
     * @param pageName
     * @param rowsPerPageOptions
     */
    this.setRowsPerPageOptions = function(pageName,rowsPerPageOptions){
        self.paginationData(pageName).rowsPerPageOptions = rowsPerPageOptions;
    }

    /**
     * get/save the viewType
     * @param pageName
     * @param viewType
     * @returns {string|Function|*|string|string}
     */
    this.viewType = function(pageName, viewType){
        if(viewType != undefined) {
            self.paginationData(pageName).viewType = viewType;
        }
        return self.paginationData(pageName).viewType;
    }

    /**
     * Toggle the View Type between list and table
     * @param pageName
     */
    this.toggleViewType = function(pageName){
       var viewType = self.paginationData(pageName).viewType;
        if(viewType == 'list') {
            viewType = 'table';
        }
        else {
            viewType = 'list';
        }
        self.viewType(pageName,viewType);
    }

    /**
     * Store the active Tab
     * @param pageName
     * @param tabName
     */
    this.activateTab = function(pageName, tabName){
        var pageData = self.paginationData(pageName,tabName);

        //deactivate the tab
        angular.forEach(pageData.tabs,function(tabData,name){
           tabData.active = false;
            if(name == tabName){
                tabData.active = true;
                pageData.activeTab = name;
            }
        });
    }

    /**
     * get the Active Tab
     * @param pageName
     * @returns {{}}
     */
    this.getActiveTabData = function(pageName) {
        var activeTabData = {};
        var pageData = self.paginationData(pageName);
        angular.forEach(pageData.tabs,function(tabData,name){
            if(tabData.active){
                activeTabData = tabData;
                return false;
            }
        });
        return activeTabData;
    }

    /**
     * get/set the Filter componenent
     * @param pageName
     * @param value
     * @returns {string|Function|*|number}
     */
    this.filter = function(pageName, value){
        if (value != undefined) {
            self.paginationData(pageName).filter = value;
        }
        return self.paginationData(pageName).filter;
    }

    /**
     * get/set the Rows Per Page
     * @param pageName
     * @param value
     * @returns {string|Function|*|number}
     */
    this.rowsPerPage = function(pageName, value){
        if (value != undefined) {
            self.paginationData(pageName).rowsPerPage = value;
        }
        return self.paginationData(pageName).rowsPerPage;
    }

    /**
     * get/set the active Sort
     * @param pageName
     * @param value
     * @returns {*}
     */
    this.sort = function(pageName, value){
        if(value) {
            self.paginationData(pageName).sort = value;
            if(value.indexOf('-') == 0){
                self.paginationData(pageName).sortDesc = true;
            }
            else {
                self.paginationData(pageName).sortDesc = false;
            }
        }
        return  self.paginationData(pageName).sort;
    }

    /**
     * Check if the current sort is descending
     * @param pageName
     * @returns {boolean}
     */
    this.isSortDescending = function(pageName){
        return  self.paginationData(pageName).sortDesc;
    }

    /**
     * get a unique Pagination Id for the Page and Tab
     * @param pageName
     * @param tabName
     * @returns {*|Function|string}
     */
    this.paginationId = function(pageName, tabName){
        if(tabName == undefined){
            tabName = pageName;
        }
        return self.paginationData(pageName,tabName).tabs[tabName].paginationId;
    }

    /**
     * get/set the Current Page Number for a Page and Tab
     * @param pageName
     * @param tabName
     * @param value
     * @returns {Function|*|currentPage|number}
     */
    this.currentPage = function(pageName, tabName,value){
        if(tabName == undefined || tabName == null){
            tabName = pageName;
        }
        if(value) {
            self.paginationData(pageName,tabName).tabs[tabName].currentPage = value;
        }
        return self.paginationData(pageName,tabName).tabs[tabName].currentPage;
    }




});var PivotTableUtil = (function () {
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
                        //camelcase it
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



/*
 * Copyright (c) 2015.
 */

var SortUtils = (function () {
    function SortUtils() {
    }


    SortUtils.sort = function(arr,attr, desc) {

        var retObj = arr.sort(function (a, b) {
            //Sort numeric values
            if (typeof (a[attr]) === "number") {

                return parseInt(a[attr]) > parseInt(b[attr]) ? 1 : -1;
            }
            //sort string values
            else {
                return a[attr] > b[attr] ? 1 : -1;
            }
        });
        //If sort is descending
        if (desc) {
            return retObj.reverse();
        }
        else {
            return retObj;
        }
    }

    return SortUtils;
})();


(function () {

    var directive = function ($window, $compile, BroadcastService) {
        return {

            link: function ($scope, element, attrs) {
                element.addClass('sticky');
                element.prepend('<div class="sticky-anchor"></div>');
                var tabsWrapper = element.find('md-tabs-wrapper');
                tabsWrapper.css('z-index', 49);
                var header = angular.element('.page-header');
                header.css('z-index', 60);

                var side = angular.element('md-sidenav');
                side.css('z-index', 61);
                tabsWrapper.css('position','fixed');
                //tabsWrapper.css('width','100%');
                var width = angular.element('#content').width();
                if(width == 0){
                    width = angular.element($window).width() - side.width();
                }
                tabsWrapper.css('width',width+'px');
                var tabsContentWrapper  = element.find('md-tabs-content-wrapper');
                tabsContentWrapper.css('margin-top','80px');


                var headerHeight = header.height();
                angular.element($window).on("resize.stickytab", function () {
                    resize();
                });
                BroadcastService.subscribe($scope, BroadcastConstants.CONTENT_WINDOW_RESIZED, onContentWindowResized);

                function onContentWindowResized() {
                    resize();
                }

                function resize() {
                    var width = angular.element('#content').width();
                    tabsWrapper.css('width', width + 'px');
                }
/*
                angular.element('#content').bind("scroll", function () {
                    var header = angular.element('.page-header');
                    var headerHeight = header.height();

                    var window_top = 0;
                    var div_top = element.find('.sticky-anchor').offset().top;

                    if (div_top < headerHeight) {
                        if (div_top < 0) {
                            tabsWrapper.css('top', Math.abs(div_top) + headerHeight + 'px')
                        }
                        else {
                            tabsWrapper.css('top', Math.abs(headerHeight - Math.abs(div_top)) + 'px')
                        }
                      //  tabsWrapper.addClass('stick');
                    } else {
                       // tabsWrapper.removeClass('stick');
                        tabsWrapper.css('top', '0px')
                    }
                });
                */
                $scope.$on('$destroy',function() {
                    //tabsWrapper.css('top', '0px')
                    angular.element($window).off("resize.stickytab");
                    //angular.element('#content').unbind("scroll");
                })

            }

            //set margin-top = top

        }
    }




    angular.module(COMMON_APP_MODULE_NAME)
        .directive('stickyTabs', directive);

})();



/**
 * Service to call out to Feed REST.
 *
 */
angular.module(COMMON_APP_MODULE_NAME).service('TableOptionsService', ['PaginationDataService', function (PaginationDataService) {
    this.sortOptions = {};
    var self = this;

  this.newSortOptions = function(key,labelValueMap, defaultValue, defaultDirection) {

        var sortOptions = Object.keys(labelValueMap).map(function(mapKey) {
        var value = labelValueMap[mapKey];
            var sortOption = {label:mapKey, value:value, direction:'', reverse:false, type:'sort'}
            if(defaultValue && value == defaultValue){
                sortOption['default'] =  defaultDirection || 'asc';
                sortOption['direction'] = defaultDirection || 'asc';
                sortOption['reverse'] = sortOption['direction'] == 'asc' ? false : true;
                sortOption['icon'] = sortOption['direction'] == 'asc' ? 'keyboard_arrow_up' : 'keyboard_arrow_down';
            }
            return sortOption;
        });
        self.sortOptions[key] = sortOptions;
      return sortOptions;
    }

    function clearOtherSorts(key,option){
        var sortOptions = self.sortOptions[key];
        if(sortOptions) {
            angular.forEach(sortOptions, function (sortOption, i) {
                if (sortOption !== option) {
                    sortOption.direction = '';
                    sortOption.icon = '';
                }
            });
        }
    }

    function getDefaultSortOption(key) {
        var sortOptions = self.sortOptions[key];
        var defaultSortOption = null;
        if(sortOptions) {
            defaultSortOption = _.find(sortOptions, function (opt) {
                return opt.default
            });
        }
        return defaultSortOption;
    }

    /**
     * Sets the sort option to either the saved value from the PaginationDataService or the default value.
     * @param key
     */
    this.initializeSortOption = function (key) {
        var currentOption = PaginationDataService.sort(key);
        if (currentOption) {
            self.setSortOption(key, currentOption)
        }
        else {
            self.saveSortOption(key, getDefaultSortOption(key))
        }
    }

    this.saveSortOption = function(key,sortOption) {
        if(sortOption) {
            var val = sortOption.value;
            if(sortOption.reverse){
                val = '-'+val;
            }
            PaginationDataService.sort(key,val);
        }
    }


    this.toggleSort = function(key,option) {
        //single column sorting, clear sort if different
        clearOtherSorts(key,option)
        var returnedSortOption = option;
        if(option.direction == undefined || option.direction == '' || option.direction == 'desc') {
            option.direction = 'asc';
            option.icon = 'keyboard_arrow_up'
            option.reverse = false;
        }
        else if(option.direction == 'asc'){
            option.direction = 'desc';
            option.icon = 'keyboard_arrow_down'
            option.reverse = true;
        }
       // self.saveSortOption(key,returnedSortOption)
        return returnedSortOption;
    }
    this.toSortString = function(option) {
        if(option.direction == 'desc'){
            return "-"+option.value;
        }
        else {
            return option.value;
        }
    }

    this.setSortOption = function(key,val){
        var dir = 'asc'
        var icon = 'keyboard_arrow_up';
        var sortColumn = val;
        if(val.indexOf('-') == 0) {
            dir = 'desc';
            icon = 'keyboard_arrow_down';
            sortColumn = val.substring(1);
        }
        var sortOptions = self.sortOptions[key];
        angular.forEach(sortOptions,function(sortOption,i) {
           if(sortOption.value == sortColumn){
               sortOption.direction = dir
               sortOption.icon = icon;
               sortOption.reverse = dir =='desc' ? true : false;
           }
            else {
               sortOption.direction = '';
               sortOption.icon = '';
               sortOption.reverse = false;
           }

        });
    }

    this.getCurrentSort = function(key){

        var sortOptions = self.sortOptions[key];
        var returnedSortOption = null;
        if(sortOptions) {
            angular.forEach(sortOptions,function(sortOption,i) {
                if(sortOption.direction && sortOption.direction != '') {
                    returnedSortOption = sortOption;
                    return false;
                }
            });
            if(returnedSortOption == null) {
                returnedSortOption = getDefaultSortOption(key);
            }
        }
    return returnedSortOption;
}



}]);angular.module("datalakeui.common").run(["$templateCache", function($templateCache) {$templateCache.put("js/shared/card-filter-header/card-filter-header-template.html"," <div style=\"white-space: nowrap;margin-top:10px;\" class=\"card-title\">{{::$cardFilterHeader.cardTitle}}</div>\n        <span flex=\"5\"></span>\n        <md-input-container flex style=\"margin-top:0px;margin-bottom:0px;\">\n            <label>Filter</label>\n            <input ng-model=\"$cardFilterHeader.filterModel\" />\n        </md-input-container>\n <div layout=\"row\" layout-align=\"\"space-between center\">\n           <tba-view-type-selection view-type=\"$cardFilterHeader.viewType\" style=\"margin-left:10px;\"></tba-view-type-selection>\n           <tba-options-menu sort-options=\"$cardFilterHeader.sortOptions\" show-pagination=\"false\" menu-key=\"{{$cardFilterHeader.pageName}}\" selected-option=\"$cardFilterHeader.selectedOption\" show-view-type=\"false\" menu-icon=\"more_vert\"></tba-options-menu>\n</div>\n");
$templateCache.put("js/shared/card-layout/card-layout-template.html","<section class=\"md-whiteframe-z1 {{::cardCss}}\">\n     <div  ng-transclude=\"header1\" class=\"card-toolbar  {{::headerCss}}\" >\n\n    </div>\n    <md-content  ng-transclude=\"body1\" class=\"card-layout-body {{::bodyCss}}\" >\n\n    </md-content>\n\n</section>");
$templateCache.put("js/shared/options-menu/options-menu-template.html","\n<md-menu >\n    <md-button aria-label=\"Options\" class=\"md-icon-button\" style=\"margin-top:-10px\" ng-click=\"openMenu($mdOpenMenu, $event)\">\n        <ng-md-icon icon=\"{{menuIcon}}\"></ng-md-icon>\n    </md-button>\n    <md-menu-content width=\"3\" >\n        <md-menu-item ng-if=\"showPagination\" >\n            <md-input-container>\n                <label>Rows per page</label>\n                <md-select class=\"\" ng-model=\"paginationData.rowsPerPage\" >\n                    <md-option ng-repeat=\"x in paginationData.rowsPerPageOptions\" value=\"{{x}}\">\n                        {{x}}\n                    </md-option>\n                </md-select>\n            </md-input-container >\n        </md-menu-item>\n        <md-menu-item  class=\"md-menu-item-header\" ng-if=\"showViewType == true\">\n            <span md-menu-align-target  class=\"md-subheader md-menu-item-header\">View Options</span>\n        </md-menu-item>\n        <md-menu-item layout=\"column\" ng-if=\"showViewType == true\">\n            <md-button ng-click=\"selectOption(viewType)\" md-prevent-menu-close=\"md-prevent-menu-close\">\n                <span style=\"float:left;\">{{viewType.label}}</span>\n                <span style=\"float:right;\"><ng-md-icon icon=\"{{viewType.icon}}\"></ng-md-icon></span>\n            </md-button>\n        </md-menu-item>\n        <md-menu-item class=\"md-menu-item-header\">\n            <span md-menu-align-target  class=\"md-subheader md-menu-item-header\">Sort Options</span>\n         </md-menu-item>\n        <md-menu-item ng-repeat=\"item in sortOptions\" layout=\"column\">\n            <md-button ng-click=\"selectOption(item)\" md-prevent-menu-close=\"md-prevent-menu-close\">\n                <span style=\"float:left;\">{{item.label}}</span>\n                <span  ng-if=\"item.icon && item.icon != \'\'\" style=\"float:right;\"><ng-md-icon icon=\"{{item.icon}}\"></ng-md-icon></span>\n            </md-button>\n        </md-menu-item>\n    </md-menu-content>\n</md-menu>");
$templateCache.put("js/shared/view-type-selection/view-type-selection-template.html","<span class=\"md-button-group\" layout-align=\"center center\">\n    <md-button class=\"icon-btn\"\n               ng-click=\"viewTypeChanged(\'table\')\"\n               ng-class=\"{\'selected\' : viewType == \'table\'}\">\n        <ng-md-icon md-icon icon=\"list\"></ng-md-icon>\n    </md-button>\n    <md-button class=\"icon-btn\" ng-click=\"viewTypeChanged(\'list\')\"\n                           ng-class=\"{\'selected\' : viewType == \'list\'}\">\n    <ng-md-icon md-icon icon=\"view_list\"></ng-md-icon>\n</md-button>\n</span>");
    $templateCache.put("js/shared/vertical-section-layout/vertical-section-layout-template.html",
        "<div layout=\"column\" class=\"layout-padding-left\" style=\"padding-top:10px;\" >\n    <div >\n        <div layout=\"row\" layout-align=\"start stretch\">\n            <ng-md-icon icon=\"check\" style=\"fill:#009933;margin-left: -9px;\" ng-if=\"showVerticalCheck == true\"></ng-md-icon>\n            <span class=\"md-subhead layout-padding-left\"  >{{sectionTitle}}</span>\n            <span flex></span>\n            <md-button class=\"icon-btn md-icon-button md-primary\" ng-click=\"edit($event)\" ng-if=\"allowEdit ==true && !editable\">\n                <ng-md-icon icon=\"edit\"></ng-md-icon>\n            </md-button>\n            <md-button class=\"icon-btn md-icon-button\" ng-click=\"cancel($event)\" ng-if=\"allowEdit ==true && editable\">\n                <ng-md-icon icon=\"cancel\" style=\"fill:grey;\"></ng-md-icon>\n            </md-button>\n        </div>\n\n        <div class=\"vertical-step-border\">\n            <div ng-if=\"!editable\" class=\"md-padding\">\n            <div ng-transclude=\"readonly\" >\n                REadonly SEction\n\n            </div>\n            </div>\n            <div ng-if=\"editable == true\" class=\"md-padding\">\n            <form name=\"{{formName}}\">\n                <div ng-transclude=\"editable\" >\n                    Editable SEction\n\n                </div>\n                <div layout=\"row\" layout-align=\"end center\">\n                    <md-button ng-click=\"delete($event)\" ng-disabled=\"allowDelete == false\" ng-if=\"onDelete !== undefined && isDeleteVisible == true\" >Delete</md-button>\n                    <span flex></span>\n                    <md-button ng-click=\"cancel($event)\">Cancel</md-button>\n                    <md-button class=\"md-primary md-raised\" ng-click=\"save($event)\"\n                               ng-disabled=\"isValid == false || (theForm != undefined && theForm.$invalid != undefined && theForm.$invalid == true)\">Save\n                    </md-button>\n                </div>\n                </form>\n            </div>\n\n        </div>\n    </div>\n\n</div>");
    $templateCache.put("js/shared/ui-router-breadcrumbs/uiBreadcrumbs.tpl.html",
        "<div ng-if=\"lastBreadcrumbs.length == 2\" layout-align=\"center\" style=\"white-space: nowrap\">\n<md-button class=\"icon-btn\" ng-click=\"navigate( lastBreadcrumbs[0])\">\n    <ng-md-icon md-icon icon=\"keyboard_arrow_left\"></ng-md-icon>\n</md-button>\n  <span>  {{ lastBreadcrumbs[0].displayName }}</span>\n</div>\n<div ng-if=\"lastBreadcrumbs.length == 1\" style=\"white-space: nowrap\">\n{{ lastBreadcrumbs[0].displayName }}\n</div>\n");
$templateCache.put("js/vendor/dirPagination/dirPagination.md-tpl.html","<!--\n  ~ Copyright (c) 2015.\n  -->\n<div layout=\"row\">\n\n    <span ng-if=\"boundaryLinks\" ng-class=\"{ disabled : pagination.current == 1 }\">\n        <a href=\"\" ng-click=\"setCurrent(1)\">&laquo;</a>\n    </span>\n    <span ng-if=\"directionLinks\" ng-class=\"{ disabled : pagination.current == 1 }\">\n        <a href=\"\" ng-click=\"setCurrent(pagination.current - 1)\">&lsaquo;</a>\n    </span>\n    <span ng-repeat=\"pageNumber in pages track by tracker(pageNumber, $index)\" ng-class=\"{ active : pagination.current == pageNumber, disabled : pageNumber == \'...\' }\">\n        <a href=\"\" ng-click=\"setCurrent(pageNumber)\">{{ pageNumber }}</a>\n    </span>\n\n    <span ng-if=\"directionLinks\" ng-class=\"{ disabled : pagination.current == pagination.last }\">\n        <a href=\"\" ng-click=\"setCurrent(pagination.current + 1)\">&rsaquo;</a>\n    </span>\n    <span ng-if=\"boundaryLinks\"  ng-class=\"{ disabled : pagination.current == pagination.last }\">\n        <a href=\"\" ng-click=\"setCurrent(pagination.last)\">&raquo;</a>\n    </span>\n</div>\n");
$templateCache.put("js/vendor/dirPagination/dirPagination.tpl.html","<div>\n    <span class=\"label\">{{label}}</span>\n    <md-select ng-model=\"rowsPerPage\" md-container-class=\"md-pagination-select\" placeholder=\"{{rowsPerPage ? rowsPerPageOptions[0] : 5}}\">\n        <md-option ng-repeat=\"x in rowsPerPageOptions\" value=\"{{x}}\">\n            {{x}}\n        </md-option>\n    </md-select>\n    <span>{{range.lower}} - {{range.upper}} of {{range.total}}</span>\n</div>\n<div>\n        <md-button ng-if=\"boundaryLinks\" type=\"button\" ng-click=\"setCurrent(1)\" ng-disabled=\"pagination.current == 1\" aria-label=\"First\">\n            <md-icon md-svg-icon=\"templates.navigate-first.html\"></md-icon>\n        </md-button>\n        <md-button type=\"button\" ng-click=\"setCurrent(pagination.current - 1)\" ng-disabled=\"pagination.current == 1\" aria-label=\"Previous\">\n            <md-icon md-svg-icon=\"templates.navigate-before.html\"></md-icon>\n        </md-button>\n        <md-button type=\"button\"  ng-click=\"setCurrent(pagination.current + 1)\" ng-disabled=\"pagination.current == pagination.last\" aria-label=\"Next\">\n            <md-icon md-svg-icon=\"templates.navigate-next.html\"></md-icon>\n        </md-button>\n        <md-button ng-if=\"boundaryLinks\" type=\"button\"  ng-click=\"setCurrent(pagination.last)\" ng-disabled=\"pagination.current == pagination.last\"  aria-label=\"Last\">\n            <md-icon md-svg-icon=\"templates.navigate-last.html\"></md-icon>\n        </md-button>\n</div>");}]);(function () {

    var directive = function ($interval) {
        return {
            restrict: "EA",
            scope: {
               startTime:"=",
               refreshTime:"@"
            },
            link: function ($scope, element, attrs) {
                $scope.time = $scope.startTime;
                $scope.$watch('startTime',function(newVal,oldVal){
                    $scope.time = $scope.startTime;
                    format();
                })
                format();
                $scope.seconds =0;
                $scope.minutes =0;
                $scope.hours =0;
                $scope.days = 0;
                $scope.months = 0;
                $scope.years = 0;
                if($scope.refreshTime == undefined){
                    $scope.refreshTime = 1000;
                }
                function update() {
                    $scope.time +=$scope.refreshTime;
                    //format it
                    format();

                }

                function formatDaysHoursMinutesSeconds(ms){
                    days = Math.floor(ms / (24*60*60*1000));
                    daysms=ms % (24*60*60*1000);
                    hours = Math.floor((daysms)/(60*60*1000));
                    hoursms=ms % (60*60*1000);
                    minutes = Math.floor((hoursms)/(60*1000));
                    minutesms=ms % (60*1000);
                    sec = Math.floor((minutesms)/(1000));


                }

                function format(){
                    $scope.seconds = Math.floor(($scope.time / 1000) % 60);
                    $scope.minutes = Math.floor((($scope.time / (60000)) % 60));
                    $scope.hours = Math.floor($scope.time / 3600000);
                    $scope.days = 0;
                    $scope.months = 0;
                    $scope.years = 0;
                    if($scope.hours <0){
                        $scope.hours = 0;
                    }
                    if($scope.minutes <0){
                        $scope.minutes = 0;
                    }
                    if($scope.seconds <0){
                        $scope.seconds = 0;
                    }
                    var str = $scope.seconds+' sec';
                    if($scope.hours >0 || ($scope.hours ==0 && $scope.minutes >0)) {
                      str = $scope.minutes+' min '+str;
                    }
                    if($scope.hours >0){
                        str =  $scope.hours+' hrs '+str;
                    }

                    element.html(str);
                    element.attr('title',str)
                }

                var interval = $interval(update, $scope.refreshTime);

                var clearInterval = function() {
                    $interval.cancel(interval);
                   interval = null;
                }
                $scope.$on('$destroy', function() {
                    clearInterval()
                });
            }

        }
    };


    angular.module(COMMON_APP_MODULE_NAME)
        .directive('tbaTimerOld',['$interval', directive]);

}());
(function () {

    var directive = function ($interval) {
        return {
            restrict: "EA",
            scope: {
               startTime:"=",
               refreshTime:"@"
            },
            link: function ($scope, element, attrs) {
                $scope.time = $scope.startTime;
                $scope.$watch('startTime',function(newVal,oldVal){
                    $scope.time = $scope.startTime;
                    format();
                })
                format();
                var seconds =0;
                var minutes =0;
                var hours =0;
                var days = 0;
                var months = 0;
                var years = 0;
                if($scope.refreshTime == undefined){
                    $scope.refreshTime = 1000;
                }
                function update() {
                    $scope.time +=$scope.refreshTime;
                    //format it
                    format();

                }

                function format(){
                    var ms = $scope.time;
                    days = Math.floor(ms / (24*60*60*1000));
                    var daysms=ms % (24*60*60*1000);
                    hours = Math.floor((daysms)/(60*60*1000));
                    var hoursms=ms % (60*60*1000);
                    minutes = Math.floor((hoursms)/(60*1000));
                    var minutesms=ms % (60*1000);
                    seconds = Math.floor((minutesms)/(1000));

                    var str = seconds+' sec';
                    if(hours >0 || (hours ==0 && minutes >0)) {
                        str = minutes+' min '+str;
                    }
                    if(days >0 || days == 0 && hours >0){
                        str =  hours+' hrs '+str;
                    }
                    if(days >0) {
                        str = days+" days "+ str;
                    }

                    element.html(str);
                    element.attr('title',str)

                }

                function formatMaxHours(){
                    $scope.seconds = Math.floor(($scope.time / 1000) % 60);
                    $scope.minutes = Math.floor((($scope.time / (60000)) % 60));
                    $scope.hours = Math.floor($scope.time / 3600000);
                    $scope.days = 0;
                    $scope.months = 0;
                    $scope.years = 0;
                    if($scope.hours <0){
                        $scope.hours = 0;
                    }
                    if($scope.minutes <0){
                        $scope.minutes = 0;
                    }
                    if($scope.seconds <0){
                        $scope.seconds = 0;
                    }
                    var str = $scope.seconds+' sec';
                    if($scope.hours >0 || ($scope.hours ==0 && $scope.minutes >0)) {
                      str = $scope.minutes+' min '+str;
                    }
                    if($scope.hours >0){
                        str =  $scope.hours+' hrs '+str;
                    }

                    element.html(str);
                    element.attr('title',str)
                }

                var interval = $interval(update, $scope.refreshTime);

                var clearInterval = function() {
                    $interval.cancel(interval);
                   interval = null;
                }
                $scope.$on('$destroy', function() {
                    clearInterval()
                });
            }

        }
    };


    angular.module(COMMON_APP_MODULE_NAME)
        .directive('tbaTimer',['$interval', directive]);

}());
(function() {

    /**
     * Config
     */
    var moduleName = COMMON_APP_MODULE_NAME;
    var templateUrl = 'js/shared/ui-router/breadcrumbs/uiBreadcrumbs.tpl.html';

    /**
     * Module
     */
    var module;
    try {
        module = angular.module(moduleName);
    } catch(err) {
        // named module does not exist, so create one
        module = angular.module(moduleName, ['ui.router']);
    }

    module.directive('uiRouterBreadcrumbs', ['$interpolate', '$state', function($interpolate, $state) {
        return {
            restrict: 'E',
            templateUrl: function(elem, attrs) {
                return attrs.templateUrl || templateUrl;
            },
            scope: {
                displaynameProperty: '@',
                abstractProxyProperty: '@?'
            },
            link: function($scope) {
                $scope.breadcrumbs = [];
                $scope.lastBreadcrumbs = [];
               /* if ($state.$current.name !== '') {
                    updateBreadcrumbsArray();
                }
                */
                $scope.$on('$stateChangeSuccess',function(event, toState, toParams, fromState, fromParams) {
                    //if the to State exists in the breadcrumb trail, reverse back to that, otherwise add it
                    if(toState.data.noBreadcrumb && toState.data.noBreadcrumb == true ){
                     //console.log('Skipping breadcrumb for ',toState)
                    }else {
                        updateBreadcrumbs(toState, toParams);
                    }
                //    console.log('crumbs ',$scope.breadcrumbs)
                });


                function updateLastBreadcrumbs(){
                        $scope.lastBreadcrumbs = $scope.breadcrumbs.slice(Math.max($scope.breadcrumbs.length - 2,0));
                    }

                function getBreadcrumbKey(state){
                    return state.name;
                }

                function getDisplayName(state) {
                    return state.data.displayName || state.name;
                }

                function isBreadcrumbRoot(state){
                    return state.data.breadcrumbRoot && state.data.breadcrumbRoot == true;
                }

                function addBreadcrumb(state, params){
                    var breadcrumbKey = getBreadcrumbKey(state);
                    var copyParams = {}
                    if(params ) {
                        angular.extend(copyParams, params);
                    }
                    var displayName = getDisplayName(state);
                    $scope.breadcrumbs.push({
                        key:breadcrumbKey,
                        displayName: displayName,
                        route: state.name,
                        params:copyParams
                    });

                    updateLastBreadcrumbs();
                }
                $scope.navigate = function(crumb) {
                    $state.go(crumb.route,crumb.params);
                }

                function getBreadcrumbIndex(state){
                    var breadcrumbKey = getBreadcrumbKey(state);
                    var matchingState = _.find($scope.breadcrumbs,function(breadcrumb){
                        return breadcrumb.key == breadcrumbKey;
                    });
                    if(matchingState){
                        return _.indexOf($scope.breadcrumbs,matchingState)
                    }
                    return -1;
                }
                function updateBreadcrumbs(state,params){
                    var index = getBreadcrumbIndex(state);
                    if(isBreadcrumbRoot(state)){
                        index = 0;
                    }
                    if(index == -1){
                        addBreadcrumb(state,params);
                    }
                    else {
                        //back track until we get to this index and then replace it with the incoming one
                        $scope.breadcrumbs =  $scope.breadcrumbs.slice(0,index);
                        addBreadcrumb(state,params);
                    }
                }
                /**
                 * Resolve the displayName of the specified state. Take the property specified by the `displayname-property`
                 * attribute and look up the corresponding property on the state's config object. The specified string can be interpolated against any resolved
                 * properties on the state config object, by using the usual {{ }} syntax.
                 * @param currentState
                 * @returns {*}
                 */
                function getDisplayName1(currentState) {
                    var interpolationContext;
                    var propertyReference;
                    var displayName;

                    if (!$scope.displaynameProperty) {
                        // if the displayname-property attribute was not specified, default to the state's name
                        return currentState.name;
                    }
                    propertyReference = getObjectValue($scope.displaynameProperty, currentState);

                    if (propertyReference === false) {
                        return false;
                    } else if (typeof propertyReference === 'undefined') {
                        return currentState.name;
                    } else {
                        // use the $interpolate service to handle any bindings in the propertyReference string.
                        interpolationContext =  (typeof currentState.locals !== 'undefined') ? currentState.locals.globals : currentState;
                        displayName = $interpolate(propertyReference)(interpolationContext);
                        return displayName;
                    }
                }

                /**
                 * Given a string of the type 'object.property.property', traverse the given context (eg the current $state object) and return the
                 * value found at that path.
                 *
                 * @param objectPath
                 * @param context
                 * @returns {*}
                 */
                function getObjectValue(objectPath, context) {
                    var i;
                    var propertyArray = objectPath.split('.');
                    var propertyReference = context;

                    for (i = 0; i < propertyArray.length; i ++) {
                        if (angular.isDefined(propertyReference[propertyArray[i]])) {
                            propertyReference = propertyReference[propertyArray[i]];
                        } else {
                            // if the specified property was not found, default to the state's name
                            return undefined;
                        }
                    }
                    return propertyReference;
                }

            }
        };
    }]);
})();
var UIGridUtil = (function () {
    function UIGridUtil() {
    }

    function UIGridUtilTag() {
    }

    this.__tag = new UIGridUtilTag();

    UIGridUtil.newStatus = function () {
        return {
            loadingGrid: false, lastRefreshed: '',
            refreshComplete: function () {
                this.lastRefreshed = new Date();
                this.loadingGrid = false;
                this.isRefreshingGrid = false;
            },
            loadGrid: function () {
                this.refresh();
            },
            refresh: function () {
                this.loadingGrid = true;
                this.isRefreshingGrid = true;
            }
        };
    }

    UIGridUtil.isRefreshing = function (gridStatus) {
        if (gridStatus.isRefreshingGrid != undefined) {
            return gridStatus.isRefreshingGrid;
        }
        return false;
    }
    UIGridUtil.gridRefreshStart = function (gridStatus) {
        if (!UIGridUtil.isRefreshing(gridStatus)) {
            gridStatus.isRefreshingGrid = true;
            return true;
        }
        return false;

    };
    UIGridUtil.gridRefreshStopped = function (gridStatus) {
        gridStatus.isRefreshingGrid = false;
        gridStatus.loadingGrid = false;
    }

    UIGridUtil.gridRefreshComplete = function (gridStatus) {
        gridStatus.isRefreshingGrid = false;
        gridStatus.lastRefreshed = new Date();
        gridStatus.loadingGrid = false;
    }
    return UIGridUtil;
})();


angular.module(COMMON_APP_MODULE_NAME).factory('Utils', function ($timeout) {

    var waitForDomRetryCounts = {};
    var data = {

        stickTabHeader : function($element) {
            var self = this;
            var tabsWrapper = element.find('md-tabs-wrapper');
            self.stickTabHeader($element,tabsWrapper);

        },
        stickTabHeader : function($element, $tabsWrapper) {
            var header = angular.element('.page-header');
            var headerHeight = header.height();

            var window_top = 0;
            var div_top = $element.find('.sticky-anchor').offset().top;
            if (div_top < headerHeight) {
                if (div_top < 0) {
                    $tabsWrapper.css('top', Math.abs(div_top) + headerHeight + 'px')
                }
                else {
                    $tabsWrapper.css('top', Math.abs(headerHeight - Math.abs(div_top)) + 'px')
                }
            } else {
                $tabsWrapper.css('top', '0px')
            }
        },
        maskProfanity:function(str){
         return str;
        },

    /**
     *
     * @param selector  element selector (i.e. #TableId)
     * @param callbackFn  // the function to execute when the selector element is found in the DOM
     */
    waitForDomElementReady : function (selector, callbackFn) {
        if (waitForDomRetryCounts[selector] == undefined) {
            waitForDomRetryCounts[selector] = 0;
        }
        var $ele = angular.element(selector);
        if ($ele.length > 0) {
            delete waitForDomRetryCounts[selector];
            callbackFn();
        }
        else {

            waitForDomRetryCounts[selector] += 1;
            if (waitForDomRetryCounts[selector] <= 50) {
              $timeout(data.waitForDomElementReady, 5, false, selector, callbackFn);
            }
        }
    },

        strEndsWith: function(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    },
    endsWith : function(str, suffix) {
        return this.strEndsWith(str,suffix);
    },
    startsWith : function(str, prefix) {
        return str.indexOf(prefix) === 0;
    },
    camelCaseToWords : function (str) {
        return str.match(/^[a-z]+|[A-Z][a-z]*/g).map(function (x) {
            return x[0].toUpperCase() + x.substr(1).toLowerCase();
        }).join(' ');
    },

        capitalizeFirstLetter : function(string) {
        if(string && string != '') {
            return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
        }
        return '';
    },

    convertDate : function (date) {
        if (date == null) {
            return "--";
        }
        var local = new Date(date);
        local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
        return local.toISOString().slice(0, 10) + " " + local.toISOString().slice(11, 19);
    },
    dateDifference : function (startDate, endDate) {
        if (startDate == null || endDate == null) {
            return "N/A";
        }
        else {
            var msec = endDate - startDate;
            return this.formatTimeMinSec(msec);
        }
    },

    dateDifferenceMs : function (startDate, endDate) {
        var diff = endDate - startDate;
        return diff;
    },
    formatTimeMinSec : function (timeInMSec) {
        //       return self.TimeDiffString(timeInMSec);
        if (timeInMSec == null) {
            return "N/A";
        }
        else {
            if(timeInMSec <0){
                return "0 sec";
            }
            var sec_num = timeInMSec/1000;
            sec_num = parseInt(sec_num,10);
            var hours   = Math.floor(sec_num / 3600);
            var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
            var seconds = sec_num - (hours * 3600) - (minutes * 60);

            var str = seconds + " sec";
            if(hours ==0){
                if(minutes != 0){
                    str = minutes+ " min "+str;
                }
            }
            else {
                str = hours+" hrs "+ minutes+" min "+ str;
            }
            return str;
        }

    },

    TimeDiffString : function (timeInMs) {
        var diffString = moment.duration(timeInMs).format('HH:mm:ss');
        if (diffString == '00') {
            diffString = '0';
        }
        var parts = diffString.split(':');
        var suffixes = ['hr', 'min', 'sec'];
        var len = parts.length;
        var suffixIndex = Math.abs(len - 3);

        var startIndex = parts.length;
        var timeString = '';
        for (var i = 0; i < parts.length; i++) {
            if (i > 0) {
                timeString += ' ';
            }
            timeString += parts[i] + ' ' + suffixes[suffixIndex];
            suffixIndex++;
        }
        return timeString;
    },
    DateDiffString : function (date1, date2) {
        var diff = date2.getTime() - date2.getTime();
        return this.TimeDiffString(diff);
    },


    getParameterByName : function (name) {
        var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
        return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
    },
    toBoolean : function (str) {
        if (typeof str == "boolean") {
            return str;
        }
        else if (typeof str == "string") {
            return str.toLowerCase() == "true";
        }
        else if (typeof str == "number") {
            return str == 1;
        }
        return false;
    },




    resolveStatusClass : function (status,exitCode) {

        var statusClass = 'status-info';
        switch (status) {
            case "STARTED":
            case "RUNNING":
            case "COMPLETED":
                if(exitCode && exitCode == 'FAILED'){
                    statusClass = 'status-errors';
                }
                else {
                    statusClass = 'status-healthy';
                }
                break;
            case "FAILED":
                statusClass = 'status-errors';
                break;
            default:
        }
        return statusClass;
    }

    };
    return data;

});
angular.module(COMMON_APP_MODULE_NAME).directive("verticalSectionLayout", function()  {
    return {
        restrict: 'E',
        scope: {
            showVerticalCheck: '=?',
            allowEdit: '=?',
            sectionTitle: '@',
            formName: '@',
            onDelete: '&?',
            isDeleteVisible: '=?',
            allowDelete: '=?',
            onEdit: '&',
            onSaveEdit: '&',
            onCancelEdit: '&',
            editable: '=?',
            keepEditableAfterSave: '=?',
            isValid: '=?',
            theForm: '=?'
        },
        transclude: {
            'readonly':'?readonlySection',
            'editable':'?editableSection'
        },
        templateUrl:'js/shared/vertical-section-layout/vertical-section-layout-template.html',
        link: function ($scope, iElem, iAttrs, ctrl, transcludeFn) {
            /**
             * Delete button is visible if this flag is true and if the method onDelete is set
             */
            if ($scope.isDeleteVisible == undefined) {
                $scope.isDeleteVisible = true;
            }

             if($scope.editable == undefined ) {
                 $scope.editable = false;
            }

            if($scope.showVerticalCheck == undefined ){
                $scope.showVerticalCheck = true;
            }

            if($scope.allowEdit == undefined ){
                $scope.allowEdit = true;
            }
            if ($scope.isValid == undefined) {
                $scope.isValid = true;
            }

            if($scope.keepEditableAfterSave == undefined){
                $scope.keepEditableAfterSave = false;
            }

            $scope.edit = function(ev){
                $scope.editable = true;
                $scope.onEdit(ev);
            }

            $scope.cancel = function(ev){
                $scope.onCancelEdit(ev);
                $scope.editable = false;
            }

            $scope.save = function(ev){
                $scope.onSaveEdit(ev);
                if(!$scope.keepEditableAfterSave) {
                    $scope.editable = false;
                }
            }

            $scope.delete = function(ev) {
                if($scope.onDelete){
                    $scope.onDelete(ev);
                }
            }




        }
    };
});
angular.module(COMMON_APP_MODULE_NAME).directive('tbaViewTypeSelection', function() {
    return {
        restrict: 'E',
        templateUrl: 'js/shared/view-type-selection/view-type-selection-template.html',
        scope: {
        viewType:'='
        },
        link: function($scope, elem, attr) {

            $scope.viewTypeChanged = function(viewType)
            {
                $scope.viewType = viewType;
             /*   if($scope.onViewTypeChanged){
                    $scope.onViewTypeChanged()(viewType);
                }
                */
            }

        }
    }
});