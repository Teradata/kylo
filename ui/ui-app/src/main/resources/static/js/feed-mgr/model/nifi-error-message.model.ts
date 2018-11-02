export interface NifiErrorMessage{
    message:string;
    category:string;
    severity:string;
    processorName?:string;
}