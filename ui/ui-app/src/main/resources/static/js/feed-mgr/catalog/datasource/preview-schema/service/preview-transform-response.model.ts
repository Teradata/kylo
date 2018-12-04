import {TransformResponse} from "../../../../visual-query/wrangler";
import {SchemaParser} from "../../../../model/field-policy";

export interface PreviewTransformResponse extends TransformResponse{
     schemaParser?:SchemaParser;
}