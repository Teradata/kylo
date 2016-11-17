(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                templateId: '=',
                templateName: '=',
                model: '=?',
                addAsNew: '=?',
                addSaveBtn: '=?'
            },
            scope: {},
            controllerAs: 'vm',
            templateUrl: 'js/register-template/template-order.html',
            controller: "TemplateOrderController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };

    function TemplateOrderController($scope, $http, $mdToast, $mdDialog, RestUrlService, StateService, RegisterTemplateService) {
        var self = this;
        self.model = [];
        //order list
        RegisterTemplateService.getRegisteredTemplates().then(function (response) {


            //order by .order
            var templates = _.sortBy(response.data, 'order');
            if (self.addAsNew && (self.templateId == null || self.templateId == undefined)) {
                //append to bottom
                templates.push({id: 'NEW', templateName: self.templateName, currentTemplate: true});
            }
            else {
                var currentTemplate = _.filter(templates, function (template) {
                    return template.id == self.templateId;
                });
                if (currentTemplate && currentTemplate.length == 1) {
                    currentTemplate[0].currentTemplate = true;
                }
            }
            self.model = templates;
        });

        self.saveOrder = function () {

            var order = [];
            _.each(self.model, function (template) {
                order.push(template.id);
            });

            var successFn = function (response) {
                //toast created!!!
                var message = 'Saved the template order';
                self.message = message;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .hideDelay(3000)
                );

            }
            var errorFn = function (err) {
                var message = 'Error ordering templates ' + err;
                self.message = message;
                $mdToast.simple()
                    .textContent(message)
                    .hideDelay(3000);
            }

            var obj = {templateIds: order};
            var promise = $http({
                url: RestUrlService.SAVE_TEMPLATE_ORDER_URL,
                method: "POST",
                data: angular.toJson(obj),
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8'
                }
            }).then(successFn, errorFn);

        }

    }

    angular.module(MODULE_FEED_MGR).controller("TemplateOrderController", TemplateOrderController);
    angular.module(MODULE_FEED_MGR).directive("thinkbigTemplateOrder", directive);
})();