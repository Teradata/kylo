import {Injectable} from "@angular/core";

@Injectable()
export class LocalStorageService {

    localStorage:Storage;

    constructor(){
        if(window.localStorage){
            this.localStorage = window.localStorage;
        }
    }


    /**
     * set an item to local storage
     * @param {string} key
     * @param value
     */
    setItem(key:string,value:any){
        let str = '';
        if(typeof value == "object"){
            str = JSON.stringify(value);
        }
        else if (typeof value != "string"){
            str = value+"";
        }
        if(this.localStorage){
            this.localStorage.setItem(key,str);
        }
    }

    /**
     * get an item from localStroage
     * @param {string} key
     * @return {any}
     */
    getItem(key:string):any{
        let returnedItem = undefined;
        if(this.localStorage){
           let item  = this.localStorage.getItem(key);
            if(item){
                try {
                    returnedItem = JSON.parse(item);
                }
                catch (e) {
                    returnedItem = item;
                }
            }
        }
        return returnedItem;
    }

    removeItem(key:string){
        if(this.localStorage){
            this.localStorage.removeItem(key);
        }
    }

    clear(){
        if(this.localStorage){
            this.localStorage.clear();
        }
    }
}