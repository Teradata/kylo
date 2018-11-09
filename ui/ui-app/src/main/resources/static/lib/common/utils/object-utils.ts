import {KyloObject} from "../common.model";

export class ObjectUtils {


    static getObjectClass(obj:any) {
        if (obj && obj.constructor && obj.constructor.toString) {
            var arr = obj.constructor.toString().match(
                /function\s*(\w+)/);

            if (arr && arr.length == 2) {
                return arr[1];
            }
        }

        return undefined;
    }

    static identity<T>(arg: T): T {
        return arg;
    }

    static isType(obj:any, type:string) {
        return (obj.objectType && obj.objectType == type);
    }

    static getAs<T extends KyloObject>(obj:any, type: { new(arg:any): T ;}, objectType ?:string) : T {
        let validType :boolean = false;
        let newInstance = null;
        let id = ObjectUtils.identity(type);
        if(objectType == undefined)
        {
           objectType = id.name;
        }
        if(ObjectUtils.isType(obj,objectType)) {
            return <T>obj;
        }
        else {
            return new type(obj);
        }
    }

    static newType<T>(options:any, type: { new(arg:any): T ;}) :T {
        return new type(options);
    }

}
