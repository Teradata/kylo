import {Output} from "@angular/core";
import * as angular from "angular";
import {moduleName} from "../../module-name";

/**
 * Provides an editor for regular expressions.
 */
export class RegExpEditorComponent {

    /**
     * Parent input container controller.
     */
    public container: any;

    /**
     * Model controller.
     */
    public model: angular.INgModelController;

    /**
     * Callback for syntax errors.
     */
    @Output()
    public onSyntaxError: (args: { error: Error }) => void;

    /**
     * List of available RegExp flags.
     */
    availableFlags = [
        {flag: "i", title: "Ignore case"},
        {flag: "m", title: "Multiline", description: "Treat beginning and end characters (^ and $) as working over multiple lines."},
        {flag: "u", title: "Unicode", description: "Treat pattern as a sequence of unicode code points"}
    ];

    /**
     * Indicates that the expression cannot be edited.
     */
    isReadonly: boolean = false;

    /**
     * CodeMirror editor options.
     */
    options: CodeMirror.EditorConfiguration = {
        indentWithTabs: false,
        lineNumbers: false,
        lineWrapping: false,
        matchBrackets: false,
        mode: "regex",
        scrollbarStyle: null,
        smartIndent: false
    } as any;

    /**
     * Regular expression pattern.
     */
    pattern: string = "";

    /**
     * Selected RegExp flags.
     */
    selectedFlags: { [k: string]: boolean } = {};

    static readonly $inject: string[] = ["$element", "$scope"];

    constructor(private $element: angular.IAugmentedJQuery, private $scope: angular.IScope) {
        this.isReadonly = ($element.attr("readonly") === "readonly");
        if (this.isReadonly) {
            this.options.readOnly = "nocursor";
        }
    }

    /**
     * Reset container when this component is destroyed.
     */
    $onDestroy() {
        if (this.container) {
            this.container.setFocused(false);
            this.container.setHasValue(false);
            this.container.input = null;
        }
    }

    /**
     * Register with parent controllers.
     */
    $onInit() {
        // Check for mdInputContainer parent
        if (this.container) {
            this.container.input = this.$element;

            // Watch for errors
            this.$scope.$watch(() => {
                return this.container.isErrorGetter ? this.container.isErrorGetter() : this.model.$invalid;
            }, isError => {
                this.container.setInvalid(isError);
            });
        }

        // Check for ngModel parent
        if (this.model) {
            this.model.$render = this.onRender.bind(this);
        }
    }

    /**
     * Update the model when the flags or pattern are changed.
     */
    onChange(newPattern: string = null) {
        if (!this.isReadonly) {
            const pattern = (newPattern !== null) ? newPattern : this.pattern;
            const flags = Object.keys(this.selectedFlags).filter(flag => this.selectedFlags[flag]).join("");
            this.model.$setViewValue(pattern.length > 0 ? new RegExp(pattern, flags) : null);
        }
    }

    /**
     * Configure a CodeMirror instance.
     */
    onLoaded(editor: CodeMirror.Editor) {
        // Show as Angular Material input element
        angular.element(editor.getWrapperElement()).addClass("md-input");

        // Set the width,height of the editor. Code mirror needs an explicit width/height
        editor.setSize("100%", 38);

        // Disable users ability to add new lines. The Formula bar is only 1 line
        editor.on("beforeChange", (instance, change) => {
            const value = change.text.join("").replace(/\n/g, ""); // remove ALL \n !
            change.update(change.from, change.to, [value]);
            return true;
        });

        // Set class based on value
        editor.on("changes", () => {
            // Check value
            const value = editor.getValue();

            // Check for compile error
            let error = null;

            try {
                new RegExp(value);
            } catch (err) {
                error = err;
            }

            // Update state
            if (this.container) {
                this.container.setHasValue(value.length > 0);
            }
            if (!this.isReadonly && error === null) {
                this.onChange(value);
            }
            if (this.model) {
                this.model.$setValidity("syntaxError", error === null);
            }
            if (this.onSyntaxError && error !== null) {
                this.onSyntaxError({error: error});
            }
        });

        // Add md-input-focused class when focused
        editor.on("blur", () => {
            if (!this.isReadonly && this.container) {
                this.container.setFocused(false);
            }
        });
        editor.on("focus", () => {
            if (!this.isReadonly && this.container) {
                this.container.setFocused(true);
            }
        });
    }

    /**
     * Update the editor when the model changes.
     */
    onRender() {
        if (this.isReadonly) {
            this.pattern = this.model.$viewValue ? this.model.$viewValue.toString() : "";
        } else if (this.model.$viewValue) {
            this.pattern = this.model.$viewValue.source;
            this.selectedFlags = {};
            this.model.$viewValue.flags.split("").forEach((flag: string) => this.selectedFlags[flag] = true);
        } else {
            this.pattern = "";
            this.selectedFlags = {};
        }
    }
}

angular.module(moduleName)
    .component("regexpEditor", {
        bindings: {
            onSyntaxError: "&"
        },
        controller: RegExpEditorComponent,
        require: {
            container: "^?mdInputContainer",
            model: "?ngModel"
        },
        template: `
          <div layout="row">
            <div flex="95" ng-model="$ctrl.pattern" rows="1" ui-codemirror="{onLoad: $ctrl.onLoaded.bind($ctrl)}" ui-codemirror-opts="$ctrl.options"></div>
            <md-menu flex="5" ng-if="!$ctrl.isReadonly">
              <md-button class="md-icon-button md-accent" arial-label="Open expression flags menu" ng-click="$mdMenu.open($event)">
                <ng-md-icon icon="flag"></ng-md-icon>
              </md-button>
              <md-menu-content>
                <md-menu-item ng-repeat="flag in $ctrl.availableFlags">
                  <md-list-item title="{{flag.description}}">
                    <md-checkbox ng-model="$ctrl.selectedFlags[flag.flag]" ng-change="$ctrl.onChange()"></md-checkbox>
                    <p>{{flag.title}}</p>
                  </md-list-item>
                </md-menu-item>
              </md-menu-content>
            </md-menu>
          </div>
        `
    });
