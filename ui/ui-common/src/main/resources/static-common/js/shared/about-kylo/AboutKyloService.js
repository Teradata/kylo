/*
 * Service to display 'about kylo' popup page, and get kylo version
 */
angular.module(COMMON_APP_MODULE_NAME).service('AboutKyloService', function ($mdDialog) {
    var self = this;

    self.showAboutDialog = function() {
        $mdDialog.show({
            controller: 'AboutKyloDialogController',
            templateUrl: 'js/shared/about-kylo/about.html',
            parent: angular.element(document.body),
            clickOutsideToClose: false,
            escapeToClose:true,
            fullscreen: false,
            locals : {

            }
        }).then(function(msg) {
            //callback (success)
        }, function() {
            //callback (failure)
        });
    }
});

(function () {
    var controller = function ($scope, $mdDialog, $http){

        var self = this;

        $http({
            method: "GET",
            url : "/proxy/v1/about/version"
        }).then(function callSuccess(response) {
            $scope.version = response.data;
        }, function callFailure(response) {
            $scope.version = "Not Available"
        });

        $scope.hide = function() {
            $mdDialog.hide();
        };

        $scope.cancel = function() {
            $mdDialog.cancel();
        };


    };
    angular.module(COMMON_APP_MODULE_NAME).controller('AboutKyloDialogController',controller);

}());