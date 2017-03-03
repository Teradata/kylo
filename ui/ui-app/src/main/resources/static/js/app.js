define([
    'angular',
    'kylo-utils/LazyLoadUtil',
    'svg-morpheus',
    'd3',
    'moment',
    'angularMaterial',
    'angularAnimate',
    'angularAria',
    'angular-fx',
    'angularMessages',
    'angular-ui-router',
    'angular-material-expansion-panel',
    'angular-material-icons',
    'angular-material-data-table',
    'angular-ui-grid',
    'dirPagination',
    'ng-fx',
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

     var module = angular.module("kylo", ['ui.router', 'oc.lazyLoad', 'ngMaterial','material.components.expansionPanels','md.data.table','ngMdIcons','angularUtils.directives.dirPagination','kylo.common','kylo.services','kylo.side-nav','ngFx','ngAnimate']);

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



    module.run(['$ocLazyLoad',function($ocLazyLoad){
        $ocLazyLoad.load({name:'kylo',files:['bower_components/angular-material-icons/angular-material-icons.css',
                                             'bower_components/angular-material-expansion-panel/dist/md-expansion-panel.css',
                                             'bower_components/angular-material-data-table/dist/md-data-table.css',
                                             'bower_components/nvd3/build/nv.d3.css',
                                             'js/common/dir-pagination/pagination.css',
                                             'bower_components/codemirror/lib/codemirror.css',
                                             'bower_components/vis/dist/vis.min.css'
        ]})
    }])


    //    <script type="text/javascript" src="/ui-common/js/vendor/svg-morpheus/svg-morpheus.js"></script>
    //
    // <link rel="stylesheet" href="/ui-common/js/vendor/angular-material-icons/angular-material-icons.css"/>



});




/*
    'ngMessages',

    'ngCookies',
*/
