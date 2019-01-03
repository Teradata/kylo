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

    static toJson(obj : any) : any {
        if (ObjectUtils.isUndefined(obj)) return undefined;
        return JSON.stringify(obj);
    }
    static isDefined(value : any) : boolean {
        return !ObjectUtils.isUndefined(value);
    }
    static isUndefined(value : any) : boolean {
        return typeof value === 'undefined';
    }
    static fromJson(obj : any) : any {
        if (ObjectUtils.isUndefined(obj)) return undefined;
        return JSON.parse(obj);
    }
    static isString(value : any) : boolean {
        return typeof value === 'string';
    }

    static sortByStringProperty( listToBeSorted : any[], propertyName : string) : any[] {
        
        if(listToBeSorted.length == 0 ){
            return [];
        }
        
        return listToBeSorted.sort((a :any , b:any) => { 
            if(!a[propertyName] || !b[propertyName]){
                return 0;
            }
            if(a[propertyName].toLowerCase() < b[propertyName].toLowerCase()){
                return -1;
            }else if(a[propertyName].toLowerCase() > b[propertyName].toLowerCase()) {
                return 1;
            }
            return 0;
        });
    }
}
