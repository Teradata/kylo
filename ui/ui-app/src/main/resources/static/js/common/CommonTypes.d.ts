import * as angular from "angular";
declare namespace Common {

    interface Collection<T> { }

    export interface Map<T> extends Collection<T> {
        [K: string]: T;
    }

    export interface LabelValue {
        label:string;
        value:string;
        description?:string;
    }

}

