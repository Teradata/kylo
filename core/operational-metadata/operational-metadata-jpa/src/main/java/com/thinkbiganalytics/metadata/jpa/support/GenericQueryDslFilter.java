package com.thinkbiganalytics.metadata.jpa.support;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.OperationImpl;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.TemplateExpressionImpl;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumExpression;
import com.querydsl.core.types.dsl.EnumOperation;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.EnumTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.thinkbiganalytics.Formatters;
import com.thinkbiganalytics.metadata.api.SearchCriteria;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Apply a Filter to a given QueryDSL object Filter based upon supplying a list of {@code SearchCriteria} classes or a Filter String: <column><operator><value> where <column> is the jpaColumn name,
 * <operator> is a valid {@code #operators} Operator, and <value> is some string value
 *
 * Example usage in a Provider:
 *
 * public Page<? extends BatchJobExecution> findAll(String filter, Pageable pageable) {
 * QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;
 * return jobExecutionRepository.findAll(GenericQueryDslFilter.buildFilter(jobExecution, filter), pageable);
 * }
 */
public class GenericQueryDslFilter {

    /**
     * Map of the String operator to respective QueryDSL operator
     */
    static final ImmutableMap<String, Operator> operators =
        new ImmutableMap.Builder<String, Operator>()
            .put("==", Ops.EQ_IGNORE_CASE)
            .put("!=", Ops.NE)
            .put(">=", Ops.GOE)
            .put("<=", Ops.LOE)
            .put("=~", Ops.LIKE_IC)
            .put(">", Ops.GT)
            .put("<", Ops.LT)
            .build();
    /**
     * Map of the QueryDSL operator to its respective QueryDSL path classes Some operators only work with specific Types This map will be used in validation when an operator of a filter is added to
     * ensure it will be applied correctly in the Query
     */
    static final ImmutableMultimap<Operator, Class<? extends Path>> operatorPaths =
        new ImmutableMultimap.Builder<Operator, Class<? extends Path>>()
            .put(Ops.GT, NumberPath.class)
            .put(Ops.GT, ComparablePath.class)
            .put(Ops.LT, NumberPath.class)
            .put(Ops.LT, ComparablePath.class)
            .put(Ops.GOE, NumberPath.class)
            .put(Ops.GOE, ComparablePath.class)
            .put(Ops.LOE, NumberPath.class)
            .put(Ops.LOE, ComparablePath.class)
            .put(Ops.LIKE_IC, StringPath.class)
            .put(Ops.LIKE_IC, ComparablePath.class)
            .build();
    private static final Logger log = LoggerFactory.getLogger(GenericQueryDslFilter.class);
    public static final String NULL_FILTER = "NULL";
    public static final String NOT_NULL_FILTER = "NOT_NULL";
    /**
     * Custom converter used to convert the filter String value to the respective Value on the QueryDSL/JPA object A custom converter is needed to add the Enum conversion types
     */
    static BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean() {
        @Override
        public Object convert(Object value, Class clazz) {
            if (clazz.isEnum()) {
                return Enum.valueOf(clazz, value.toString());
            } else {
                return super.convert(value, clazz);
            }
        }

        public Object convert(String value, Class clazz) {
            if (clazz.isEnum()) {
                return Enum.valueOf(clazz, value.toString());
            } else {
                return super.convert(value, clazz);
            }
        }
    });


    /**
     * build the Filter for the base QueryDSL class and the passed in filter list
     *
     * @param filters name:test, age>10
     */
    public static <T> BooleanBuilder buildFilter(EntityPathBase basePath, List<SearchCriteria> filters) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (filters != null && !filters.isEmpty()) {
            filters.stream().forEach(filter -> {
                Path p = buildPathFromFilterColumn(basePath, filter.getKey());
                if (p != null) {
                    Object value = filter.getValue();
                    Operator op = operators.get(filter.getOperation());
                    if (validateOperatorPath(op, p, filter.getKey(), value)) {
                        if (Ops.LIKE_IC.equals(op) && value instanceof String && value != null && !((String) value).endsWith("%")) {
                            value = ((String) value) + "%";
                        }
                        if (value == null || (value instanceof String && ((String) value).equalsIgnoreCase(NULL_FILTER))) {
                            op = Ops.IS_NULL;
                            value = null;
                        } else if (value instanceof String && ((String) value).equalsIgnoreCase(NOT_NULL_FILTER)) {
                            op = Ops.IS_NOT_NULL;
                            value = null;
                        }

                        if (value != null) {
                            Object convertedValue = getValueForQuery(p, filter.getKey(), op, value);
                            if (convertedValue != null && convertedValue instanceof Collection) {
                                op = Ops.IN;
                            }
                            if (convertedValue != null) {
                                Expression e = null;
                                if(convertedValue instanceof Comparable) {
                                    e = Expressions.asComparable((Comparable) convertedValue);
                                }
                                else {
                                    e = Expressions.constant(convertedValue);
                                }

                                //reset the operator if looking for UUID
                                if(convertedValue instanceof UUID || convertedValue instanceof Enum){
                                    op = Ops.EQ;
                                }


                                //reset the operator if looking for UUID
                               // if(convertedValue instanceof UUID){
                               //     op = Ops.EQ;
                               // }

                                if(filter.isOrFilter()) {
                                     booleanBuilder.or(Expressions.predicate(op, p, e));
                                    }
                                    else {
                                    booleanBuilder.and(Expressions.predicate(op, p, e));
                                }
                            }

                        } else {
                            if(filter.isOrFilter()) {
                                booleanBuilder.or(Expressions.predicate(op, p));
                            }
                            else {
                                booleanBuilder.and(Expressions.predicate(op, p));
                            }
                        }

                    }
                }
            });
        }
        return booleanBuilder;
    }


    /**
     * Build the QueryDSL where filter from the filter string
     *
     * Example filter strings:
     * endTime==NULL  will be  where JOB_EXECUTION.END_TIME IS NULL
     * endTime==NOT_NULL  will be  where JOB_EXECUTION.END_TIME IS NOT NULL
     * jobinstance.name==jobName,jobExcutionId>=200  will be  where JOB_EXECUTION.JOB_INSTANCE.JOB_NAME = 'jobName' AND JOB_EXECUTION.JOB_EXECUTION_ID >=200
     * jobInstance.feed.feedType==FEED,jobInstance.feed.name==movies.new_releases will be JOB_EXECUTION.JOB_INSTANCE.FEED.FEED_TYPE = 'FEED' AND JOB_EXECUTION.JOB_INSTANCE.FEED = 'movies.new_releases'
     * status=="COMPLETED,FAILED" -  in clause.  This is mapped with == and the values surrounded by quotes \"VALUE1,VALUE2\"
     *
     * @param basePath     Example: QJpaBatchJobExecution.jpaBatchJobExecution
     * @param filterString <column><operator><value> Example: jobinstance.name==jobName,jobExcutionId>=200.  that will search for all jobs named 'jobName' and jobExecutionId >= 200
     */
    public static BooleanBuilder buildFilter(EntityPathBase basePath, String filterString) {
        List<SearchCriteria> searchCriterias = parseFilterString(filterString);
        return buildFilter(basePath, searchCriterias);
    }
    public static BooleanBuilder buildOrFilter(EntityPathBase basePath, String filterString) {
        List<SearchCriteria> searchCriterias = parseFilterString(filterString);
        searchCriterias.stream().forEach(searchCriteria -> searchCriteria.setAndOr(SearchCriteria.AND_OR.OR));
        return buildFilter(basePath, searchCriterias);
    }

    /**
     * convert a passed in filter string to a list of criteria objects
     *
     * @param filterString a filter, <column><operator><value> Example: jobinstance.name==jobName,jobExcutionId>=200.  that will search for all jobs named 'jobName' and jobExecutionId >= 200
     * @return a list of criteria objects
     */
    public static List<SearchCriteria> parseFilterString(String filterString) {
        List<SearchCriteria> searchCriterias = new ArrayList<>();
        if (StringUtils.isNotBlank(filterString)) {

            //first match and split on , for various filters
            String[] filterConditions = filterString.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
            List<String> filterConditionsList = Arrays.asList(filterConditions);

            //Pattern used to match the <column><operator><value>
            String validOperatorsRegEx = operators.keySet().stream().map(key -> key).collect(Collectors.joining("|"));
            Pattern columnOperatorValuePattern = Pattern.compile("(.*)(" + validOperatorsRegEx + ")(.*)");

            filterConditionsList.stream().forEach(filter -> {
                Matcher matcher = columnOperatorValuePattern.matcher(filter);
                while (matcher.find()) {
                    String field = matcher.group(1);
                    String operator = matcher.group(2);
                    String value = matcher.group(3);
                    searchCriterias.add(new SearchCriteria(field, operator, value));
                }
            });
        }
        return searchCriterias;
    }


    /**
     * Validate the Operator is ok for the passed in Path. If not it will exclude it from the query and log a warning as to why
     *
     * @param operator the operator
     * @param path     the path to the field/column
     * @param column   the column to filter
     * @param value    the value to look for/match
     * @return true if the operator is valid for the given column data type, false if not
     */
    private static boolean validateOperatorPath(Operator operator, Path path, String column, Object value) {
        if (operatorPaths.containsKey(operator)) {
            ImmutableCollection<Class<? extends Path>> validPathClasses = operatorPaths.get(operator);
            boolean valid = validPathClasses.stream().anyMatch(p -> path.getClass().isAssignableFrom(p));
            if (!valid) {
                log.warn(
                    "Filter {} {} {} will not be applied.  Operator {} is not valid for the field {}.  It will not be applied to the query.  The Field {} is a type of {}.  it needs to be a type of {} ",
                    column, operator, value, operator, column, column, path, validPathClasses);
            }
            return valid;
        } else {
            return true;
        }
    }

    private static Long convertDateTimeStringToMillis(String filterKey, Object value) {
        Long millis = null;
        if (CommonFilterTranslations.isDateStoredAsMillisField(filterKey) && value instanceof String) {
            //attempt to convert the incoming value to a long
            try {
                DateTime dateTime = Formatters.parseDateTime((String) value);
                millis = dateTime.getMillis();
            } catch (IllegalArgumentException e) {
                //uanble to parse the value
            }
        }
        return millis;
    }

    private static UUID convertToUUID(Object value){
        if(value instanceof UUID){
            return (UUID)value;
        }
        else if(value instanceof  String) {
            String stringValue = (String)value;
            if (((String) value).endsWith("%")) {
                stringValue = StringUtils.substringBeforeLast(stringValue, "%");
            }
            try {
                UUID uuid = UUID.fromString(stringValue);
                return uuid;
            } catch (IllegalArgumentException e) {
                //unable to convert to uuid
            }
        }
      return  null;
    }

    /**
     * converts the String filter value to the proper QueryDSL Type based upon the Path provided
     *
     * @param path  the path to the field to query
     * @param op    the operator to use
     * @param value the value to match
     * @return the object value applying a comma separated string to a list of objects for IN clause functions
     */
    private static Object getValueForQuery(Path path, String filterKey, Operator op, Object value) {
        Class type = path.getType();
        Object o = null;
        boolean isDateTimeMillisField = CommonFilterTranslations.isDateStoredAsMillisField(filterKey);

        if (Ops.EQ.equals(op) || Ops.EQ_IGNORE_CASE.equals(op)) {
            // handle the in clause correctly
            if (value instanceof String) {
                String sValue = (String) value;
                //remove the Quotes
                sValue = sValue.replace("\"", "");
                //split on ,
                String[] items = StringUtils.split(sValue, ",");
                //add to List
                List<Object> convertedItems = new ArrayList<>();
                for (String item : items) {
                    try {
                        Object convertedListItem = null;
                        if (isDateTimeMillisField) {
                            convertedListItem = convertDateTimeStringToMillis(filterKey, item);
                        }
                        if (convertedListItem == null) {
                            if(type.isAssignableFrom(UUID.class)){
                                convertedListItem = convertToUUID(item);
                            }
                            else {
                                convertedListItem = beanUtilsBean.getConvertUtils().convert(item, type);
                            }
                        }
                        if(convertedListItem != null) {
                            convertedItems.add(convertedListItem);
                        }
                    } catch (Exception e) {
                        //handle conversion error
                    }
                }
                if(convertedItems.size() ==1) {
                    o = convertedItems.get(0);
                }
                else {
                    o = convertedItems;
                }

            }
        } else {
            if (isDateTimeMillisField) {
                o = convertDateTimeStringToMillis(filterKey, value);
            }
            if (o == null) {
                if(value instanceof  String && type.isAssignableFrom(UUID.class)){
                    o = convertToUUID(value);
                } else {
                    o = beanUtilsBean.getConvertUtils().convert(value, type);
                }
            }
        }
        return o;
    }

    /**
     * Gets the correct path walking the objects if supplied via dot notation. Example a column value of jobInstance.jobName will return the Path for the jobName If an error occurs it will be logged
     * and not included in the query
     */
    private static Path<?> buildPathFromFilterColumn(EntityPathBase basePath, String column) {
        try {

            String resolvedFilter = CommonFilterTranslations.resolvedFilter(basePath, column);
            return (Path<?>) QueryDslPathInspector.getFieldObject(basePath, resolvedFilter);
        } catch (IllegalAccessException e) {
            log.warn("Unable to add {} to Query filter.  Unable to return the correct Query Path for field: {} on Object: {}, Error: {} ", column, column, basePath, e.getMessage());
        }
        return null;
    }


}
