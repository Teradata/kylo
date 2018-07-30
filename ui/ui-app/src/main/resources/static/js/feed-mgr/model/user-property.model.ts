import * as _ from "underscore";
export class UserProperty {


    id:string;
    description: string;
    displayName: string;
    locked: boolean;
    order: number;
    required: boolean;
    systemName: string;
    value: string;

    public constructor(init?: Partial<UserProperty>) {
        this.initialize(init ? init.order : 0);
        Object.assign(this, init);

    }

    private initialize(order?:number){
        this.id = _.uniqueId("userProperty_");
        this.description = null;
        this.displayName =null;
        this.locked =false;
        this.order = order || 0;
        this.required =true;
        this.systemName = ""
        this.value = "";

    }

    public equals(userProperty:UserProperty): boolean
    {
        return this.systemName == userProperty.systemName;
    }

}


export  class UserPropertyUtil {

    static updatePropertyIds(oldProperties:UserProperty[], newProperties:UserProperty[]){
        let oldUserPropertiesGroup = _.groupBy(oldProperties,"systemName");
        newProperties.forEach(property => {
            let oldProperty = oldUserPropertiesGroup[property.systemName];
            if(oldProperty && oldProperty.length >0){
                property.id = oldProperty[0].id;
            }
        })
    }
}
