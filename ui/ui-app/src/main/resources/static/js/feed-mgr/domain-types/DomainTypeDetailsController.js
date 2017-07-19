define(["angular", "feed-mgr/domain-types/module-name"], function (angular, moduleName) {

    /**
     * Adds or updates domain types.
     *
     * @constructor
     */
    function DomainTypeDetailsController($mdDialog, $mdToast, $transition$, DomainTypesService, FeedFieldPolicyRuleService, StateService) {
        var self = this;

        /**
         * Editable section form.
         * @type {Object}
         */
        self.domainTypeForm = {};

        /**
         * Domain type model for the edit view.
         * @type {DomainType}
         */
        self.editModel = {};

        /**
         * CodeMirror editor options.
         */
        self.editorOptions = {
            indentWithTabs: false,
            lineNumbers: false,
            lineWrapping: false,
            matchBrackets: false,
            mode: "regex",
            scrollbarStyle: null,
            smartIndent: false
        };

        /**
         * Indicates if the edit view is displayed.
         * @type {boolean}
         */
        self.isEditable = false;

        /**
         * Indicates that the domain type is currently being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Domain type model for the read-only view.
         * @type {DomainType}
         */
        self.model = {};

        /**
         * Last syntax error when compiling regular expression.
         * @type {string}
         */
        self.regexSyntaxError = "";

        /**
         * CodeMirror viewer options.
         */
        self.viewerOptions = _.defaults({
            readOnly: "nocursor"
        }, self.editorOptions);

        /**
         * Indicates if the domain type can be deleted. The main requirement is that the domain type exists.
         *
         * @returns {boolean} {@code true} if the domain type can be deleted, or {@code false} otherwise
         */
        self.canDelete = function () {
            return !self.isNew();
        };

        /**
         * Configure a CodeMirror instance.
         */
        self.codemirrorLoaded = function (editor) {
            // Show as Angular Material input element
            $(editor.getWrapperElement()).addClass("md-input");

            // Set the width,height of the editor. Code mirror needs an explicit width/height
            editor.setSize("100%", 38);

            // Disable users ability to add new lines. The Formula bar is only 1 line
            editor.on("beforeChange", function (instance, change) {
                var newtext = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
                change.update(change.from, change.to, [newtext]);
                return true;
            });

            // Set class based on value
            editor.on("changes", function () {
                var container = $(editor.getWrapperElement()).parents("md-input-container");
                var value = editor.getValue();

                // Add md-input-has-value class when value is not empty
                if (value.length === 0) {
                    container.removeClass("md-input-has-value");
                } else {
                    container.addClass("md-input-has-value")
                }

                // Add md-input-invalid when regex does not compile
                try {
                    new RegExp(value);
                    container.removeClass("md-input-invalid");
                    self.regexSyntaxError = "";
                } catch (err) {
                    container.addClass("md-input-invalid");
                    self.regexSyntaxError = err.toString();
                }

                if (angular.isDefined(self.domainTypeForm["regex"])) {
                    self.domainTypeForm["regex"].$setValidity("syntaxError", self.regexSyntaxError.length === 0);
                }
            });

            // Add md-input-focused class when focused
            editor.on("blur", function () {
                $(editor.getWrapperElement()).parents("md-input-container").removeClass("md-input-focused")
            });
            editor.on("focus", function () {
                $(editor.getWrapperElement()).parents("md-input-container").addClass("md-input-focused");
            });
        };

        /**
         * Gets a list of all field policies for the specified domain type.
         */
        self.getAllFieldPolicies = function (domainType) {
            return FeedFieldPolicyRuleService.getAllPolicyRules(domainType.fieldPolicy);
        };

        /**
         * Indicates if the specified domain type has any field policies.
         */
        self.hasFieldPolicies = function (domainType) {
            return (domainType.fieldPolicy.standardization.length > 0 || domainType.fieldPolicy.validation.length > 0);
        };

        /**
         * Indicates if this domain type is newly created.
         */
        self.isNew = function () {
            return (!angular.isString(self.model.id) || self.model.id.length === 0);
        };

        /**
         * Cancels the current edit operation. If a new domain type is being created then redirects to the domain types page.
         */
        self.onCancel = function () {
            if (self.isNew()) {
                StateService.FeedManager().DomainType().navigateToDomainTypes();
            }
        };

        /**
         * Deletes the current domain type.
         */
        self.onDelete = function () {
            DomainTypesService.findById(self.model.id)
                .then(function () {
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent("Successfully deleted the domain type " + self.model.title)
                            .hideDelay(3000)
                    );
                    StateService.FeedManager().DomainType().navigateToDomainTypes();
                }, function (err) {
                    $mdDialog.show(
                        $mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Delete Failed")
                            .textContent("The domain type " + self.model.title + " could not be deleted. " + err.data.message)
                            .ariaLabel("Failed to delete domain type")
                            .ok("Got it!")
                    );
                });
        };

        /**
         * Creates a copy of the domain type model for editing.
         */
        self.onEdit = function () {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Loads the domain type details.
         */
        self.onLoad = function () {
            if (angular.isString($transition$.params().domainTypeId)) {
                DomainTypesService.findById($transition$.params().domainTypeId)
                    .then(function (domainType) {
                        self.model = domainType;
                        self.loading = false;
                    });
            } else {
                self.model = DomainTypesService.newDomainType();
                self.loading = false;

                self.onEdit();
                self.isEditable = true;
            }
        };

        /**
         * Saves the current domain type.
         */
        self.onSave = function () {
            var model = angular.copy(self.editModel);
            DomainTypesService.save(model)
                .then(function () {
                    self.model = model;
                }, function (err) {
                    $mdDialog.show(
                        $mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Save Failed")
                            .textContent("The domain type " + self.model.title + " could not be saved. " + err.data.message)
                            .ariaLabel("Failed to save domain type")
                            .ok("Got it!")
                    );
                });
        };

        /**
         * Shows the dialog for updating field policy rules.
         */
        self.showFieldRuleDialog = function (domainType) {
            $mdDialog.show({
                controller: "FeedFieldPolicyRuleDialogController",
                templateUrl: "js/feed-mgr/shared/feed-field-policy-rules/define-feed-data-processing-field-policy-dialog.html",
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: null,
                    field: domainType.fieldPolicy
                }
            });
        };

        /**
         * Shows the icon picker dialog.
         */
        self.showIconPicker = function () {
            $mdDialog.show({
                controller: "IconPickerDialog",
                templateUrl: "js/common/icon-picker-dialog/icon-picker-dialog.html",
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: self.editModel
                }
            }).then(function (msg) {
                if (msg) {
                    self.editModel.icon = msg.icon;
                    self.editModel.iconColor = msg.color;
                }
            });
        };

        // Load the domain type details
        self.onLoad();
    }

    angular.module(moduleName).controller("DomainTypeDetailsController", ["$mdDialog", "$mdToast", "$transition$", "DomainTypesService", "FeedFieldPolicyRuleService", "StateService",
                                                                          DomainTypeDetailsController]);
});
