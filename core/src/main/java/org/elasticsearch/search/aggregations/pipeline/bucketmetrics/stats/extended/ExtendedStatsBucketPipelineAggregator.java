begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.bucketmetrics.stats.extended
package|package
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
name|bucketmetrics
operator|.
name|stats
operator|.
name|extended
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
name|BucketHelpers
operator|.
name|GapPolicy
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
name|pipeline
operator|.
name|PipelineAggregatorBuilder
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
name|PipelineAggregatorStreams
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
name|bucketmetrics
operator|.
name|BucketMetricsPipelineAggregatorBuilder
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
name|bucketmetrics
operator|.
name|BucketMetricsPipelineAggregator
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

begin_class
DECL|class|ExtendedStatsBucketPipelineAggregator
specifier|public
class|class
name|ExtendedStatsBucketPipelineAggregator
extends|extends
name|BucketMetricsPipelineAggregator
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
literal|"extended_stats_bucket"
argument_list|)
decl_stmt|;
DECL|field|STREAM
specifier|public
specifier|final
specifier|static
name|PipelineAggregatorStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|PipelineAggregatorStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ExtendedStatsBucketPipelineAggregator
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ExtendedStatsBucketPipelineAggregator
name|result
init|=
operator|new
name|ExtendedStatsBucketPipelineAggregator
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
name|PipelineAggregatorStreams
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
name|InternalExtendedStatsBucket
operator|.
name|registerStreams
argument_list|()
expr_stmt|;
block|}
DECL|field|sum
specifier|private
name|double
name|sum
init|=
literal|0
decl_stmt|;
DECL|field|count
specifier|private
name|long
name|count
init|=
literal|0
decl_stmt|;
DECL|field|min
specifier|private
name|double
name|min
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
DECL|field|max
specifier|private
name|double
name|max
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
DECL|field|sumOfSqrs
specifier|private
name|double
name|sumOfSqrs
init|=
literal|1
decl_stmt|;
DECL|field|sigma
specifier|private
name|double
name|sigma
decl_stmt|;
DECL|method|ExtendedStatsBucketPipelineAggregator
specifier|protected
name|ExtendedStatsBucketPipelineAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|,
name|double
name|sigma
parameter_list|,
name|GapPolicy
name|gapPolicy
parameter_list|,
name|ValueFormatter
name|formatter
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
name|bucketsPaths
argument_list|,
name|gapPolicy
argument_list|,
name|formatter
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|sigma
operator|=
name|sigma
expr_stmt|;
block|}
DECL|method|ExtendedStatsBucketPipelineAggregator
name|ExtendedStatsBucketPipelineAggregator
parameter_list|()
block|{
comment|// For Serialization
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
annotation|@
name|Override
DECL|method|preCollection
specifier|protected
name|void
name|preCollection
parameter_list|()
block|{
name|sum
operator|=
literal|0
expr_stmt|;
name|count
operator|=
literal|0
expr_stmt|;
name|min
operator|=
name|Double
operator|.
name|POSITIVE_INFINITY
expr_stmt|;
name|max
operator|=
name|Double
operator|.
name|NEGATIVE_INFINITY
expr_stmt|;
name|sumOfSqrs
operator|=
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collectBucketValue
specifier|protected
name|void
name|collectBucketValue
parameter_list|(
name|String
name|bucketKey
parameter_list|,
name|Double
name|bucketValue
parameter_list|)
block|{
name|sum
operator|+=
name|bucketValue
expr_stmt|;
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|min
argument_list|,
name|bucketValue
argument_list|)
expr_stmt|;
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|max
argument_list|,
name|bucketValue
argument_list|)
expr_stmt|;
name|count
operator|+=
literal|1
expr_stmt|;
name|sumOfSqrs
operator|+=
name|bucketValue
operator|*
name|bucketValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|protected
name|InternalAggregation
name|buildAggregation
parameter_list|(
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
name|metadata
parameter_list|)
block|{
return|return
operator|new
name|InternalExtendedStatsBucket
argument_list|(
name|name
argument_list|()
argument_list|,
name|count
argument_list|,
name|sum
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|sumOfSqrs
argument_list|,
name|sigma
argument_list|,
name|formatter
argument_list|,
name|pipelineAggregators
argument_list|,
name|metadata
argument_list|)
return|;
block|}
DECL|class|ExtendedStatsBucketPipelineAggregatorBuilder
specifier|public
specifier|static
class|class
name|ExtendedStatsBucketPipelineAggregatorBuilder
extends|extends
name|BucketMetricsPipelineAggregatorBuilder
argument_list|<
name|ExtendedStatsBucketPipelineAggregatorBuilder
argument_list|>
block|{
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|PROTOTYPE
init|=
operator|new
name|ExtendedStatsBucketPipelineAggregatorBuilder
argument_list|(
literal|""
argument_list|,
literal|""
argument_list|)
decl_stmt|;
DECL|field|sigma
specifier|private
name|double
name|sigma
init|=
literal|2.0
decl_stmt|;
DECL|method|ExtendedStatsBucketPipelineAggregatorBuilder
specifier|public
name|ExtendedStatsBucketPipelineAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
operator|new
name|String
index|[]
block|{
name|bucketsPath
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|ExtendedStatsBucketPipelineAggregatorBuilder
specifier|private
name|ExtendedStatsBucketPipelineAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|TYPE
operator|.
name|name
argument_list|()
argument_list|,
name|bucketsPaths
argument_list|)
expr_stmt|;
block|}
comment|/**          * Set the value of sigma to use when calculating the standard deviation          * bounds          */
DECL|method|sigma
specifier|public
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|sigma
parameter_list|(
name|double
name|sigma
parameter_list|)
block|{
if|if
condition|(
name|sigma
operator|<
literal|0.0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|ExtendedStatsBucketParser
operator|.
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must be a non-negative double"
argument_list|)
throw|;
block|}
name|this
operator|.
name|sigma
operator|=
name|sigma
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Get the value of sigma to use when calculating the standard deviation          * bounds          */
DECL|method|sigma
specifier|public
name|double
name|sigma
parameter_list|()
block|{
return|return
name|sigma
return|;
block|}
annotation|@
name|Override
DECL|method|createInternal
specifier|protected
name|PipelineAggregator
name|createInternal
parameter_list|(
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
name|ExtendedStatsBucketPipelineAggregator
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|,
name|sigma
argument_list|,
name|gapPolicy
argument_list|()
argument_list|,
name|formatter
argument_list|()
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doValidate
specifier|public
name|void
name|doValidate
parameter_list|(
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
index|[]
name|aggFactories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregatorBuilder
argument_list|>
name|pipelineAggregatorFactories
parameter_list|)
block|{
if|if
condition|(
name|bucketsPaths
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|Parser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must contain a single entry for aggregation ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|sigma
operator|<
literal|0.0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|ExtendedStatsBucketParser
operator|.
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must be a non-negative double"
argument_list|)
throw|;
block|}
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
name|builder
operator|.
name|field
argument_list|(
name|ExtendedStatsBucketParser
operator|.
name|SIGMA
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|sigma
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|innerReadFrom
specifier|protected
name|BucketMetricsPipelineAggregatorBuilder
name|innerReadFrom
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|factory
init|=
operator|new
name|ExtendedStatsBucketPipelineAggregatorBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|)
decl_stmt|;
name|factory
operator|.
name|sigma
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
return|return
name|factory
return|;
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
name|out
operator|.
name|writeDouble
argument_list|(
name|sigma
argument_list|)
expr_stmt|;
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
name|sigma
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
name|BucketMetricsPipelineAggregatorBuilder
name|obj
parameter_list|)
block|{
name|ExtendedStatsBucketPipelineAggregatorBuilder
name|other
init|=
operator|(
name|ExtendedStatsBucketPipelineAggregatorBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|sigma
argument_list|,
name|other
operator|.
name|sigma
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

