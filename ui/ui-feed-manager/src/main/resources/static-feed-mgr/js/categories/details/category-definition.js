(function() {
    /**
     * Manages the Category Definition section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param $mdDialog the Angular Material dialog service
     * @param $mdToast the Angular Material toast service
     * @param {AccessControlService} AccessControlService the access control service
     * @param CategoriesService the category service
     * @param StateService the URL service
     * @param FeedSecurityGroups the feed security groups service
     * @param FeedService the feed service
     */
    function CategoryDefinitionController($scope, $mdDialog, $mdToast, AccessControlService, CategoriesService, StateService, FeedSecurityGroups, FeedService) {
        var self = this;

        /**
         * Error message object that maps keys to a boolean indicating the error state.
         * @type {{duplicateName: boolean}}
         */
        self.$error = {duplicateName: false};

        /**
         * Indicates if the category definition may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Category data used in "edit" mode.
         * @type {CategoryModel}
         */
        self.editModel = angular.copy(CategoriesService.model);

        /**
         * Indicates if the view is in "edit" mode.
         * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
         */
        self.isEditable = !angular.isString(CategoriesService.model.id);

        /**
         * Indicates if the edit form is valid.
         * @type {boolean}
         */
        self.isValid = false;

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        self.model = CategoriesService.model;

        this.categorySecurityGroups = FeedSecurityGroups;
        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;

        // Update isValid when $error is updated
        $scope.$watch(
                function() {return self.$error},
                function() {
                    self.isValid = _.reduce(self.$error, function(memo, value) {
                        return memo && !value;
                    }, true);
                },
                true
        );

        self.splitSecurityGroups = function() {
            if(self.model.securityGroups) {
                return _.map(self.model.securityGroups, function(securityGroup) { return securityGroup.name}).join(",") ;
            }

        };

        /**
         * Indicates if the category can be deleted.
         * @return {boolean} {@code true} if the category can be deleted, or {@code false} otherwise
         */
        self.canDelete = function() {
            return (angular.isString(self.model.id) && (!angular.isArray(self.model.relatedFeedSummaries) || self.model.relatedFeedSummaries.length === 0));
        };

        /**
         * Returns to the category list page if creating a new category.
         */
        self.onCancel = function() {
            if (!angular.isString(self.model.id)) {
                StateService.navigateToCategories();
            }
        };

        /**
         * Deletes this category.
         */
        self.onDelete = function() {
            var name = self.editModel.name;
            CategoriesService.delete(self.editModel).then(function() {
                CategoriesService.reload();
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Successfully deleted the category ' + name)
                        .hideDelay(3000)
                );
                //redirect
                StateService.navigateToCategories();
            }, function(err) {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Unable to delete the category')
                        .textContent('Unable to delete the category ' + name + ". " + err.data.message)
                        .ariaLabel('Unable to delete the category')
                        .ok('Got it!')
                );
            });
        };

        /**
         * Switches to "edit" mode.
         */
        self.onEdit = function() {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Check for duplicate system names.
         */
        self.onNameChange = function() {
            if (!angular.isString(self.model.systemName)) {
                FeedService.getSystemName(self.editModel.name)
                        .then(function(response) {
                            var systemName = response.data;
                            self.$error.duplicateName = _.some(CategoriesService.categories, function(category) {
                                return (category.systemName === systemName);
                            });
                        });
            } else {
                self.$error.duplicateName = false;
            }
        };

        /**
         * Saves the category definition.
         */
        self.onSave = function() {
            var model = angular.copy(CategoriesService.model);
            model.name = self.editModel.name;
            model.description = self.editModel.description;
            model.icon = self.editModel.icon;
            model.iconColor = self.editModel.iconColor;
            model.userProperties = (self.model.id === null) ? self.editModel.userProperties : null;
            model.securityGroups = self.editModel.securityGroups;

            CategoriesService.save(model).then(function(response) {
                CategoriesService.reload();
                self.model = CategoriesService.model = response.data;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
            });
        };

        /**
         * Shows the icon picker dialog.
         */
        self.showIconPicker = function() {
            var self = this;
            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/shared/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: self.editModel
                }
            })
            .then(function(msg) {
                if (msg) {
                    self.editModel.icon = msg.icon;
                    self.editModel.iconColor = msg.color;
                }
            });
        };

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_EDIT, actionSet.actions);
                });

        // Fetch the existing categories
        CategoriesService.reload().then(self.onNameChange);

        // Watch for changes to name
        $scope.$watch(
                function() {return self.editModel.name},
                self.onNameChange
        );
    }

    /**
     * Creates a directive for the Category Definition section.
     *
     * @returns {Object} the directive
     */
    function thinkbigCategoryDefinition() {
        return {
            controller: "CategoryDefinitionController",
            controllerAs: "vm",
            restrict: "E",
            scope: {},
            templateUrl: "js/categories/details/category-definition.html"
        };
    }

    angular.module(MODULE_FEED_MGR).controller('CategoryDefinitionController', CategoryDefinitionController);
    angular.module(MODULE_FEED_MGR).directive('thinkbigCategoryDefinition', thinkbigCategoryDefinition);
})();
