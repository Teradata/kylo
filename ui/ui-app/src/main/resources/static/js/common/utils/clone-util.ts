import {AbstractControl} from "@angular/forms/src/model";

export class CloneUtil {

    static deepObjectCopy(obj: any,objectTracker?:CloneObjectTracker) {
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
            obj.forEach((item:any,i:number) => newObj[i] = CloneUtil.deepObjectCopy(obj[i], objectTracker));
            return newObj;
        }

        // Handle Map
        if (obj instanceof Map) {
            newObj = new Map(obj);
            return newObj;
        }

        // Handle Object
        if (obj instanceof Object) {
            if(objectTracker == undefined){
                objectTracker = new CloneObjectTracker();
            }

            let cloned = objectTracker.getCloned(obj);
            if(!cloned) {

                newObj = Object.create(obj);
                //track this object to prevent circular references
                objectTracker.setCloned(obj,newObj);

                for (const attr in obj) {
                    if (obj.hasOwnProperty(attr)) {
                        newObj[attr] = CloneUtil.deepObjectCopy(obj[attr], objectTracker);
                    }
                }

                return newObj;
            }
            else {
                if(objectTracker.isReferenceCircularObject()) {
                    return cloned;
                }
                else {
                    return null;
                }
            }
        }

        throw new Error("Unable to copy obj! Its type isn't supported.");
    }
    
    static shallowCopy<T>(obj:T) : any {
        if (obj instanceof Array) {
           let newObj = [];
            obj.forEach((item:any,i:number) => newObj[i] = CloneUtil.shallowCopy(obj[i]));
            return newObj;
        }
        else {
            return Object.assign({}, obj);

        }
    }

    static deepCopy<T>(obj: T, objectTracker?:CloneObjectTracker): T {
        if(objectTracker == undefined){
            objectTracker = new CloneObjectTracker();
        }

        const newObj = Object.create(obj as any);
        //handle the nested objects
        for (const i in obj) {
            if (obj.hasOwnProperty(i)) {
                newObj[i] = CloneUtil.deepObjectCopy(obj[i], objectTracker);
            }
        }
        return newObj;
    }

    static deepCopyWithoutCircularReferences<T>(obj: T): T {
        return this.deepCopy(obj,new CloneObjectTracker(CircularReferenceStrategy.REMOVE));
    }


    /**
     * private deepCopy(obj:any):any {
        return JSON.parse(JSON.stringify( obj ));
    }
     */
}

enum CircularReferenceStrategy{
    REFERENCE =1, REMOVE =2
}

export class CloneObjectTracker {
    /**
     * map of old object to cloned object
     * @type {Map<any, any>}
     */
    clonedObjectMap = new Map();

    isCloned(oldObj:any){
        return this.clonedObjectMap.get(oldObj) != undefined;
    }

    getCloned(oldObj:any){
        return this.clonedObjectMap.get(oldObj);
    }

    setCloned(oldObj:any, clonedObj:any){
      this.clonedObjectMap.set(oldObj,clonedObj);
    }

    isReferenceCircularObject(){
        return this.circularReferenceStrategy == CircularReferenceStrategy.REFERENCE;
    }


    constructor(public circularReferenceStrategy:CircularReferenceStrategy=CircularReferenceStrategy.REFERENCE){

    }
}