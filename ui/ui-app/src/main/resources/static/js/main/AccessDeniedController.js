define(['angular'], function (angular) {
    /**
     * Displays the home page.
     *
     * @constructor
     * @param {Object} $scope the application model
     * @param $mdDialog the dialog service
     * @param {AccessControlService} AccessControlService the access control service
     * @param StateService the state service
     */
    function AccessDeniedController($scope, $mdDialog, AccessControlService, StateService, $transition$) {
        var self = this;


        self.attemptedState = $transition$.params().attemptedState;
        if( self.attemptedState == undefined){
            self.attemptedState = {displayName:'the page'};
        }
        else if( self.attemptedState.displayName == undefined){
            self.attemptedState.displayName = self.attemptedState.name;
        }




    }

    angular.module('kylo').controller('AccessDeniedController', ["$scope", "$mdDialog", "AccessControlService", "StateService", "$transition$",AccessDeniedController]);

});
