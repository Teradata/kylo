var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
define(["require", "exports", "@angular/core", "angular"], function (require, exports, core_1, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Provides an editor for regular expressions.
     */
    var RegExpEditorComponent = /** @class */ (function () {
        function RegExpEditorComponent($element, $scope) {
            this.$element = $element;
            this.$scope = $scope;
            /**
             * List of available RegExp flags.
             */
            this.availableFlags = [
                { flag: "i", title: "Ignore case" },
                { flag: "m", title: "Multiline", description: "Treat beginning and end characters (^ and $) as working over multiple lines." },
                { flag: "u", title: "Unicode", description: "Treat pattern as a sequence of unicode code points" }
            ];
            /**
             * Indicates that the expression cannot be edited.
             */
            this.isReadonly = false;
            /**
             * CodeMirror editor options.
             */
            this.options = {
                indentWithTabs: false,
                lineNumbers: false,
                lineWrapping: false,
                matchBrackets: false,
                mode: "regex",
                scrollbarStyle: null,
                smartIndent: false
            };
            /**
             * Regular expression pattern.
             */
            this.pattern = "";
            /**
             * Selected RegExp flags.
             */
            this.selectedFlags = {};
            this.isReadonly = ($element.attr("readonly") === "readonly");
            if (this.isReadonly) {
                this.options.readOnly = "nocursor";
            }
        }
        /**
         * Reset container when this component is destroyed.
         */
        RegExpEditorComponent.prototype.$onDestroy = function () {
            if (this.container) {
                this.container.setFocused(false);
                this.container.setHasValue(false);
                this.container.input = null;
            }
        };
        /**
         * Register with parent controllers.
         */
        RegExpEditorComponent.prototype.$onInit = function () {
            var _this = this;
            // Check for mdInputContainer parent
            if (this.container) {
                this.container.input = this.$element;
                // Watch for errors
                this.$scope.$watch(function () {
                    return _this.container.isErrorGetter ? _this.container.isErrorGetter() : _this.model.$invalid;
                }, function (isError) {
                    _this.container.setInvalid(isError);
                });
            }
            // Check for ngModel parent
            if (this.model) {
                this.model.$render = this.onRender.bind(this);
            }
        };
        /**
         * Update the model when the flags or pattern are changed.
         */
        RegExpEditorComponent.prototype.onChange = function (newPattern) {
            var _this = this;
            if (newPattern === void 0) { newPattern = null; }
            if (!this.isReadonly) {
                var pattern = (newPattern !== null) ? newPattern : this.pattern;
                var flags = Object.keys(this.selectedFlags).filter(function (flag) { return _this.selectedFlags[flag]; }).join("");
                this.model.$setViewValue(pattern.length > 0 ? new RegExp(pattern, flags) : null);
            }
        };
        /**
         * Configure a CodeMirror instance.
         */
        RegExpEditorComponent.prototype.onLoaded = function (editor) {
            var _this = this;
            // Show as Angular Material input element
            angular.element(editor.getWrapperElement()).addClass("md-input");
            // Set the width,height of the editor. Code mirror needs an explicit width/height
            editor.setSize("100%", 38);
            // Disable users ability to add new lines. The Formula bar is only 1 line
            editor.on("beforeChange", function (instance, change) {
                var value = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
                change.update(change.from, change.to, [value]);
                return true;
            });
            // Set class based on value
            editor.on("changes", function () {
                // Check value
                var value = editor.getValue();
                // Check for compile error
                var error = null;
                try {
                    new RegExp(value);
                }
                catch (err) {
                    error = err;
                }
                // Update state
                if (_this.container) {
                    _this.container.setHasValue(value.length > 0);
                }
                if (!_this.isReadonly && error === null) {
                    _this.onChange(value);
                }
                if (_this.model) {
                    _this.model.$setValidity("syntaxError", error === null);
                }
                if (_this.onSyntaxError && error !== null) {
                    _this.onSyntaxError({ error: error });
                }
            });
            // Add md-input-focused class when focused
            editor.on("blur", function () {
                if (!_this.isReadonly && _this.container) {
                    _this.container.setFocused(false);
                }
            });
            editor.on("focus", function () {
                if (!_this.isReadonly && _this.container) {
                    _this.container.setFocused(true);
                }
            });
        };
        /**
         * Update the editor when the model changes.
         */
        RegExpEditorComponent.prototype.onRender = function () {
            var _this = this;
            if (this.isReadonly) {
                this.pattern = this.model.$viewValue ? this.model.$viewValue.toString() : "";
            }
            else if (this.model.$viewValue) {
                this.pattern = this.model.$viewValue.source;
                this.selectedFlags = {};
                this.model.$viewValue.flags.split("").forEach(function (flag) { return _this.selectedFlags[flag] = true; });
            }
            else {
                this.pattern = "";
                this.selectedFlags = {};
            }
        };
        RegExpEditorComponent.$inject = ["$element", "$scope"];
        __decorate([
            core_1.Output(),
            __metadata("design:type", Function)
        ], RegExpEditorComponent.prototype, "onSyntaxError", void 0);
        return RegExpEditorComponent;
    }());
    exports.RegExpEditorComponent = RegExpEditorComponent;
    angular.module(require("feed-mgr/domain-types/module-name"))
        .component("regexpEditor", {
        bindings: {
            onSyntaxError: "&"
        },
        controller: RegExpEditorComponent,
        require: {
            container: "^?mdInputContainer",
            model: "?ngModel"
        },
        template: "\n          <div layout=\"row\">\n            <div flex=\"95\" ng-model=\"$ctrl.pattern\" rows=\"1\" ui-codemirror=\"{onLoad: $ctrl.onLoaded.bind($ctrl)}\" ui-codemirror-opts=\"$ctrl.options\"></div>\n            <md-menu flex=\"5\" ng-if=\"!$ctrl.isReadonly\">\n              <md-button class=\"md-icon-button md-accent\" arial-label=\"Open expression flags menu\" ng-click=\"$mdMenu.open($event)\">\n                <ng-md-icon icon=\"flag\"></ng-md-icon>\n              </md-button>\n              <md-menu-content>\n                <md-menu-item ng-repeat=\"flag in $ctrl.availableFlags\">\n                  <md-list-item title=\"{{flag.description}}\">\n                    <md-checkbox ng-model=\"$ctrl.selectedFlags[flag.flag]\" ng-change=\"$ctrl.onChange()\"></md-checkbox>\n                    <p>{{flag.title}}</p>\n                  </md-list-item>\n                </md-menu-item>\n              </md-menu-content>\n            </md-menu>\n          </div>\n        "
    });
});
//# sourceMappingURL=regexp-editor.component.js.map