import { FieldConfig } from "./FieldConfig";
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
 cron
 */
export declare enum InputType {
    color = "color",
    date = "date",
    datetime_local = "datetime-local",
    email = "email",
    month = "month",
    number = "number",
    password = "password",
    search = "search",
    tel = "tel",
    text = "text",
    time = "time",
    url = "url",
    week = "week",
    cron = "cron"
}
export declare class InputText extends FieldConfig<string> {
    static CONTROL_TYPE: string;
    controlType: string;
    type: string;
    readonly: boolean;
    constructor(options?: {});
}
