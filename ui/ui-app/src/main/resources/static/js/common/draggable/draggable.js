define(['angular','common/module-name','draggabilly'], function (angular,moduleName, Draggabilly) {

    angular.module(moduleName).directive('ngDraggable', function () {
        return {
            restrict: 'A',
            scope: {
                dragOptions: '=ngDraggable'
            },
            link: function (scope, elem, attr) {
                if (scope.dragOptions == undefined) {
                    scope.dragOptions = {};
                }
                var draggable = new Draggabilly(elem[0], scope.dragOptions);
            }
        }

    })
});
