define(['angular','common/module-name'], function (angular,moduleName) {
    angular.module(moduleName)
        .run(['$templateCache', function ($templateCache) {

            $templateCache.put('menu-toggle.tmpl.html',
            '<div class="collapsible-item" ng-class="{open: section.expanded}" ng-if="section.hidden == false" id="{{section.elementId}}">'
            + '<div class="title" ng-class="{disabled: section.disabled}" ng-click="toggle()" flex layout-align="start start" layout="row">'
            + '   <span flex>{{section.text}}</span>'
            + '   <ng-md-icon md-icon icon="{{section.expandIcon}}" ng-if="!isCollapsed()"></ng-md-icon>'
            + '</div>'
            + ' <div class="accordion-body">'
            + ' <md-list id="menu-{{section.text}}" class="accordion-list">\n'
            + '  <md-list-item ng-repeat="item in section.links" ng-if-permission="{{item.permission}}">\n'
            + '    <menu-link section="item"></menu-link>\n'
            + '  </md-list-item>\n'
            + '</md-list> '
            + ' </div>'
            + '</div>');






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

                    scope.isOpened = scope.section.expanded;


                    if(scope.isOpened) {
                        scope.section.expandIcon = 'expand_less';
                    }
                    else {
                        scope.section.expandIcon = 'expand_more';
                    }


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
                                if(permissionStr != undefined) {
                                    var arr = [];
                                    if(angular.isArray(permissionStr)) {
                                        arr = permissionStr;
                                        //the directive template uses the permission key to check.  it needs to be a string.
                                        item.permission = arr.join(",")
                                    }
                                    else {
                                        arr = permissionStr.split(',');
                                    }
                                    allPermissions = _.union(allPermissions, arr);
                                }
                            });
                            return allPermissions;
                    }
                    checkPermissions();



                    scope.toggle = function () {
                        if(!scope.section.expanded) {
                            controller.openToggleItem(scope.section);
                        }
                        else {
                            scope.section.expanded = false;
                            scope.section.expandIcon = 'expand_more';
                        }

                    };

                }
            };
        }]);
});