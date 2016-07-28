begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.unit
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
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
name|common
operator|.
name|Strings
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|Period
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
name|PeriodType
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
name|PeriodFormat
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
name|PeriodFormatter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
DECL|class|TimeValue
specifier|public
class|class
name|TimeValue
implements|implements
name|Writeable
block|{
comment|/** How many nano-seconds in one milli-second */
DECL|field|NSEC_PER_MSEC
specifier|public
specifier|static
specifier|final
name|long
name|NSEC_PER_MSEC
init|=
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|convert
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
decl_stmt|;
DECL|field|TIME_UNIT_BYTE_MAP
specifier|private
specifier|static
name|Map
argument_list|<
name|TimeUnit
argument_list|,
name|Byte
argument_list|>
name|TIME_UNIT_BYTE_MAP
decl_stmt|;
DECL|field|BYTE_TIME_UNIT_MAP
specifier|private
specifier|static
name|Map
argument_list|<
name|Byte
argument_list|,
name|TimeUnit
argument_list|>
name|BYTE_TIME_UNIT_MAP
decl_stmt|;
static|static
block|{
specifier|final
name|Map
argument_list|<
name|TimeUnit
argument_list|,
name|Byte
argument_list|>
name|timeUnitByteMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|,
operator|(
name|byte
operator|)
literal|0
argument_list|)
expr_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|,
operator|(
name|byte
operator|)
literal|1
argument_list|)
expr_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|(
name|byte
operator|)
literal|2
argument_list|)
expr_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|SECONDS
argument_list|,
operator|(
name|byte
operator|)
literal|3
argument_list|)
expr_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|,
operator|(
name|byte
operator|)
literal|4
argument_list|)
expr_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|HOURS
argument_list|,
operator|(
name|byte
operator|)
literal|5
argument_list|)
expr_stmt|;
name|timeUnitByteMap
operator|.
name|put
argument_list|(
name|TimeUnit
operator|.
name|DAYS
argument_list|,
operator|(
name|byte
operator|)
literal|6
argument_list|)
expr_stmt|;
specifier|final
name|Set
argument_list|<
name|Byte
argument_list|>
name|bytes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|TimeUnit
name|value
range|:
name|TimeUnit
operator|.
name|values
argument_list|()
control|)
block|{
assert|assert
name|timeUnitByteMap
operator|.
name|containsKey
argument_list|(
name|value
argument_list|)
operator|:
name|value
assert|;
assert|assert
name|bytes
operator|.
name|add
argument_list|(
name|timeUnitByteMap
operator|.
name|get
argument_list|(
name|value
argument_list|)
argument_list|)
assert|;
block|}
specifier|final
name|Map
argument_list|<
name|Byte
argument_list|,
name|TimeUnit
argument_list|>
name|byteTimeUnitMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|TimeUnit
argument_list|,
name|Byte
argument_list|>
name|entry
range|:
name|timeUnitByteMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|byteTimeUnitMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TIME_UNIT_BYTE_MAP
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|timeUnitByteMap
argument_list|)
expr_stmt|;
name|BYTE_TIME_UNIT_MAP
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|byteTimeUnitMap
argument_list|)
expr_stmt|;
block|}
DECL|field|MINUS_ONE
specifier|public
specifier|static
specifier|final
name|TimeValue
name|MINUS_ONE
init|=
name|timeValueMillis
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
DECL|field|ZERO
specifier|public
specifier|static
specifier|final
name|TimeValue
name|ZERO
init|=
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|method|timeValueNanos
specifier|public
specifier|static
name|TimeValue
name|timeValueNanos
parameter_list|(
name|long
name|nanos
parameter_list|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|nanos
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
return|;
block|}
DECL|method|timeValueMillis
specifier|public
specifier|static
name|TimeValue
name|timeValueMillis
parameter_list|(
name|long
name|millis
parameter_list|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|millis
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
DECL|method|timeValueSeconds
specifier|public
specifier|static
name|TimeValue
name|timeValueSeconds
parameter_list|(
name|long
name|seconds
parameter_list|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|seconds
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
DECL|method|timeValueMinutes
specifier|public
specifier|static
name|TimeValue
name|timeValueMinutes
parameter_list|(
name|long
name|minutes
parameter_list|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|minutes
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
return|;
block|}
DECL|method|timeValueHours
specifier|public
specifier|static
name|TimeValue
name|timeValueHours
parameter_list|(
name|long
name|hours
parameter_list|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|hours
argument_list|,
name|TimeUnit
operator|.
name|HOURS
argument_list|)
return|;
block|}
DECL|field|duration
specifier|private
specifier|final
name|long
name|duration
decl_stmt|;
comment|// visible for testing
DECL|method|duration
name|long
name|duration
parameter_list|()
block|{
return|return
name|duration
return|;
block|}
DECL|field|timeUnit
specifier|private
specifier|final
name|TimeUnit
name|timeUnit
decl_stmt|;
comment|// visible for testing
DECL|method|timeUnit
name|TimeUnit
name|timeUnit
parameter_list|()
block|{
return|return
name|timeUnit
return|;
block|}
DECL|method|TimeValue
specifier|public
name|TimeValue
parameter_list|(
name|long
name|millis
parameter_list|)
block|{
name|this
argument_list|(
name|millis
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
DECL|method|TimeValue
specifier|public
name|TimeValue
parameter_list|(
name|long
name|duration
parameter_list|,
name|TimeUnit
name|timeUnit
parameter_list|)
block|{
name|this
operator|.
name|duration
operator|=
name|duration
expr_stmt|;
name|this
operator|.
name|timeUnit
operator|=
name|timeUnit
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|TimeValue
specifier|public
name|TimeValue
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|duration
operator|=
name|in
operator|.
name|readZLong
argument_list|()
expr_stmt|;
name|timeUnit
operator|=
name|BYTE_TIME_UNIT_MAP
operator|.
name|get
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeZLong
argument_list|(
name|duration
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|TIME_UNIT_BYTE_MAP
operator|.
name|get
argument_list|(
name|timeUnit
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|nanos
specifier|public
name|long
name|nanos
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toNanos
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getNanos
specifier|public
name|long
name|getNanos
parameter_list|()
block|{
return|return
name|nanos
argument_list|()
return|;
block|}
DECL|method|micros
specifier|public
name|long
name|micros
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toMicros
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getMicros
specifier|public
name|long
name|getMicros
parameter_list|()
block|{
return|return
name|micros
argument_list|()
return|;
block|}
DECL|method|millis
specifier|public
name|long
name|millis
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toMillis
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getMillis
specifier|public
name|long
name|getMillis
parameter_list|()
block|{
return|return
name|millis
argument_list|()
return|;
block|}
DECL|method|seconds
specifier|public
name|long
name|seconds
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toSeconds
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getSeconds
specifier|public
name|long
name|getSeconds
parameter_list|()
block|{
return|return
name|seconds
argument_list|()
return|;
block|}
DECL|method|minutes
specifier|public
name|long
name|minutes
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toMinutes
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getMinutes
specifier|public
name|long
name|getMinutes
parameter_list|()
block|{
return|return
name|minutes
argument_list|()
return|;
block|}
DECL|method|hours
specifier|public
name|long
name|hours
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toHours
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getHours
specifier|public
name|long
name|getHours
parameter_list|()
block|{
return|return
name|hours
argument_list|()
return|;
block|}
DECL|method|days
specifier|public
name|long
name|days
parameter_list|()
block|{
return|return
name|timeUnit
operator|.
name|toDays
argument_list|(
name|duration
argument_list|)
return|;
block|}
DECL|method|getDays
specifier|public
name|long
name|getDays
parameter_list|()
block|{
return|return
name|days
argument_list|()
return|;
block|}
DECL|method|microsFrac
specifier|public
name|double
name|microsFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|nanos
argument_list|()
operator|)
operator|/
name|C1
return|;
block|}
DECL|method|getMicrosFrac
specifier|public
name|double
name|getMicrosFrac
parameter_list|()
block|{
return|return
name|microsFrac
argument_list|()
return|;
block|}
DECL|method|millisFrac
specifier|public
name|double
name|millisFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|nanos
argument_list|()
operator|)
operator|/
name|C2
return|;
block|}
DECL|method|getMillisFrac
specifier|public
name|double
name|getMillisFrac
parameter_list|()
block|{
return|return
name|millisFrac
argument_list|()
return|;
block|}
DECL|method|secondsFrac
specifier|public
name|double
name|secondsFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|nanos
argument_list|()
operator|)
operator|/
name|C3
return|;
block|}
DECL|method|getSecondsFrac
specifier|public
name|double
name|getSecondsFrac
parameter_list|()
block|{
return|return
name|secondsFrac
argument_list|()
return|;
block|}
DECL|method|minutesFrac
specifier|public
name|double
name|minutesFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|nanos
argument_list|()
operator|)
operator|/
name|C4
return|;
block|}
DECL|method|getMinutesFrac
specifier|public
name|double
name|getMinutesFrac
parameter_list|()
block|{
return|return
name|minutesFrac
argument_list|()
return|;
block|}
DECL|method|hoursFrac
specifier|public
name|double
name|hoursFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|nanos
argument_list|()
operator|)
operator|/
name|C5
return|;
block|}
DECL|method|getHoursFrac
specifier|public
name|double
name|getHoursFrac
parameter_list|()
block|{
return|return
name|hoursFrac
argument_list|()
return|;
block|}
DECL|method|daysFrac
specifier|public
name|double
name|daysFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|nanos
argument_list|()
operator|)
operator|/
name|C6
return|;
block|}
DECL|method|getDaysFrac
specifier|public
name|double
name|getDaysFrac
parameter_list|()
block|{
return|return
name|daysFrac
argument_list|()
return|;
block|}
DECL|field|defaultFormatter
specifier|private
specifier|final
name|PeriodFormatter
name|defaultFormatter
init|=
name|PeriodFormat
operator|.
name|getDefault
argument_list|()
operator|.
name|withParseType
argument_list|(
name|PeriodType
operator|.
name|standard
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|format
specifier|public
name|String
name|format
parameter_list|()
block|{
name|Period
name|period
init|=
operator|new
name|Period
argument_list|(
name|millis
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|defaultFormatter
operator|.
name|print
argument_list|(
name|period
argument_list|)
return|;
block|}
DECL|method|format
specifier|public
name|String
name|format
parameter_list|(
name|PeriodType
name|type
parameter_list|)
block|{
name|Period
name|period
init|=
operator|new
name|Period
argument_list|(
name|millis
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|PeriodFormat
operator|.
name|getDefault
argument_list|()
operator|.
name|withParseType
argument_list|(
name|type
argument_list|)
operator|.
name|print
argument_list|(
name|period
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|duration
operator|<
literal|0
condition|)
block|{
return|return
name|Long
operator|.
name|toString
argument_list|(
name|duration
argument_list|)
return|;
block|}
name|long
name|nanos
init|=
name|nanos
argument_list|()
decl_stmt|;
if|if
condition|(
name|nanos
operator|==
literal|0
condition|)
block|{
return|return
literal|"0s"
return|;
block|}
name|double
name|value
init|=
name|nanos
decl_stmt|;
name|String
name|suffix
init|=
literal|"nanos"
decl_stmt|;
if|if
condition|(
name|nanos
operator|>=
name|C6
condition|)
block|{
name|value
operator|=
name|daysFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"d"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nanos
operator|>=
name|C5
condition|)
block|{
name|value
operator|=
name|hoursFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"h"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nanos
operator|>=
name|C4
condition|)
block|{
name|value
operator|=
name|minutesFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"m"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nanos
operator|>=
name|C3
condition|)
block|{
name|value
operator|=
name|secondsFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"s"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nanos
operator|>=
name|C2
condition|)
block|{
name|value
operator|=
name|millisFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"ms"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nanos
operator|>=
name|C1
condition|)
block|{
name|value
operator|=
name|microsFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"micros"
expr_stmt|;
block|}
return|return
name|Strings
operator|.
name|format1Decimals
argument_list|(
name|value
argument_list|,
name|suffix
argument_list|)
return|;
block|}
DECL|method|getStringRep
specifier|public
name|String
name|getStringRep
parameter_list|()
block|{
if|if
condition|(
name|duration
operator|<
literal|0
condition|)
block|{
return|return
name|Long
operator|.
name|toString
argument_list|(
name|duration
argument_list|)
return|;
block|}
switch|switch
condition|(
name|timeUnit
condition|)
block|{
case|case
name|NANOSECONDS
case|:
return|return
name|duration
operator|+
literal|"nanos"
return|;
case|case
name|MICROSECONDS
case|:
return|return
name|duration
operator|+
literal|"micros"
return|;
case|case
name|MILLISECONDS
case|:
return|return
name|duration
operator|+
literal|"ms"
return|;
case|case
name|SECONDS
case|:
return|return
name|duration
operator|+
literal|"s"
return|;
case|case
name|MINUTES
case|:
return|return
name|duration
operator|+
literal|"m"
return|;
case|case
name|HOURS
case|:
return|return
name|duration
operator|+
literal|"h"
return|;
case|case
name|DAYS
case|:
return|return
name|duration
operator|+
literal|"d"
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown time unit: "
operator|+
name|timeUnit
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
DECL|method|parseTimeValue
specifier|public
specifier|static
name|TimeValue
name|parseTimeValue
parameter_list|(
name|String
name|sValue
parameter_list|,
name|String
name|settingName
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|settingName
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|sValue
argument_list|)
expr_stmt|;
return|return
name|parseTimeValue
argument_list|(
name|sValue
argument_list|,
literal|null
argument_list|,
name|settingName
argument_list|)
return|;
block|}
DECL|method|parseTimeValue
specifier|public
specifier|static
name|TimeValue
name|parseTimeValue
parameter_list|(
name|String
name|sValue
parameter_list|,
name|TimeValue
name|defaultValue
parameter_list|,
name|String
name|settingName
parameter_list|)
block|{
name|settingName
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|settingName
argument_list|)
expr_stmt|;
if|if
condition|(
name|sValue
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
specifier|final
name|String
name|normalized
init|=
name|sValue
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|normalized
operator|.
name|endsWith
argument_list|(
literal|"nanos"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|5
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|endsWith
argument_list|(
literal|"micros"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|6
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|MICROSECONDS
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|endsWith
argument_list|(
literal|"ms"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|2
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|endsWith
argument_list|(
literal|"s"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|1
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"m"
argument_list|)
condition|)
block|{
comment|// parsing minutes should be case sensitive as `M` is generally
comment|// accepted to mean months not minutes. This is the only case where
comment|// the upper and lower case forms indicate different time units
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|1
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|endsWith
argument_list|(
literal|"h"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|1
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|HOURS
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|endsWith
argument_list|(
literal|"d"
argument_list|)
condition|)
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|parse
argument_list|(
name|sValue
argument_list|,
name|normalized
argument_list|,
literal|1
argument_list|)
argument_list|,
name|TimeUnit
operator|.
name|DAYS
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|matches
argument_list|(
literal|"-0*1"
argument_list|)
condition|)
block|{
return|return
name|TimeValue
operator|.
name|MINUS_ONE
return|;
block|}
elseif|else
if|if
condition|(
name|normalized
operator|.
name|matches
argument_list|(
literal|"0+"
argument_list|)
condition|)
block|{
return|return
name|TimeValue
operator|.
name|ZERO
return|;
block|}
else|else
block|{
comment|// Missing units:
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse setting [{}] with value [{}] as a time value: unit is missing or unrecognized"
argument_list|,
name|settingName
argument_list|,
name|sValue
argument_list|)
throw|;
block|}
block|}
DECL|method|parse
specifier|private
specifier|static
name|long
name|parse
parameter_list|(
specifier|final
name|String
name|initialInput
parameter_list|,
specifier|final
name|String
name|normalized
parameter_list|,
specifier|final
name|int
name|suffixLength
parameter_list|)
block|{
specifier|final
name|String
name|s
init|=
name|normalized
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|normalized
operator|.
name|length
argument_list|()
operator|-
name|suffixLength
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
try|try
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|s
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
specifier|final
name|NumberFormatException
name|e
parameter_list|)
block|{
try|try
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|final
name|double
name|ignored
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|s
argument_list|)
decl_stmt|;
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse [{}], fractional time values are not supported"
argument_list|,
name|e
argument_list|,
name|initialInput
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
specifier|final
name|NumberFormatException
name|ignored
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse [{}]"
argument_list|,
name|e
argument_list|,
name|initialInput
argument_list|)
throw|;
block|}
block|}
block|}
DECL|field|C0
specifier|private
specifier|static
specifier|final
name|long
name|C0
init|=
literal|1L
decl_stmt|;
DECL|field|C1
specifier|private
specifier|static
specifier|final
name|long
name|C1
init|=
name|C0
operator|*
literal|1000L
decl_stmt|;
DECL|field|C2
specifier|private
specifier|static
specifier|final
name|long
name|C2
init|=
name|C1
operator|*
literal|1000L
decl_stmt|;
DECL|field|C3
specifier|private
specifier|static
specifier|final
name|long
name|C3
init|=
name|C2
operator|*
literal|1000L
decl_stmt|;
DECL|field|C4
specifier|private
specifier|static
specifier|final
name|long
name|C4
init|=
name|C3
operator|*
literal|60L
decl_stmt|;
DECL|field|C5
specifier|private
specifier|static
specifier|final
name|long
name|C5
init|=
name|C4
operator|*
literal|60L
decl_stmt|;
DECL|field|C6
specifier|private
specifier|static
specifier|final
name|long
name|C6
init|=
name|C5
operator|*
literal|24L
decl_stmt|;
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|TimeValue
name|timeValue
init|=
operator|(
name|TimeValue
operator|)
name|o
decl_stmt|;
return|return
name|timeUnit
operator|.
name|toNanos
argument_list|(
name|duration
argument_list|)
operator|==
name|timeValue
operator|.
name|timeUnit
operator|.
name|toNanos
argument_list|(
name|timeValue
operator|.
name|duration
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|long
name|normalized
init|=
name|timeUnit
operator|.
name|toNanos
argument_list|(
name|duration
argument_list|)
decl_stmt|;
return|return
name|Long
operator|.
name|hashCode
argument_list|(
name|normalized
argument_list|)
return|;
block|}
DECL|method|nsecToMSec
specifier|public
specifier|static
name|long
name|nsecToMSec
parameter_list|(
name|long
name|ns
parameter_list|)
block|{
return|return
name|ns
operator|/
name|NSEC_PER_MSEC
return|;
block|}
block|}
end_class

end_unit

