import * as angular from "angular";
import {moduleName} from "../module-name";
import 'kylo-common-module';
import 'kylo-services';


var getBlockNodes = function (nodes: any) {
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

angular.module(moduleName).directive("ngIfPermission",
  ['$animate', '$compile','$q', 'AccessControlService', ($animate: any, $compile: any, $q,AccessControlService: any) => {
          return {
               scope: {entity:'=?', entityType:'=?'},
            multiElement: true,
            transclude: 'element',
         //   priority: 600,
            terminal: true,
            restrict: 'A',
            $$tlb: true,
            link: function ($scope: any, $element: any, $attr: any, ctrl: any, $transclude: any) {
                var block: any, childScope: any, previousElements: any;
                $attr.$observe('ngIfPermission', function (value: any, old: any) {
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
                function check(permissions: any, entity: any, entityType: any) {

                    if (angular.isDefined(entity) && angular.isDefined(entityType)) {
                        validate(AccessControlService.hasEntityAccess(permissions, entity,entityType));
                    }
                    else {
                        AccessControlService.getUserAllowedActions(AccessControlService.ACCESS_MODULES.SERVICES, true)
                            .then(function (actionSet: any) {
                                var valid = AccessControlService.hasAnyAction(permissions, actionSet.actions);
                                validate(valid);
                            }, true);
                    }

                }

                function validate(valid: any) {
                    $q.when(valid).then(function (isValid: any) {
                        if (isValid) {
                            if (!childScope) {
                                $transclude(function (clone: any, newScope: any) {
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
                                $animate.leave(previousElements).done(function (response: any) {
                                    if (response !== false) previousElements = null;
                                });
                                block = null;
                            }
                        }
                    });

                }
            }
        }
  }
  ]);
