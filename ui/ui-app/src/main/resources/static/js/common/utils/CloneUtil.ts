export class CloneUtil {

    static deepObjectCopy(obj: any) {
        let newObj: any;

        // if its not an obj or its not defined return
        if (null == obj || "object" !== typeof obj) {
            return obj;
        }

        // Handle Date
        if (obj instanceof Date) {
            newObj = new Date();
            newObj.setTime(obj.getTime());
            return newObj;
        }

        // Handle Array
        if (obj instanceof Array) {
            newObj = [];            
            obj.forEach((item:any,i:number) => newObj[i] = CloneUtil.deepObjectCopy(obj[i]));
            return newObj;
        }

        // Handle Map
        if (obj instanceof Map) {
            newObj = new Map(obj);
            return newObj;
        }

        // Handle Object
        if (obj instanceof Object) {
            newObj = Object.create(obj);
            for (const attr in obj) {
                if (obj.hasOwnProperty(attr)) {
                    newObj[attr] = CloneUtil.deepObjectCopy(obj[attr]);
                }
            }
            return newObj;
        }

        throw new Error("Unable to copy obj! Its type isn't supported.");
    }
    
    static shallowCopy<T>(obj:T) : T {
        return Object.assign({},obj);
    }

    static deepCopy<T>(obj: T): T {

        const newObj = Object.create(obj as any);
        //handle the nested objects
        for (const i in obj) {
            if (obj.hasOwnProperty(i)) {
                newObj[i] = CloneUtil.deepObjectCopy(obj[i]);              
            }
        }
        return newObj;
    }
}