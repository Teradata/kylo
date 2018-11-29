import * as angular from 'angular';
import AccessConstants from "../../constants/AccessConstants";
import {moduleName} from "./module-name";
import "../../codemirror-require/module";
import "kylo-feedmgr";
import "kylo-common";
import "kylo-services";
import "./codemirror-regex.css";
import "./details/matchers/regexp-editor.component.scss";


class ModuleFactory  {
    module: ng.IModule;
    constructor () {
       // Window.CodeMirror = CodeMirror;
        this.module = angular.module(moduleName,["ui.codemirror"]);
        this.module.config(["$stateProvider", "$compileProvider", this.configFn.bind(this)]); 
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
                        component : "domainTypesController"
                    }
                },
                resolve: {
                    loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                        return import(/* webpackChunkName: "admin.domain-types.controller" */ './DomainTypesController')
                            .then(mod => {

                                return $ocLazyLoad.load(mod.default)
                            })
                            .catch(err => {
                                throw new Error("Failed to load DomainTypesController, " + err);
                            });
                    }]
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
                    loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                        return import(/* webpackChunkName: "admin.domain-types-details.component" */ './details/details.component')
                            .then(mod => {

                                return $ocLazyLoad.load(mod.default)
                            })
                            .catch(err => {
                                throw new Error("Failed to load domainTypeDetailsComponent, " + err);
                            });
                    }]
                },
                data: {
                    breadcrumbRoot: false,
                    displayName: "Domain Type Details",
                    module: moduleName,
                    permissions: AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.permissions
                }
            });
    }
}
const module = new ModuleFactory();
export default module;

