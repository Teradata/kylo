import {Output, Component, Input, ElementRef, SimpleChanges, EventEmitter} from "@angular/core";
import * as $ from "jquery";
/**
 * Provides an editor for regular expressions.
 */
@Component({
    selector: 'regexp-editor',
    templateUrl: './regexp-editor.component.html',
    styleUrls: ['./regexp-editor.component.scss']
})
export class RegExpEditorComponent {

    /**
     * Parent input container controller.
     */
    @Input() public container: any;

    /**
     * Model controller.
     */
    @Input() public model: any;

    /**
     * Callback for syntax errors.
     */
    @Output() ("on-syntax-error") onSyntaxError: (args: { error: Error }) => void;

    @Output() modelChange: EventEmitter<any> = new EventEmitter<any>();
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

    constructor(private element: ElementRef) {
        this.isReadonly = (this.element.nativeElement.attributes["readonly"]);
        if (this.isReadonly) {
            this.options.readOnly = "nocursor";
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.model && changes.model.currentValue && !changes.model.firstChange) {
            this.onRender();
        }
    }

    /**
     * Reset container when this component is destroyed.
     */
    ngOnDestroy() {
        if (this.container) {
            this.container.setFocused(false);
            this.container.setHasValue(false);
            this.container.input = null;
        }
    }

    /**
     * Register with parent controllers.
     */
    ngOnInit() {
        // Check for mdInputContainer parent
        if (this.container) {
            this.container.input = this.element.nativeElement;

            // Watch for errors
            // this.$scope.$watch(() => {
            //     return this.container.isErrorGetter ? this.container.isErrorGetter() : this.model.$invalid;
            // }, isError => {
            //     this.container.setInvalid(isError);
            // });
        }

        // Check for ngModel parent
        if (this.model) {
            this.onRender();
        }
    }

    /**
     * Update the model when the flags or pattern are changed.
     */
    onChange(newPattern: string = null) {
        event.stopPropagation();
        if (!this.isReadonly) {
            const pattern = (newPattern !== null) ? newPattern : this.pattern;
            const flags = Object.keys(this.selectedFlags).filter(flag => this.selectedFlags[flag]).join("");
            this.model = pattern.length > 0 ? new RegExp(pattern, flags) : null;
        }
    }

    /**
     * Configure a CodeMirror instance.
     */
    onLoaded(editor: CodeMirror.Editor) {
        // Show as Angular Material input element
        $(editor.getWrapperElement()).addClass("md-input");

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
            this.pattern = this.model ? this.model.toString() : "";
        } else if (this.model) {
            this.pattern = this.model.source;
            this.selectedFlags = {};
            this.model.flags.split("").forEach((flag: string) => this.selectedFlags[flag] = true);
        } else {
            this.pattern = "";
            this.selectedFlags = {};
        }
    }

    patternChange(event: any) {
        if (this.model.source != this.pattern) {
            let newModel = new RegExp(this.pattern, this.model.fieldNameFlags)
            this.modelChange.emit(newModel);
        }
    }
}


