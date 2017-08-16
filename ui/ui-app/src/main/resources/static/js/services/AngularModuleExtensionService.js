define(['angular', 'services/module-name', 'constants/AccessConstants', 'kylo-services-module'], function (angular, moduleName, AccessConstants) {

    return angular.module(moduleName).factory("AngularModuleExtensionService", ["$http", "$q", "$timeout", "$uiRouter", "CommonRestUrlService", "UserGroupService",
                                                                       function ($http, $q, $timeout, $uiRouter, CommonRestUrlService, UserGroupService) {

                                                                           function buildLazyRoute(state,extensionModule) {
                                                                                var lazyState = {
                                                                                    name:state.state+".**",
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



                                                                           function registerStates(extensionModule){
                                                                               var lazyStates = [];
                                                                               if(angular.isDefined(extensionModule.states)) {
                                                                                   _.each(extensionModule.states, function(state) {
                                                                                       $uiRouter.stateRegistry.register(buildLazyRoute(state, extensionModule));
                                                                                   });
                                                                               }
                                                                           }

                                                                           var initialized = false;

                                                                           var data = {

                                                                               isInitialized:function(){
                                                                                   return initialized;
                                                                               },
                                                                               /**
                                                                                * Registers the state with angular.
                                                                                * Returns the promise
                                                                                */
                                                                               registerModules:function(){
                                                                                  return $http.get(CommonRestUrlService.ANGULAR_EXTENSION_MODULES_URL).then(function (response) {
                                                                                       if(response.data){
                                                                                           _.each(response.data,function(extensionModule) {
                                                                                              registerStates(extensionModule);
                                                                                           });
                                                                                           initialized=true;
                                                                                       }
                                                                                   },function(err){
                                                                                      console.log('err',err)
                                                                                  });
                                                                                       
                                                                                   }
                                                                                       
                                                                           };

                                                                           return data;
                                                                       }]);
});
                                                                       

    