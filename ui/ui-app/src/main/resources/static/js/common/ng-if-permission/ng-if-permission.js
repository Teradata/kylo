/**
 * Add as an attribute to any element to show/hide based upon the users allowed permissions
 *
 * <div ng-if-permission="a_permission">
 *
 * For multiple permissions to check on as an "OR" clause separate with a comma
 * Below will render if the current user has either a_permission or b_permission
 *
 * <div ng-if-permission="a_permission, b_permission">
 *
 *
 * For Entity Level accesc you can also supplie the Access Controlled Entity
 *
 *
 * <div ng-if-permission="a_permission, b_permission", entity="vm.feedModel">
 *
 */
define(['angular','common/module-name','kylo-common-module','kylo-services'], function (angular,moduleName) {

    var getBlockNodes = function (nodes) {
        if (!nodes || !nodes.length) {
            return angular.element();
        }

        var startNode = nodes[0];
        var endNode = nodes[nodes.length - 1];

        if (startNode === endNode) {
            return angular.element(startNode);
        }

        var element = startNode;
        var elements = [element];

        do {
            element = element.nextSibling;
            if (!element) {
                break;
            }
            elements.push(element);
        }
        while (element !== endNode);

        return angular.element(elements);
    };

    var directive = ['$animate', '$compile','$q', 'AccessControlService', function ($animate, $compile, $q,AccessControlService) {
        return {
            scope: {entity:'=?', entityType:'=?'},
            multiElement: true,
            transclude: 'element',
         //   priority: 600,
            terminal: true,
            restrict: 'A',
            $$tlb: true,
            link: function ($scope, $element, $attr, ctrl, $transclude) {
                var block, childScope, previousElements;
                $attr.$observe('ngIfPermission', function (value, old) {
                    var value2 = $attr.ngIfPermission;

                    if (value != undefined) {

                        var permissions = value.split(',');

                        check(permissions, $scope.entity, $scope.entityType)
                    }
                    else {
                        validate(true);
                    }
                });

                /**
                 *
                 * @param permissions array of permissions needed (only 1 is needed)
                 * @param entity optional entity
                 */
                function check(permissions, entity, entityType) {

                    if (angular.isDefined(entity) && angular.isDefined(entityType)) {
                        validate(AccessControlService.hasEntityAccess(permissions, entity,entityType));
                    }
                    else {
                        AccessControlService.getUserAllowedActions(AccessControlService.ACCESS_MODULES.SERVICES, true)
                            .then(function (actionSet) {
                                var valid = AccessControlService.hasAnyAction(permissions, actionSet.actions);
                                validate(valid);
                            }, true);
                    }

                }

                function validate(valid) {
                    $q.when(valid).then(function (isValid) {
                        if (isValid) {
                            if (!childScope) {
                                $transclude(function (clone, newScope) {
                                    childScope = newScope;
                                    clone[clone.length++] = $compile.$$createComment('end ngIfPermission', $attr.ngIfPermission);
                                    // Note: We only need the first/last node of the cloned nodes.
                                    // However, we need to keep the reference to the jqlite wrapper as it might be changed later
                                    // by a directive with templateUrl when its template arrives.
                                    block = {
                                        clone: clone
                                    };
                                    $animate.enter(clone, $element.parent(), $element);
                                });
                            }
                        }
                        else {
                            if (previousElements) {
                                previousElements.remove();
                                previousElements = null;
                            }
                            if (childScope) {
                                childScope.$destroy();
                                childScope = null;
                            }
                            if (block) {
                                previousElements = getBlockNodes(block.clone);
                                $animate.leave(previousElements).done(function (response) {
                                    if (response !== false) previousElements = null;
                                });
                                block = null;
                            }
                        }
                    });

                }
            }
        };
    }];

    angular.module(moduleName)
        .directive('ngIfPermission', directive);
});
