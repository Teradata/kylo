package com.thinkbiganalytics.jobrepo.rest.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Created by sr186054 on 7/23/17.
 */
public class QueryUtils {

    /**
     * This will evaluate the {@code incomingFilter} and append/set the value including the {@code defaultFilter} and return a new String with the updated filter
     */
    public static String ensureDefaultFilter(String incomingFilter, String defaultFilter) {
        String filter = incomingFilter;
        if (StringUtils.isBlank(filter) || !StringUtils.containsIgnoreCase(filter, defaultFilter)) {
            if (StringUtils.isNotBlank(filter)) {
                if (StringUtils.endsWith(filter, ",")) {
                    filter += defaultFilter;
                } else {
                    filter += "," + defaultFilter;
                }
            } else {
                filter = defaultFilter;
            }
        }
        return filter;
    }

    public static PageRequest pageRequest(Integer start, Integer limit, String sort) {
        if (StringUtils.isNotBlank(sort)) {
            Sort.Direction dir = Sort.Direction.ASC;
            if (sort.startsWith("-")) {
                dir = Sort.Direction.DESC;
                sort = sort.substring(1);
            }
            return new PageRequest((start / limit), limit, dir, sort);
        } else {
            return new PageRequest((start / limit), limit);
        }
    }


}
