define([
    'angular',
    'angular-material-icons',
    'angularMaterial',
    'angularAnimate',
    'angularAria',
    'angularMessages','urlParams'], function (angular) {

    var module = angular.module('app', [
        'ngMaterial',
        'ngMdIcons']);

    module.config(['$mdThemingProvider','$mdIconProvider',function($mdThemingProvider, $mdIconProvider){
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

    var controller = function(){
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


    };

    module.controller('LoginController',controller);

    return angular.bootstrap(document,['app']);
});
