/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.model;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 12/22/15.
 */
public class DailyJobStatusCountResult implements DailyJobStatusCount {

    private DatabaseQuerySubstitution.DATE_PART datePart;
    private Integer interval;
    private List<JobStatusCount> jobStatusCounts;

    public DailyJobStatusCountResult(DatabaseQuerySubstitution.DATE_PART datePart, Integer interval, List<JobStatusCount> jobStatusCounts) {
        this.datePart = datePart;
        this.interval = interval;
        this.jobStatusCounts = jobStatusCounts;
    }

    @Override
    public DatabaseQuerySubstitution.DATE_PART getDatePart() {
        return datePart;
    }

    @Override
    public void setDatePart(DatabaseQuerySubstitution.DATE_PART datePart) {
        this.datePart = datePart;
    }

    @Override
    public Integer getInterval() {
        return interval;
    }

    @Override
    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    @Override
    public List<JobStatusCount> getJobStatusCounts() {
        return jobStatusCounts;
    }

    @Override
    public void setJobStatusCounts(List<JobStatusCount> jobStatusCounts) {
        this.jobStatusCounts = jobStatusCounts;
    }

    private JobStatusCount findMin(){

        Ordering<JobStatusCount> o = new Ordering<JobStatusCount>() {
            @Override
            public int compare(JobStatusCount left, JobStatusCount right) {
                return Longs.compare(left.getDate().getTime(), right.getDate().getTime());
            }
        };
        return o.min(jobStatusCounts);
    }

    @Override
    public void checkAndAddStartDate(){
        if(!hasStartDate()){
            //add in a startDate to the Date List
            if(jobStatusCounts != null && !jobStatusCounts.isEmpty()){
                JobStatusCount first = jobStatusCounts.get(0);
                JobStatusCount min = new JobStatusCountResult(first);
                min.setDate(getStartDate());
                min.setCount(new Long(0));
                getJobStatusCounts().add(min);
            }

        }
    }

    private boolean hasStartDate(){
        if(jobStatusCounts != null && !jobStatusCounts.isEmpty()) {
            Date minDate = findMin().getDate();
            Date startDate = getStartDate();
            if (minDate != null && startDate != null) {
                return DateUtils.isSameDay(minDate, getStartDate()) || minDate.before(startDate);
            }
        }
        return false;
    }

    private Date getStartDate(){
        switch(datePart) {
            case DAY:
                return  DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -interval);
            case WEEK:
                return  DateUtils.addWeeks(DateUtils.truncate(new Date(), Calendar.DATE), -interval);
            case MONTH:
                return  DateUtils.addMonths(DateUtils.truncate(new Date(), Calendar.DATE), -interval);
            case YEAR:
                return  DateUtils.addYears(DateUtils.truncate(new Date(), Calendar.DATE), -interval);
        }
        return null;
    }

    private Date now(){
        return DateUtils.truncate(new Date(), Calendar.DATE);
    }

    private Date tomorrow(){
       return  DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), 1);
    }

    private Date yesterday(){
        return  DateUtils.addDays(DateUtils.truncate(new Date(), Calendar.DATE), -1);
    }


}
