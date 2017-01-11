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
name|Rounding
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
DECL|field|rounding
specifier|private
name|Rounding
name|rounding
decl_stmt|;
DECL|method|DateHistogramAggregatorFactory
specifier|public
name|DateHistogramAggregatorFactory
parameter_list|(
name|String
name|name
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
name|Rounding
name|rounding
parameter_list|,
name|ExtendedBounds
name|extendedBounds
parameter_list|,
name|SearchContext
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
name|this
operator|.
name|rounding
operator|=
name|rounding
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
name|extendedBounds
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

