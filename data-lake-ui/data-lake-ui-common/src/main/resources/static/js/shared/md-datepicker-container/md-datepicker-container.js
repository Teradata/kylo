(function() {
angular.module(COMMON_APP_MODULE_NAME).directive("mdDatepickerContainer", function()  {
    return {
        scope: {label:'@'},
        link: function ($scope, $element, attrs, ctrl) {
        $element.prepend('<label>'+$scope.label+'</label>');

        }
    };
})

}());