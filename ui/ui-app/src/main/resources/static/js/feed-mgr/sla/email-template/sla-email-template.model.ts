

export interface SlaEmailTemplate {
    id:string;
    name:string;
    enabled:boolean;
    default?:boolean;
    subject:string;
    template:string;
}