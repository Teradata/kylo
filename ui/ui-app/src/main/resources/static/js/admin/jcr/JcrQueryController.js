define(["angular", "admin/module-name"], function (angular, moduleName) {

    /**
     * Identifier for this page.
     * @type {string}
     */
    var PAGE_NAME = "adminJcr";

    /**
     * Displays a list of data sources.
     *
     * @constructor
     * @param $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param AddButtonService the Add button service
     * @param DatasourcesService the data sources service
     * @param PaginationDataService the table pagination service
     * @param StateService the page state service
     * @param TableOptionsService the table options service
     */
    var JcrQueryController = function ($scope, $http, AccessControlService) {
        var self = this;

        var init = function() {
            self.sql = 'SELECT fs.[jcr:title], fd.[tba:state], c.[tba:systemName] '
                       + 'FROM [tba:feed] as e '
                       + 'JOIN [tba:feedSummary] AS fs ON ISCHILDNODE(fs, e) '
                       + 'JOIN [tba:feedData] AS fd ON ISCHILDNODE(fd, e) '
                       +'JOIN [tba:categoryDetails] AS cd ON ISCHILDNODE(e, cd)'
                       + 'JOIN [tba:category] as c on ISCHILDNODE(cd,c)';
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

        self.results = [];
        this.executeQuery = function(){
            query();
        }


        function query(schema, table) {
            self.loading = true;
            self.errorMessage = null;
            var successFn = function(response) {
                self.loading = false;
                self.results = transformResults(response.data);
            };
            var errorFn = function(err) {
                self.loading = false;
                self.results = [];
                slef.errorMessage = 'Error performing query '+err
            };
            var promise = $http.get('/proxy/v1/metadata/debug/jcr-sql',{params:{query:self.sql}});
            promise.then(successFn, errorFn);
            return promise;
        }


        function transformResults(result) {
            var data = {};
            var rows = [];
            var columns = [];
           self.queryTime = result.queryTime;


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
        };


        init();


    };

    angular.module(moduleName).controller("JcrQueryController", ["$scope", "$http","AccessControlService",JcrQueryController]);
});
