begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.joda
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|joda
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|field
operator|.
name|DividedDateTimeField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|field
operator|.
name|OffsetDateTimeField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|field
operator|.
name|ScaledDurationField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|Joda
specifier|public
class|class
name|Joda
block|{
DECL|method|forPattern
specifier|public
specifier|static
name|FormatDateTimeFormatter
name|forPattern
parameter_list|(
name|String
name|input
parameter_list|)
block|{
return|return
name|forPattern
argument_list|(
name|input
argument_list|,
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
comment|/**      * Parses a joda based pattern, including some named ones (similar to the built in Joda ISO ones).      */
DECL|method|forPattern
specifier|public
specifier|static
name|FormatDateTimeFormatter
name|forPattern
parameter_list|(
name|String
name|input
parameter_list|,
name|Locale
name|locale
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|input
operator|=
name|input
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|input
operator|==
literal|null
operator|||
name|input
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No date pattern provided"
argument_list|)
throw|;
block|}
name|DateTimeFormatter
name|formatter
decl_stmt|;
if|if
condition|(
literal|"basicDate"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_date"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicDate
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicDateTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_date_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicDateTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicDateTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_date_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicDateTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicOrdinalDate"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_ordinal_date"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicOrdinalDate
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicOrdinalDateTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_ordinal_date_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicOrdinalDateTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicOrdinalDateTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_ordinal_date_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicOrdinalDateTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicTTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_t_Time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicTTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicTTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_t_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicTTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicWeekDate"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_week_date"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicWeekDate
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicWeekDateTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_week_date_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicWeekDateTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"basicWeekDateTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"basic_week_date_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|basicWeekDateTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"date"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|date
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateHour"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_hour"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateHour
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateHourMinute"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_hour_minute"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateHourMinute
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateHourMinuteSecond"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_hour_minute_second"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateHourMinuteSecond
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateHourMinuteSecondFraction"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_hour_minute_second_fraction"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateHourMinuteSecondFraction
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateHourMinuteSecondMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_hour_minute_second_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateHourMinuteSecondMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateOptionalTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_optional_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
comment|// in this case, we have a separate parser and printer since the dataOptionalTimeParser can't print
comment|// this sucks we should use the root local by default and not be dependent on the node
return|return
operator|new
name|FormatDateTimeFormatter
argument_list|(
name|input
argument_list|,
name|ISODateTimeFormat
operator|.
name|dateOptionalTimeParser
argument_list|()
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
name|ISODateTimeFormat
operator|.
name|dateTime
argument_list|()
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
name|locale
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"dateTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"dateTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"date_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|dateTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hour"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|hour
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hourMinute"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"hour_minute"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|hourMinute
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hourMinuteSecond"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"hour_minute_second"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|hourMinuteSecond
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hourMinuteSecondFraction"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"hour_minute_second_fraction"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|hourMinuteSecondFraction
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"hourMinuteSecondMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"hour_minute_second_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|hourMinuteSecondMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"ordinalDate"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"ordinal_date"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|ordinalDate
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"ordinalDateTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"ordinal_date_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|ordinalDateTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"ordinalDateTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"ordinal_date_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|ordinalDateTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|time
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"tTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"t_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|tTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"tTimeNoMillis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"t_time_no_millis"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|tTimeNoMillis
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"weekDate"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"week_date"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|weekDate
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"weekDateTime"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"week_date_time"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|weekDateTime
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"weekyear"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"week_year"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|weekyear
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"weekyearWeek"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|weekyearWeek
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"year"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|year
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"yearMonth"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"year_month"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|yearMonth
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"yearMonthDay"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
operator|||
literal|"year_month_day"
operator|.
name|equals
argument_list|(
name|input
argument_list|)
condition|)
block|{
name|formatter
operator|=
name|ISODateTimeFormat
operator|.
name|yearMonthDay
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|input
argument_list|)
operator|&&
name|input
operator|.
name|contains
argument_list|(
literal|"||"
argument_list|)
condition|)
block|{
name|String
index|[]
name|formats
init|=
name|Strings
operator|.
name|delimitedListToStringArray
argument_list|(
name|input
argument_list|,
literal|"||"
argument_list|)
decl_stmt|;
name|DateTimeParser
index|[]
name|parsers
init|=
operator|new
name|DateTimeParser
index|[
name|formats
operator|.
name|length
index|]
decl_stmt|;
if|if
condition|(
name|formats
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|formatter
operator|=
name|forPattern
argument_list|(
name|input
argument_list|,
name|locale
argument_list|)
operator|.
name|parser
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|DateTimeFormatter
name|dateTimeFormatter
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|formats
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|DateTimeFormatter
name|currentFormatter
init|=
name|forPattern
argument_list|(
name|formats
index|[
name|i
index|]
argument_list|,
name|locale
argument_list|)
operator|.
name|parser
argument_list|()
decl_stmt|;
if|if
condition|(
name|dateTimeFormatter
operator|==
literal|null
condition|)
block|{
name|dateTimeFormatter
operator|=
name|currentFormatter
expr_stmt|;
block|}
name|parsers
index|[
name|i
index|]
operator|=
name|currentFormatter
operator|.
name|getParser
argument_list|()
expr_stmt|;
block|}
name|DateTimeFormatterBuilder
name|builder
init|=
operator|new
name|DateTimeFormatterBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|dateTimeFormatter
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|getPrinter
argument_list|()
argument_list|,
name|parsers
argument_list|)
decl_stmt|;
name|formatter
operator|=
name|builder
operator|.
name|toFormatter
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
try|try
block|{
name|formatter
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
name|input
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid format: ["
operator|+
name|input
operator|+
literal|"]: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
operator|new
name|FormatDateTimeFormatter
argument_list|(
name|input
argument_list|,
name|formatter
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
name|locale
argument_list|)
return|;
block|}
DECL|field|Quarters
specifier|public
specifier|static
specifier|final
name|DurationFieldType
name|Quarters
init|=
operator|new
name|DurationFieldType
argument_list|(
literal|"quarters"
argument_list|)
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|8167713675442491871L
decl_stmt|;
specifier|public
name|DurationField
name|getField
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
operator|new
name|ScaledDurationField
argument_list|(
name|chronology
operator|.
name|months
argument_list|()
argument_list|,
name|Quarters
argument_list|,
literal|3
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|QuarterOfYear
specifier|public
specifier|static
specifier|final
name|DateTimeFieldType
name|QuarterOfYear
init|=
operator|new
name|DateTimeFieldType
argument_list|(
literal|"quarterOfYear"
argument_list|)
block|{
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|5677872459807379123L
decl_stmt|;
specifier|public
name|DurationFieldType
name|getDurationType
parameter_list|()
block|{
return|return
name|Quarters
return|;
block|}
specifier|public
name|DurationFieldType
name|getRangeDurationType
parameter_list|()
block|{
return|return
name|DurationFieldType
operator|.
name|years
argument_list|()
return|;
block|}
specifier|public
name|DateTimeField
name|getField
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
operator|new
name|OffsetDateTimeField
argument_list|(
operator|new
name|DividedDateTimeField
argument_list|(
operator|new
name|OffsetDateTimeField
argument_list|(
name|chronology
operator|.
name|monthOfYear
argument_list|()
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|,
name|QuarterOfYear
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|1
argument_list|)
return|;
block|}
block|}
decl_stmt|;
block|}
end_class

end_unit

