import * as angular from 'angular';
const moduleName = require('./module-name');
import AccessConstants from "../../constants/AccessConstants";
import {Lazy} from '../../kylo-utils/LazyLoadUtil';

class ModuleFactory {

    module: ng.IModule;

    constructor() {
        this.module = angular.module(moduleName, ['ui.router']);
        this.module.config(['$stateProvider', '$compileProvider', this.configFn.bind(this)]);
    }

    configFn($stateProvider: any, $compileProvider: any) {
        $compileProvider.preAssignBindingsEnabled(true);

        //preassign modules until directives are rewritten to use the $onInit method.
        //https://docs.angularjs.org/guide/migration#migrating-from-1-5-to-1-6
        $compileProvider.preAssignBindingsEnabled(true);

        $stateProvider.state(AccessConstants.UI_STATES.CATEGORIES.state, {
            url: '/categories',
            params: {},
            views: {
                'content': {
                    component: "categoriesController"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    const onModuleLoad = () => {
                        return import(/* webpackChunkName: "feeds.categories.controller" */ './CategoriesController')
                            .then(Lazy.onModuleImport($ocLazyLoad));
                    };
                    return import(/* webpackChunkName: "feed-mgr.module-require" */ "../module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
                }]
            },
            data: {
                breadcrumbRoot: true,
                displayName: 'Categories',
                module: moduleName,
                permissions: AccessConstants.UI_STATES.CATEGORIES.permissions
            }
        }).state(AccessConstants.UI_STATES.CATEGORY_DETAILS.state, {
            url: '/category-details/{categoryId}',
            params: {
                categoryId: null
            },
            views: {
                'content': {
                    component: "categoryDetailsController"
                }
            },
            resolve: {
                loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                    const onModuleLoad = () => {
                        return import(/* webpackChunkName: "feeds.category-details" */ './category-details')
                            .then(Lazy.onModuleImport($ocLazyLoad));
                    };
                    return import(/* webpackChunkName: "feed-mgr.module-require" */ "../module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
                }]
            },
            data: {
                breadcrumbRoot: false,
                displayName: 'Category Details',
                module: moduleName,
                permissions: AccessConstants.UI_STATES.CATEGORY_DETAILS.permissions
            }
        })
    }
}

const moduleFactory = new ModuleFactory();
export default moduleFactory;
