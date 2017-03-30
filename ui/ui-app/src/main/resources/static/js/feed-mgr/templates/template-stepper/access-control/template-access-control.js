define(['angular',"feed-mgr/templates/module-name"], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                stepIndex: '@'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/templates/template-stepper/access-control/template-access-control.html',
            controller: "TemplateAccessControlController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };

    function TemplateAccessControlController($scope,RegisterTemplateService) {

        /**
         * ref back to this controller
         * @type {TemplateAccessControlController}
         */
        var self = this;

        this.templateAccessControlForm = {};

        this.model = RegisterTemplateService.model;




    }

    angular.module(moduleName).controller("TemplateAccessControlController",["$scope","RegisterTemplateService", TemplateAccessControlController]);

    angular.module(moduleName).directive("thinkbigTemplateAccessControl", directive);
});

