package com.thinkbiganalytics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.convert.DurationConverter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;
import org.springframework.util.ReflectionUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sr186054 on 2/29/16.
 */
public class DateTimeUtil {
/*
    public static Date convertToUTC(Date date) {
        DateTime time = new DateTime(date.getTime());
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime utc = time.withZone(dtZone);
        return utc.toDate();
    }

    public static DateTime convertToUTC(DateTime date) {
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime utc = date.withZone(dtZone);
        return new DateTime(utc);
    }
    */
public static Date convertToUTC(Date date) {
    DateTimeZone tz = DateTimeZone.getDefault();
    Date utc = new Date(tz.convertLocalToUTC(date.getTime(), false));
    return utc;
}

    public static DateTime convertToUTC(DateTime date) {
        DateTimeZone tz = DateTimeZone.getDefault();
        Date utc = new Date(tz.convertLocalToUTC(date.getMillis(), false));
        return new DateTime(utc);
    }

    public static Date getUTCTime() {
        return convertToUTC(new Date());
    }

    public static DateTime getNowUTCTime() {
        return convertToUTC(DateTime.now());
    }


    static PeriodParser periodParser = new PeriodFormatterBuilder()
        .appendYears().appendSuffix("Y").appendSeparatorIfFieldsAfter(" ")
        .appendMonths().appendSuffix("M").appendSeparatorIfFieldsAfter(" ")
        .appendWeeks().appendSuffix("W").appendSeparatorIfFieldsAfter(" ")
        .appendDays().appendSuffix("D").appendSeparatorIfFieldsAfter(" ")
        .appendHours().appendSuffix("h").appendSeparatorIfFieldsAfter(" ")
        .appendMinutes().appendSuffix("m").appendSeparatorIfFieldsAfter(" ")
        .appendMinutes().appendSuffix("s")
        .toParser();


    public static class StringPeriodParser implements PeriodParser {

        enum DATE_PART {
            YEAR("Y"),MONTH("M"),WEEK("W"),DAY("D"),HOUR("h"),MINUTE("m"),SECOND("s");
            private String abbreviation;

            DATE_PART(String abbreviation){
                this.abbreviation = abbreviation;
            }
        }


        private Integer getValue(String part, String unit){
            String val = part.substring(0, part.indexOf(unit));
            return val.length() > 0 ? Integer.valueOf(val) : 0;
        }

        private DATE_PART getDatePart(String part){
            if(part.contains(DATE_PART.YEAR.abbreviation)){
                return DATE_PART.YEAR;
            }
            else if(part.contains(DATE_PART.MONTH.abbreviation)){
                return DATE_PART.MONTH;
            }
            else if(part.contains(DATE_PART.WEEK.abbreviation)){
                return DATE_PART.WEEK;
            }
            else if(part.contains(DATE_PART.DAY.abbreviation)){
                return DATE_PART.DAY;
            }
            else if(part.contains(DATE_PART.HOUR.abbreviation)){
                return DATE_PART.HOUR;
            }
            else if(part.contains(DATE_PART.MINUTE.abbreviation)){
                return DATE_PART.MINUTE;
            }
            else if(part.contains(DATE_PART.SECOND.abbreviation)){
                return DATE_PART.SECOND;
            }
            else {
                return null;
            }
        }

        private void addToPeriod(ReadWritablePeriod period, String part){
            DATE_PART datePart = getDatePart(part);
            Integer value = getValue(part,datePart.abbreviation);

            switch(datePart){
                case YEAR:
                    period.addYears(getValue(part,datePart.abbreviation));
                    break;
                case MONTH:
                    period.addMonths(getValue(part, datePart.abbreviation));
                    break;
                case WEEK:
                    period.addWeeks(getValue(part, datePart.abbreviation));
                    break;
                case DAY:
                    period.addDays(getValue(part, datePart.abbreviation));
                    break;
                case HOUR:
                    period.addHours(getValue(part, datePart.abbreviation));
                    break;
                case MINUTE:
                    period.addMinutes(getValue(part, datePart.abbreviation));
                    break;
                case SECOND:
                    period.addSeconds(getValue(part, datePart.abbreviation));
                    break;
                default:
                    break;
            }
        }


        @Override
        public int parseInto(ReadWritablePeriod period, String periodStr,
                             int position, Locale locale) {
            String parts[] = periodStr.split(" ");
            period.addYears(0);
            period.addMonths(0);
            period.addWeeks(0);
            period.addDays(0);
            period.addHours(0);
            period.addMinutes(0);
            period.addSeconds(0);
            Arrays.asList(parts).stream().forEach(part ->  addToPeriod(period,part));
            return periodStr.length();
        }
    }



    static PeriodFormatter periodFormatter  = new PeriodFormatterBuilder().append(null,new StringPeriodParser()).toFormatter();

    public static Period period(String period){

        return periodFormatter.parsePeriod(period);
    }
}
