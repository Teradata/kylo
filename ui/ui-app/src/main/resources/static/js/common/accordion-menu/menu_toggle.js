define(['angular','common/module-name'], function (angular,moduleName) {
    angular.module(moduleName)
        .run(['$templateCache', function ($templateCache) {
            $templateCache.put('menu-toggle.tmpl.html',
                '<div style="width:100%;background-color:#F9F9F9;" flex layout-align="start space-between" layout="column">\n'+
                '<md-button class="md-button-toggle" '+
                '  ng-click="toggle()"\n' +
                '  aria-controls="docs-menu-{{section.text}}"\n' +
                '  flex layout="row"\n' +
                '  aria-expanded="{{isOpen()}}" ng-if="section.hidden == false">\n' +
                ' <span class="toggle-label layout-padding-left-8" flex>{{section.text}}</span> ' +
                '  <ng-md-icon md-icon icon="{{section.expandIcon}}" ng-if="!isCollapsed()"></ng-md-icon>'+
                '<span flex="5"></span>' +
                '</md-button>' +
                '</div>\n' +
                '<md-list ng-show="isOpen()" id="docs-menu-{{section.text}}" class="menu-toggle-list fx-fade-up fx-dur-600 fx-ease-none" >\n' +
                '  <md-list-item ng-repeat="item in section.links" ng-if-permission="{{item.permission}}">\n' +
                '    <menu-link section="item"></menu-link>\n' +
                '  </md-list-item>\n' +
                '</md-list>\n' +
                '');
        }])
        .directive('menuToggle', ['$timeout','AccessControlService', function ($timeout,AccessControlService) {
            return {
                scope: {
                    section: '='
                },
                require: '^accordionMenu',
                templateUrl: 'menu-toggle.tmpl.html',
                link: function (scope, element,attrs,controller) {

                    scope.section.hidden = true;
                    scope.$watch('section.expanded',function(newVal,oldVal){
                        if(newVal == true){
                            scope.section.expandIcon = 'expand_less';
                        }
                        else {
                            scope.section.expandIcon = 'expand_more';
                        }
                    });

                    scope.isCollapsed= controller.isCollapsed;

                    scope.$watch('section.collapsed',function(newVal,oldVal){
                        if(newVal == true){
                            element.find('.toggle-label').addClass('collapsed-toggle-header').removeClass('layout-padding-left-8');
                            element.find('.menu-link').removeClass('layout-padding-left-8');
                        }
                        else {
                            element.find('.toggle-label').removeClass('collapsed-toggle-header').addClass('layout-padding-left-8')
                            element.find('.menu-link').addClass('layout-padding-left-8');
                        }
                    })

                    var checkPermissions = function(){
                        AccessControlService.doesUserHavePermission(getTogglePermissions()).then(function(allowed){
                            //if not allowed, remove the links;
                            if(!allowed){
                                scope.section.links = [];
                            }

                            scope.section.hidden = !allowed;
                        })
                    }


                    var getTogglePermissions = function(){
                            var allPermissions = [];
                            _.each(scope.section.links,function(item){
                                var permissionStr = item.permission;
                                var arr = permissionStr.split(',');
                                allPermissions = _.union(allPermissions,arr);
                            });
                            return allPermissions;
                    }
                    checkPermissions();


                    scope.isOpen = function () {
                        return controller.isOpen(scope.section);
                    };
                    scope.toggle = function () {
                        controller.toggleOpen(scope.section);
                    };

                }
            };
        }]);
});