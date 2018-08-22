import * as angular from "angular";
import {DialogService} from "./api/services/dialog.service";

export class WranglerFormField {
    builder : DialogBuilder;
    name: string;
    label: string;
    type: string;
    tip: string;
    validation: string;
    validationText: string;
    required: boolean;
    showIf: string;
    choices: string[];
    value:any;
    validator: any;

    constructor(type:string, name: string) {
        this.type = type;
        this.name = name;
        this.required = true;
    }

    optional() : WranglerFormField {
        this.required = false;
        return this;
    }

    // A custom validator function taking the value as an argumant
    withValidator(cb : any) : WranglerFormField {
        this.validator = cb;
        return this;
    }

    withTip(tip : string) : WranglerFormField {
        this.tip = tip;
        return this;
    }

    withValidation(pattern : string, message: string) : WranglerFormField {
        this.validation = pattern;
        this.validationText = message;
        return this;
    }

    withShowIf(pattern: string) : WranglerFormField {
        this.showIf = pattern;
        return this;
    }

    withLabel(label: string) : WranglerFormField {
        this.label = label;
        return this;
    }

    default(value: any) : WranglerFormField {
        this.value = value;
        return this;
    }

    anyNumber() : WranglerFormField {
        this.validator = function(v:any) {
            if (v != null) {
                return !(v.toString().isNaN());
            }
        }
        this.validationText = "Valid number required.";
        return this;
    }

    intNumeric() : WranglerFormField {
        this.validation = "[0-9]+";
        this.validationText = "Positive integer required.";
        return this;
    }

    validName() : WranglerFormField {
        this.validation = "^[a-zA-Z_][a-zA-Z0-9_]*$";
        this.validationText = "Valid fieldname required.";
        return this;
    }

    minIntValidator(min:number) : WranglerFormField {
        this.intNumeric();
        this.validationText = `Integer must be greater or equal to ${min}`;
        this.validator = function(v:any) {
            if (v != null) {
                return (v.toString().toNumber() >= min);
            }
            return true;
        };
        return this;
    }

    withChoices(choices:string[]) : WranglerFormField {
        this.choices = choices;
        return this;
    }

    isValid() : boolean {
        // TODO:
        if (this.required && this.value == null) {
            return false;
        }
        if (this.validation != null && this.value != null) {
            if (this.value.toString().match(this.validation) == null) {
                return false;
            }
        }
        if (this.validator != null) {
            return this.validator(this.value);
        }
        return true;
    }
    
    // Retrieve value as a number or null
    getValueAsNumber() : number {
        if(this.value != null && typeof this.value == 'number') {
            return <number>this.value;
        }
        else if (this.value != null && !this.value.toString().isNaN()) {
            return this.value.toString().toNumber();
        }
        return null;
    }

    // add the field to the dialog
    build() : DialogBuilder {
        let dialogBuilder : DialogBuilder = this.builder;
        this.builder = null;
        dialogBuilder.addField(this);
        return dialogBuilder;
    }
}


export class DialogBuilder {

    fields : WranglerFormField[] = [];

    title : string = "(No title)";

    validator : any;

    constructor(protected $mdDialog: angular.material.IDialogService, protected dialog?: DialogService) {

    }

    public withValidator(cb : any) : DialogBuilder {
        this.validator = cb;
        return this;
    }

    public withTitle(title : string) : DialogBuilder {
        this.title = title;
        return this;
    }

    public addField(field : WranglerFormField) {
        this.fields.push(field);
    }

    public selectbox(name:string) : WranglerFormField {
        let field : WranglerFormField = new WranglerFormField("select", name);
        field.builder = this;
        return field;
    }

    public inputbox(name:string) : WranglerFormField {
        let field : WranglerFormField = new WranglerFormField("input", name);
        field.builder = this;
        return field;
    }

    public checkbox(name:string) : WranglerFormField {
        let field : WranglerFormField = new WranglerFormField("checkbox", name);
        field.builder = this;
        field.required = false;
        return field;
    }

