begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|action
operator|.
name|support
operator|.
name|IndicesOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
operator|.
name|Context
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
operator|.
name|DateMathExpressionResolver
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
name|settings
operator|.
name|Settings
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
name|ESTestCase
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
name|DateTime
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
name|format
operator|.
name|DateTimeFormat
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
operator|.
name|UTC
import|;
end_import

begin_class
DECL|class|DateMathExpressionResolverTests
specifier|public
class|class
name|DateMathExpressionResolverTests
extends|extends
name|ESTestCase
block|{
DECL|field|expressionResolver
specifier|private
specifier|final
name|DateMathExpressionResolver
name|expressionResolver
init|=
operator|new
name|DateMathExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
DECL|field|context
specifier|private
specifier|final
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|IndicesOptions
operator|.
name|strictExpand
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|testNormal
specifier|public
name|void
name|testNormal
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numIndexExpressions
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|9
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|indexExpressions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numIndexExpressions
argument_list|)
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
name|numIndexExpressions
condition|;
name|i
operator|++
control|)
block|{
name|indexExpressions
operator|.
name|add
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|indexExpressions
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexExpressions
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indexExpressions
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|indexExpressions
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testExpression
specifier|public
name|void
name|testExpression
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|indexExpressions
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now}>"
argument_list|,
literal|"<.watch_history-{now}>"
argument_list|,
literal|"<logstash-{now}>"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|indexExpressions
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".watch_history-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"logstash-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Collections
operator|.
expr|<
name|String
operator|>
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_Static
specifier|public
name|void
name|testExpression_Static
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-test>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_MultiParts
specifier|public
name|void
name|testExpression_MultiParts
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.text1-{now/d}-text2-{now/M}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".text1-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
operator|+
literal|"-text2-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
operator|.
name|withDayOfMonth
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_CustomFormat
specifier|public
name|void
name|testExpression_CustomFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d{YYYY.MM.dd}}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_EscapeStatic
specifier|public
name|void
name|testExpression_EscapeStatic
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.mar\\{v\\}el-{now/d}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".mar{v}el-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_EscapeDateFormat
specifier|public
name|void
name|testExpression_EscapeDateFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d{'\\{year\\}'YYYY}}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"'{year}'YYYY"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_MixedArray
specifier|public
name|void
name|testExpression_MixedArray
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|result
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"name1"
argument_list|,
literal|"<.marvel-{now/d}>"
argument_list|,
literal|"name2"
argument_list|,
literal|"<.logstash-{now/M{YYYY.MM}}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"name1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"name2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".logstash-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM"
argument_list|)
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|context
operator|.
name|getStartTime
argument_list|()
argument_list|,
name|UTC
argument_list|)
operator|.
name|withDayOfMonth
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_CustomTimeZoneInSetting
specifier|public
name|void
name|testExpression_CustomTimeZoneInSetting
parameter_list|()
throws|throws
name|Exception
block|{
name|DateTimeZone
name|timeZone
decl_stmt|;
name|int
name|hoursOffset
decl_stmt|;
name|int
name|minutesOffset
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|hoursOffset
operator|=
name|randomIntBetween
argument_list|(
operator|-
literal|12
argument_list|,
literal|14
argument_list|)
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|hoursOffset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hoursOffset
operator|=
name|randomIntBetween
argument_list|(
operator|-
literal|11
argument_list|,
literal|13
argument_list|)
expr_stmt|;
name|minutesOffset
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|59
argument_list|)
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHoursMinutes
argument_list|(
name|hoursOffset
argument_list|,
name|minutesOffset
argument_list|)
expr_stmt|;
block|}
name|DateTime
name|now
decl_stmt|;
if|if
condition|(
name|hoursOffset
operator|>=
literal|0
condition|)
block|{
comment|// rounding to next day 00:00
name|now
operator|=
name|DateTime
operator|.
name|now
argument_list|(
name|UTC
argument_list|)
operator|.
name|plusHours
argument_list|(
name|hoursOffset
argument_list|)
operator|.
name|plusMinutes
argument_list|(
name|minutesOffset
argument_list|)
operator|.
name|withHourOfDay
argument_list|(
literal|0
argument_list|)
operator|.
name|withMinuteOfHour
argument_list|(
literal|0
argument_list|)
operator|.
name|withSecondOfMinute
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// rounding to today 00:00
name|now
operator|=
name|DateTime
operator|.
name|now
argument_list|(
name|UTC
argument_list|)
operator|.
name|withHourOfDay
argument_list|(
literal|0
argument_list|)
operator|.
name|withMinuteOfHour
argument_list|(
literal|0
argument_list|)
operator|.
name|withSecondOfMinute
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"date_math_expression_resolver.default_time_zone"
argument_list|,
name|timeZone
operator|.
name|getID
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DateMathExpressionResolver
name|expressionResolver
init|=
operator|new
name|DateMathExpressionResolver
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|this
operator|.
name|context
operator|.
name|getState
argument_list|()
argument_list|,
name|this
operator|.
name|context
operator|.
name|getOptions
argument_list|()
argument_list|,
name|now
operator|.
name|getMillis
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d{YYYY.MM.dd}}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"timezone: [{}], now [{}], name: [{}]"
argument_list|,
name|timeZone
argument_list|,
name|now
argument_list|,
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|withZone
argument_list|(
name|timeZone
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExpression_CustomTimeZoneInIndexName
specifier|public
name|void
name|testExpression_CustomTimeZoneInIndexName
parameter_list|()
throws|throws
name|Exception
block|{
name|DateTimeZone
name|timeZone
decl_stmt|;
name|int
name|hoursOffset
decl_stmt|;
name|int
name|minutesOffset
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|hoursOffset
operator|=
name|randomIntBetween
argument_list|(
operator|-
literal|12
argument_list|,
literal|14
argument_list|)
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|hoursOffset
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hoursOffset
operator|=
name|randomIntBetween
argument_list|(
operator|-
literal|11
argument_list|,
literal|13
argument_list|)
expr_stmt|;
name|minutesOffset
operator|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|59
argument_list|)
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHoursMinutes
argument_list|(
name|hoursOffset
argument_list|,
name|minutesOffset
argument_list|)
expr_stmt|;
block|}
name|DateTime
name|now
decl_stmt|;
if|if
condition|(
name|hoursOffset
operator|>=
literal|0
condition|)
block|{
comment|// rounding to next day 00:00
name|now
operator|=
name|DateTime
operator|.
name|now
argument_list|(
name|UTC
argument_list|)
operator|.
name|plusHours
argument_list|(
name|hoursOffset
argument_list|)
operator|.
name|plusMinutes
argument_list|(
name|minutesOffset
argument_list|)
operator|.
name|withHourOfDay
argument_list|(
literal|0
argument_list|)
operator|.
name|withMinuteOfHour
argument_list|(
literal|0
argument_list|)
operator|.
name|withSecondOfMinute
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// rounding to today 00:00
name|now
operator|=
name|DateTime
operator|.
name|now
argument_list|(
name|UTC
argument_list|)
operator|.
name|withHourOfDay
argument_list|(
literal|0
argument_list|)
operator|.
name|withMinuteOfHour
argument_list|(
literal|0
argument_list|)
operator|.
name|withSecondOfMinute
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|Context
name|context
init|=
operator|new
name|Context
argument_list|(
name|this
operator|.
name|context
operator|.
name|getState
argument_list|()
argument_list|,
name|this
operator|.
name|context
operator|.
name|getOptions
argument_list|()
argument_list|,
name|now
operator|.
name|getMillis
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|results
init|=
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d{YYYY.MM.dd|"
operator|+
name|timeZone
operator|.
name|getID
argument_list|()
operator|+
literal|"}}>"
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"timezone: [{}], now [{}], name: [{}]"
argument_list|,
name|timeZone
argument_list|,
name|now
argument_list|,
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|".marvel-"
operator|+
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
literal|"YYYY.MM.dd"
argument_list|)
operator|.
name|print
argument_list|(
name|now
operator|.
name|withZone
argument_list|(
name|timeZone
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ElasticsearchParseException
operator|.
name|class
argument_list|)
DECL|method|testExpression_Invalid_Unescaped
specifier|public
name|void
name|testExpression_Invalid_Unescaped
parameter_list|()
throws|throws
name|Exception
block|{
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.mar}vel-{now/d}>"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ElasticsearchParseException
operator|.
name|class
argument_list|)
DECL|method|testExpression_Invalid_DateMathFormat
specifier|public
name|void
name|testExpression_Invalid_DateMathFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d{}>"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ElasticsearchParseException
operator|.
name|class
argument_list|)
DECL|method|testExpression_Invalid_EmptyDateMathFormat
specifier|public
name|void
name|testExpression_Invalid_EmptyDateMathFormat
parameter_list|()
throws|throws
name|Exception
block|{
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d{}}>"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|ElasticsearchParseException
operator|.
name|class
argument_list|)
DECL|method|testExpression_Invalid_OpenEnded
specifier|public
name|void
name|testExpression_Invalid_OpenEnded
parameter_list|()
throws|throws
name|Exception
block|{
name|expressionResolver
operator|.
name|resolve
argument_list|(
name|context
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"<.marvel-{now/d>"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

