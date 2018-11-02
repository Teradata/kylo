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


        //custom td themes

        var tdOrange = $mdThemingProvider.extendPalette('orange', {
            "50": "#FCE3DB",
            "100": "#FCE3DB",
            "200": "#FAC7B7",
            "300": "#FAC7B7",
            "400": "#F7AA93",
            "500": "#F7AA93",
            "600": "#F58E6F",
            "700": "#F58E6F",
            "800": "#F3753F",
            "900": "#F3753F",
            "contrastDefaultColor": "light",
            "contrastDarkColors": [
                "50",
                "100",
                "200",
                "A100"
            ],
            "contrastLightColors": [
                "300",
                "400",
                "500",
                "600",
                "700",
                "800",
                "900",
                "A200",
                "A400",
                "A700"
            ]
        });



        $mdThemingProvider.definePalette("td-orange", tdOrange);

        var tdSlate = $mdThemingProvider.extendPalette('grey', {
            "50": "#D8DBDC",
            "100": "#D8DBDC",
            "200": "#B0B6B9",
            "300": "#B0B6B9",
            "400": "#899296",
            "500": "#899296",
            "600": "#616D73",
            "700": "#616D73",
            "800": "#394851",
            "900": "#394851",
            "contrastDefaultColor": "dark",
            "contrastDarkColors": [
                "50",
                "100",
                "200",
                "300",
                "400",
                "500",
                "800",
                "900",
                "A100",
                "A200",
                "A400",
                "A700"
            ],
            "contrastLightColors": [
                "600",
                "700"

            ]
        });

        $mdThemingProvider.definePalette("td-slate", tdSlate);

        var tdTeal = $mdThemingProvider.extendPalette('teal',{
            "50": "#CFF0EF",
            "100": "#CFF0EF",
            "200": "#9FE1E0",
            "300": "#9FE1E0",
            "400": "#6FD2D0",
            "500": "#6FD2D0",
            "600": "#3FC3C1",
            "700": "#3FC3C1",
            "800": "#00B2B1",
            "900": "#00B2B1",
            "contrastDefaultColor": "dark",
            "contrastDarkColors": [
                "50",
                "100",
                "200",
                "300",
                "400",
                "500",
                "600",
                "700",
                "A100",
                "A200",
                "A400",
                "A700"
            ],
            "contrastLightColors": [
                "800",
                "900"
            ]
        });
        $mdThemingProvider.definePalette("td-teal", tdTeal);

        var tdYellow = $mdThemingProvider.extendPalette('yellow',{
            "50": "#FFF4DF",
            "100": "#FFF4DF",
            "200": "#FEE9C0",
            "300": "#FEE9C0",
            "400": "#FEDEA0",
            "500": "#FEDEA0",
            "600": "#FDD381",
            "700": "#FDD381",
            "800": "#FEC64D",
            "900": "#FEC64D",
            "contrastDefaultColor": "light",
            "contrastDarkColors": [
                "50",
                "100",
                "200",
                "A100"
            ],
            "contrastLightColors": [
                "300",
                "400",
                "500",
                "600",
                "700",
                "800",
                "900",
                "A200",
                "A400",
                "A700"
            ]
        });

        $mdThemingProvider.definePalette("td-yellow", tdYellow)




        var tdBlue = $mdThemingProvider.extendPalette("blue", {
            "50": "#D1EAF4",
            "100": "#D1EAF4",
            "200": "#A4D6E8",
            "300": "#A4D6E8",
            "400": "#76C1DD",
            "500": "#76C1DD",
            "600": "#49ADD1",
            "700": "#49ADD1",
            "800": "#0098C9",
            "900": "#0098C9",
            "contrastDefaultColor": "light",
            "contrastDarkColors": [
                "50",
                "100",
                "200",
                "A100"
            ],
            "contrastLightColors": [
                "300",
                "400",
                "500",
                "600",
                "700",
                "800",
                "900",
                "A200",
                "A400",
                "A700"]
        });

        $mdThemingProvider.definePalette("td-blue", tdBlue);







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
            $mdThemingProvider.theme('kylo')
                .primaryPalette('td-slate', {'default':'700','hue-1':'100', 'hue-2':'900'})
                .accentPalette('td-teal',{'default':'800','hue-1':'100', 'hue-2':'900'})
                .warnPalette('deep-orange',{'default':'800','hue-1':'100', 'hue-2':'900'})
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
