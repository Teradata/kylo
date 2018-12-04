import {HttpClient} from '@angular/common/http';
import {Injectable} from "@angular/core";
import * as _ from "underscore";
import {ObjectUtils} from "../../lib/common/utils/object-utils";
import {BroadcastService} from "./broadcast-service";
import {CommonRestUrlService} from "./CommonRestUrlService";

@Injectable()
export class AngularModuleExtensionService{

constructor (private http: HttpClient,
             private commonRestUrlService: CommonRestUrlService,
             private broadcastService: BroadcastService) {}

    EXTENSION_MODULES_INITIALIZED_EVENT = 'extensionModulesInitialized';
    /**
    * The array of extension module metadata
    * @type {Array}
    */
    modules: any = [];
    nullValue: any = null;
    /**
    * Map of the menu group to additional links that pertain to the extension modules
    * @type {{}}
    */
    menuMap: any = {};

    stateNames : any= [];

    urlMatchers: any = [];

    urlMatcherToStateMap : any= {};

    /**
    * Map of the link -> Feed Link objects.
    * These are registered via the 'module-definition.json' as plugins and get pushed into the Feed Details page on the right side.
    * @type {{}}
    */
    feedNavigationMap: any = {};

    /**
    * Map of the link -> Template Link objects.
    * These are registered via the 'module-definition.json' as plugins and get pushed into the Template Details page on the right side.
    * @type {{}}
    */
    templateNavigationMap : any= {};

    lazyStateName(state: any){
        return state.state+".**";
    }

    buildLazyRoute(state: any,extensionModule: any) {

        let lazyState = {
            name:this.lazyStateName(state),
            url:state.url,
            params:state.params,
            lazyLoad: function (transition: any) {
                transition.injector().get('$ocLazyLoad').load(extensionModule.moduleJsUrl).then(function success(args: any) {
                    //upon success go back to the state
                    transition.router.stateService.go(state.state, transition.params())
                    return args;
                }, function error(err: any) {
                    console.log("Error loading "+state.state, err);
                    return err;
                });

            }
        }
        return lazyState;
    }

    buildModuleNavigationLinks(extensionModule: any){

        if(extensionModule.navigation && extensionModule.navigation.length >0){

            _.each(extensionModule.navigation,function(menu: any) {
                this.group = menu.toggleGroupName;
                if(this.group != undefined && this.group != null){
                    if(this.menuMap[this.group] == undefined){
                    this.menuMap[this.group] = menu
                        if(menu.links == undefined || menu.links == null) {
                            menu.links = [];
                        }
                    }
                    else {
                        _.each(menu.links,function(link: any){
                        this.menuMap[this.group].links.push(link);
                        })

                    }
                }

            });
        }

        if(extensionModule.feedNavigation && extensionModule.feedNavigation.length >0) {
            _.each(extensionModule.feedNavigation,function(feedNav: any) {
            this.feedNavigationMap[feedNav.linkText] = feedNav;
            });
        }

        if(extensionModule.templateNavigation && extensionModule.templateNavigation.length >0) {
            _.each(extensionModule.templateNavigation,function(templateNav: any) {
            this.templateNavigationMap[templateNav.linkText] = templateNav;
            });
        }
    }



    registerStates(extensionModule: any){

        if(ObjectUtils.isDefined(extensionModule.states)) {
            _.each(extensionModule.states, function(state: any) {
                this.stateNames.push(state.state);
                this.urlMatcher = this.$urlMatcherFactory.compile(state.url);
                this.urlMatchers.push(this.urlMatcher);
                this.urlMatcherToStateMap[this.urlMatcher] = state.state;
                this.exists =  this.$uiRouter.stateRegistry.get(this.lazyStateName(state));
                if(this.exists) {
                this.$uiRouter.stateRegistry.deregister(this.lazyStateName(state))
                }
                this.$uiRouter.stateRegistry.register(this.buildLazyRoute(state, extensionModule));
            });
            this.modules.push(extensionModule);
            this.buildModuleNavigationLinks(extensionModule);
        }
    }

    initialized = false;

//    data: any = {
    INITIALIZED_EVENT = this.EXTENSION_MODULES_INITIALIZED_EVENT;

    onInitialized(callback: any){
        this.broadcastService.subscribeOnce(this.INITIALIZED_EVENT, callback);
    }
    isInitialized(){
        return this.initialized;
    }
    getModules(){
        return this.modules;
    }
    getNavigationMenu(){
        return this.menuMap;
    }
    stateExists(stateName: any) {
        return _.indexOf(this.stateNames, stateName) > -1;
    }
    urlExists(url: any) {
        let urlMatcher : any= _.find(this.urlMatchers,function(matcher: any){
            let params = matcher.exec(url);
            return _.isObject(params);

        });
        return urlMatcher != undefined;
    }
    getFeedNavigation(){
        return Object.keys(this.feedNavigationMap);// Object.values(feedNavigationMap);
    }
    getTemplateNavigation(){
        return Object.keys(this.templateNavigationMap); // object.values
    }
    /**
    * return the state and associated parameters for a url
    * @param url
    * @return {{state: null, params: null, url: *, valid: boolean}}
    */

    stateAndParamsForUrl(url: any) {
        let data = {state:this.nullValue,params:this.nullValue,url:url,valid:false}

        let urlMatcher = _.find(this.urlMatchers,function(matcher: any){
            let params = matcher.exec(url);
            if(_.isObject(params)){
                data.params=params;
                return true;
            }
            return false;
        });
        if(urlMatcher != undefined)
        {
            let state = this.urlMatcherToStateMap[urlMatcher];
            data.state = state;
            data.valid = true;
        }
        return data;
    }
    /**
    * Registers the state with angular.
    * Returns the promise
    */
    registerModules(){
        let promise = this.http.get(this.commonRestUrlService.ANGULAR_EXTENSION_MODULES_URL).toPromise();
        promise.then( (response: any) => {
            if(response){
                _.each(response,(extensionModule)=> {
                    this.registerStates(extensionModule);
                });
                this.initialized=true;
            }
            this.broadcastService.notify(this.EXTENSION_MODULES_INITIALIZED_EVENT);
        },(err: any)=>{
            console.log('err',err)
        });
        return promise;
    }
}
