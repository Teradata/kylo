import {TableColumnDefinition} from "../../../../model/TableColumnDefinition";
import {TableFieldPolicy} from "../../../../model/TableFieldPolicy";
import {DomainType} from "../../../../services/DomainTypesService";
import * as _ from "underscore";
import {CloneUtil} from "../../../../../common/utils/clone-util";
import {ObjectUtils} from "../../../../../../lib/common/utils/object-utils";

export class SelectedColumn {

    standardizers: TableFieldPolicy[];
    validators: TableFieldPolicy[];
    standardizerCount: number = 0;
    validatorCount: number = 0;
    domainType: DomainType;

    tagNames:string[];

    origTagNames:string[];

    /**
     * comma separated tags
     */
    tagList:string;
    /**
     * comma separated standardizers
     */
    standardizerList:string;

    /**
     * comma separated validators
     */
    validatorList:string;

    constructor(public field: TableColumnDefinition, public fieldPolicy: TableFieldPolicy) {

        this.update();
    };

    get selectedSampleValue() {
        return this.field.selectedSampleValue;
    }

    set selectedSampleValue(sampleValue: any) {
        this.field.selectedSampleValue = sampleValue
    }

    /**
     * After the field policies have been updated, change the counter references
     */
    update() {
        if (this.fieldPolicy) {
            this.standardizers = this.fieldPolicy.standardization || [];
            this.validators = this.fieldPolicy.validation || [];
            this.standardizerCount = this.standardizers.length;
            this.validatorCount = this.validators.length;



            this.standardizerList = this.standardizers.map(_.property("name")).join(", ");

            this.validatorList = this.validators.map(_.property("name")).join(", ");

            this.tagNames = _.isArray(this.field.tags) ? this.field.tags.map(_.property("name")) : [];
            this.origTagNames = _.isArray(this.field.tags) ? this.field.tags.map(_.property("name")) : [];
            this.tagList = this.tagNames.join(", ");

        }
    }

    onAddTag(tagName:string){
        this.field.tags.push({name:tagName});
    }
    onRemoveTag(tagName:string){
       let tag = this.field.tags.find(tag => tag.name == tagName);
       if(tag){
           let index = this.field.tags.indexOf(tag);
           this.field.tags.splice(index,1);
       }
    }

    setDomainType(availableDomainTypes:DomainType[]){
        if(this.fieldPolicy) {
            // Find domain type from id
            let domainType = _.find(availableDomainTypes, (domainType: DomainType) => {
                return (domainType.id === this.fieldPolicy.domainTypeId);
            });
            this.domainType = domainType;
        }
    }

    hasDomainType(){
        return this.domainType != undefined;
    }

    getDomainTypeName(){
        return this.hasDomainType() ? this.domainType.title : "Not set"
    }

    /**
     * Show the domain type selection dialog
     * @return {boolean}
     */
    showDomainTypeDialog() {
        let show = false;
        if ((this.domainType && (this.domainType.field.derivedDataType !== null
                && this.domainType.field.derivedDataType !== this.field.derivedDataType
                || ( this.domainType.field.precisionScale && this.domainType.field.precisionScale !== this.field.precisionScale)))
                || (_.isArray(this.fieldPolicy.standardization) && this.fieldPolicy.standardization.length > 0)
                || (_.isArray(this.fieldPolicy.field.tags) && this.fieldPolicy.field.tags.length > 0)
                || (_.isArray(this.fieldPolicy.validation) && this.fieldPolicy.validation.length > 0)) {
                show = true;
            }
        return show;
    }


    applyDomainType(domainType:DomainType){
        this.field.applyDomainType(domainType);
        this.domainType = domainType;
        this.update();
    }


}
