import * as angular from "angular";
import * as lazyLoad from "./kylo-utils/LazyLoadUtil";
import * as _ from "underscore";
const SVGMorpheus = require("../bower_components/svg-morpheus/compile/minified/svg-morpheus");
//declare const SVGMorpheus: any;
declare const d3:any;
import * as moment from "moment";
import "angularMaterial";
import "angularAnimate";
import "angularAria";
import "angularMessages";
import "@uirouter/angularjs";
import "angular-material-expansion-panel";
import "angular-material-icons";
import "angular-material-data-table";
import "angular-sanitize";
import "angular-ui-grid";
import "dirPagination";
import "ng-fx";
import "ng-text-truncate";
import "pascalprecht.translate";
import "angular-translate-loader-static-files";
import "angular-translate-storage-local";
import "angular-translate-handler-log";
import "angular-translate-storage-cookie";
import "angular-cookies";
import "tmh.dynamicLocale";
import "ocLazyLoad";
import "kylo-common";
import "kylo-services";
import "kylo-side-nav";
'use strict';

class App {
module: ng.IModule;
constructor() {
    (<any>window).SVGMorpheus = SVGMorpheus;
    //d3 is needed here as nv.d3 isnt correctly getting it via its internal require call
    (<any>window).d3 = d3;
    if(!(<any>window).moment){
        (<any>window).moment = moment;
    }
    var env = {};
    // Import variables if present (from env.js)
    if(window && (<any>window).__env){
        Object.assign(env, (<any>window).__env);
    }

    this.module = angular.module("kylo", ['ui.router', 'ui.router.upgrade', 'oc.lazyLoad', 'ngMaterial','material.components.expansionPanels','md.data.table','ngMdIcons',
                                          'angularUtils.directives.dirPagination','kylo.common','kylo.services','kylo.side-nav','ngFx','ngAnimate','ngSanitize','ngTextTruncate', 'ui.grid',
                                          'ui.grid.resizeColumns',
                                          'ui.grid.autoResize',
                                          'ui.grid.moveColumns',
                                          'ui.grid.pagination', 'ui.grid.selection', 'ngMessages',
                                          'pascalprecht.translate', 'tmh.dynamicLocale', 'ngCookies']);
    this.module.constant('LOCALES', {
           'locales': {
               'en_US': 'English'
            },
            'preferredLocale': 'en_US'
        });
        this.module.constant('__env', env)
        this.module.config(['$mdAriaProvider','$mdThemingProvider','$mdIconProvider','$urlServiceProvider',
                            'ngMdIconServiceProvider','$qProvider', '$translateProvider', 
                            'tmhDynamicLocaleProvider','__env',this.configFn.bind(this)]);
         this.module.run(['$ocLazyLoad', '$translate', this.runFn.bind(this)]); 
    }

    configFn($mdAriaProvider: any,$mdThemingProvider: any, $mdIconProvider: any, $urlService: any,
             ngMdIconServiceProvider: any,$qProvider: any, $translateProvider: any, tmhDynamicLocaleProvider: any, __env:any){
       //disable the aria-label warnings in the console
        $mdAriaProvider.disableWarnings();

        $qProvider.errorOnUnhandledRejections(false);
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

        tmhDynamicLocaleProvider.localeLocationPattern('../bower_components/angular-i18n/angular-locale_{{locale}}.js');

        //read in any theme info from the __env
        if(__env.themes) {
            var themes = __env.themes;
            var definitions = themes.definePalette;
            if(definitions && definitions.length >0){
                // define the new palettes
                var newPalettes = {};
                _.each(definitions,function(palette:any) {
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
    }

    runFn($ocLazyLoad: any, $translate: any){
        $ocLazyLoad.load({name:'kylo',files:['bower_components/angular-material-icons/angular-material-icons.css',
                                             'bower_components/angular-material-expansion-panel/dist/md-expansion-panel.css',
                                             'bower_components/angular-material-data-table/dist/md-data-table.css',
                                             'bower_components/nvd3/build/nv.d3.css',
                                             'bower_components/codemirror/lib/codemirror.css',
                                             'bower_components/vis/dist/vis.min.css'
        ]})

    }
}
const app = new App();
export default app;
