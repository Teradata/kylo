package com.thinkbiganalytics.jobrepo.repository;

import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import org.joda.time.DateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthutton on 7/26/15.
 */
public class RepositoryUtils {

    /**
     * Corrects for timezone. The date-time is stored in MYSQL as UTC (without encoding timezone)
     * and interpreted by JOOQ as local time
     */
    public static DateTime utcDateTimeCorrection(DateTime utcDate) {
        DateTime newUTC = null;
        if (utcDate == null) {
            newUTC = new DateTime();
        } else { 
            newUTC = utcDate;
/*
        	String isoDate = utcDate.toDateTimeISO().toString();
            if (!isoDate.endsWith("Z")) {
            	// Strip the timezone information and replace it
                int tzIndex = isoDate.lastIndexOf("-");
                isoDate = isoDate.substring(0, tzIndex-1)+"-00:00";
            }
            org.joda.time.format.DateTimeFormatter parser    = ISODateTimeFormat.dateTimeParser();
            newUTC = parser.parseDateTime(isoDate);
*/
        }
        return newUTC;
    }

    public static DateTime utcDateTimeCorrection(Date utcDate) {
        if (utcDate != null) {
            return utcDateTimeCorrection(new DateTime(utcDate));
        } else {
            return utcDateTimeCorrection((DateTime)null);
        }
    }

    public static  Map<String,Object> convertJobParameters(JobParameters jobParameters){
        Map<String,Object> params = new HashMap<>();
        for(Map.Entry<String,JobParameter> entry: jobParameters.getParameters().entrySet()){
            String key = entry.getKey();
            params.put(key,entry.getValue().getValue());
        }
        return params;
    }

    public static ExecutionStatus convertBatchStatus(BatchStatus batchStatus){
        return ExecutionStatus.valueOf(batchStatus.name());
    }


}
