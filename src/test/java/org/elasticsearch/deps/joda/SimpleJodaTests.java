begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.deps.joda
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|deps
operator|.
name|joda
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|joda
operator|.
name|FormatDateTimeFormatter
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
name|joda
operator|.
name|Joda
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
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchTestCase
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
name|DateTimeZone
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
name|MutableDateTime
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Date
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleJodaTests
specifier|public
class|class
name|SimpleJodaTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testMultiParsers
specifier|public
name|void
name|testMultiParsers
parameter_list|()
block|{
name|DateTimeFormatterBuilder
name|builder
init|=
operator|new
name|DateTimeFormatterBuilder
argument_list|()
decl_stmt|;
name|DateTimeParser
index|[]
name|parsers
init|=
operator|new
name|DateTimeParser
index|[
literal|3
index|]
decl_stmt|;
name|parsers
index|[
literal|0
index|]
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"MM/dd/yyyy"
argument_list|)
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|getParser
argument_list|()
expr_stmt|;
name|parsers
index|[
literal|1
index|]
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"MM-dd-yyyy"
argument_list|)
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|getParser
argument_list|()
expr_stmt|;
name|parsers
index|[
literal|2
index|]
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"yyyy-MM-dd HH:mm:ss"
argument_list|)
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|getParser
argument_list|()
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"MM/dd/yyyy"
argument_list|)
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
expr_stmt|;
name|DateTimeFormatter
name|formatter
init|=
name|builder
operator|.
name|toFormatter
argument_list|()
decl_stmt|;
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"2009-11-15 14:12:12"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsoDateFormatDateTimeNoMillisUTC
specifier|public
name|void
name|testIsoDateFormatDateTimeNoMillisUTC
parameter_list|()
block|{
name|DateTimeFormatter
name|formatter
init|=
name|ISODateTimeFormat
operator|.
name|dateTimeNoMillis
argument_list|()
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|long
name|millis
init|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00Z"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testUpperBound
specifier|public
name|void
name|testUpperBound
parameter_list|()
block|{
name|MutableDateTime
name|dateTime
init|=
operator|new
name|MutableDateTime
argument_list|(
literal|3000
argument_list|,
literal|12
argument_list|,
literal|31
argument_list|,
literal|23
argument_list|,
literal|59
argument_list|,
literal|59
argument_list|,
literal|999
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|DateTimeFormatter
name|formatter
init|=
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
decl_stmt|;
name|String
name|value
init|=
literal|"2000-01-01"
decl_stmt|;
name|int
name|i
init|=
name|formatter
operator|.
name|parseInto
argument_list|(
name|dateTime
argument_list|,
name|value
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|i
argument_list|,
name|equalTo
argument_list|(
name|value
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|dateTime
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2000-01-01T23:59:59.999Z"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsoDateFormatDateOptionalTimeUTC
specifier|public
name|void
name|testIsoDateFormatDateOptionalTimeUTC
parameter_list|()
block|{
name|DateTimeFormatter
name|formatter
init|=
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
decl_stmt|;
name|long
name|millis
init|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00Z"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00.001Z"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00.1Z"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00.1"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970 kuku"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"formatting should fail"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// all is well
block|}
comment|// test offset in format
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00-02:00"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|2
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIsoVsCustom
specifier|public
name|void
name|testIsoVsCustom
parameter_list|()
block|{
name|DateTimeFormatter
name|formatter
init|=
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
decl_stmt|;
name|long
name|millis
init|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970-01-01T00:00:00"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|formatter
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss"
argument_list|)
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
expr_stmt|;
name|millis
operator|=
name|formatter
operator|.
name|parseMillis
argument_list|(
literal|"1970/01/01 00:00:00"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|FormatDateTimeFormatter
name|formatter2
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss"
argument_list|)
decl_stmt|;
name|millis
operator|=
name|formatter2
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
literal|"1970/01/01 00:00:00"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|millis
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testWriteAndParse
specifier|public
name|void
name|testWriteAndParse
parameter_list|()
block|{
name|DateTimeFormatter
name|dateTimeWriter
init|=
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
decl_stmt|;
name|DateTimeFormatter
name|formatter
init|=
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
decl_stmt|;
name|Date
name|date
init|=
operator|new
name|Date
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|formatter
operator|.
name|parseMillis
argument_list|(
name|dateTimeWriter
operator|.
name|print
argument_list|(
name|date
operator|.
name|getTime
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|date
operator|.
name|getTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSlashInFormat
specifier|public
name|void
name|testSlashInFormat
parameter_list|()
block|{
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"MM/yyyy"
argument_list|)
decl_stmt|;
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
literal|"01/2001"
argument_list|)
expr_stmt|;
name|formatter
operator|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss"
argument_list|)
expr_stmt|;
name|long
name|millis
init|=
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
literal|"1970/01/01 00:00:00"
argument_list|)
decl_stmt|;
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|millis
argument_list|)
expr_stmt|;
try|try
block|{
name|millis
operator|=
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
literal|"1970/01/01"
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// it really can't parse this one
block|}
block|}
annotation|@
name|Test
DECL|method|testMultipleFormats
specifier|public
name|void
name|testMultipleFormats
parameter_list|()
block|{
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss||yyyy/MM/dd"
argument_list|)
decl_stmt|;
name|long
name|millis
init|=
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
literal|"1970/01/01 00:00:00"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
literal|"1970/01/01 00:00:00"
argument_list|,
name|is
argument_list|(
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|millis
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMultipleDifferentFormats
specifier|public
name|void
name|testMultipleDifferentFormats
parameter_list|()
block|{
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss||yyyy/MM/dd"
argument_list|)
decl_stmt|;
name|String
name|input
init|=
literal|"1970/01/01 00:00:00"
decl_stmt|;
name|long
name|millis
init|=
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
name|input
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|input
argument_list|,
name|is
argument_list|(
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|millis
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss||yyyy/MM/dd||dateOptionalTime"
argument_list|)
expr_stmt|;
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateOptionalTime||yyyy/MM/dd HH:mm:ss||yyyy/MM/dd"
argument_list|)
expr_stmt|;
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"yyyy/MM/dd HH:mm:ss||dateOptionalTime||yyyy/MM/dd"
argument_list|)
expr_stmt|;
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"date_time||date_time_no_millis"
argument_list|)
expr_stmt|;
name|Joda
operator|.
name|forPattern
argument_list|(
literal|" date_time || date_time_no_millis"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testInvalidPatterns
specifier|public
name|void
name|testInvalidPatterns
parameter_list|()
block|{
name|expectInvalidPattern
argument_list|(
literal|"does_not_exist_pattern"
argument_list|,
literal|"Invalid format: [does_not_exist_pattern]: Illegal pattern component: o"
argument_list|)
expr_stmt|;
name|expectInvalidPattern
argument_list|(
literal|"OOOOO"
argument_list|,
literal|"Invalid format: [OOOOO]: Illegal pattern component: OOOOO"
argument_list|)
expr_stmt|;
name|expectInvalidPattern
argument_list|(
literal|null
argument_list|,
literal|"No date pattern provided"
argument_list|)
expr_stmt|;
name|expectInvalidPattern
argument_list|(
literal|""
argument_list|,
literal|"No date pattern provided"
argument_list|)
expr_stmt|;
name|expectInvalidPattern
argument_list|(
literal|" "
argument_list|,
literal|"No date pattern provided"
argument_list|)
expr_stmt|;
name|expectInvalidPattern
argument_list|(
literal|"||date_time_no_millis"
argument_list|,
literal|"No date pattern provided"
argument_list|)
expr_stmt|;
name|expectInvalidPattern
argument_list|(
literal|"date_time_no_millis||"
argument_list|,
literal|"No date pattern provided"
argument_list|)
expr_stmt|;
block|}
DECL|method|expectInvalidPattern
specifier|private
name|void
name|expectInvalidPattern
parameter_list|(
name|String
name|pattern
parameter_list|,
name|String
name|errorMessage
parameter_list|)
block|{
try|try
block|{
name|Joda
operator|.
name|forPattern
argument_list|(
name|pattern
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Pattern "
operator|+
name|pattern
operator|+
literal|" should have thrown an exception but did not"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
name|errorMessage
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testRounding
specifier|public
name|void
name|testRounding
parameter_list|()
block|{
name|long
name|TIME
init|=
name|utcTimeInMillis
argument_list|(
literal|"2009-02-03T01:01:01"
argument_list|)
decl_stmt|;
name|MutableDateTime
name|time
init|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|TIME
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|monthOfYear
argument_list|()
operator|.
name|roundFloor
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-01T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|TIME
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|hourOfDay
argument_list|()
operator|.
name|roundFloor
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-03T01:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|TIME
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|dayOfMonth
argument_list|()
operator|.
name|roundFloor
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-03T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRoundingSetOnTime
specifier|public
name|void
name|testRoundingSetOnTime
parameter_list|()
block|{
name|MutableDateTime
name|time
init|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|time
operator|.
name|setRounding
argument_list|(
name|time
operator|.
name|getChronology
argument_list|()
operator|.
name|monthOfYear
argument_list|()
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-03T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-01T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-01T00:00:00.000Z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-05-03T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-05-01T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-05-01T00:00:00.000Z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
expr_stmt|;
name|time
operator|.
name|setRounding
argument_list|(
name|time
operator|.
name|getChronology
argument_list|()
operator|.
name|dayOfMonth
argument_list|()
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-03T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-03T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-03T00:00:00.000Z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-02T23:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-02T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-02T00:00:00.000Z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
expr_stmt|;
name|time
operator|.
name|setRounding
argument_list|(
name|time
operator|.
name|getChronology
argument_list|()
operator|.
name|weekOfWeekyear
argument_list|()
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2011-05-05T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2011-05-02T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2011-05-02T00:00:00.000Z"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRoundingWithTimeZone
specifier|public
name|void
name|testRoundingWithTimeZone
parameter_list|()
block|{
name|MutableDateTime
name|time
init|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|time
operator|.
name|setZone
argument_list|(
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
operator|-
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|.
name|setRounding
argument_list|(
name|time
operator|.
name|getChronology
argument_list|()
operator|.
name|dayOfMonth
argument_list|()
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
name|MutableDateTime
name|utcTime
init|=
operator|new
name|MutableDateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|utcTime
operator|.
name|setRounding
argument_list|(
name|utcTime
operator|.
name|getChronology
argument_list|()
operator|.
name|dayOfMonth
argument_list|()
argument_list|,
name|MutableDateTime
operator|.
name|ROUND_FLOOR
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-03T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|utcTime
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-03T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-02T00:00:00.000-02:00"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|utcTime
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-03T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
comment|// the time is on the 2nd, and utcTime is on the 3rd, but, because time already encapsulates
comment|// time zone, the millis diff is not 24, but 22 hours
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTime
operator|.
name|getMillis
argument_list|()
operator|-
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|22
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|time
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-04T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|utcTime
operator|.
name|setMillis
argument_list|(
name|utcTimeInMillis
argument_list|(
literal|"2009-02-04T01:01:01"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-03T00:00:00.000-02:00"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|utcTime
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2009-02-04T00:00:00.000Z"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|time
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|utcTime
operator|.
name|getMillis
argument_list|()
operator|-
name|TimeValue
operator|.
name|timeValueHours
argument_list|(
literal|22
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|utcTimeInMillis
specifier|private
name|long
name|utcTimeInMillis
parameter_list|(
name|String
name|time
parameter_list|)
block|{
return|return
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
operator|.
name|parseMillis
argument_list|(
name|time
argument_list|)
return|;
block|}
block|}
end_class

end_unit

