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

    function TemplateAccessControlController($scope,RegisterTemplateService,AccessControlService, EntityAccessControlService) {

        /**
         * ref back to this controller
         * @type {TemplateAccessControlController}
         */
        var self = this;

        this.templateAccessControlForm = {};

        this.model = RegisterTemplateService.model;

        this.allowEdit = false;

        //allow edit if the user has ability to change permissions on the entity if its an existing registered template, or if it is new
        if(this.model.id && RegisterTemplateService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.CHANGE_TEMPLATE_PERMISSIONS)){
            self.allowEdit = true;
        }
        else {
            self.allowEdit = this.model.id == undefined;
        }




    }

    angular.module(moduleName).controller("TemplateAccessControlController",["$scope","RegisterTemplateService", "AccessControlService","EntityAccessControlService",TemplateAccessControlController]);

    angular.module(moduleName).directive("thinkbigTemplateAccessControl", directive);
});

