/**
 *
 *

 define([
    'angular', 
    './module-name', 
    'codemirror-require/module', 
    'kylo-common', 
    'kylo-services', 
    'jquery',
    'angular-drag-and-drop-lists',
    './catalog/catalog.module',
    'fattable'
], function (angular, moduleName) {
    var module = angular.module(moduleName, ['ui.codemirror','dndLists']);

    module.run(['$ocLazyLoad', function ($ocLazyLoad) {
        $ocLazyLoad.load({
            name: 'kylo',
            files: ['../../bower_components/angular-ui-grid/ui-grid.css',
                    'assets/ui-grid-material.css',
                    'js/feed-mgr/shared/cron-expression-preview/cron-expression-preview.css',
                    'js/feed-mgr/shared/apply-domain-type/apply-table-domain-types.component.css',
                    "../../bower_components/fattable/fattable.css",
                    'js/feed-mgr/services/fattable/fattable-service.css'
            ], serie: true
        })
    }]);
    return module;

});

*/


import * as angular from 'angular';
import {moduleName} from "./module-name";
import "kylo-common";
import "kylo-services";
import "codemirror-require/module";
import "jquery";
import  'angular-drag-and-drop-lists';
import 'fattable';
import './catalog/catalog.module';
import './shared/apply-domain-type/apply-table-domain-types.component.scss';

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]);
        this.module.config(['$compileProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }
    configFn($compileProvider:angular.ICompileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }
    runFn($ocLazyLoad: any){
        $ocLazyLoad.load({
            name: 'kylo',
            files: [
            ], serie: true
        })

    }

}

const module = new ModuleFactory();
export default module;


