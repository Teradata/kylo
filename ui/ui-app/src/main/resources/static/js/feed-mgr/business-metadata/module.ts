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
                    templateUrl: 'js/feed-mgr/business-metadata/business-metadata.html',
                    controller:'BusinessMetadataController',
                    controllerAs:'vm'
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/business-metadata/BusinessMetadataController'])
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Business Metadata',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.BUSINESS_METADATA.permissions
            }
        });
    }  

   
    
    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,"feed-mgr/business-metadata/module-require");
    }

} 
const module = new ModuleFactory();
export default module;

