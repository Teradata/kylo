/**','kylo-feedmgr','kylo-common','kylo-services' */
import * as angular from 'angular';
import {moduleName} from "./module-name";
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from '../../constants/AccessConstants';
import "kylo-feedmgr";
import "kylo-common";
import {KyloServicesModule} from "../../services/services.module";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]); 
        this.module.config(['$stateProvider','$compileProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad',this.runFn.bind(this)]);

    }

    runFn($ocLazyLoad: any) {
        return import(/* webpackChunkName: "categories.module" */ "./module-require")
            .then(mod => {
                $ocLazyLoad.load({name: moduleName});
            })
            .catch(err => {
                throw new Error("Failed to load feed-mgr/business-metadata/module-require, " + err);
            });
    }

    configFn($stateProvider:any, $compileProvider: any) {
        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.BUSINESS_METADATA.state,{
            url:'/business-metadata',
            params: {},
            views: {
                'content': {
                    component : "businessMetadataController"
                }
            },
            resolve: {
                // loadMyCtrl: this.lazyLoadController(['feed-mgr/business-metadata/BusinessMetadataController'])
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    return import(/* webpackChunkName: "feeds.business-metadata.controller" */ './BusinessMetadataController')
                        .then(mod => {
                            console.log('imported BusinessMetadataController mod', mod);
                            return $ocLazyLoad.load(mod.default)
                        })
                        .catch(err => {
                            throw new Error("Failed to load BusinessMetadataController, " + err);
                        });
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Business Metadata',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.BUSINESS_METADATA.permissions
            }
        });
    }  
}
const module = new ModuleFactory();
export default module;

