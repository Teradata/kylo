/**
 *
 */
angular.module('app').service('AuthenticationService', function ($http, $rootScope) {
    var service = {};

    service.Login = Login;
    service.SetCredentials = SetCredentials;
    service.ClearCredentials = ClearCredentials;

    return service;

    function Login(username, password, callback) {
        $http.post('/login', { username: username, password: password })
            .success(function (response) {
                SetCredentials(username);
                callback(response);
            });
    }

    function SetCredentials(username) {

        $rootScope.globals = {
            currentUser: {
                username: username
            }
        };
  }

    function ClearCredentials() {
        $rootScope.globals = {};
    }


});