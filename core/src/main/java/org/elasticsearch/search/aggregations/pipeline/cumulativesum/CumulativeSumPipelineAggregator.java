begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.cumulativesum
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
name|cumulativesum
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
name|ReduceContext
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
name|InternalAggregations
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
name|bucket
operator|.
name|histogram
operator|.
name|InternalHistogram
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
name|InternalSimpleValue
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
name|support
operator|.
name|format
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
name|format
operator|.
name|ValueFormatterStreams
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
name|ArrayList
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
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|StreamSupport
import|;
end_import

begin_import
import|import static
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
name|resolveBucketValue
import|;
end_import

begin_class
DECL|class|CumulativeSumPipelineAggregator
specifier|public
class|class
name|CumulativeSumPipelineAggregator
extends|extends
name|PipelineAggregator
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
literal|"cumulative_sum"
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
name|in
lambda|->
block|{
name|CumulativeSumPipelineAggregator
name|result
init|=
operator|new
name|CumulativeSumPipelineAggregator
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
block|}
DECL|field|formatter
specifier|private
name|ValueFormatter
name|formatter
decl_stmt|;
DECL|method|CumulativeSumPipelineAggregator
specifier|public
name|CumulativeSumPipelineAggregator
parameter_list|()
block|{     }
DECL|method|CumulativeSumPipelineAggregator
specifier|public
name|CumulativeSumPipelineAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
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
name|metadata
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|,
name|metadata
argument_list|)
expr_stmt|;
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
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
DECL|method|reduce
specifier|public
name|InternalAggregation
name|reduce
parameter_list|(
name|InternalAggregation
name|aggregation
parameter_list|,
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|InternalHistogram
name|histo
init|=
operator|(
name|InternalHistogram
operator|)
name|aggregation
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|InternalHistogram
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|histo
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|InternalHistogram
operator|.
name|Factory
argument_list|<
name|?
extends|extends
name|InternalHistogram
operator|.
name|Bucket
argument_list|>
name|factory
init|=
name|histo
operator|.
name|getFactory
argument_list|()
decl_stmt|;
name|List
name|newBuckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|double
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InternalHistogram
operator|.
name|Bucket
name|bucket
range|:
name|buckets
control|)
block|{
name|Double
name|thisBucketValue
init|=
name|resolveBucketValue
argument_list|(
name|histo
argument_list|,
name|bucket
argument_list|,
name|bucketsPaths
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|GapPolicy
operator|.
name|INSERT_ZEROS
argument_list|)
decl_stmt|;
name|sum
operator|+=
name|thisBucketValue
expr_stmt|;
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggs
init|=
name|StreamSupport
operator|.
name|stream
argument_list|(
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|spliterator
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|map
argument_list|(
parameter_list|(
name|p
parameter_list|)
lambda|->
block|{
return|return
operator|(
name|InternalAggregation
operator|)
name|p
return|;
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|aggs
operator|.
name|add
argument_list|(
operator|new
name|InternalSimpleValue
argument_list|(
name|name
argument_list|()
argument_list|,
name|sum
argument_list|,
name|formatter
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|PipelineAggregator
argument_list|>
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|InternalHistogram
operator|.
name|Bucket
name|newBucket
init|=
name|factory
operator|.
name|createBucket
argument_list|(
name|bucket
operator|.
name|getKey
argument_list|()
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
operator|new
name|InternalAggregations
argument_list|(
name|aggs
argument_list|)
argument_list|,
name|bucket
operator|.
name|getKeyed
argument_list|()
argument_list|,
name|bucket
operator|.
name|getFormatter
argument_list|()
argument_list|)
decl_stmt|;
name|newBuckets
operator|.
name|add
argument_list|(
name|newBucket
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
operator|.
name|create
argument_list|(
name|newBuckets
argument_list|,
name|histo
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|public
name|void
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|formatter
operator|=
name|ValueFormatterStreams
operator|.
name|readOptional
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|public
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|ValueFormatterStreams
operator|.
name|writeOptional
argument_list|(
name|formatter
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

