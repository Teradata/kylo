import * as angular from 'angular';
import {Templates} from "./TemplateTypes";
import Property = Templates.Property;
import Processor = Templates.Processor;

declare interface FeedPropertyService {

    /**
     * If the property is sensitive we should store off the long encrypted value and show just asterisks
     * @param property
     */
    initSensitivePropertyForEditing(property:Property):void;
    /**
     * Return true if every char in value == '*'
     */
    isMasked(value:any):boolean;
    /**
     * If the property is sensitive and hasnt changed we should set it back to the encrypted value.
     * @param property
     */
    initSensitivePropertyForSaving(property:Property):void;

    initSensitivePropertiesForEditing(properties:Property[]) :void;

    /**
     * Sets the displayValue attribute for the incoming property
     * @param property a NiFiProperty
     */
    updateDisplayValue(property:Property):void;

    /**
     * Update the property display values for the list of processors
     * @param processors a list of processors that have a list of properties
     */
    updateDisplayValueForProcessors(processors:Processor[]):void;

}