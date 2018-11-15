/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { KyloObject } from "../common.model";
export class ObjectUtils {
    /**
     * @param {?} obj
     * @return {?}
     */
    static getObjectClass(obj) {
        if (obj && obj.constructor && obj.constructor.toString) {
            var /** @type {?} */ arr = obj.constructor.toString().match(/function\s*(\w+)/);
            if (arr && arr.length == 2) {
                return arr[1];
            }
        }
        return undefined;
    }
    /**
     * @template T
     * @param {?} arg
     * @return {?}
     */
    static identity(arg) {
        return arg;
    }
    /**
     * @param {?} obj
     * @param {?} type
     * @return {?}
     */
    static isType(obj, type) {
        return (obj.objectType && obj.objectType == type);
    }
    /**
     * @template T
     * @param {?} obj
     * @param {?} type
     * @param {?=} objectType
     * @return {?}
     */
    static getAs(obj, type, objectType) {
        let /** @type {?} */ validType = false;
        let /** @type {?} */ newInstance = null;
        let /** @type {?} */ id = ObjectUtils.identity(type);
        if (objectType == undefined) {
            objectType = id.name;
        }
        if (ObjectUtils.isType(obj, objectType)) {
            return /** @type {?} */ (obj);
        }
        else {
            return new type(obj);
        }
    }
    /**
     * @template T
     * @param {?} options
     * @param {?} type
     * @return {?}
     */
    static newType(options, type) {
        return new type(options);
    }
    /**
     * @param {?} obj
     * @return {?}
     */
    static toJson(obj) {
        if (ObjectUtils.isUndefined(obj))
            return undefined;
        return JSON.stringify(obj);
    }
    /**
     * @param {?} value
     * @return {?}
     */
    static isDefined(value) {
        return !ObjectUtils.isUndefined(value);
    }
    /**
     * @param {?} value
     * @return {?}
     */
    static isUndefined(value) {
        return typeof value === 'undefined';
    }
}
//# sourceMappingURL=object-utils.js.map