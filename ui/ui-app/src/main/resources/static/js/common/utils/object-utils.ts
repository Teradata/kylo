import {KyloObject} from "../common.model";
import {CloneUtil} from "./clone-util";

export class ObjectUtils {

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


}