define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    return angular.module(moduleName).factory('VisualQueryService', function () {

        var data = {

            model: {},
            resetModel: function () {
                this.model = {};
            }

        };
        return data;

    });
});