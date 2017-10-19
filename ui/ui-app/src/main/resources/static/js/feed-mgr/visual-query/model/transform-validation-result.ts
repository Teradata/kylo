/**
 * Indicates the reason that a value was rejected by a validator.
 */
export interface TransformValidationResult {

    scope: string;
    field: string;
    type: string;
    rule: string;
    reason: string;
}
