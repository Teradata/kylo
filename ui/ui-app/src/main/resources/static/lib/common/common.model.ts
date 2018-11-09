export interface KyloObject {
    objectType:string;
}
export enum RestResponseStatusType {
    SUCCESS="SUCCESS",ERROR="ERROR"
}

export interface RestResponseStatus {
    status:RestResponseStatusType;
    message:string;
    developerMessage:string;
    url:string;
    validationError:boolean;
}

export interface ObjectChanged<T> {
    oldValue:T;
    newValue:T;
}