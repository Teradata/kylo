import * as angular from "angular";
declare namespace FeedServiceTypes {

export interface MergeStrategy {
    name:string;
    type:string;
    hint:string;
    disabled:boolean;
}

}