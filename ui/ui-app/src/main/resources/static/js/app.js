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

     var module = angular.module("kylo", ['ui.router', 'ui.router.upgrade', 'oc.lazyLoad', 'ngMaterial','material.components.expansionPanels','md.data.table','ngMdIcons',
                                          'angularUtils.directives.dirPagination','kylo.common','kylo.services','kylo.side-nav','ngFx','ngAnimate','ngSanitize','ngTextTruncate', 'ui.grid',
                                          'ui.grid.resizeColumns',
                                          'ui.grid.autoResize',
                                          'ui.grid.moveColumns',
                                          'ui.grid.pagination', 'ngMessages']);

    module.config(['$mdAriaProvider','$mdThemingProvider','$mdIconProvider','$urlServiceProvider','ngMdIconServiceProvider','$qProvider', function($mdAriaProvider,$mdThemingProvider, $mdIconProvider, $urlService, ngMdIconServiceProvider,$qProvider){
       //disable the aria-label warnings in the console
        $mdAriaProvider.disableWarnings();

        $qProvider.errorOnUnhandledRejections(false);

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

        // Tell UI-Router to wait to synchronize the URL (until all bootstrapping is complete)e
        $urlService.deferIntercept();

        // Register custom fonts
        ngMdIconServiceProvider
            .addShape('fa-database', '<path d="M896 768q237 0 443-43t325-127v170q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-170q119 84 325 127t443 43zm0 768q237 0 443-43t325-127v170q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-170q119 84 325 127t443 43zm0-384q237 0 443-43t325-127v170q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-170q119 84 325 127t443 43zm0-1152q208 0 385 34.5t280 93.5 103 128v128q0 69-103 128t-280 93.5-385 34.5-385-34.5-280-93.5-103-128v-128q0-69 103-128t280-93.5 385-34.5z"/>')
            .addViewBox('fa-database', '0 0 1792 1792');
    }]);





    module.run(['$ocLazyLoad',function($ocLazyLoad){
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
