(function () {

    angular.module(MODULE_FEED_MGR).directive('ngDraggable', function ($document) {
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
})();
