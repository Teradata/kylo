import * as angular from 'angular';
import {moduleName} from './module-name';
import AccessConstants from '../constants/AccessConstants';
import 'kylo-services-module';
import * as _ from "underscore";
import CommonRestUrlService from "./CommonRestUrlService";
import {BroadcastService} from "./broadcast-service";


export default class AngularModuleExtensionService{
constructor (private $http: any,
             private $q: any,
             private $timeout: any,
             private $uiRouter: any,
             private CommonRestUrlService: any,
             private BroadcastService: any,
             private $urlMatcherFactory: any) {
                   var EXTENSION_MODULES_INITIALIZED_EVENT = 'extensionModulesInitialized'
       /**
        * The array of extension module metadata
        * @type {Array}
        */
        var modules: any = [];
        var nullValue: any = null;
       /**
        * Map of the menu group to additional links that pertain to the extension modules
        * @type {{}}
        */
        var menuMap: any = {};

        var stateNames : any= [];

        var urlMatchers: any = [];

        var urlMatcherToStateMap : any= {};

       /**
        * Map of the link -> Feed Link objects.
        * These are registered via the 'module-definition.json' as plugins and get pushed into the Feed Details page on the right side.
        * @type {{}}
        */
        var feedNavigationMap: any = {};

       /**
        * Map of the link -> Template Link objects.
        * These are registered via the 'module-definition.json' as plugins and get pushed into the Template Details page on the right side.
        * @type {{}}
        */
       var templateNavigationMap : any= {};

        function lazyStateName(state: any){
            return state.state+".**";
        }

       function buildLazyRoute(state: any,extensionModule: any) {
            var lazyState = {
                name:lazyStateName(state),
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

       function buildModuleNavigationLinks(extensionModule: any){
           if(extensionModule.navigation && extensionModule.navigation.length >0){

               _.each(extensionModule.navigation,function(menu: any) {
                   var group = menu.toggleGroupName;
                  if(group != undefined && group != null){
                      if(menuMap[group] == undefined){
                          menuMap[group] = menu
                          if(menu.links == undefined || menu.links == null) {
                              menu.links = [];
                          }
                      }
                      else {
                          _.each(menu.links,function(link: any){
                              menuMap[group].links.push(link);
                          })

                      }
                  }

               });
           }

           if(extensionModule.feedNavigation && extensionModule.feedNavigation.length >0) {
               _.each(extensionModule.feedNavigation,function(feedNav: any) {
                   feedNavigationMap[feedNav.linkText] = feedNav;
               });
           }

           if(extensionModule.templateNavigation && extensionModule.templateNavigation.length >0) {
               _.each(extensionModule.templateNavigation,function(templateNav: any) {
                   templateNavigationMap[templateNav.linkText] = templateNav;
               });
           }
       }



       function registerStates(extensionModule: any){
           var lazyStates: any[] = [];
           if(angular.isDefined(extensionModule.states)) {
               _.each(extensionModule.states, function(state: any) {
                   stateNames.push(state.state);
                   var urlMatcher = $urlMatcherFactory.compile(state.url);
                   urlMatchers.push(urlMatcher);
                   urlMatcherToStateMap[urlMatcher] = state.state;
                   var exists =  $uiRouter.stateRegistry.get(lazyStateName(state));
                   if(exists) {
                       $uiRouter.stateRegistry.deregister(lazyStateName(state))
                   }
                       $uiRouter.stateRegistry.register(buildLazyRoute(state, extensionModule));
               });
               modules.push(extensionModule);
               buildModuleNavigationLinks(extensionModule);
           }
       }

       var initialized = false;

       var data: any = {
           INITIALIZED_EVENT:EXTENSION_MODULES_INITIALIZED_EVENT,
           onInitialized:function(callback: any){
               BroadcastService.subscribeOnce(data.INITIALIZED_EVENT, callback);
           },
           isInitialized:function(){
               return initialized;
           },
           getModules:function(){
               return modules;
           },
           getNavigationMenu:function(){
               return menuMap;
           },
           stateExists:function(stateName: any) {
             return _.indexOf(stateNames, stateName) > -1;
           },
           urlExists:function(url: any) {
               var urlMatcher : any= _.find(urlMatchers,function(matcher: any){
                   var params = matcher.exec(url);
                   return angular.isObject(params);

               });
               return urlMatcher != undefined;
           },
           getFeedNavigation(){
             return Object.keys(feedNavigationMap);// Object.values(feedNavigationMap);
           },
           getTemplateNavigation(){
               return Object.keys(templateNavigationMap); // object.values
           },
           /**
            * return the state and associated parameters for a url
            * @param url
            * @return {{state: null, params: null, url: *, valid: boolean}}
            */
           
           stateAndParamsForUrl:function(url: any) {
             var data = {state:nullValue,params:nullValue,url:url,valid:false}

              var urlMatcher = _.find(urlMatchers,function(matcher: any){
                   var params = matcher.exec(url);
                   if(angular.isObject(params)){
                       data.params=params;
                       return true;
                   }
                   return false;
               });
              if(urlMatcher != undefined)
              {
                  var state = urlMatcherToStateMap[urlMatcher];
                  data.state = state;
                  data.valid = true;
               }
              return data;
           },
           /**
            * Registers the state with angular.
            * Returns the promise
            */
           registerModules:function(){
              var promise = $http.get(CommonRestUrlService.ANGULAR_EXTENSION_MODULES_URL);
              promise.then(function (response: any) {
                   if(response.data){
                       _.each(response.data,function(extensionModule) {
                          registerStates(extensionModule);
                       });
                       initialized=true;
                   }
                  BroadcastService.notify(EXTENSION_MODULES_INITIALIZED_EVENT);
               },function(err: any){
                  console.log('err',err)
              });
                return promise;
               }

       };

       return data;
             }
}


angular.module(moduleName)
.service('BroadcastService', ["$rootScope", "$timeout",BroadcastService])
.service('CommonRestUrlService',CommonRestUrlService)
.factory("AngularModuleExtensionService",
 ["$http", "$q", "$timeout", "$uiRouter", "CommonRestUrlService","BroadcastService","$urlMatcherFactory",AngularModuleExtensionService]);
