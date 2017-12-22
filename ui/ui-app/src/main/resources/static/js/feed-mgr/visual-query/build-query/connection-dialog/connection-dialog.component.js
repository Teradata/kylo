define(["require", "exports"], function (require, exports) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require("feed-mgr/visual-query/module-name");
    /**
     * Controls the connection dialog for creating a join between two nodes in the Build Query flow chart.
     */
    var ConnectionDialog = /** @class */ (function () {
        /**
         * Constructs a {@code ConnectionDialog}.
         */
        function ConnectionDialog($scope, $mdDialog, isNew, connectionDataModel, source, dest) {
            $scope.isValid = false;
            $scope.connectionDataModel = angular.copy(connectionDataModel);
            $scope.source = angular.copy(source);
            $scope.dest = angular.copy(dest);
            $scope.joinTypes = [{ name: "Inner Join", value: "INNER JOIN" }, { name: "Left Join", value: "LEFT JOIN" }, { name: "Right Join", value: "RIGHT JOIN" }];
            $scope.isNew = isNew;
            if (isNew) {
                //attempt to auto find matches
                var sourceNames_1 = [];
                var destNames_1 = [];
                angular.forEach(source.data.nodeAttributes.attributes, function (attr) {
                    sourceNames_1.push(attr.name);
                });
                angular.forEach(dest.data.nodeAttributes.attributes, function (attr) {
                    destNames_1.push(attr.name);
                });
                var matches = _.intersection(sourceNames_1, destNames_1);
                if (matches && matches.length && matches.length > 0) {
                    var col = matches[0];
                    if (matches.length > 1) {
                        if (matches[0] == 'id') {
                            col = matches[1];
                        }
                    }
                    $scope.connectionDataModel.joinKeys.sourceKey = col;
                    $scope.connectionDataModel.joinKeys.destKey = col;
                    $scope.connectionDataModel.joinType = "INNER JOIN";
                }
            }
            $scope.onJoinTypeChange = function () {
                //    .log('joinType changed')
            };
            $scope.hide = function () {
                $mdDialog.hide();
            };
            $scope.validate = function () {
                $scope.isValid =
                    $scope.connectionDataModel.joinType != '' && $scope.connectionDataModel.joinType != null && $scope.connectionDataModel.joinKeys.sourceKey != null
                        && $scope.connectionDataModel.joinKeys.destKey != null;
            };
            $scope.save = function () {
                connectionDataModel.name = $scope.connectionDataModel.name;
                connectionDataModel.joinType = $scope.connectionDataModel.joinType;
                connectionDataModel.joinKeys = $scope.connectionDataModel.joinKeys;
                $mdDialog.hide('save');
            };
            $scope.cancel = function () {
                $mdDialog.hide('cancel');
            };
            $scope.delete = function () {
                $mdDialog.hide('delete');
            };
            $scope.validate();
        }
        return ConnectionDialog;
    }());
    exports.ConnectionDialog = ConnectionDialog;
    angular.module(moduleName).controller("ConnectionDialog", ["$scope", "$mdDialog", "isNew", "connectionDataModel", "source", "dest", ConnectionDialog]);
});
//# sourceMappingURL=connection-dialog.component.js.map