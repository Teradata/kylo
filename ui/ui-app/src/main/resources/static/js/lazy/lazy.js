define(['angular','@uirouter/angularjs'], function (angular) {
    var  lazyModule = angular.module('lazy', ['ui.router']);


lazyModule.config(function($stateProvider) {
    $stateProvider.state('lazy', {
    url: '/lazy',
        views: {
            "content": {
                component: 'lazyComponent'
            }
        }
});

$stateProvider.state('lazy.foo', {
    url: '/foo',
    component: 'fooComponent',
    resolve: { fooData:function() {"Some foo resolve data" }}
});

$stateProvider.state('lazy.bar', {
    url: '/bar',
    component: 'barComponent',
    resolve: { serviceData: function(lazyService) { return lazyService.getServiceData(); } }
});
});

lazyModule.service('lazyService', function($http) {
    this.getServiceData = function() {
        return $http.get('serviceData.json').then(function(resp) { return resp.data;});
    }
})


lazyModule.component('lazyComponent', {
    template: '  <h1>Lazy Module component!</h1> <a ui-sref="lazy.foo">Foo</a><br> <a ui-sref="lazy.bar">Bar</a><br>  <ui-view></ui-view>'
});

lazyModule.component('fooComponent', {
    bindings: { fooData: '<' },
    template: '<h3>The foo component</h3> {{ $ctrl.fooData }}'
});

lazyModule.component('barComponent', {
    bindings: { serviceData: '<' },
    template: '<h3>The bar component</h3> Data from lazy service:  <pre>{{ $ctrl.serviceData | json }}</pre>'
});

});
