define([
    'angular',
    'kylo-utils/LazyLoadUtil',
    'svg-morpheus',
    'd3',
    'moment',
    'angularMaterial',
    'angularAnimate',
    'angularAria',
    'angularMessages',
    '@uirouter/angularjs',
    'angular-material-expansion-panel',
    'angular-material-icons',
    'angular-material-data-table',
    'angular-sanitize',
    'angular-ui-grid',
    'dirPagination',
    'ng-fx',
    'ng-text-truncate',
    'pascalprecht.translate',
    'angular-translate-loader-static-files',
    'angular-translate-storage-local',
    'angular-translate-handler-log',
    'angular-translate-storage-cookie',
    'angular-cookies',
    'tmh.dynamicLocale',
    'ocLazyLoad',
    'kylo-common',
    'kylo-services',
    'kylo-side-nav'], function (angular,lazyLoad,SVGMorpheus,d3,moment) {
    'use strict';
    window.SVGMorpheus = SVGMorpheus;
    //d3 is needed here as nv.d3 isnt correctly getting it via its internal require call
    window.d3 = d3;
    if(!window.moment){
        window.moment = moment;
    }
    var env = {};
    // Import variables if present (from env.js)
    if(window && window.__env){
        Object.assign(env, window.__env);
    }

     var module = angular.module("kylo", ['ui.router', 'ui.router.upgrade', 'oc.lazyLoad', 'ngMaterial','material.components.expansionPanels','md.data.table','ngMdIcons',
                                          'angularUtils.directives.dirPagination','kylo.common','kylo.services','kylo.side-nav','ngFx','ngAnimate','ngSanitize','ngTextTruncate', 'ui.grid',
                                          'ui.grid.resizeColumns',
                                          'ui.grid.autoResize',
                                          'ui.grid.moveColumns',
                                          'ui.grid.pagination', 'ui.grid.selection', 'ngMessages',
                                          'pascalprecht.translate', 'tmh.dynamicLocale', 'ngCookies']);

    module.constant('__env', env);


    module.config(['$mdAriaProvider','$mdThemingProvider','$mdIconProvider','$urlServiceProvider','ngMdIconServiceProvider','$qProvider', '$translateProvider', 'tmhDynamicLocaleProvider','__env',
           function($mdAriaProvider,$mdThemingProvider, $mdIconProvider, $urlService, ngMdIconServiceProvider,$qProvider, $translateProvider, tmhDynamicLocaleProvider,__env){
       //disable the aria-label warnings in the console
        $mdAriaProvider.disableWarnings();

        $qProvider.errorOnUnhandledRejections(false);
        $translateProvider.useStaticFilesLoader({
            prefix: 'locales/',  // path to translations files
            suffix: '.json'      // suffix, currently- extension of the translations
        });
        $translateProvider.determinePreferredLanguage();
        $translateProvider.fallbackLanguage('en');

        tmhDynamicLocaleProvider.localeLocationPattern('../bower_components/angular-i18n/angular-locale_{{locale}}.js');



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

        // Tell UI-Router to wait to synchronize the URL (until all bootstrapping is complete)e
        $urlService.deferIntercept();

        // Register custom fonts
        ngMdIconServiceProvider
            .addShape('fa-database', '<path d="M896 768q237 0 443-43t325-127v170q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-170q119 84 325 127t443 43zm0 768q237 0 443-43t325-127v170q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-170q119 84 325 127t443 43zm0-384q237 0 443-43t325-127v170q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-170q119 84 325 127t443 43zm0-1152q208 0 385 34.5t280 93.5 103 128v128q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-128q0-69 103-128t280-93.5 385-34.5z"/>')
            .addViewBox('fa-database', '0 0 1792 1792')
            .addShape('fa-hashtag', '<path d="M991 1024l64-256h-254l-64 256h254zm768-504l-56 224q-7 24-31 24h-327l-64 256h311q15 0 25 12 10 14 6 28l-56 224q-5 24-31 24h-327l-81 328q-7 24-31 24h-224q-16 0-26-12-9-12-6-28l78-312h-254l-81 328q-7 24-31 24h-225q-15 0-25-12-9-12-6-28l78-312h-311q-15 0-25-12-9-12-6-28l56-224q7-24 31-24h327l64-256h-311q-15 0-25-12-10-14-6-28l56-224q5-24 31-24h327l81-328q7-24 32-24h224q15 0 25 12 9 12 6 28l-78 312h254l81-328q7-24 32-24h224q15 0 25 12 9 12 6 28l-78 312h311q15 0 25 12 9 12 6 28z"/>')
            .addViewBox('fa-hashtag', '0 0 1792 1792');
    }]);





    module.run(['$ocLazyLoad', '$translate', function($ocLazyLoad, $translate){
        $ocLazyLoad.load({name:'kylo',files:['bower_components/angular-material-icons/angular-material-icons.css',
                                             'bower_components/angular-material-expansion-panel/dist/md-expansion-panel.css',
                                             'bower_components/angular-material-data-table/dist/md-data-table.css',
                                             'bower_components/nvd3/build/nv.d3.css',
                                             'bower_components/codemirror/lib/codemirror.css',
                                             'bower_components/vis/dist/vis.min.css'
        ]})
    }])





});




/*
    'ngMessages',

    'ngCookies',
*/
