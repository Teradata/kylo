import {UnderscoreStatic} from "underscore";

declare const _: UnderscoreStatic;
declare const angular: angular.IAngularStatic;

const moduleName: string = require("feed-mgr/visual-query/module-name");

/**
 * Controls the connection dialog for creating a join between two nodes in the Build Query flow chart.
 */
export class ConnectionDialog {

    /**
     * Constructs a {@code ConnectionDialog}.
     */
    constructor($scope: any, $mdDialog: angular.material.IDialogService, isNew: any, connectionDataModel: any, source: any, dest: any) {

        $scope.isValid = false;
        $scope.connectionDataModel = angular.copy(connectionDataModel);
        $scope.source = angular.copy(source);
        $scope.dest = angular.copy(dest);
        $scope.joinTypes = [{name: "Inner Join", value: "INNER JOIN"}, {name: "Left Join", value: "LEFT JOIN"}, {name: "Right Join", value: "RIGHT JOIN"}];
        $scope.isNew = isNew;

        if (isNew) {
            //attempt to auto find matches
            let sourceNames: any = [];
            let destNames: any = [];
            angular.forEach(source.data.nodeAttributes.attributes, function (attr: any) {
                sourceNames.push(attr.name);
            });

            angular.forEach(dest.data.nodeAttributes.attributes, function (attr: any) {
                destNames.push(attr.name);
            });

            let matches = _.intersection(sourceNames, destNames);
            if (matches && matches.length && matches.length > 0) {
                let col = matches[0];
                if (matches.length > 1) {
                    if (matches[0] == 'id') {
                        col = matches[1];
                    }
                }
                $scope.connectionDataModel.joinKeys.sourceKey = col;
                $scope.connectionDataModel.joinKeys.destKey = col;
                $scope.connectionDataModel.joinType = "INNER JOIN"
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
}

angular.module(moduleName).controller("ConnectionDialog", ["$scope", "$mdDialog", "isNew", "connectionDataModel", "source", "dest", ConnectionDialog]);
