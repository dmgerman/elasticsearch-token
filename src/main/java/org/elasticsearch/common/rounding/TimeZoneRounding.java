begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.rounding
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|rounding
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
name|unit
operator|.
name|TimeValue
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
name|DateTimeField
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
name|DurationField
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|TimeZoneRounding
specifier|public
specifier|abstract
class|class
name|TimeZoneRounding
extends|extends
name|Rounding
block|{
DECL|method|builder
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|(
name|DateTimeUnit
name|unit
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|unit
argument_list|)
return|;
block|}
DECL|method|builder
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|(
name|TimeValue
name|interval
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|interval
argument_list|)
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|unit
specifier|private
name|DateTimeUnit
name|unit
decl_stmt|;
DECL|field|interval
specifier|private
name|long
name|interval
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
init|=
name|DateTimeZone
operator|.
name|UTC
decl_stmt|;
DECL|field|factor
specifier|private
name|float
name|factor
init|=
literal|1.0f
decl_stmt|;
DECL|field|offset
specifier|private
name|long
name|offset
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|DateTimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|interval
operator|=
operator|-
literal|1
expr_stmt|;
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|TimeValue
name|interval
parameter_list|)
block|{
name|this
operator|.
name|unit
operator|=
literal|null
expr_stmt|;
if|if
condition|(
name|interval
operator|.
name|millis
argument_list|()
operator|<
literal|1
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Zero or negative time interval not supported"
argument_list|)
throw|;
name|this
operator|.
name|interval
operator|=
name|interval
operator|.
name|millis
argument_list|()
expr_stmt|;
block|}
DECL|method|timeZone
specifier|public
name|Builder
name|timeZone
parameter_list|(
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|offset
specifier|public
name|Builder
name|offset
parameter_list|(
name|long
name|offset
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|factor
specifier|public
name|Builder
name|factor
parameter_list|(
name|float
name|factor
parameter_list|)
block|{
name|this
operator|.
name|factor
operator|=
name|factor
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|Rounding
name|build
parameter_list|()
block|{
name|Rounding
name|timeZoneRounding
decl_stmt|;
if|if
condition|(
name|unit
operator|!=
literal|null
condition|)
block|{
name|timeZoneRounding
operator|=
operator|new
name|TimeUnitRounding
argument_list|(
name|unit
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|timeZoneRounding
operator|=
operator|new
name|TimeIntervalRounding
argument_list|(
name|interval
argument_list|,
name|timeZone
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|offset
operator|!=
literal|0
condition|)
block|{
name|timeZoneRounding
operator|=
operator|new
name|OffsetRounding
argument_list|(
name|timeZoneRounding
argument_list|,
name|offset
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|factor
operator|!=
literal|1.0f
condition|)
block|{
name|timeZoneRounding
operator|=
operator|new
name|FactorRounding
argument_list|(
name|timeZoneRounding
argument_list|,
name|factor
argument_list|)
expr_stmt|;
block|}
return|return
name|timeZoneRounding
return|;
block|}
block|}
DECL|class|TimeUnitRounding
specifier|static
class|class
name|TimeUnitRounding
extends|extends
name|TimeZoneRounding
block|{
DECL|field|ID
specifier|static
specifier|final
name|byte
name|ID
init|=
literal|1
decl_stmt|;
DECL|field|unit
specifier|private
name|DateTimeUnit
name|unit
decl_stmt|;
DECL|field|field
specifier|private
name|DateTimeField
name|field
decl_stmt|;
DECL|field|durationField
specifier|private
name|DurationField
name|durationField
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
decl_stmt|;
DECL|method|TimeUnitRounding
name|TimeUnitRounding
parameter_list|()
block|{
comment|// for serialization
block|}
DECL|method|TimeUnitRounding
name|TimeUnitRounding
parameter_list|(
name|DateTimeUnit
name|unit
parameter_list|,
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|unit
operator|.
name|field
argument_list|()
expr_stmt|;
name|this
operator|.
name|durationField
operator|=
name|field
operator|.
name|getDurationField
argument_list|()
expr_stmt|;
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|roundKey
specifier|public
name|long
name|roundKey
parameter_list|(
name|long
name|utcMillis
parameter_list|)
block|{
name|long
name|timeLocal
init|=
name|utcMillis
decl_stmt|;
name|timeLocal
operator|=
name|timeZone
operator|.
name|convertUTCToLocal
argument_list|(
name|utcMillis
argument_list|)
expr_stmt|;
name|long
name|rounded
init|=
name|field
operator|.
name|roundFloor
argument_list|(
name|timeLocal
argument_list|)
decl_stmt|;
return|return
name|timeZone
operator|.
name|convertLocalToUTC
argument_list|(
name|rounded
argument_list|,
literal|false
argument_list|,
name|utcMillis
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|valueForKey
specifier|public
name|long
name|valueForKey
parameter_list|(
name|long
name|time
parameter_list|)
block|{
assert|assert
name|roundKey
argument_list|(
name|time
argument_list|)
operator|==
name|time
assert|;
return|return
name|time
return|;
block|}
annotation|@
name|Override
DECL|method|nextRoundingValue
specifier|public
name|long
name|nextRoundingValue
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|long
name|timeLocal
init|=
name|time
decl_stmt|;
name|timeLocal
operator|=
name|timeZone
operator|.
name|convertUTCToLocal
argument_list|(
name|time
argument_list|)
expr_stmt|;
name|long
name|nextInLocalTime
init|=
name|durationField
operator|.
name|add
argument_list|(
name|timeLocal
argument_list|,
literal|1
argument_list|)
decl_stmt|;
return|return
name|timeZone
operator|.
name|convertLocalToUTC
argument_list|(
name|nextInLocalTime
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|unit
operator|=
name|DateTimeUnit
operator|.
name|resolve
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|field
operator|=
name|unit
operator|.
name|field
argument_list|()
expr_stmt|;
name|durationField
operator|=
name|field
operator|.
name|getDurationField
argument_list|()
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|in
operator|.
name|readString
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
name|writeByte
argument_list|(
name|unit
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|timeZone
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TimeIntervalRounding
specifier|static
class|class
name|TimeIntervalRounding
extends|extends
name|TimeZoneRounding
block|{
DECL|field|ID
specifier|final
specifier|static
name|byte
name|ID
init|=
literal|2
decl_stmt|;
DECL|field|interval
specifier|private
name|long
name|interval
decl_stmt|;
DECL|field|timeZone
specifier|private
name|DateTimeZone
name|timeZone
decl_stmt|;
DECL|method|TimeIntervalRounding
name|TimeIntervalRounding
parameter_list|()
block|{
comment|// for serialization
block|}
DECL|method|TimeIntervalRounding
name|TimeIntervalRounding
parameter_list|(
name|long
name|interval
parameter_list|,
name|DateTimeZone
name|timeZone
parameter_list|)
block|{
if|if
condition|(
name|interval
operator|<
literal|1
condition|)
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Zero or negative time interval not supported"
argument_list|)
throw|;
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
name|this
operator|.
name|timeZone
operator|=
name|timeZone
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|ID
return|;
block|}
annotation|@
name|Override
DECL|method|roundKey
specifier|public
name|long
name|roundKey
parameter_list|(
name|long
name|utcMillis
parameter_list|)
block|{
name|long
name|timeLocal
init|=
name|utcMillis
decl_stmt|;
name|timeLocal
operator|=
name|timeZone
operator|.
name|convertUTCToLocal
argument_list|(
name|utcMillis
argument_list|)
expr_stmt|;
name|long
name|rounded
init|=
name|Rounding
operator|.
name|Interval
operator|.
name|roundValue
argument_list|(
name|Rounding
operator|.
name|Interval
operator|.
name|roundKey
argument_list|(
name|timeLocal
argument_list|,
name|interval
argument_list|)
argument_list|,
name|interval
argument_list|)
decl_stmt|;
return|return
name|timeZone
operator|.
name|convertLocalToUTC
argument_list|(
name|rounded
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|valueForKey
specifier|public
name|long
name|valueForKey
parameter_list|(
name|long
name|time
parameter_list|)
block|{
assert|assert
name|roundKey
argument_list|(
name|time
argument_list|)
operator|==
name|time
assert|;
return|return
name|time
return|;
block|}
annotation|@
name|Override
DECL|method|nextRoundingValue
specifier|public
name|long
name|nextRoundingValue
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|long
name|timeLocal
init|=
name|time
decl_stmt|;
name|timeLocal
operator|=
name|timeZone
operator|.
name|convertUTCToLocal
argument_list|(
name|time
argument_list|)
expr_stmt|;
name|long
name|next
init|=
name|timeLocal
operator|+
name|interval
decl_stmt|;
return|return
name|timeZone
operator|.
name|convertLocalToUTC
argument_list|(
name|next
argument_list|,
literal|false
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|interval
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|timeZone
operator|=
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|in
operator|.
name|readString
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
name|writeVLong
argument_list|(
name|interval
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|timeZone
operator|.
name|getID
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

