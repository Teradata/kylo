/**
 *
 */
(function () {

    var controller = function($scope,$location, AuthenticationService){
        this.username;
        this.password;
        this.error = '';
        var self = this;

        this.init = function() {
            // reset login status
            this.error = '';
            var errorParam = $.urlParam('error');
           if(errorParam == true || errorParam == "true") {
               this.error = "Invalid username or password."
           }

            $('#username').focus();
        }
        this.init();

        this.login = function() {
            this.dataLoading = true;


            AuthenticationService.Login(this.username, this.password, function (response) {
               if (response.success) {
                    $location.path('/');
                } else {
                    self.error = "Invalid login "+response.message;
                    self.dataLoading = false;
                }
            });
        };



        $scope.$on('$destroy', function(){

        });
    };

    angular.module('app').controller('LoginController',controller);



}());