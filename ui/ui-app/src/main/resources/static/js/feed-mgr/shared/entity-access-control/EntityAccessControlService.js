
define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('EntityAccessControlDialogService',["$mdDialog", function ($mdDialog) {

        var data = {
            showAccessControlDialog: function (entity,entityTitle, onSave, onCancel) {

                var callbackEvents = {onSave:onSave,onCancel:onCancel}
               return $mdDialog.show({
                    controller: 'EntityAccessControlDialogController',
                    templateUrl: 'js/feed-mgr/shared/entity-access-control/entity-access-control-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {entity:entity,entityTitle:entityTitle,callbackEvents:callbackEvents}
                }).then(function (msg) {
                    //respond to action in dialog if necessary... currently dont need to do anything
                }, function () {

                });
            }
        };
        return data;
    }]);



    var controller = function ($scope,$mdDialog,entity,entityTitle,callbackEvents){

        $scope.entityTitle = entityTitle;

        /**
         * The Angular form for validation
         * @type {{}}
         */
        $scope.theForm = {}

        /**
         * The entity to provide Access Control
         */
        $scope.entity = entity;

        /**
         * The access control model
         * @type {{roles: Array, owner: null}}
         */
        $scope.model = {roles:[],owner:null};

        $scope.onSave = function($event){
            var m = $scope.model;

            if(angular.isFunction(callbackEvents.onSave)) {
                callbackEvents.onSave();
            }
            $mdDialog.hide();
        }

        $scope.onCancel = function($event){
            var m = $scope.model;
            $mdDialog.hide();
            if(angular.isFunction(callbackEvents.onCancel)) {
                callbackEvents.onCancel();
            }
        }


    }


    angular.module(moduleName).controller('EntityAccessControlDialogController', ["$scope",'$mdDialog',"entity","entityTitle","callbackEvents",controller]);

});