begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|histogram
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|ParseField
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
name|collect
operator|.
name|MapBuilder
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
name|DateMathParser
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
name|rounding
operator|.
name|DateTimeUnit
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
name|rounding
operator|.
name|Rounding
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
name|rounding
operator|.
name|TimeZoneRounding
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|Aggregator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|ValueType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|DateHistogramParser
specifier|public
class|class
name|DateHistogramParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
DECL|field|EXTENDED_BOUNDS
specifier|static
specifier|final
name|ParseField
name|EXTENDED_BOUNDS
init|=
operator|new
name|ParseField
argument_list|(
literal|"extended_bounds"
argument_list|)
decl_stmt|;
DECL|field|dateFieldUnits
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|DateTimeUnit
argument_list|>
name|dateFieldUnits
decl_stmt|;
DECL|method|DateHistogramParser
specifier|public
name|DateHistogramParser
parameter_list|()
block|{
name|dateFieldUnits
operator|=
name|MapBuilder
operator|.
expr|<
name|String
operator|,
name|DateTimeUnit
operator|>
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"year"
argument_list|,
name|DateTimeUnit
operator|.
name|YEAR_OF_CENTURY
argument_list|)
operator|.
name|put
argument_list|(
literal|"1y"
argument_list|,
name|DateTimeUnit
operator|.
name|YEAR_OF_CENTURY
argument_list|)
operator|.
name|put
argument_list|(
literal|"quarter"
argument_list|,
name|DateTimeUnit
operator|.
name|QUARTER
argument_list|)
operator|.
name|put
argument_list|(
literal|"1q"
argument_list|,
name|DateTimeUnit
operator|.
name|QUARTER
argument_list|)
operator|.
name|put
argument_list|(
literal|"month"
argument_list|,
name|DateTimeUnit
operator|.
name|MONTH_OF_YEAR
argument_list|)
operator|.
name|put
argument_list|(
literal|"1M"
argument_list|,
name|DateTimeUnit
operator|.
name|MONTH_OF_YEAR
argument_list|)
operator|.
name|put
argument_list|(
literal|"week"
argument_list|,
name|DateTimeUnit
operator|.
name|WEEK_OF_WEEKYEAR
argument_list|)
operator|.
name|put
argument_list|(
literal|"1w"
argument_list|,
name|DateTimeUnit
operator|.
name|WEEK_OF_WEEKYEAR
argument_list|)
operator|.
name|put
argument_list|(
literal|"day"
argument_list|,
name|DateTimeUnit
operator|.
name|DAY_OF_MONTH
argument_list|)
operator|.
name|put
argument_list|(
literal|"1d"
argument_list|,
name|DateTimeUnit
operator|.
name|DAY_OF_MONTH
argument_list|)
operator|.
name|put
argument_list|(
literal|"hour"
argument_list|,
name|DateTimeUnit
operator|.
name|HOUR_OF_DAY
argument_list|)
operator|.
name|put
argument_list|(
literal|"1h"
argument_list|,
name|DateTimeUnit
operator|.
name|HOUR_OF_DAY
argument_list|)
operator|.
name|put
argument_list|(
literal|"minute"
argument_list|,
name|DateTimeUnit
operator|.
name|MINUTES_OF_HOUR
argument_list|)
operator|.
name|put
argument_list|(
literal|"1m"
argument_list|,
name|DateTimeUnit
operator|.
name|MINUTES_OF_HOUR
argument_list|)
operator|.
name|put
argument_list|(
literal|"second"
argument_list|,
name|DateTimeUnit
operator|.
name|SECOND_OF_MINUTE
argument_list|)
operator|.
name|put
argument_list|(
literal|"1s"
argument_list|,
name|DateTimeUnit
operator|.
name|SECOND_OF_MINUTE
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalDateHistogram
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|ValuesSourceParser
name|vsParser
init|=
name|ValuesSourceParser
operator|.
name|numeric
argument_list|(
name|aggregationName
argument_list|,
name|InternalDateHistogram
operator|.
name|TYPE
argument_list|,
name|context
argument_list|)
operator|.
name|targetValueType
argument_list|(
name|ValueType
operator|.
name|DATE
argument_list|)
operator|.
name|requiresSortedValues
argument_list|(
literal|true
argument_list|)
operator|.
name|formattable
argument_list|(
literal|true
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|boolean
name|keyed
init|=
literal|false
decl_stmt|;
name|long
name|minDocCount
init|=
literal|1
decl_stmt|;
name|ExtendedBounds
name|extendedBounds
init|=
literal|null
decl_stmt|;
name|InternalOrder
name|order
init|=
operator|(
name|InternalOrder
operator|)
name|Histogram
operator|.
name|Order
operator|.
name|KEY_ASC
decl_stmt|;
name|String
name|interval
init|=
literal|null
decl_stmt|;
name|boolean
name|preZoneAdjustLargeInterval
init|=
literal|false
decl_stmt|;
name|DateTimeZone
name|preZone
init|=
name|DateTimeZone
operator|.
name|UTC
decl_stmt|;
name|DateTimeZone
name|postZone
init|=
name|DateTimeZone
operator|.
name|UTC
decl_stmt|;
name|long
name|preOffset
init|=
literal|0
decl_stmt|;
name|long
name|postOffset
init|=
literal|0
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|vsParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|)
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"time_zone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"timeZone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|preZone
operator|=
name|DateMathParser
operator|.
name|parseZone
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"pre_zone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"preZone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|preZone
operator|=
name|DateMathParser
operator|.
name|parseZone
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"post_zone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"postZone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|postZone
operator|=
name|DateMathParser
operator|.
name|parseZone
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"pre_offset"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"preOffset"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|preOffset
operator|=
name|parseOffset
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"post_offset"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"postOffset"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|postOffset
operator|=
name|parseOffset
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"interval"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|interval
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_BOOLEAN
condition|)
block|{
if|if
condition|(
literal|"keyed"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|keyed
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"pre_zone_adjust_large_interval"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"preZoneAdjustLargeInterval"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|preZoneAdjustLargeInterval
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
literal|"min_doc_count"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"minDocCount"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|minDocCount
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"time_zone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"timeZone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|preZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"pre_zone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"preZone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|preZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"post_zone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"postZone"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|postZone
operator|=
name|DateTimeZone
operator|.
name|forOffsetHours
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"order"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|String
name|dir
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|boolean
name|asc
init|=
literal|"asc"
operator|.
name|equals
argument_list|(
name|dir
argument_list|)
decl_stmt|;
name|order
operator|=
name|resolveOrder
argument_list|(
name|currentFieldName
argument_list|,
name|asc
argument_list|)
expr_stmt|;
comment|//TODO should we throw an error if the value is not "asc" or "desc"???
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|EXTENDED_BOUNDS
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|extendedBounds
operator|=
operator|new
name|ExtendedBounds
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"min"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|extendedBounds
operator|.
name|minAsStr
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"max"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|extendedBounds
operator|.
name|maxAsStr
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown extended_bounds key for a "
operator|+
name|token
operator|+
literal|" in aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
literal|"min"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|extendedBounds
operator|.
name|min
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"max"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|extendedBounds
operator|.
name|max
operator|=
name|parser
operator|.
name|longValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown extended_bounds key for a "
operator|+
name|token
operator|+
literal|" in aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|interval
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Missing required field [interval] for histogram aggregation ["
operator|+
name|aggregationName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|TimeZoneRounding
operator|.
name|Builder
name|tzRoundingBuilder
decl_stmt|;
name|DateTimeUnit
name|dateTimeUnit
init|=
name|dateFieldUnits
operator|.
name|get
argument_list|(
name|interval
argument_list|)
decl_stmt|;
if|if
condition|(
name|dateTimeUnit
operator|!=
literal|null
condition|)
block|{
name|tzRoundingBuilder
operator|=
name|TimeZoneRounding
operator|.
name|builder
argument_list|(
name|dateTimeUnit
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// the interval is a time value?
name|tzRoundingBuilder
operator|=
name|TimeZoneRounding
operator|.
name|builder
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|interval
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Rounding
name|rounding
init|=
name|tzRoundingBuilder
operator|.
name|preZone
argument_list|(
name|preZone
argument_list|)
operator|.
name|postZone
argument_list|(
name|postZone
argument_list|)
operator|.
name|preZoneAdjustLargeInterval
argument_list|(
name|preZoneAdjustLargeInterval
argument_list|)
operator|.
name|preOffset
argument_list|(
name|preOffset
argument_list|)
operator|.
name|postOffset
argument_list|(
name|postOffset
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
operator|new
name|HistogramAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|vsParser
operator|.
name|config
argument_list|()
argument_list|,
name|rounding
argument_list|,
name|order
argument_list|,
name|keyed
argument_list|,
name|minDocCount
argument_list|,
name|extendedBounds
argument_list|,
name|InternalDateHistogram
operator|.
name|FACTORY
argument_list|)
return|;
block|}
DECL|method|resolveOrder
specifier|private
specifier|static
name|InternalOrder
name|resolveOrder
parameter_list|(
name|String
name|key
parameter_list|,
name|boolean
name|asc
parameter_list|)
block|{
if|if
condition|(
literal|"_key"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
operator|||
literal|"_time"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
call|(
name|InternalOrder
call|)
argument_list|(
name|asc
condition|?
name|InternalOrder
operator|.
name|KEY_ASC
else|:
name|InternalOrder
operator|.
name|KEY_DESC
argument_list|)
return|;
block|}
if|if
condition|(
literal|"_count"
operator|.
name|equals
argument_list|(
name|key
argument_list|)
condition|)
block|{
return|return
call|(
name|InternalOrder
call|)
argument_list|(
name|asc
condition|?
name|InternalOrder
operator|.
name|COUNT_ASC
else|:
name|InternalOrder
operator|.
name|COUNT_DESC
argument_list|)
return|;
block|}
return|return
operator|new
name|InternalOrder
operator|.
name|Aggregation
argument_list|(
name|key
argument_list|,
name|asc
argument_list|)
return|;
block|}
DECL|method|parseOffset
specifier|private
name|long
name|parseOffset
parameter_list|(
name|String
name|offset
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|offset
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'-'
condition|)
block|{
return|return
operator|-
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|offset
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|null
argument_list|)
operator|.
name|millis
argument_list|()
return|;
block|}
name|int
name|beginIndex
init|=
name|offset
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'+'
condition|?
literal|1
else|:
literal|0
decl_stmt|;
return|return
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|offset
operator|.
name|substring
argument_list|(
name|beginIndex
argument_list|)
argument_list|,
literal|null
argument_list|)
operator|.
name|millis
argument_list|()
return|;
block|}
block|}
end_class

end_unit

