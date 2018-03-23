import * as angular from 'angular';
import lazyLoadUtil from "../../kylo-utils/LazyLoadUtil";
import AccessConstants from "../../constants/AccessConstants";
import {moduleName} from "./module-name";
import "../../codemirror-require/module";
import "kylo-feedmgr";
import "kylo-common";
import "kylo-services";

class ModuleFactory  {
    module: ng.IModule;
    constructor () {
       // Window.CodeMirror = CodeMirror;
        this.module = angular.module(moduleName,[]); 
        this.module.config(["$stateProvider", "$compileProvider", this.configFn.bind(this)]); 
        this.module.run(['$ocLazyLoad', this.runFn.bind(this)]);
    }
    runFn($ocLazyLoad:any){
            $ocLazyLoad.load({
                name: 'kylo',
                files: [
                    "js/feed-mgr/domain-types/codemirror-regex.css",
                    "js/feed-mgr/domain-types/details/matchers/regexp-editor.component.css"
                ]
            });
     }

    configFn($stateProvider: any, $compileProvider: any){
           //preassign modules until directives are rewritten to use the $onInit method.
            //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
            $compileProvider.preAssignBindingsEnabled(true);

            $stateProvider.state(AccessConstants.UI_STATES.DOMAIN_TYPES.state, {
                url: "/domain-types",
                params: {},
                views: {
                    content: {
                        templateUrl: "js/feed-mgr/domain-types/domain-types.html",
                        controller: "DomainTypesController",
                        controllerAs: "vm"
                    }
                },
                resolve: {
                    loadMyCtrl: this.lazyLoadController(["feed-mgr/domain-types/DomainTypesController"])
                },
                data: {
                    breadcrumbRoot: true,
                    displayName: "Domain Types",
                    module: moduleName,
                    permissions: AccessConstants.UI_STATES.DOMAIN_TYPES.permissions
                }
            }).state(AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.state, {
                url: "/domain-type-details/{domainTypeId}",
                params: {
                    domainTypeId: null
                },
                views: {
                    content: {
                        component: "domainTypeDetailsComponent"
                    }
                },
                resolve: {
                    model:  ($transition$: any, DomainTypesService: any)=> {
                        if (angular.isString($transition$.params().domainTypeId)) {
                            return DomainTypesService.findById($transition$.params().domainTypeId);
                        } else {
                            return DomainTypesService.newDomainType();
                        }
                    },
                    loadMyCtrl: this.lazyLoadController(["feed-mgr/domain-types/details/details.component"])
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: "Domain Type Details",
                    module: moduleName,
                    permissions: AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.permissions
                }
            });
    }

    lazyLoadController(path: any) {
            return lazyLoadUtil.lazyLoadController(path, "feed-mgr/domain-types/module-require");
        }
} 
const module = new ModuleFactory();
export default module;

