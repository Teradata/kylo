/**
 * Angular 1 module
 * */

import * as angular from 'angular';
import {moduleName} from "./module-name";
import "kylo-common";
import "kylo-services";
import "codemirror-require/module";
import "jquery";
import  'angular-drag-and-drop-lists';
import 'feed-mgr/catalog/catalog.module';
import    'fattable';
import "./build-query/flowchart/flowchart_directive"
import {QueryEngineFactory} from "./wrangler/query-engine-factory.service";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,["kylo.common", "kylo.feedmgr", "kylo.services", "kylo.ui-codemirror", "flowChart", "nvd3"]);
        this.module.config(['$compileProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);


        this.module
            .service("VisualQueryEngineFactory", QueryEngineFactory)
            .provider("$$wranglerInjector", {
                $get: function () {
                    return QueryEngineFactory.$$wranglerInjector;
                }
            });


    }
    configFn($compileProvider:angular.ICompileProvider) {
        //pre-assign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);
    }
    runFn($ocLazyLoad: any){
        $ocLazyLoad.load({
            name: 'kylo',
            files: ["bower_components/fattable/fattable.css",
                    "js/feed-mgr/visual-query/visual-query.component.css",
                    "js/feed-mgr/visual-query/build-query/flowchart/flowchart.css",
                    "js/feed-mgr/visual-query/transform-data/transform-data.component.css"
            ], serie: true
        })

    }

}

const module = new ModuleFactory();

export default module;
