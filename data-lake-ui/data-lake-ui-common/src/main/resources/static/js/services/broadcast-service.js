angular.module(COMMON_APP_MODULE_NAME).factory('BroadcastService', function($rootScope) {
    return {
        notify: function(event, data) {
            $rootScope.$emit(event, data);
        },
        subscribe: function(scope, event, callback) {
            var handler = $rootScope.$on(event, callback);
            scope.$on('$destroy', handler);
        }

    }
});