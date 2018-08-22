import {FormControl, FormGroup, Validators} from "@angular/forms";
import {FieldConfig} from "../model/FieldConfig";
import {TranslateService} from "@ngx-translate/core";

export class DynamicFormUtil {

    static toFormGroup(fields: FieldConfig<any>[],_translateService?: TranslateService ) :FormGroup {
        let group: any = {};

        fields.forEach(field => {
            group[field.key] = DynamicFormUtil.toFormControl(field,_translateService);
        });
        return new FormGroup(group);
    }

   private static translate(key:string,defaultValue:string,_translateService: TranslateService){
        let trans = _translateService.instant(key);
        if(trans == key && defaultValue){
            return defaultValue;
        }
        else {
            return trans
        }
    }

    static resolveLocaleKeys(field:FieldConfig<any>,_translateService?: TranslateService) {
        if(_translateService) {

            if (field.placeholderLocaleKey != undefined) {
                let placeholder = DynamicFormUtil.translate(field.placeholderLocaleKey,field.placeholder,_translateService)
                field.placeholder = placeholder;
            }
        }
    }

    static addToFormGroup(fields:FieldConfig<any>[], formGroup:FormGroup, _translateService?: TranslateService):FormControl[]{//: {[key: FieldConfig<any>]: FormControl}{
        //  let group:  {[key: FieldConfig<any>]: FormControl} = {};
        let formControls:FormControl[] = [];

        fields.forEach(field => {
            let control:FormControl =DynamicFormUtil.toFormControl(field,_translateService);
            formGroup.addControl(field.key,control);
            formControls.push(control);

            control.valueChanges.debounceTime(200).subscribe(value=> {
                if(field.model && field.modelValueProperty){
                    console.log("update the model for ",field, 'with ',value);
                    field.model[field.modelValueProperty] = value;
                    if(field.onModelChange){
                        field.onModelChange(field.model);
                    }
                }
            })
            //    group[field] = control;
        });
        //   return group;
        return formControls;

    }

    static toFormControl(field:FieldConfig<any>,_translateService?: TranslateService) : FormControl {
        if(_translateService) {
            DynamicFormUtil.resolveLocaleKeys(field, _translateService);
        }
        let validatorOpts = field.validators || null;
        if(field.required){
            if(validatorOpts == null){
                validatorOpts = [];
            }
            validatorOpts.push(Validators.required)
        }

        return new FormControl(field.value || '', validatorOpts)


    }
}