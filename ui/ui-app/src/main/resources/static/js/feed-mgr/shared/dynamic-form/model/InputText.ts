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
export enum InputType{
    color="color",
date="date",
datetime_local="datetime-local",
email="email",
month="month",
number="number",
password="password",
search="search",
tel="tel",
text="text",
time="time",
url="url",
week="week"
}

export class InputText extends FieldConfig<string> {
    static CONTROL_TYPE = 'textbox';
    controlType = InputText.CONTROL_TYPE;
    type: string;

    constructor(options: {} = {}) {
        super(options);
        this.type = options['type'] || '';
    }
}