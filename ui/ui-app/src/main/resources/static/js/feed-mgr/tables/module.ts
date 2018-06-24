import * as angular from 'angular';
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import {moduleName} from "./module-name";
import "kylo-feedmgr";
import "kylo-common";
import "kylo-services";
import "jquery";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
       // Window.CodeMirror = CodeMirror;
        this.module = angular.module(moduleName,[]); 
        this.module.config(["$stateProvider",this.configFn.bind(this)]); 
        
    }
  

    configFn($stateProvider: any){
       $stateProvider.state(AccessConstants.UI_STATES.CATALOG.state,{
            url:'/tables',
            params: {
            },
            views: {
                'content': {
                    component:"catalogController",
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/CatalogController'])
            },
            data:{
                breadcrumbRoot:true,
                displayName:'Catalog',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.CATALOG.permissions
            }
        });

        $stateProvider.state(AccessConstants.UI_STATES.SCHEMAS.state,{
            url:'/tables/{datasource}/schemas',
            params: {
                datasource: null
            },
            views: {
                'content': {
                    component:"schemasController",
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/SchemasController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Schemas',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.SCHEMAS.permissions
            }
        });

        $stateProvider.state(AccessConstants.UI_STATES.TABLES.state,{
            url:'/tables/{datasource}/schemas/{schema}',
            params: {
                datasource: null,
                schema: null
            },
            views: {
                'content': {
                    component:"tablesController"
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/TablesController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Tables',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.TABLES.permissions
            }
        });

        $stateProvider.state(AccessConstants.UI_STATES.TABLE.state,{
            url:'/tables/{datasource}/schemas/{schema}/{tableName}',
            params: {
                datasource: null,
                schema: null,
                tableName: null
            },
            views: {
                'content': {
                   component : 'tableController'
                }
            },
            resolve: {
                loadMyCtrl: this.lazyLoadController(['feed-mgr/tables/TableController'])
            },
            data:{
                breadcrumbRoot:false,
                displayName:'Table Details',
                module:moduleName,
                permissions:AccessConstants.UI_STATES.TABLE.permissions
            }
        });
    }

    lazyLoadController(path: any) {
            return lazyLoadUtil.lazyLoadController(path, "feed-mgr/tables/module-require");
        }
} 
const module = new ModuleFactory();
export default module;

