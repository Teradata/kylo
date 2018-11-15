import { KyloObject } from "../common.model";
export declare class ObjectUtils {
    static getObjectClass(obj: any): any;
    static identity<T>(arg: T): T;
    static isType(obj: any, type: string): boolean;
    static getAs<T extends KyloObject>(obj: any, type: {
        new (arg: any): T;
    }, objectType?: string): T;
    static newType<T>(options: any, type: {
        new (arg: any): T;
    }): T;
    static toJson(obj: any): any;
    static isDefined(value: any): boolean;
    static isUndefined(value: any): boolean;
}
