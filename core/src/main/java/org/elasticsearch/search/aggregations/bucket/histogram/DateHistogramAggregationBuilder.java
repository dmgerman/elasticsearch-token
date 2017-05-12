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
name|ObjectParser
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
name|XContentBuilder
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
name|query
operator|.
name|QueryParseContext
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
name|AggregatorFactories
operator|.
name|Builder
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
name|BucketOrder
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
name|InternalOrder
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
name|InternalOrder
operator|.
name|CompoundOrder
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
name|ValuesSource
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
name|ValuesSource
operator|.
name|Numeric
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
name|ValuesSourceAggregationBuilder
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
name|ValuesSourceAggregatorFactory
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
name|ValuesSourceParserHelper
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
name|ValuesSourceType
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
name|HashMap
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  * A builder for histograms on date fields.  */
end_comment

begin_class
DECL|class|DateHistogramAggregationBuilder
specifier|public
class|class
name|DateHistogramAggregationBuilder
extends|extends
name|ValuesSourceAggregationBuilder
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|,
name|DateHistogramAggregationBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"date_histogram"
decl_stmt|;
DECL|field|DATE_FIELD_UNITS
specifier|public
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|DateTimeUnit
argument_list|>
name|DATE_FIELD_UNITS
decl_stmt|;
static|static
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|DateTimeUnit
argument_list|>
name|dateFieldUnits
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"year"
argument_list|,
name|DateTimeUnit
operator|.
name|YEAR_OF_CENTURY
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1y"
argument_list|,
name|DateTimeUnit
operator|.
name|YEAR_OF_CENTURY
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"quarter"
argument_list|,
name|DateTimeUnit
operator|.
name|QUARTER
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1q"
argument_list|,
name|DateTimeUnit
operator|.
name|QUARTER
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"month"
argument_list|,
name|DateTimeUnit
operator|.
name|MONTH_OF_YEAR
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1M"
argument_list|,
name|DateTimeUnit
operator|.
name|MONTH_OF_YEAR
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"week"
argument_list|,
name|DateTimeUnit
operator|.
name|WEEK_OF_WEEKYEAR
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1w"
argument_list|,
name|DateTimeUnit
operator|.
name|WEEK_OF_WEEKYEAR
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"day"
argument_list|,
name|DateTimeUnit
operator|.
name|DAY_OF_MONTH
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1d"
argument_list|,
name|DateTimeUnit
operator|.
name|DAY_OF_MONTH
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"hour"
argument_list|,
name|DateTimeUnit
operator|.
name|HOUR_OF_DAY
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1h"
argument_list|,
name|DateTimeUnit
operator|.
name|HOUR_OF_DAY
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"minute"
argument_list|,
name|DateTimeUnit
operator|.
name|MINUTES_OF_HOUR
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1m"
argument_list|,
name|DateTimeUnit
operator|.
name|MINUTES_OF_HOUR
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"second"
argument_list|,
name|DateTimeUnit
operator|.
name|SECOND_OF_MINUTE
argument_list|)
expr_stmt|;
name|dateFieldUnits
operator|.
name|put
argument_list|(
literal|"1s"
argument_list|,
name|DateTimeUnit
operator|.
name|SECOND_OF_MINUTE
argument_list|)
expr_stmt|;
name|DATE_FIELD_UNITS
operator|=
name|unmodifiableMap
argument_list|(
name|dateFieldUnits
argument_list|)
expr_stmt|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|DateHistogramAggregationBuilder
argument_list|,
name|QueryParseContext
argument_list|>
name|PARSER
decl_stmt|;
static|static
block|{
name|PARSER
operator|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|DateHistogramAggregationBuilder
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|ValuesSourceParserHelper
operator|.
name|declareNumericFields
argument_list|(
name|PARSER
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
parameter_list|(
name|histogram
parameter_list|,
name|interval
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|interval
operator|instanceof
name|Long
condition|)
block|{
name|histogram
operator|.
name|interval
argument_list|(
operator|(
name|long
operator|)
name|interval
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|histogram
operator|.
name|dateHistogramInterval
argument_list|(
operator|(
name|DateHistogramInterval
operator|)
name|interval
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|p
lambda|->
block|{
if|if
condition|(
name|p
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
return|return
name|p
operator|.
name|longValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|DateHistogramInterval
argument_list|(
name|p
operator|.
name|text
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|,
name|Histogram
operator|.
name|INTERVAL_FIELD
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|LONG
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
name|DateHistogramAggregationBuilder
operator|::
name|offset
argument_list|,
name|p
lambda|->
block|{
if|if
condition|(
name|p
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
return|return
name|p
operator|.
name|longValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|DateHistogramAggregationBuilder
operator|.
name|parseStringOffset
argument_list|(
name|p
operator|.
name|text
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|,
name|Histogram
operator|.
name|OFFSET_FIELD
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|LONG
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareBoolean
argument_list|(
name|DateHistogramAggregationBuilder
operator|::
name|keyed
argument_list|,
name|Histogram
operator|.
name|KEYED_FIELD
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
name|DateHistogramAggregationBuilder
operator|::
name|minDocCount
argument_list|,
name|Histogram
operator|.
name|MIN_DOC_COUNT_FIELD
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareField
argument_list|(
name|DateHistogramAggregationBuilder
operator|::
name|extendedBounds
argument_list|,
name|parser
lambda|->
name|ExtendedBounds
operator|.
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
argument_list|,
name|ExtendedBounds
operator|.
name|EXTENDED_BOUNDS_FIELD
argument_list|,
name|ObjectParser
operator|.
name|ValueType
operator|.
name|OBJECT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareObjectArray
argument_list|(
name|DateHistogramAggregationBuilder
operator|::
name|order
argument_list|,
name|InternalOrder
operator|.
name|Parser
operator|::
name|parseOrderParam
argument_list|,
name|Histogram
operator|.
name|ORDER_FIELD
argument_list|)
expr_stmt|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|DateHistogramAggregationBuilder
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PARSER
operator|.
name|parse
argument_list|(
name|context
operator|.
name|parser
argument_list|()
argument_list|,
operator|new
name|DateHistogramAggregationBuilder
argument_list|(
name|aggregationName
argument_list|)
argument_list|,
name|context
argument_list|)
return|;
block|}
DECL|field|interval
specifier|private
name|long
name|interval
decl_stmt|;
DECL|field|dateHistogramInterval
specifier|private
name|DateHistogramInterval
name|dateHistogramInterval
decl_stmt|;
DECL|field|offset
specifier|private
name|long
name|offset
init|=
literal|0
decl_stmt|;
DECL|field|extendedBounds
specifier|private
name|ExtendedBounds
name|extendedBounds
decl_stmt|;
DECL|field|order
specifier|private
name|BucketOrder
name|order
init|=
name|BucketOrder
operator|.
name|key
argument_list|(
literal|true
argument_list|)
decl_stmt|;
DECL|field|keyed
specifier|private
name|boolean
name|keyed
init|=
literal|false
decl_stmt|;
DECL|field|minDocCount
specifier|private
name|long
name|minDocCount
init|=
literal|0
decl_stmt|;
comment|/** Create a new builder with the given name. */
DECL|method|DateHistogramAggregationBuilder
specifier|public
name|DateHistogramAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|ValuesSourceType
operator|.
name|NUMERIC
argument_list|,
name|ValueType
operator|.
name|DATE
argument_list|)
expr_stmt|;
block|}
comment|/** Read from a stream, for internal use only. */
DECL|method|DateHistogramAggregationBuilder
specifier|public
name|DateHistogramAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|ValuesSourceType
operator|.
name|NUMERIC
argument_list|,
name|ValueType
operator|.
name|DATE
argument_list|)
expr_stmt|;
name|order
operator|=
name|InternalOrder
operator|.
name|Streams
operator|.
name|readHistogramOrder
argument_list|(
name|in
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|keyed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|minDocCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|interval
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|dateHistogramInterval
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|DateHistogramInterval
operator|::
operator|new
argument_list|)
expr_stmt|;
name|offset
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|extendedBounds
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|ExtendedBounds
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerWriteTo
specifier|protected
name|void
name|innerWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalOrder
operator|.
name|Streams
operator|.
name|writeHistogramOrder
argument_list|(
name|order
argument_list|,
name|out
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|minDocCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|interval
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|dateHistogramInterval
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|extendedBounds
argument_list|)
expr_stmt|;
block|}
comment|/** Get the current interval in milliseconds that is set on this builder. */
DECL|method|interval
specifier|public
name|long
name|interval
parameter_list|()
block|{
return|return
name|interval
return|;
block|}
comment|/** Set the interval on this builder, and return the builder so that calls can be chained.      *  If both {@link #interval()} and {@link #dateHistogramInterval()} are set, then the      *  {@link #dateHistogramInterval()} wins. */
DECL|method|interval
specifier|public
name|DateHistogramAggregationBuilder
name|interval
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
if|if
condition|(
name|interval
operator|<
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[interval] must be 1 or greater for histogram aggregation ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Get the current date interval that is set on this builder. */
DECL|method|dateHistogramInterval
specifier|public
name|DateHistogramInterval
name|dateHistogramInterval
parameter_list|()
block|{
return|return
name|dateHistogramInterval
return|;
block|}
comment|/** Set the interval on this builder, and return the builder so that calls can be chained.      *  If both {@link #interval()} and {@link #dateHistogramInterval()} are set, then the      *  {@link #dateHistogramInterval()} wins. */
DECL|method|dateHistogramInterval
specifier|public
name|DateHistogramAggregationBuilder
name|dateHistogramInterval
parameter_list|(
name|DateHistogramInterval
name|dateHistogramInterval
parameter_list|)
block|{
if|if
condition|(
name|dateHistogramInterval
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[dateHistogramInterval] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|dateHistogramInterval
operator|=
name|dateHistogramInterval
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Get the offset to use when rounding, which is a number of milliseconds. */
DECL|method|offset
specifier|public
name|long
name|offset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
comment|/** Set the offset on this builder, which is a number of milliseconds, and      *  return the builder so that calls can be chained. */
DECL|method|offset
specifier|public
name|DateHistogramAggregationBuilder
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
comment|/** Set the offset on this builder, as a time value, and      *  return the builder so that calls can be chained. */
DECL|method|offset
specifier|public
name|DateHistogramAggregationBuilder
name|offset
parameter_list|(
name|String
name|offset
parameter_list|)
block|{
if|if
condition|(
name|offset
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[offset] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|offset
argument_list|(
name|parseStringOffset
argument_list|(
name|offset
argument_list|)
argument_list|)
return|;
block|}
DECL|method|parseStringOffset
specifier|static
name|long
name|parseStringOffset
parameter_list|(
name|String
name|offset
parameter_list|)
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
argument_list|,
name|DateHistogramAggregationBuilder
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".parseOffset"
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
argument_list|,
name|DateHistogramAggregationBuilder
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".parseOffset"
argument_list|)
operator|.
name|millis
argument_list|()
return|;
block|}
comment|/** Return extended bounds for this histogram, or {@code null} if none are set. */
DECL|method|extendedBounds
specifier|public
name|ExtendedBounds
name|extendedBounds
parameter_list|()
block|{
return|return
name|extendedBounds
return|;
block|}
comment|/** Set extended bounds on this histogram, so that buckets would also be      *  generated on intervals that did not match any documents. */
DECL|method|extendedBounds
specifier|public
name|DateHistogramAggregationBuilder
name|extendedBounds
parameter_list|(
name|ExtendedBounds
name|extendedBounds
parameter_list|)
block|{
if|if
condition|(
name|extendedBounds
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[extendedBounds] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|extendedBounds
operator|=
name|extendedBounds
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Return the order to use to sort buckets of this histogram. */
DECL|method|order
specifier|public
name|BucketOrder
name|order
parameter_list|()
block|{
return|return
name|order
return|;
block|}
comment|/** Set a new order on this builder and return the builder so that calls      *  can be chained. A tie-breaker may be added to avoid non-deterministic ordering. */
DECL|method|order
specifier|public
name|DateHistogramAggregationBuilder
name|order
parameter_list|(
name|BucketOrder
name|order
parameter_list|)
block|{
if|if
condition|(
name|order
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[order] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|order
operator|instanceof
name|CompoundOrder
operator|||
name|InternalOrder
operator|.
name|isKeyOrder
argument_list|(
name|order
argument_list|)
condition|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
comment|// if order already contains a tie-breaker we are good to go
block|}
else|else
block|{
comment|// otherwise add a tie-breaker by using a compound order
name|this
operator|.
name|order
operator|=
name|BucketOrder
operator|.
name|compound
argument_list|(
name|order
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Sets the order in which the buckets will be returned. A tie-breaker may be added to avoid non-deterministic      * ordering.      */
DECL|method|order
specifier|public
name|DateHistogramAggregationBuilder
name|order
parameter_list|(
name|List
argument_list|<
name|BucketOrder
argument_list|>
name|orders
parameter_list|)
block|{
if|if
condition|(
name|orders
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[orders] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// if the list only contains one order use that to avoid inconsistent xcontent
name|order
argument_list|(
name|orders
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|?
name|BucketOrder
operator|.
name|compound
argument_list|(
name|orders
argument_list|)
else|:
name|orders
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Return whether buckets should be returned as a hash. In case      *  {@code keyed} is false, buckets will be returned as an array. */
DECL|method|keyed
specifier|public
name|boolean
name|keyed
parameter_list|()
block|{
return|return
name|keyed
return|;
block|}
comment|/** Set whether to return buckets as a hash or as an array, and return the      *  builder so that calls can be chained. */
DECL|method|keyed
specifier|public
name|DateHistogramAggregationBuilder
name|keyed
parameter_list|(
name|boolean
name|keyed
parameter_list|)
block|{
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Return the minimum count of documents that buckets need to have in order      *  to be included in the response. */
DECL|method|minDocCount
specifier|public
name|long
name|minDocCount
parameter_list|()
block|{
return|return
name|minDocCount
return|;
block|}
comment|/** Set the minimum count of matching documents that buckets need to have      *  and return this builder so that calls can be chained. */
DECL|method|minDocCount
specifier|public
name|DateHistogramAggregationBuilder
name|minDocCount
parameter_list|(
name|long
name|minDocCount
parameter_list|)
block|{
if|if
condition|(
name|minDocCount
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[minDocCount] must be greater than or equal to 0. Found ["
operator|+
name|minDocCount
operator|+
literal|"] in ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|minDocCount
operator|=
name|minDocCount
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|XContentBuilder
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dateHistogramInterval
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Histogram
operator|.
name|INTERVAL_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|interval
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|Histogram
operator|.
name|INTERVAL_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|dateHistogramInterval
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|Histogram
operator|.
name|OFFSET_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
name|order
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Histogram
operator|.
name|ORDER_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|order
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|Histogram
operator|.
name|KEYED_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|keyed
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Histogram
operator|.
name|MIN_DOC_COUNT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|minDocCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|extendedBounds
operator|!=
literal|null
condition|)
block|{
name|extendedBounds
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|innerBuild
specifier|protected
name|ValuesSourceAggregatorFactory
argument_list|<
name|Numeric
argument_list|,
name|?
argument_list|>
name|innerBuild
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|Numeric
argument_list|>
name|config
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
name|Rounding
name|rounding
init|=
name|createRounding
argument_list|()
decl_stmt|;
name|ExtendedBounds
name|roundedBounds
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|extendedBounds
operator|!=
literal|null
condition|)
block|{
comment|// parse any string bounds to longs and round
name|roundedBounds
operator|=
name|this
operator|.
name|extendedBounds
operator|.
name|parseAndValidate
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|)
operator|.
name|round
argument_list|(
name|rounding
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|DateHistogramAggregatorFactory
argument_list|(
name|name
argument_list|,
name|config
argument_list|,
name|interval
argument_list|,
name|dateHistogramInterval
argument_list|,
name|offset
argument_list|,
name|order
argument_list|,
name|keyed
argument_list|,
name|minDocCount
argument_list|,
name|rounding
argument_list|,
name|roundedBounds
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
DECL|method|createRounding
specifier|private
name|Rounding
name|createRounding
parameter_list|()
block|{
name|Rounding
operator|.
name|Builder
name|tzRoundingBuilder
decl_stmt|;
if|if
condition|(
name|dateHistogramInterval
operator|!=
literal|null
condition|)
block|{
name|DateTimeUnit
name|dateTimeUnit
init|=
name|DATE_FIELD_UNITS
operator|.
name|get
argument_list|(
name|dateHistogramInterval
operator|.
name|toString
argument_list|()
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
name|Rounding
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
name|Rounding
operator|.
name|builder
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|dateHistogramInterval
operator|.
name|toString
argument_list|()
argument_list|,
literal|null
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".interval"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// the interval is an integer time value in millis?
name|tzRoundingBuilder
operator|=
name|Rounding
operator|.
name|builder
argument_list|(
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
name|interval
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timeZone
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|tzRoundingBuilder
operator|.
name|timeZone
argument_list|(
name|timeZone
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Rounding
name|rounding
init|=
name|tzRoundingBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|rounding
return|;
block|}
annotation|@
name|Override
DECL|method|innerHashCode
specifier|protected
name|int
name|innerHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|order
argument_list|,
name|keyed
argument_list|,
name|minDocCount
argument_list|,
name|interval
argument_list|,
name|dateHistogramInterval
argument_list|,
name|minDocCount
argument_list|,
name|extendedBounds
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|innerEquals
specifier|protected
name|boolean
name|innerEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|DateHistogramAggregationBuilder
name|other
init|=
operator|(
name|DateHistogramAggregationBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|order
argument_list|,
name|other
operator|.
name|order
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|keyed
argument_list|,
name|other
operator|.
name|keyed
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|minDocCount
argument_list|,
name|other
operator|.
name|minDocCount
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|interval
argument_list|,
name|other
operator|.
name|interval
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|dateHistogramInterval
argument_list|,
name|other
operator|.
name|dateHistogramInterval
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|offset
argument_list|,
name|other
operator|.
name|offset
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|extendedBounds
argument_list|,
name|other
operator|.
name|extendedBounds
argument_list|)
return|;
block|}
block|}
end_class

end_unit

