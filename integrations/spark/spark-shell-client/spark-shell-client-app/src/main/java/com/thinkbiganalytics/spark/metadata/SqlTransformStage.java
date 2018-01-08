package com.thinkbiganalytics.spark.metadata;

/*-
 * #%L
 * kylo-spark-shell-client-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.discovery.util.ParserHelper;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.jdbc.DataSourceSupplier;
import com.thinkbiganalytics.spark.jdbc.JdbcUtil;
import com.thinkbiganalytics.spark.jdbc.RowTransform;
import com.thinkbiganalytics.spark.model.TransformResult;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;
import com.thinkbiganalytics.spark.util.ScalaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.rdd.JdbcRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.jdbc.JdbcDialects$;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.DecimalType;
import org.apache.spark.sql.types.MetadataBuilder;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

/**
 * Creates a transform result from a SQL transformation.
 */
public class SqlTransformStage implements Supplier<TransformResult>, ResultSetExtractor<TransformResult> {

    private static final Logger log = LoggerFactory.getLogger(SqlTransformStage.class);

    /**
     * SQL data source.
     */
    @Nonnull
    private final Supplier<DataSource> dataSource;

    /**
     * JDBC dialect
     */
    @Nonnull
    private final JdbcDialect dialect;

    /**
     * Spark context service.
     */
    @Nonnull
    private final SparkContextService sparkContextService;

    /**
     * SQL query.
     */
    @Nonnull
    private final String sql;

    /**
     * Spark context.
     */
    @Nonnull
    private final SQLContext sqlContext;

    /**
     * Constructs a {@code SqlTransformStage}.
     */
    public SqlTransformStage(@Nonnull final String sql, @Nonnull final JdbcDatasource datasource, @Nonnull final SQLContext sqlContext, @Nonnull final SparkContextService sparkContextService) {
        this.dataSource = new DataSourceSupplier(datasource);
        this.dialect = JdbcDialects$.MODULE$.get(datasource.getDatabaseConnectionUrl());
        this.sparkContextService = sparkContextService;
        this.sql = sql;
        this.sqlContext = sqlContext;
    }

    /**
     * Executes the SQL query and returns the result.
     */
    @Nonnull
    @Override
    public TransformResult get() {
        return new JdbcTemplate(dataSource.get()).query(sql, this);
    }

    @Override
    public TransformResult extractData(@Nonnull final ResultSet rs) throws SQLException {
        final ResultSetMetaData metaData = rs.getMetaData();
        final TransformResult result = new TransformResult();
        final StructType schema = extractSchema(metaData, result);

        // Create data set
        final Function0<Connection> getConnection = ScalaUtil.wrap(Suppliers.compose(JdbcUtil.getDataSourceConnection(), dataSource));
        final Function1<ResultSet, Row> mapRow = ScalaUtil.wrap(new RowTransform());
        //noinspection RedundantCast,unchecked
        final ClassTag<Row> classTag = (ClassTag) ClassTag$.MODULE$.apply(Row.class);

        final RDD<Row> rdd = new JdbcRDD<Row>(sqlContext.sparkContext(), getConnection, "SELECT * FROM (" + sql + ") rdd WHERE ? = ?", 1, 1, 1, mapRow, classTag);
        result.setDataSet(sparkContextService.toDataSet(sqlContext, rdd.toJavaRDD(), schema));

        return result;
    }

    /**
     * Builds the Spark SQL schema from the specified result set.
     */
    @Nonnull
    private StructType extractSchema(@Nonnull final ResultSetMetaData rsmd, @Nonnull final TransformResult result) throws SQLException {
        final int columnCount = rsmd.getColumnCount();
        final List<QueryResultColumn> columns = new ArrayList<>(columnCount);
        final Map<String, Integer> displayNameMap = new HashMap<>();
        final StructField[] fields = new StructField[columnCount];

        for (int i = 0; i < columnCount; ++i) {
            final String columnLabel = rsmd.getColumnLabel(i + 1);
            final String columnName = rsmd.getColumnName(i + 1);
            final int columnType = rsmd.getColumnType(i + 1);
            final String columnTypeName = rsmd.getColumnTypeName(i + 1);
            final int precision = rsmd.getPrecision(i + 1);
            final int scale = rsmd.getScale(i + 1);
            final boolean isNullable = rsmd.isNullable(i + 1) != ResultSetMetaData.columnNoNulls;
            final boolean isSigned = rsmd.isSigned(i + 1);

            final DefaultQueryResultColumn column = new DefaultQueryResultColumn();
            column.setField(columnName);
            column.setHiveColumnLabel(columnLabel);
            final String displayName = StringUtils.contains(columnLabel, ".") ? StringUtils.substringAfterLast(columnLabel, ".") : columnLabel;
            Integer count = 0;
            if (displayNameMap.containsKey(displayName)) {
                count = displayNameMap.get(displayName);
                count++;
            }
            displayNameMap.put(displayName, count);
            column.setDisplayName(displayName + "" + (count > 0 ? count : ""));
            column.setTableName(StringUtils.substringAfterLast(columnName, "."));
            column.setDataType(ParserHelper.sqlTypeToHiveType(columnType));
            column.setNativeDataType(columnTypeName);
            if (scale != 0) {
                column.setPrecisionScale(precision + "," + scale);
            } else if (precision != 0) {
                column.setPrecisionScale(Integer.toString(precision));
            }
            columns.add(column);

            final MetadataBuilder metadata = new MetadataBuilder();
            final Option<DataType> oct = dialect.getCatalystType(columnType, columnTypeName, precision, metadata);
            DataType catalystType;
            if (oct.isDefined()) {
                catalystType = oct.get();
            } else {
                catalystType = getCatalystType(columnType, precision, scale, isSigned);
            }
            fields[i] = new StructField(columnLabel, catalystType, isNullable, metadata.build());
        }

        result.setColumns(columns);
        return new StructType(fields);
    }

    /**
     * Gets the Spark SQL data type for the specified JDBC data type.
     */
    @Nonnull
    @SuppressWarnings("squid:S1479")
    private DataType getCatalystType(final int sqlType, final int precision, final int scale, final boolean signed) {
        switch (sqlType) {
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
                return DataTypes.BinaryType;

            case Types.BIT:
            case Types.BOOLEAN:
                return DataTypes.BooleanType;

            case Types.DATE:
                return DataTypes.DateType;

            case Types.DECIMAL:
            case Types.NUMERIC:
                if (precision != 0 || scale != 0) {
                    return new DecimalType(precision, scale);
                } else {
                    return DecimalType.SYSTEM_DEFAULT();
                }

            case Types.DOUBLE:
            case Types.REAL:
                return DataTypes.DoubleType;

            case Types.FLOAT:
                return DataTypes.FloatType;

            case Types.SMALLINT:
            case Types.TINYINT:
                return DataTypes.IntegerType;

            case Types.INTEGER:
                return signed ? DataTypes.IntegerType : DataTypes.LongType;

            case Types.ROWID:
                return DataTypes.LongType;

            case Types.BIGINT:
                return signed ? DataTypes.LongType : new DecimalType(20, 0);

            case Types.CHAR:
            case Types.CLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NCLOB:
            case Types.NVARCHAR:
            case Types.REF:
            case Types.SQLXML:
            case Types.STRUCT:
            case Types.VARCHAR:
                return DataTypes.StringType;

            case Types.TIME:
            case Types.TIMESTAMP:
                return DataTypes.TimestampType;

            default:
                log.debug("Unsupported SQL type: {}", sqlType);
                return DataTypes.StringType;
        }
    }
}
