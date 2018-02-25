define([
    'angular',
    'angular-material-icons',
    'angularMaterial',
    'angularAnimate',
    'angularAria',
    'angularCookies',
    'pascalprecht.translate',
    'angular-translate-loader-static-files',
    'angular-translate-storage-local',
    'angular-translate-handler-log',
    'angular-translate-storage-cookie',

    'angularMessages','urlParams'], function (angular) {

    var module = angular.module('app', [
        'ngMaterial',
        'ngMdIcons',
        'pascalprecht.translate',
        'ngCookies']);

    module.constant('LOCALES', {
        'locales': {
            'en_US': 'English'
        },
        'preferredLocale': 'en_US'
    });

    var env = {};
    // Import variables if present (from env.js)
    if(window && window.__env){
        Object.assign(env, window.__env);
    }
    module.constant('__env', env);

    module.config(['$mdThemingProvider','$mdIconProvider','$locationProvider','$translateProvider','__env',function($mdThemingProvider, $mdIconProvider, $locationProvider,$translateProvider,__env){
        $locationProvider.html5Mode(true);

        $translateProvider.useStaticFilesLoader({
            prefix: 'locales/',  // path to translations files
            suffix: '.json'      // suffix, currently- extension of the translations
        });

        $translateProvider
            .registerAvailableLanguageKeys(["en"], {
                "en_*": "en",
                "*": "en"
            })
            .determinePreferredLanguage()
            .fallbackLanguage('en')
            .useLocalStorage();  // saves selected language to localStorage



        //read in any theme info from the __env
        if(__env.themes) {
            var themes = __env.themes;
            var definitions = themes.definePalette;
            if(definitions && definitions.length >0){
                // define the new palettes
                var newPalettes = {};
                _.each(definitions,function(palette) {
                    if(palette.name && palette.name != '' && !_.isEmpty(palette.details)) {
                        if (palette.extend && palette.extend != "") {
                            var p1 = $mdThemingProvider.extendPalette(palette.extend, palette.details);
                            $mdThemingProvider.definePalette(palette.name, p1);
                            newPalettes[palette.name] = p1;
                        }
                        else {
                            $mdThemingProvider.definePalette(palette.name, palette.details);
                        }
                    }
                    else {
                        console.log("Unable to regsiter definition.  It needs to contain a valid 'name' and 'details'")
                    }
                });
            }
            //register the palette types
            if(themes.primaryPalette && !_.isEmpty(themes.primaryPalette)){
                var dark = themes.primaryPalette.dark || false;
                var hues = themes.primaryPalette.details || null;
                $mdThemingProvider.theme('kylo').primaryPalette(themes.primaryPalette.name,hues).dark(dark);
                console.log('Applied primaryPalette',themes.primaryPalette.name)
            }
            if(themes.accentPalette && !_.isEmpty(themes.accentPalette)){
                var dark = themes.accentPalette.dark || false;
                var hues = themes.accentPalette.details || null;
                $mdThemingProvider.theme('kylo').accentPalette(themes.accentPalette.name,hues).dark(dark)
                console.log('Applied accentPalette',themes.accentPalette.name)
            }
            if(themes.warnPalette && !_.isEmpty(themes.warnPalette)){
                var dark = themes.warnPalette.dark || false;
                var hues = themes.warnPalette.details || null;
                $mdThemingProvider.theme('kylo').warnPalette(themes.warnPalette.name,hues).dark(dark)
                console.log('Applied warnPalette',themes.warnPalette.name)
            }
            if(themes.backgroundPalette && !_.isEmpty(themes.backgroundPalette)){
                var dark = themes.backgroundPalette.dark || false;
                var hues = themes.backgroundPalette.details || null;
                $mdThemingProvider.theme('kylo').backgroundPalette(themes.backgroundPalette.name,hues).dark(dark)
                console.log('Applied backgroundPalette',themes.backgroundPalette.name)
            }
        }
        else {
            //default blue/orange kylo theme
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
        }

        $mdThemingProvider.setDefaultTheme('kylo');

    }]);

    var controller = function($location, LoginService, $translate, $scope){
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

        $scope.changeLanguage = function (langKey) {
            $translate.use(langKey);
        };

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

    module.controller('LoginController',['$location','LoginService','$translate', '$scope',controller]);


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
