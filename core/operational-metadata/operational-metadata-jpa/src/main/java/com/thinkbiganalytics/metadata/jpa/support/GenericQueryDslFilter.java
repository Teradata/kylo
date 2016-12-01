package com.thinkbiganalytics.metadata.jpa.support;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.thinkbiganalytics.metadata.api.SearchCriteria;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Apply a Filter to a given QueryDSL object Filter based upon supplying a list of {@code SearchCriteria} classes or a Filter String: <column><operator><value> where <column> is the jpaColumn name,
 * <operator> is a valid {@code this#operators} Operator, and <value> is some string value
 *
 * Example usage in a Provider:
 *
 *  public Page<? extends BatchJobExecution> findAll(String filter, Pageable pageable) {
      QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;
      return jobExecutionRepository.findAll(GenericQueryDslFilter.buildFilter(jobExecution, filter), pageable);
    }
 *
 * Created by sr186054 on 11/29/16.
 */
public class GenericQueryDslFilter {

    private static final Logger log = LoggerFactory.getLogger(GenericQueryDslFilter.class);

    /**
     * Map of the String operator to respective QueryDSL operator
     */
    static final ImmutableMap<String, Operator> operators =
        new ImmutableMap.Builder<String, Operator>()
            .put("==", Ops.EQ)
            .put("!=", Ops.NE)
            .put(">", Ops.GT)
            .put("<", Ops.LT)
            .put(">=", Ops.GOE)
            .put("<=", Ops.LOE)
            .put("=~", Ops.LIKE_IC)
            .build();

    /**
     * Map of the QueryDSL operator to its respective QueryDSL path classes Some operators only work with specific Types This map will be used in validation when an operator of a filter is added to
     * ensure it will be applied correctly in the Query
     */
    static final ImmutableMultimap<Operator, Class<? extends Path>> operatorPaths =
        new ImmutableMultimap.Builder<Operator, Class<? extends Path>>()
            .put(Ops.GT, NumberPath.class)
            .put(Ops.LT, NumberPath.class)
            .put(Ops.GOE, NumberPath.class)
            .put(Ops.LOE, NumberPath.class)
            .put(Ops.LIKE_IC, StringPath.class)
            .build();

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
                        booleanBuilder.and(Expressions.predicate(operators.get(filter.getOperation()), p, getValueForQuery(p, value)));
                    }
                }
            });
        }
        return booleanBuilder;
    }

    /**
     * Buld the QueryDSL where filter from the filter string
     *
     * @param basePath     Example: QJpaBatchJobExecution.jpaBatchJobExecution
     * @param filterString <column><operator><value> Example: jobinstance.name==jobName,jobExcutionId>=200.  that will search for all jobs named 'jobName' and jobExecutionId >= 200
     */
    public static BooleanBuilder buildFilter(EntityPathBase basePath, String filterString) {
        List<SearchCriteria> searchCriterias = new ArrayList<>();
        if (StringUtils.isNotBlank(filterString)) {

            //first match and split on , for various filters
            String[] filterConditions = filterString.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
            List<String> filterConditionsList = Arrays.asList(filterConditions);

            //Pattern used to match the <column><operator><value>
            Pattern columnOperatorValuePattern = Pattern.compile("(.*)(==|>=|<=|=~|<|>)(.*)");

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
        return buildFilter(basePath, searchCriterias);


    }


    /**
     * Validate the Operator is ok for the passed in Path. If not it will exclude it from the query and log a warning as to why
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

    /**
     * converts the String filter value to the proper QueryDSL Type based upon the Path provided
     */
    private static Expression getValueForQuery(Path path, Object value) {
        Class type = path.getType();
        Object o = beanUtilsBean.getConvertUtils().convert(value, type);
        return Expressions.constant(o);
    }

    /**
     * Gets the correct path walking the objects if supplied via dot notation. Example a column value of jobInstance.jobName will return the Path for the jobName If an error occurs it will be logged
     * and not included in the query
     */
    private static Path<?> buildPathFromFilterColumn(EntityPathBase basePath, String column) {
        try {
            return (Path<?>) QueryDslPathInspector.getFieldObject(basePath, column);
        } catch (IllegalAccessException e) {
            log.warn("Unable to add {} to Query filter.  Unable to return the correct Query Path for field: {} on Object: {}, Error: {} ", column, column, basePath, e.getMessage());
        }
        return null;
    }


}
