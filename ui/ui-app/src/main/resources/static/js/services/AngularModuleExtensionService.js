define(['angular', 'services/module-name', 'constants/AccessConstants', 'kylo-services-module'], function (angular, moduleName, AccessConstants) {

    return angular.module(moduleName).factory("AngularModuleExtensionService", ["$http", "$q", "$timeout", "$uiRouter", "CommonRestUrlService","BroadcastService","$urlMatcherFactory",
   function ($http, $q, $timeout, $uiRouter, CommonRestUrlService, BroadcastService,$urlMatcherFactory) {

       var EXTENSION_MODULES_INITIALIZED_EVENT = 'extensionModulesInitialized'
       /**
        * The array of extension module metadata
        * @type {Array}
        */
        var modules = [];

       /**
        * Map of the menu group to additional links that pertain to the extension modules
        * @type {{}}
        */
        var menuMap = {};

        var stateNames = [];

        var urlMatchers = [];

        var urlMatcherToStateMap = {};

        function lazyStateName(state){
            return state.state+".**";
        }

       function buildLazyRoute(state,extensionModule) {
            var lazyState = {
                name:lazyStateName(state),
                url:state.url,
                params:state.params,
                lazyLoad: function (transition) {
                    transition.injector().get('$ocLazyLoad').load(extensionModule.moduleJsUrl).then(function success(args) {
                        //upon success go back to the state
                        transition.router.stateService.go(state.state, transition.params())
                        return args;
                    }, function error(err) {
                        console.log("Error loading "+state.state, err);
                        return err;
                    });

                }
            }
            return lazyState;
       }

       function buildModuleNavigationLinks(extensionModule){
           if(extensionModule.navigation && extensionModule.navigation.length >0){

               _.each(extensionModule.navigation,function(menu) {
                   var group = menu.toggleGroupName;
                  if(group != undefined && group != null){
                      if(menuMap[group] == undefined){
                          menuMap[group] = menu
                          if(menu.links == undefined || menu.links == null) {
                              menu.links = [];
                          }
                      }
                      else {
                          _.each(menu.links,function(link){
                              menuMap[group].links.push(link);
                          })

                      }
                  }

               });
           }
       }



       function registerStates(extensionModule){
           var lazyStates = [];
           if(angular.isDefined(extensionModule.states)) {
               _.each(extensionModule.states, function(state) {
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

       var data = {
           INITIALIZED_EVENT:EXTENSION_MODULES_INITIALIZED_EVENT,
           onInitialized:function(callback){
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
           stateExists:function(stateName) {
             return _.indexOf(stateNames, stateName) > -1;
           },
           urlExists:function(url) {
               var urlMatcher = _.find(urlMatchers,function(matcher){
                   var params = matcher.exec(url);
                   return angular.isObject(params);

               });
               return urlMatcher != undefined;
           },
           /**
            * return the state and associated parameters for a url
            * @param url
            * @return {{state: null, params: null, url: *, valid: boolean}}
            */
           stateAndParamsForUrl:function(url) {
             var data = {state:null,params:null,url:url,valid:false}

              var urlMatcher = _.find(urlMatchers,function(matcher){
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
              promise.then(function (response) {
                   if(response.data){
                       _.each(response.data,function(extensionModule) {
                          registerStates(extensionModule);
                       });
                       initialized=true;
                   }
                  BroadcastService.notify(EXTENSION_MODULES_INITIALIZED_EVENT);
               },function(err){
                  console.log('err',err)
              });
                return promise;
               }

       };

       return data;
   }]);
});
                                                                       

    