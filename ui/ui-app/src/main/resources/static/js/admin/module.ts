import * as angular from 'angular';
import AccessConstants from '../constants/AccessConstants';
//const lazyLoadUtil = require('../kylo-utils/LazyLoadUtil');
import lazyLoadUtil from "../kylo-utils/LazyLoadUtil";
//const codeMirrorRequire = require('../codemirror-require/module');
import "../codemirror-require/module";
import {KyloServicesModule} from "../services/services.module";
//const moduleName =require("./module-name");
import {moduleName} from "./module-name";
//export * from "../codemirror-require/module"; 

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
        this.module = angular.module(moduleName,[]); 
        this.module.config(['$stateProvider',this.configFn.bind(this)]);
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]); 
    }
    configFn($stateProvider:any) {
       $stateProvider.state('jcr-query',{
            url:'/admin/jcr-query',
            views: {
                'content': {
                    templateUrl: 'js/admin/jcr/jcr-query.html',
                    controller:"JcrQueryController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['admin/jcr/JcrQueryController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'JCR Admin',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.JCR_ADMIN.permissions
            }
        });

        $stateProvider.state('cluster',{
            url:'/admin/cluster',
            views: {
                'content': {
                    templateUrl: 'js/admin/cluster/cluster-test.html',
                    controller:"ClusterController",
                    controllerAs:"vm"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['admin/cluster/ClusterController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Kylo Cluster',
                module:moduleName,
                permissions:[]
            }
        })
    }  

    runFn($ocLazyLoad: any){
        $ocLazyLoad.load({
            name: 'kylo', 
            files: ['bower_components/angular-ui-grid/ui-grid.css', 'assets/ui-grid-material.css'],
            serie: true
            })
    }
    
    lazyLoadController(path:any){
        return lazyLoadUtil.lazyLoadController(path,"admin/module-require");
    }

} 
const module = new ModuleFactory();
export default module;

