define(["angular", "admin/module-name"], function (angular, moduleName) {

    var JcrQueryController = function ($scope, $http, $mdDialog, $mdToast,AccessControlService) {
        var self = this;

        var init = function() {
            self.sql = 'SELECT fs.[jcr:title], fd.[tba:state], c.[tba:systemName] \n'
                       + 'FROM [tba:feed] as e \n'
                       + 'JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) \n'
                       + 'JOIN [tba:feedData] AS fd ON ISCHILDNODE(fd, e) \n'
                       +'JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd) \n'
                       + 'JOIN [tba:category] as c on ISCHILDNODE(cd,c)';

            getIndexes();

        };
        this.loading = false;
        self.errorMessage = null;
        this.queryTime = null;


            this.codemirrorOptions = {
            lineWrapping: true,
            indentWithTabs: true,
            smartIndent: true,
            lineNumbers: true,
            matchBrackets: true,
            autofocus: true,
            mode: 'text/x-hive'
        };

        this.gridOptions = {
            columnDefs: [],
            data: null,
            enableColumnResizing: true,
            enableGridMenu: true,
            flatEntityAccess: true
        };

        self.resultSize = 0;

        self.indexes = [];


        self.index = {
            indexName:'',
            nodeType:'',
            propertyName:'',
            propertyType:''
        };

        self.indexTable = {
            currentPage:1,
            rowsPerPage:5
        }

        self.previousQueries = [];
        self.previousQuery = '';

        self.explainPlan = null;


        this.executeQuery = function(){
            query();
        }

        this.propertyTypes = [{name:"String",value:1},
        {name:"Binary",value:2},
        {name:"Long",value:3},
        {name:"Double",value:4},
        {name:"Date",value:5},
        {name:"Boolean",value:6},
        {name:"Name",value:7},
        {name:"Path",value:8},
        {name:"Reference",value:9},
        {name:"Weak Referebce",value:10},
        {name:"URI",value:11},
        {name:"Decimal",value:12}]

        this.indexKinds = ["VALUE","ENUMERATED_VALUE","UNIQUE_VALUE","TEXT","NODE_TYPE"]

        this.registerIndex = function(){
            showDialog("Adding Index", "Adding index. Please wait...");
            var successFn = function (response) {
                if (response.data) {
                    hideDialog();
                    getIndexes();
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Added the index')
                            .hideDelay(3000)
                    );
                }
            }
            var errorFn = function (err) {
                hideDialog();
            }

            var indexCopy = angular.extend({},self.index);
            var promise = $http({
                url: "/proxy/v1/metadata/debug/jcr-index/register",
                method: "POST",
                data: angular.toJson(indexCopy),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);
        }

        this.unregisterIndex = function(indexName){

            if(angular.isDefined(indexName)) {
                var successFn = function (response) {
                    if (response.data) {
                        getIndexes();
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('Removed the index '+indexName)
                                .hideDelay(3000)
                        );
                    }
                }
                var errorFn = function (err) {

                }

                var promise = $http({
                    url: "/proxy/v1/metadata/debug/jcr-index/" + indexName + "/unregister",
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
            }
        }

        this.changePreviousQuery = function(){
            self.sql = self.previousQuery;
        }

        function showDialog(title,message){
            $mdDialog.show(
                $mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(false)
                    .title(title)
                    .textContent(message)
                    .ariaLabel(title)
            );
        }

        function hideDialog(){
            $mdDialog.hide();
        }

        this.reindex = function(){
                showDialog("Reindexing", "Reindexing. Please wait...");



                var successFn = function (response) {
                    hideDialog();
                    if (response.data) {
                        $mdToast.show(
                            $mdToast.simple()
                                .textContent('Successfully reindexed')
                                .hideDelay(3000)
                        );
                    }
                }
                var errorFn = function (err) {
                    hideDialog();
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Error reindexing ')
                            .hideDelay(3000)
                    );
                }

                var promise = $http({
                    url: "/proxy/v1/metadata/debug/jcr-index/reindex",
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
        }

        function query() {
            self.loading = true;
            self.errorMessage = null;
            self.explainPlan = null;
            var sql = self.sql;
            var successFn = function(response) {
                if(_.indexOf(self.previousQueries,sql) == -1) {
                    self.previousQueries.push(sql);
                }
                self.loading = false;
                transformResults(response.data);
            };
            var errorFn = function(err) {
                self.resultSize = 0;
                self.loading = false;
                if(err && err.data && err.data.developerMessage){
                    self.errorMessage = err.data.developerMessage;
                }
                else {
                    self.errorMessage = 'Error performing query ';
                }

            };
            var promise = $http.get('/proxy/v1/metadata/debug/jcr-sql',{params:{query:self.sql}});
            promise.then(successFn, errorFn);
            return promise;
        }

        function getIndexes(){
            var successFn = function(response) {
                self.indexes = response.data;
            };
            var errorFn = function(err) {
                self.indexes = [];
                self.indexesErrorMessage = 'Error getting indexes '+err
            };
            var promise = $http.get('/proxy/v1/metadata/debug/jcr-index');
            promise.then(successFn, errorFn);
            return promise;
        }


        function transformResults(result) {

            var data = {};
            var rows = [];
            var columns = [];
           self.queryTime = result.queryTime;
           self.explainPlan = result.explainPlan;


            angular.forEach(result.columns,function(col,i){
                        columns.push({
                            displayName: col.name,
                            headerTooltip: col.name,
                            minWidth: 150,
                            name: 'col_'+i
                        });
            });

            angular.forEach(result.rows,function(row,i){

              var rowObj = {}
               _.each(row.columnValues,function(col,i){
                   rowObj['col_'+i] = col.value;
               });
              rows.push(rowObj);
            });

            data.columns = columns;
            data.rows = rows;
            self.gridOptions.columnDefs = columns;
            self.gridOptions.data = rows;
            self.resultSize = data.rows.length;
            return data;
        };


        init();


    };

    angular.module(moduleName).controller("JcrQueryController", ["$scope", "$http","$mdDialog", "$mdToast","AccessControlService",JcrQueryController]);
});
