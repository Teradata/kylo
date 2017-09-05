define(["angular", "feed-mgr/domain-types/module-name"], function (angular, moduleName) {

    /**
     * Adds or updates domain types.
     *
     * @constructor
     */
    function DomainTypeDetailsController($mdDialog, $mdToast, $scope, $transition$, DomainTypesService, FeedFieldPolicyRuleService, FeedService, FeedTagService, StateService) {
        var self = this;

        /**
         * Standard data types for column definitions
         * @type {Array.<string>}
         */
        self.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();

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
        $scope.$watch(function () {
            return self.editModel.$regexpFlags;
        }, function () {
            self.editModel.regexFlags = _.chain(self.editModel.$regexpFlags)
                .pairs()
                .filter(function (entry) {
                    return entry[1];
                })
                .map(function (entry) {
                    return entry[0];
                })
                .value()
                .join("");
        }, true);

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
         * Provides a list of available tags.
         * @type {FeedTagService}
         */
        self.feedTagService = FeedTagService;

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
        $scope.$watch(function () {
            return self.model;
        }, function () {
            self.model.$regex = "/" + self.model.regexPattern + "/" + self.model.regexFlags;
        });

        /**
         * List of available RegExp flags.
         * @type {Array.<{flag: string, title: string, description: string}>}
         */
        self.regexpFlags = [
            {flag: "i", title: "Ignore case"},
            {flag: "m", title: "Multiline", description: "Treat beginning and end characters (^ and $) as working over multiple lines."},
            {flag: "u", title: "Unicode", description: "Treat pattern as a sequence of unicode code points"}
        ];

        /**
         * Last syntax error when compiling regular expression.
         * @type {string}
         */
        self.regexpSyntaxError = "";

        /**
         * Metadata for the tags.
         * @type {{searchText: null, selectedItem: null}}
         */
        self.tagChips = {searchText: null, selectedItem: null};

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
                    self.regexpSyntaxError = "";
                } catch (err) {
                    container.addClass("md-input-invalid");
                    self.regexpSyntaxError = err.toString();
                }

                if (angular.isDefined(self.domainTypeForm["regexPattern"])) {
                    self.domainTypeForm["regexPattern"].$setValidity("syntaxError", self.regexpSyntaxError.length === 0);
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
         * Updates the field when the data type is changed.
         */
        self.onDataTypeChange = function () {
            if (self.editModel.field.derivedDataType !== "decimal") {
                self.editModel.field.precisionScale = null;
            }
        };

        /**
         * Deletes the current domain type.
         */
        self.onDelete = function () {
            var confirm = $mdDialog.confirm()
                .title("Delete Domain Type")
                .textContent("Are you sure you want to delete this domain type? Columns using this domain type will appear to have no domain type.")
                .ariaLabel("Delete Domain Type")
                .ok("Please do it!")
                .cancel("Nope");
            $mdDialog.show(confirm).then(function () {
                DomainTypesService.deleteById(self.model.id)
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
            });
        };

        /**
         * Creates a copy of the domain type model for editing.
         */
        self.onEdit = function () {
            self.editModel = angular.copy(self.model);

            // Map flags to object
            self.editModel.$regexpFlags = {};
            for (var i = 0; i < self.editModel.regexFlags.length; ++i) {
                self.editModel.$regexpFlags[self.editModel.regexFlags[i]] = true;
            }

            // Ensure tags is array
            if (!angular.isObject(self.editModel.field)) {
                self.editModel.field = {};
            }
            if (!angular.isArray(self.editModel.field.tags)) {
                self.editModel.field.tags = [];
            }
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
                .then(function (savedModel) {
                    self.model = savedModel;
                }, function (err) {
                    self.isEditable = true;
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

        /**
         * Transforms the specified chip into a tag.
         * @param {string} chip the chip
         * @returns {Object} the tag
         */
        self.transformChip = function (chip) {
            return angular.isObject(chip) ? chip : {name: chip};
        };

        // Load the domain type details
        self.onLoad();
    }

    angular.module(moduleName)
        .controller("DomainTypeDetailsController", ["$mdDialog", "$mdToast", "$scope", "$transition$", "DomainTypesService", "FeedFieldPolicyRuleService", "FeedService", "FeedTagService",
                                                    "StateService", DomainTypeDetailsController]);
});
