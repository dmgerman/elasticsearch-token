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
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|FieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
operator|.
name|DateFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|FieldContext
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
name|ValuesSourceConfig
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
name|numeric
operator|.
name|NumericValuesSource
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
name|numeric
operator|.
name|ValueFormatter
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
name|numeric
operator|.
name|ValueParser
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|ValuesSourceConfig
argument_list|<
name|NumericValuesSource
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|<>
argument_list|(
name|NumericValuesSource
operator|.
name|class
argument_list|)
decl_stmt|;
name|String
name|field
init|=
literal|null
decl_stmt|;
name|String
name|script
init|=
literal|null
decl_stmt|;
name|String
name|scriptLang
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|scriptParams
init|=
literal|null
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
name|String
name|format
init|=
literal|null
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
name|boolean
name|assumeSorted
init|=
literal|false
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
literal|"field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|field
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
literal|"script"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|script
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
literal|"lang"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|scriptLang
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
elseif|else
if|if
condition|(
literal|"format"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|format
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
literal|"script_values_sorted"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"scriptValuesSorted"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|assumeSorted
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
literal|"params"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|scriptParams
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
elseif|else
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
name|SearchScript
name|searchScript
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|searchScript
operator|=
name|context
operator|.
name|scriptService
argument_list|()
operator|.
name|search
argument_list|(
name|context
operator|.
name|lookup
argument_list|()
argument_list|,
name|scriptLang
argument_list|,
name|script
argument_list|,
name|scriptParams
argument_list|)
expr_stmt|;
name|config
operator|.
name|script
argument_list|(
name|searchScript
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|assumeSorted
condition|)
block|{
comment|// we need values to be sorted and unique for efficiency
name|config
operator|.
name|ensureSorted
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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
name|TimeZoneRounding
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
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|config
operator|.
name|formatter
argument_list|(
operator|new
name|ValueFormatter
operator|.
name|DateTime
argument_list|(
name|format
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|searchScript
operator|!=
literal|null
condition|)
block|{
name|ValueParser
name|valueParser
init|=
operator|new
name|ValueParser
operator|.
name|DateMath
argument_list|(
operator|new
name|DateMathParser
argument_list|(
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|,
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|TIME_UNIT
argument_list|)
argument_list|)
decl_stmt|;
name|config
operator|.
name|parser
argument_list|(
name|valueParser
argument_list|)
expr_stmt|;
return|return
operator|new
name|HistogramAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|config
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
comment|// falling back on the get field data context
return|return
operator|new
name|HistogramAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|config
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
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|context
operator|.
name|smartNameFieldMapper
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
name|config
operator|.
name|unmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
name|config
operator|.
name|formatter
argument_list|(
operator|new
name|ValueFormatter
operator|.
name|DateTime
argument_list|(
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|config
operator|.
name|parser
argument_list|(
operator|new
name|ValueParser
operator|.
name|DateMath
argument_list|(
operator|new
name|DateMathParser
argument_list|(
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
argument_list|,
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|TIME_UNIT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|HistogramAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|config
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
if|if
condition|(
operator|!
operator|(
name|mapper
operator|instanceof
name|DateFieldMapper
operator|)
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"date histogram can only be aggregated on date fields but ["
operator|+
name|field
operator|+
literal|"] is not a date field"
argument_list|)
throw|;
block|}
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
name|config
operator|.
name|fieldContext
argument_list|(
operator|new
name|FieldContext
argument_list|(
name|field
argument_list|,
name|indexFieldData
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
name|config
operator|.
name|formatter
argument_list|(
operator|new
name|ValueFormatter
operator|.
name|DateTime
argument_list|(
operator|(
operator|(
name|DateFieldMapper
operator|)
name|mapper
operator|)
operator|.
name|dateTimeFormatter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|config
operator|.
name|parser
argument_list|(
operator|new
name|ValueParser
operator|.
name|DateMath
argument_list|(
operator|new
name|DateMathParser
argument_list|(
operator|(
operator|(
name|DateFieldMapper
operator|)
name|mapper
operator|)
operator|.
name|dateTimeFormatter
argument_list|()
argument_list|,
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|TIME_UNIT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|HistogramAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|config
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
DECL|method|parseZone
specifier|private
name|DateTimeZone
name|parseZone
parameter_list|(
name|String
name|text
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|index
init|=
name|text
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
operator|-
literal|1
condition|)
block|{
name|int
name|beginIndex
init|=
name|text
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
comment|// format like -02:30
return|return
name|DateTimeZone
operator|.
name|forOffsetHoursMinutes
argument_list|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|text
operator|.
name|substring
argument_list|(
name|beginIndex
argument_list|,
name|index
argument_list|)
argument_list|)
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|text
operator|.
name|substring
argument_list|(
name|index
operator|+
literal|1
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
comment|// id, listed here: http://joda-time.sourceforge.net/timezones.html
return|return
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|text
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