    private buildField(field: WranglerFormField): string {

        let html = ''

        let required = (field.required ? `required=""` : '');
        let ngIf = (field.showIf );
        let tipLabel = (field.tip ? `tipLabel=${field.tip}` : '');
        let pattern = (field.validation ?  `pattern="${field.validation}"` : '');

        switch (field.type) {
            case 'checkbox':
                html += `<md-checkbox ng-model="dialog.fields['${field.name}'].value" ${required} ${tipLabel}  aria-label="${field.label}">${field.label}</md-checkbox>`
                break;
            case 'select':
                html += `<label>${field.label}</label>`
                html += `<md-select ng-class="mat-form-field-infix" ng-model="dialog.fields['${field.name}'].value" ${required} ${tipLabel}>`;
                for (let choice of field.choices) {
                    html += `<md-option value="${choice}">${choice}</md-option>`;
                }
                html += `</md-select>`;
                break;
            case 'input':
                html += `<label>${field.label}</label>
                            <input type="text" ng-class="mat-form-field-infix"  aria-label="${field.label}" ng-model="dialog.fields['${field.name}'].value" ${pattern}" ${required} ${tipLabel} [formControl]="${field.name}">
                            </input>
                        `
                break;
            default:
                throw new Error(`Unexpected field type ${field.type}`);
        }
        if (field.validation) {
            html += `<span style="color:red" ng-show="!(dialog.fields['${field.name}'].isValid())">${field.validationText}</span>`
        }

        return html;
    }

    private buildForm(fields: WranglerFormField[], title: string): string {

        let form =
            `    
                   <style>
                    .mat-toolbar-row, .mat-toolbar-single-row {
                        height: 64px;
                    }
                    .mat-toolbar-row, .mat-toolbar-single-row {
                        display: flex;
                        box-sizing: border-box;
                        padding: 0 16px;
                        width: 100%;
                        flex-direction: row;
                        align-items: center;
                        white-space: nowrap;
                    }
                    .mat-dialog-title {
                        margin: 0 0 20px;
                        display: block;
                    } 
                    .mat-dialog-content {
                        display: block;
                        margin: 0 0px;
                        padding: 0 24px;
                        max-height: 65vh;
                        overflow: auto;
                        -webkit-overflow-scrolling: touch;
                        -webkit-backface-visibility: hidden;
                        backface-visibility: hidden;                    
                    }                      
                                        
                    .mat-form-field-infix {
                        display: block;
                        position: relative;
                        flex: auto;
                        min-width: 0;
                        width: 180px;
                    }                                     
                  </style>
                  <md-dialog style="max-width: 640px;min-width: 320px;" class="mat-dialog-container">
                  <div class="mat-dialog-title mat-toolbar mat-primary mat-toolbar-single-row">${title}</div>
                  
                  <md-dialog-content class="mat-dialog-content" role="document" tabIndex="-1" layout="column">
                    <form class="layout-column ng-untouched ng-pristine ng-invalid" novalidate="">
                     <!--<p _ngcontent-c11="">&nbsp;</p>-->`;
        for (let field of fields) {
            form += this.buildField(field);
        }

        form += ` </form></md-dialog-content>
                  <md-dialog-actions>
                      <md-button ng-click="dialog.cancel()" class="md-cancel-button" md-autofocus="false">Cancel</md-button>
                      <md-button ng-click="dialog.apply()" ng-disabled="!dialog.valid()" class="md-primary md-confirm-button" md-autofocus="true">Ok</md-button>
                  </md-dialog-actions>
                  </md-dialog>
                `
        return form;
    }

    public showDialog(cb: any) : void {
        let self = this;
        let title = this.title;
        let fields = this.fields;
        let validator = this.validator;
        self.$mdDialog.show({
            clickOutsideToClose: true,
            controller: class {
                fields : Map<String,WranglerFormField> = new Map();
                static readonly $inject = ["$mdDialog"];

                constructor(private $mdDialog: angular.material.IDialogService) {
                    for (let field of fields) {
                        this.fields[field.name] = field;
                    }
                }

                valid(): boolean {
                    for (let field of fields) {
                        if (!field.isValid()) {
                            return false;
                        }
                    }
                    if (validator != null) {
                        return validator(this.fields);
                    }
                    return true;
                }

                cancel() {
                    this.$mdDialog.hide();
                }

                apply() {
                    this.$mdDialog.hide();
                    cb(this.fields);
                }
            },
            controllerAs: "dialog",
            parent: angular.element("body"),
            template: self.buildForm(fields, title)
        });
    }

}


export class WranglerFormBuilder {


    constructor(protected $mdDialog: angular.material.IDialogService, protected dialog?: DialogService) {

    }

    public newInstance() : DialogBuilder {
        return new DialogBuilder(this.$mdDialog, this.dialog);
    }

}



