import * as _ from "underscore";
import Property = Templates.Property;
import Processor = Templates.Processor;
import {Templates} from "../../../lib/feed-mgr/services/TemplateTypes";
import {FeedPropertyService} from "./FeedPropertyService";
import { Injectable } from '@angular/core';
import {NifiFeedPropertyUtil} from "./nifi-feed-property-util";

@Injectable()
export class DefaultFeedPropertyService implements FeedPropertyService{

    /**
     * If the property is sensitive we should store off the long encrypted value and show just asterisks
     * @param property
     */
    initSensitivePropertyForEditing= NifiFeedPropertyUtil.initSensitivePropertiesForEditing;
    /**
     * Return true if every char in value == '*'
     */
    isMasked= NifiFeedPropertyUtil.isMasked;
    /**
     * If the property is sensitive and hasnt changed we should set it back to the encrypted value.
     * @param property
     */
    initSensitivePropertyForSaving= NifiFeedPropertyUtil.initSensitivePropertyForSaving;

    initSensitivePropertiesForEditing= NifiFeedPropertyUtil.initSensitivePropertiesForEditing;

    /**
     * Sets the displayValue attribute for the incoming property
     * @param property a NiFiProperty
     */
    updateDisplayValue= NifiFeedPropertyUtil.updateDisplayValue;

    /**
     * Update the property display values for the list of processors
     * @param processors a list of processors that have a list of properties
     */
    updateDisplayValueForProcessors= NifiFeedPropertyUtil.updateDisplayValueForProcessors;

}
