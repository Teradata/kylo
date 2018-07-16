import {FieldConfig} from "./FieldConfig";

/**
 * supported types:
 *
 color
 date
 datetime-local
 email
 month
 number
 password
 search
 tel
 text
 time
 url
 week
 */
export class InputText extends FieldConfig<string> {
    controlType = 'textbox';
    type: string;

    constructor(options: {} = {}) {
        super(options);
        this.type = options['type'] || '';
    }
}