define([
    'angular',
    'angular-material-icons',
    'angularMaterial',
    'angularAnimate',
    'angularAria',
    'angularCookies',
    'angularMessages','urlParams'], function (angular) {

    var module = angular.module('app', [
        'ngMaterial',
        'ngMdIcons',
        'ngCookies']);

    module.config(['$mdThemingProvider','$mdIconProvider','$locationProvider',function($mdThemingProvider, $mdIconProvider, $locationProvider){
        $locationProvider.html5Mode(true);

        var primaryBlue = $mdThemingProvider.extendPalette('blue', {
            '500': '3483BA',
            '900':'2B6C9A'
        });

        var accentOrange = $mdThemingProvider.extendPalette('orange', {
            'A200': 'F08C38'
        });


        $mdThemingProvider.definePalette('primaryBlue', primaryBlue);
        $mdThemingProvider.definePalette('accentOrange', accentOrange);

        $mdThemingProvider.theme('kylo')
            .primaryPalette('primaryBlue', {
                'hue-2':'900'
            })
            .accentPalette('accentOrange');

        $mdThemingProvider.setDefaultTheme('kylo');

    }]);

    var controller = function($location, LoginService){
        this.username;
        this.password;
        this.error = '';
        var self = this;
        this.targetUrl = "/#"+$location.hash();
        var url = $location.url();
        if("/login.html?logout" == url) {
            LoginService.resetCookieValue();
        }

        if(!LoginService.isValidTarget(self.targetUrl)){
              var previousTarget= LoginService.getTargetUrl();
                self.targetUrl = previousTarget;
        }
        LoginService.setTargetUrl(self.targetUrl);

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


    };

    module.controller('LoginController',['$location','LoginService',controller]);


    var loginService = function($cookies){
        var self = this;
        this.targetUrl = '';

        this.getTargetUrl = function(){
            if(self.isValidTarget(self.targetUrl)){
                return self.targetUrl;
            }
            else {
                var cookieValue = $cookies.get('kyloTargetUrl');
                if(self.isValidTarget(cookieValue)){
                    self.targetUrl = cookieValue;
                }
            }
            return self.targetUrl || '';
        }
        this.setTargetUrl = function(targetUrl){
            if(self.isValidTarget(targetUrl)){
                $cookies.put('kyloTargetUrl',targetUrl);
                self.targetUrl = targetUrl;
            }
        }
        this.isValidTarget = function(targetUrl){
            return targetUrl != null && targetUrl != "" && targetUrl != "/#";
        }
        this.resetCookieValue = function(){
            $cookies.remove('kyloTargetUrl');
        }


    }

    module.service('LoginService',['$cookies',loginService]);

    return angular.bootstrap(document,['app']);
});
