begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles.tdigest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|percentiles
operator|.
name|tdigest
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationStreams
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
name|metrics
operator|.
name|percentiles
operator|.
name|InternalPercentile
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
name|metrics
operator|.
name|percentiles
operator|.
name|Percentile
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
name|metrics
operator|.
name|percentiles
operator|.
name|PercentileRanks
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
name|format
operator|.
name|ValueFormatter
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
name|Iterator
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

begin_comment
comment|/** * */
end_comment

begin_class
DECL|class|InternalTDigestPercentileRanks
specifier|public
class|class
name|InternalTDigestPercentileRanks
extends|extends
name|AbstractInternalTDigestPercentiles
implements|implements
name|PercentileRanks
block|{
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
name|PercentileRanks
operator|.
name|TYPE_NAME
argument_list|,
literal|"t_digest_percentile_ranks"
argument_list|)
decl_stmt|;
DECL|field|STREAM
specifier|public
specifier|final
specifier|static
name|AggregationStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|AggregationStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InternalTDigestPercentileRanks
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalTDigestPercentileRanks
name|result
init|=
operator|new
name|InternalTDigestPercentileRanks
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
decl_stmt|;
DECL|method|registerStreams
specifier|public
specifier|static
name|void
name|registerStreams
parameter_list|()
block|{
name|AggregationStreams
operator|.
name|registerStream
argument_list|(
name|STREAM
argument_list|,
name|TYPE
operator|.
name|stream
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|InternalTDigestPercentileRanks
name|InternalTDigestPercentileRanks
parameter_list|()
block|{}
comment|// for serialization
DECL|method|InternalTDigestPercentileRanks
specifier|public
name|InternalTDigestPercentileRanks
parameter_list|(
name|String
name|name
parameter_list|,
name|double
index|[]
name|cdfValues
parameter_list|,
name|TDigestState
name|state
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|ValueFormatter
name|formatter
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
block|{
name|super
argument_list|(
name|name
argument_list|,
name|cdfValues
argument_list|,
name|state
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Percentile
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|Iter
argument_list|(
name|keys
argument_list|,
name|state
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|percent
specifier|public
name|double
name|percent
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|percentileRank
argument_list|(
name|state
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|percentAsString
specifier|public
name|String
name|percentAsString
parameter_list|(
name|double
name|value
parameter_list|)
block|{
return|return
name|valueAsString
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|double
name|value
parameter_list|(
name|double
name|key
parameter_list|)
block|{
return|return
name|percent
argument_list|(
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createReduced
specifier|protected
name|AbstractInternalTDigestPercentiles
name|createReduced
parameter_list|(
name|String
name|name
parameter_list|,
name|double
index|[]
name|keys
parameter_list|,
name|TDigestState
name|merged
parameter_list|,
name|boolean
name|keyed
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
block|{
return|return
operator|new
name|InternalTDigestPercentileRanks
argument_list|(
name|name
argument_list|,
name|keys
argument_list|,
name|merged
argument_list|,
name|keyed
argument_list|,
name|valueFormatter
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|percentileRank
specifier|static
name|double
name|percentileRank
parameter_list|(
name|TDigestState
name|state
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|double
name|percentileRank
init|=
name|state
operator|.
name|cdf
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|percentileRank
operator|<
literal|0
condition|)
block|{
name|percentileRank
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|percentileRank
operator|>
literal|1
condition|)
block|{
name|percentileRank
operator|=
literal|1
expr_stmt|;
block|}
return|return
name|percentileRank
operator|*
literal|100
return|;
block|}
DECL|class|Iter
specifier|public
specifier|static
class|class
name|Iter
implements|implements
name|Iterator
argument_list|<
name|Percentile
argument_list|>
block|{
DECL|field|values
specifier|private
specifier|final
name|double
index|[]
name|values
decl_stmt|;
DECL|field|state
specifier|private
specifier|final
name|TDigestState
name|state
decl_stmt|;
DECL|field|i
specifier|private
name|int
name|i
decl_stmt|;
DECL|method|Iter
specifier|public
name|Iter
parameter_list|(
name|double
index|[]
name|values
parameter_list|,
name|TDigestState
name|state
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|i
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hasNext
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|i
operator|<
name|values
operator|.
name|length
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|Percentile
name|next
parameter_list|()
block|{
specifier|final
name|Percentile
name|next
init|=
operator|new
name|InternalPercentile
argument_list|(
name|percentileRank
argument_list|(
name|state
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
decl_stmt|;
operator|++
name|i
expr_stmt|;
return|return
name|next
return|;
block|}
annotation|@
name|Override
DECL|method|remove
specifier|public
specifier|final
name|void
name|remove
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

