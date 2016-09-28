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
name|AggregatorFactories
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
name|InternalAggregation
operator|.
name|Type
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
name|pipeline
operator|.
name|PipelineAggregator
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
name|AggregationContext
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

begin_class
DECL|class|DateHistogramAggregatorFactory
specifier|public
specifier|final
class|class
name|DateHistogramAggregatorFactory
extends|extends
name|ValuesSourceAggregatorFactory
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|,
name|DateHistogramAggregatorFactory
argument_list|>
block|{
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
DECL|field|dateHistogramInterval
specifier|private
specifier|final
name|DateHistogramInterval
name|dateHistogramInterval
decl_stmt|;
DECL|field|interval
specifier|private
specifier|final
name|long
name|interval
decl_stmt|;
DECL|field|offset
specifier|private
specifier|final
name|long
name|offset
decl_stmt|;
DECL|field|order
specifier|private
specifier|final
name|InternalOrder
name|order
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|field|minDocCount
specifier|private
specifier|final
name|long
name|minDocCount
decl_stmt|;
DECL|field|extendedBounds
specifier|private
specifier|final
name|ExtendedBounds
name|extendedBounds
decl_stmt|;
DECL|method|DateHistogramAggregatorFactory
specifier|public
name|DateHistogramAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|Numeric
argument_list|>
name|config
parameter_list|,
name|long
name|interval
parameter_list|,
name|DateHistogramInterval
name|dateHistogramInterval
parameter_list|,
name|long
name|offset
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|ExtendedBounds
name|extendedBounds
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactories
operator|.
name|Builder
name|subFactoriesBuilder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|config
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
name|this
operator|.
name|dateHistogramInterval
operator|=
name|dateHistogramInterval
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|minDocCount
operator|=
name|minDocCount
expr_stmt|;
name|this
operator|.
name|extendedBounds
operator|=
name|extendedBounds
expr_stmt|;
block|}
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
DECL|method|doCreateInternal
specifier|protected
name|Aggregator
name|doCreateInternal
parameter_list|(
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|collectsFromSingleBucket
operator|==
literal|false
condition|)
block|{
return|return
name|asMultiBucketAggregator
argument_list|(
name|this
argument_list|,
name|context
argument_list|,
name|parent
argument_list|)
return|;
block|}
return|return
name|createAggregator
argument_list|(
name|valuesSource
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
DECL|method|createAggregator
specifier|private
name|Aggregator
name|createAggregator
parameter_list|(
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
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
comment|// we need to round the bounds given by the user and we have to do it
comment|// for every aggregator we create
comment|// as the rounding is not necessarily an idempotent operation.
comment|// todo we need to think of a better structure to the factory/agtor
comment|// code so we won't need to do that
name|ExtendedBounds
name|roundedBounds
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|extendedBounds
operator|!=
literal|null
condition|)
block|{
comment|// parse any string bounds to longs and round them
name|roundedBounds
operator|=
name|extendedBounds
operator|.
name|parseAndValidate
argument_list|(
name|name
argument_list|,
name|context
operator|.
name|searchContext
argument_list|()
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
name|DateHistogramAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|rounding
argument_list|,
name|offset
argument_list|,
name|order
argument_list|,
name|keyed
argument_list|,
name|minDocCount
argument_list|,
name|roundedBounds
argument_list|,
name|valuesSource
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
name|Aggregator
name|parent
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|createAggregator
argument_list|(
literal|null
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
block|}
end_class

end_unit

