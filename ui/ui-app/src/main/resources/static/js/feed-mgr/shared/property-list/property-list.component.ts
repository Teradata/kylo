import * as _ from "underscore";
import {UserProperty} from "../../model/user-property.model";
import {CloneUtil} from "../../../common/utils/clone-util";
import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from "@angular/core";
import {AbstractControl, FormControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {FormControlValidation} from "../../../../lib/common/utils/form-control-validation";
import {FieldPolicyProperty} from "../../model/field-policy";


/**
 * A user-defined property (or business metadata) on a category or feed.
 *
 * @typedef {Object} UserProperty
 * @property {string|null} description a human-readable specification
 * @property {string|null} displayName a human-readable title
 * @property {boolean} locked indicates that only the value may be changed
 * @property {number} order index for the display order from 0 and up
 * @property {boolean} required indicates that the value cannot be empty
 * @property {string} systemName an internal identifier
 * @property {string} value the value assign to the property
 * @property {Object.<string, boolean>} [$error] used for validation
 */

/**
 * Manages a view containing a list of properties.
 *

 */
@Component({
    selector: "property-list",
    styleUrls: ["./property-list.component.css"],
    templateUrl: "./property-list.component.html"
})
export class PropertyListComponent  implements OnInit, OnDestroy{

    @Input()
    title?:string;

    @Input()
    editable:boolean;

    /**
     * List of properties in the model.
     * @type {Array.<UserProperty>}
     */
    @Input()
    properties: UserProperty[] = [];

    @Output()
    propertiesChange = new EventEmitter<UserProperty[]>();

    @Input()
    parentFormGroup:FormGroup;

    userPropertyForm: FormGroup;

    component:PropertyListComponent = this;


    constructor() {
         this.userPropertyForm = new FormGroup({});
    }

    ngOnInit() {
        if(this.parentFormGroup) {
            this.parentFormGroup.addControl("userPropertyForm",this.userPropertyForm);
        }
        if(this.editable) {
            this.properties.forEach((property) => this.registerFormControls(property));
        }
    }

    ngOnDestroy() {

    }

    /**
     * Update the Model 'properties' array with the form values
     */
    updateModel(){

        this.properties.map(property => {
            property.systemName = this.userPropertyForm.value[this.formControlKey(property,"systemName")];
            property.value = this.userPropertyForm.value[this.formControlKey(property,"value")];
        })
    }

    reset(properties:UserProperty[]){
        //remove all controls
        Object.keys(this.userPropertyForm.controls).forEach(name => this.userPropertyForm.removeControl(name));
        this.properties = properties;
        this.properties.forEach((property) => this.registerFormControls(property));
    }


    /**
     * Check to see if the property 'systemName' or 'value' has errors
     * @param {UserProperty} property
     * @param {string} type
     * @return {boolean}
     */
    hasError(property:UserProperty, type:string){
        let controlKey = this.formControlKey(property,type)
        let control = this.userPropertyForm.get(controlKey);
        return control == undefined || (control && !control.valid) ;
    }

    /**
     * returns the error message for the given property
     * @param {UserProperty} property
     * @param {string} type
     * @return {string}
     */
    getErrorMessage(property:UserProperty, type:string):string{
        let controlKey = this.formControlKey(property,type)
        return FormControlValidation.getErrorMessage(this.userPropertyForm,controlKey);
    }




    /**
     * Adds a new user-defined property.
     */
    addProperty() {
        let property = new UserProperty({order:this.properties.length});
        this.properties.push(property);
        this.registerFormControls(property);
        this.propertiesChange.emit(this.properties)

    };

    /**
     * Deletes the item at the specified index from the user-defined properties list.
     *
     * @param {number} index the index of the property to delete
     */
    removeProperty(index: any) {
        let property= this.properties[index];
        this.properties.splice(index, 1);
        //remove the form controls
        this.userPropertyForm.removeControl(this.formControlKey(property,"value"))
        this.userPropertyForm.removeControl(this.formControlKey(property,"systemName"))
        this.propertiesChange.emit(this.properties)
    }

    /**
     * register the property wiht form controls for the systemName and value
     *
     * @param {UserProperty} property
     */
    private registerFormControls(property:UserProperty){
        if(!property.id){
            property.id = _.uniqueId("userProperty_");
        }
        let valueValidators = [];
        if (property.required) {
            valueValidators.push(Validators.required)
        }
        let valueControl = new FormControl(property.value, valueValidators);
        let systemNameControl = new FormControl(property.systemName, [Validators.required, this.duplicatePropertyValidator(property)])
        this.userPropertyForm.addControl(this.formControlKey(property, "value"), valueControl);
        this.userPropertyForm.addControl(this.formControlKey(property, "systemName"), systemNameControl)

    }

    /**
     * check to see if the passed in property has a unique 'systemName'
     *
     * @param {UserProperty} property
     * @return {boolean} true if name is unique, false if not
     */
    private isUniqueName(property:UserProperty){
        let match:UserProperty = null;

        let control = this.userPropertyForm.get(this.formControlKey(property,"systemName"));
        if(control) {
            let propertyName = control.value;
            let otherProperties = this.properties.filter(prop => prop.id != property.id);

            if (otherProperties && otherProperties.length >0) {
                match = otherProperties.find(prop => {
                    let sysNameFormControl = this.userPropertyForm.get(this.formControlKey(prop, "systemName"));
                    if(sysNameFormControl) {
                        return sysNameFormControl.value == propertyName
                    }
                    else {
                        return false;
                    }
                });
            }
        }
        return match == undefined || match ==null;
    }

    /**
     * return the unique user interface property id for needed for form controls
     * @param {UserProperty} property
     * @param {string} type
     * @return {string}
     */
    private formControlKey(property:UserProperty, type:string){
        return property.id+'_'+type;
    }

    /**
     * Validator to check if the given property has a unique name
     * @param {UserProperty} currentProperty
     * @return {ValidatorFn}
     */
    private duplicatePropertyValidator(currentProperty:UserProperty) : ValidatorFn{
        return (control: AbstractControl): { [key: string]: boolean } | null => {
            if(!this.isUniqueName(currentProperty)){
                return {"duplicate": true};
            }
            else {
                return null;
            }
        }
    }


}

