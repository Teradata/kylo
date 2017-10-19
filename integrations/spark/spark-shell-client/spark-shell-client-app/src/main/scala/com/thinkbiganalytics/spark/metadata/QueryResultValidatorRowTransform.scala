package com.thinkbiganalytics.spark.metadata

import java.util

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.thinkbiganalytics.spark.rest.model.{TransformQueryResult, TransformValidationResult}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

/** Helper for building [[TransformQueryResult]] objects from a Spark dataset containing validation results. */
class QueryResultValidatorRowTransform(schema: StructType, destination: String) extends QueryResultRowTransform(StructType(schema.fields.dropRight(1)), destination) {

    /** JSON deserializer */
    private val objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /** Object type for reject reasons column */
    private val validationResultType = new TypeReference[util.List[TransformValidationResult]] {}

    /** Adds the specified row and validation results to the query result. */
    override def addRow(row: Row, result: TransformQueryResult): Unit = {
        val rejectReasons = Option(row(row.length - 1))
            .map(_.asInstanceOf[String])
            .filterNot(_.isEmpty)
            .map(objectMapper.readValue(_, validationResultType).asInstanceOf[util.List[TransformValidationResult]])
            .orNull
        result.addRow(getQueryResultRow(row), rejectReasons)
    }
}
