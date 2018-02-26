define(["require", "exports", "angular", "../module-name", "kylo-common-module", "kylo-services"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
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
        } while (element !== endNode);
        return angular.element(elements);
    };
    angular.module(module_name_1.moduleName).directive("ngIfPermission", ['$animate', '$compile', '$q', 'AccessControlService', function ($animate, $compile, $q, AccessControlService) {
            return {
                scope: { entity: '=?', entityType: '=?' },
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
                            check(permissions, $scope.entity, $scope.entityType);
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
                            validate(this.AccessControlService.hasEntityAccess(permissions, entity, entityType));
                        }
                        else {
                            this.AccessControlService.getUserAllowedActions(this.AccessControlService.ACCESS_MODULES.SERVICES, true)
                                .then(function (actionSet) {
                                var valid = this.AccessControlService.hasAnyAction(permissions, actionSet.actions);
                                validate(valid);
                            }, true);
                        }
                    }
                    function validate(valid) {
                        this.$q.when(valid).then(function (isValid) {
                            if (isValid) {
                                if (!childScope) {
                                    $transclude(function (clone, newScope) {
                                        childScope = newScope;
                                        clone[clone.length++] = this.$compile.$$createComment('end ngIfPermission', $attr.ngIfPermission);
                                        // Note: We only need the first/last node of the cloned nodes.
                                        // However, we need to keep the reference to the jqlite wrapper as it might be changed later
                                        // by a directive with templateUrl when its template arrives.
                                        block = {
                                            clone: clone
                                        };
                                        this.$animate.enter(clone, $element.parent(), $element);
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
                                    this.$animate.leave(previousElements).done(function (response) {
                                        if (response !== false)
                                            previousElements = null;
                                    });
                                    block = null;
                                }
                            }
                        });
                    }
                }
            };
        }
    ]);
});
//# sourceMappingURL=ng-if-permission.js.map