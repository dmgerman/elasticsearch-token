begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_class
DECL|class|DateMathParserTests
specifier|public
class|class
name|DateMathParserTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|formatter
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateOptionalTime"
argument_list|)
decl_stmt|;
DECL|field|parser
name|DateMathParser
name|parser
init|=
operator|new
name|DateMathParser
argument_list|(
name|formatter
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
DECL|method|assertDateMathEquals
name|void
name|assertDateMathEquals
parameter_list|(
name|String
name|toTest
parameter_list|,
name|String
name|expected
parameter_list|)
block|{
name|assertDateMathEquals
argument_list|(
name|toTest
argument_list|,
name|expected
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|assertDateMathEquals
name|void
name|assertDateMathEquals
parameter_list|(
name|String
name|toTest
parameter_list|,
name|String
name|expected
parameter_list|,
name|long
name|now
parameter_list|,
name|boolean
name|roundUp
parameter_list|,
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
name|DateMathParser
name|parser
init|=
operator|new
name|DateMathParser
argument_list|(
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateOptionalTime"
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
name|long
name|gotMillis
init|=
name|parser
operator|.
name|parse
argument_list|(
name|toTest
argument_list|,
name|now
argument_list|,
name|roundUp
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|long
name|expectedMillis
init|=
name|parser
operator|.
name|parse
argument_list|(
name|expected
argument_list|,
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|gotMillis
operator|!=
name|expectedMillis
condition|)
block|{
name|fail
argument_list|(
literal|"Date math not equal\n"
operator|+
literal|"Original              : "
operator|+
name|toTest
operator|+
literal|"\n"
operator|+
literal|"Parsed                : "
operator|+
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|gotMillis
argument_list|)
operator|+
literal|"\n"
operator|+
literal|"Expected              : "
operator|+
name|expected
operator|+
literal|"\n"
operator|+
literal|"Expected milliseconds : "
operator|+
name|expectedMillis
operator|+
literal|"\n"
operator|+
literal|"Actual milliseconds   : "
operator|+
name|gotMillis
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testBasicDates
specifier|public
name|void
name|testBasicDates
parameter_list|()
block|{
name|assertDateMathEquals
argument_list|(
literal|"2014"
argument_list|,
literal|"2014-01-01T00:00:00.000"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-05"
argument_list|,
literal|"2014-05-01T00:00:00.000"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-05-30"
argument_list|,
literal|"2014-05-30T00:00:00.000"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-05-30T20"
argument_list|,
literal|"2014-05-30T20:00:00.000"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-05-30T20:21"
argument_list|,
literal|"2014-05-30T20:21:00.000"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-05-30T20:21:35"
argument_list|,
literal|"2014-05-30T20:21:35.000"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-05-30T20:21:35.123"
argument_list|,
literal|"2014-05-30T20:21:35.123"
argument_list|)
expr_stmt|;
block|}
DECL|method|testBasicMath
specifier|public
name|void
name|testBasicMath
parameter_list|()
block|{
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+y"
argument_list|,
literal|"2015-11-18"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||-2y"
argument_list|,
literal|"2012-11-18"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+3M"
argument_list|,
literal|"2015-02-18"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||-M"
argument_list|,
literal|"2014-10-18"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+1w"
argument_list|,
literal|"2014-11-25"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||-3w"
argument_list|,
literal|"2014-10-28"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+22d"
argument_list|,
literal|"2014-12-10"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||-423d"
argument_list|,
literal|"2013-09-21"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||+13h"
argument_list|,
literal|"2014-11-19T03"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||-1h"
argument_list|,
literal|"2014-11-18T13"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||+13H"
argument_list|,
literal|"2014-11-19T03"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||-1H"
argument_list|,
literal|"2014-11-18T13"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||+10240m"
argument_list|,
literal|"2014-11-25T17:07"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||-10m"
argument_list|,
literal|"2014-11-18T14:17"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32||+60s"
argument_list|,
literal|"2014-11-18T14:28:32"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32||-3600s"
argument_list|,
literal|"2014-11-18T13:27:32"
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultipleAdjustments
specifier|public
name|void
name|testMultipleAdjustments
parameter_list|()
block|{
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+1M-1M"
argument_list|,
literal|"2014-11-18"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+1M-1m"
argument_list|,
literal|"2014-12-17T23:59"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||-1m+1M"
argument_list|,
literal|"2014-12-17T23:59"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+1M/M"
argument_list|,
literal|"2014-12-01"
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||+1M/M+1h"
argument_list|,
literal|"2014-12-01T01"
argument_list|)
expr_stmt|;
block|}
DECL|method|testNow
specifier|public
name|void
name|testNow
parameter_list|()
block|{
name|long
name|now
init|=
name|parser
operator|.
name|parse
argument_list|(
literal|"2014-11-18T14:27:32"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"now"
argument_list|,
literal|"2014-11-18T14:27:32"
argument_list|,
name|now
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"now+M"
argument_list|,
literal|"2014-12-18T14:27:32"
argument_list|,
name|now
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"now-2d"
argument_list|,
literal|"2014-11-16T14:27:32"
argument_list|,
name|now
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"now/m"
argument_list|,
literal|"2014-11-18T14:27"
argument_list|,
name|now
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|testRounding
specifier|public
name|void
name|testRounding
parameter_list|()
block|{
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/y"
argument_list|,
literal|"2014-01-01"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/y"
argument_list|,
literal|"2014-12-31T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014||/y"
argument_list|,
literal|"2014-01-01"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014||/y"
argument_list|,
literal|"2014-12-31T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/M"
argument_list|,
literal|"2014-11-01"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/M"
argument_list|,
literal|"2014-11-30T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11||/M"
argument_list|,
literal|"2014-11-01"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11||/M"
argument_list|,
literal|"2014-11-30T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/w"
argument_list|,
literal|"2014-11-17"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/w"
argument_list|,
literal|"2014-11-23T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/w"
argument_list|,
literal|"2014-11-17"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/w"
argument_list|,
literal|"2014-11-23T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/d"
argument_list|,
literal|"2014-11-18"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/d"
argument_list|,
literal|"2014-11-18T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/d"
argument_list|,
literal|"2014-11-18"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18||/d"
argument_list|,
literal|"2014-11-18T23:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||/h"
argument_list|,
literal|"2014-11-18T14"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||/h"
argument_list|,
literal|"2014-11-18T14:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/H"
argument_list|,
literal|"2014-11-18T14"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/H"
argument_list|,
literal|"2014-11-18T14:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||/h"
argument_list|,
literal|"2014-11-18T14"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||/h"
argument_list|,
literal|"2014-11-18T14:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/H"
argument_list|,
literal|"2014-11-18T14"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14||/H"
argument_list|,
literal|"2014-11-18T14:59:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32||/m"
argument_list|,
literal|"2014-11-18T14:27"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32||/m"
argument_list|,
literal|"2014-11-18T14:27:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||/m"
argument_list|,
literal|"2014-11-18T14:27"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27||/m"
argument_list|,
literal|"2014-11-18T14:27:59.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32.123||/s"
argument_list|,
literal|"2014-11-18T14:27:32"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32.123||/s"
argument_list|,
literal|"2014-11-18T14:27:32.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32||/s"
argument_list|,
literal|"2014-11-18T14:27:32"
argument_list|,
literal|0
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertDateMathEquals
argument_list|(
literal|"2014-11-18T14:27:32||/s"
argument_list|,
literal|"2014-11-18T14:27:32.999"
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|assertParseException
name|void
name|assertParseException
parameter_list|(
name|String
name|msg
parameter_list|,
name|String
name|date
parameter_list|)
block|{
try|try
block|{
name|parser
operator|.
name|parse
argument_list|(
name|date
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Date: "
operator|+
name|date
operator|+
literal|"\n"
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
DECL|method|testIllegalMathFormat
specifier|public
name|void
name|testIllegalMathFormat
parameter_list|()
block|{
name|assertParseException
argument_list|(
literal|"Expected date math unsupported operator exception"
argument_list|,
literal|"2014-11-18||*5"
argument_list|)
expr_stmt|;
name|assertParseException
argument_list|(
literal|"Expected date math incompatible rounding exception"
argument_list|,
literal|"2014-11-18||/2m"
argument_list|)
expr_stmt|;
name|assertParseException
argument_list|(
literal|"Expected date math illegal unit type exception"
argument_list|,
literal|"2014-11-18||+2a"
argument_list|)
expr_stmt|;
name|assertParseException
argument_list|(
literal|"Expected date math truncation exception"
argument_list|,
literal|"2014-11-18||+12"
argument_list|)
expr_stmt|;
name|assertParseException
argument_list|(
literal|"Expected date math truncation exception"
argument_list|,
literal|"2014-11-18||-"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

