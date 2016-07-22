begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.serialdiff
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
name|serialdiff
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
name|Nullable
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
name|EvictingQueue
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
name|search
operator|.
name|DocValueFormat
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
name|bucket
operator|.
name|MultiBucketsAggregation
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
name|MultiBucketsAggregation
operator|.
name|Bucket
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
name|HistogramFactory
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
DECL|class|SerialDiffPipelineAggregator
specifier|public
class|class
name|SerialDiffPipelineAggregator
extends|extends
name|PipelineAggregator
block|{
DECL|field|formatter
specifier|private
name|DocValueFormat
name|formatter
decl_stmt|;
DECL|field|gapPolicy
specifier|private
name|GapPolicy
name|gapPolicy
decl_stmt|;
DECL|field|lag
specifier|private
name|int
name|lag
decl_stmt|;
DECL|method|SerialDiffPipelineAggregator
specifier|public
name|SerialDiffPipelineAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|bucketsPaths
parameter_list|,
annotation|@
name|Nullable
name|DocValueFormat
name|formatter
parameter_list|,
name|GapPolicy
name|gapPolicy
parameter_list|,
name|int
name|lag
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
name|this
operator|.
name|gapPolicy
operator|=
name|gapPolicy
expr_stmt|;
name|this
operator|.
name|lag
operator|=
name|lag
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|SerialDiffPipelineAggregator
specifier|public
name|SerialDiffPipelineAggregator
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
argument_list|)
expr_stmt|;
name|formatter
operator|=
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|DocValueFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|gapPolicy
operator|=
name|GapPolicy
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|lag
operator|=
name|in
operator|.
name|readVInt
argument_list|()
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
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|formatter
argument_list|)
expr_stmt|;
name|gapPolicy
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|lag
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|SerialDiffPipelineAggregationBuilder
operator|.
name|NAME
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
name|MultiBucketsAggregation
name|histo
init|=
operator|(
name|MultiBucketsAggregation
operator|)
name|aggregation
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|buckets
init|=
name|histo
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|HistogramFactory
name|factory
init|=
operator|(
name|HistogramFactory
operator|)
name|histo
decl_stmt|;
name|List
argument_list|<
name|Bucket
argument_list|>
name|newBuckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|EvictingQueue
argument_list|<
name|Double
argument_list|>
name|lagWindow
init|=
operator|new
name|EvictingQueue
argument_list|<>
argument_list|(
name|lag
argument_list|)
decl_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
for|for
control|(
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
name|gapPolicy
argument_list|)
decl_stmt|;
name|Bucket
name|newBucket
init|=
name|bucket
decl_stmt|;
name|counter
operator|+=
literal|1
expr_stmt|;
comment|// Still under the initial lag period, add nothing and move on
name|Double
name|lagValue
decl_stmt|;
if|if
condition|(
name|counter
operator|<=
name|lag
condition|)
block|{
name|lagValue
operator|=
name|Double
operator|.
name|NaN
expr_stmt|;
block|}
else|else
block|{
name|lagValue
operator|=
name|lagWindow
operator|.
name|peek
argument_list|()
expr_stmt|;
comment|// Peek here, because we rely on add'ing to always move the window
block|}
comment|// Normalize null's to NaN
if|if
condition|(
name|thisBucketValue
operator|==
literal|null
condition|)
block|{
name|thisBucketValue
operator|=
name|Double
operator|.
name|NaN
expr_stmt|;
block|}
comment|// Both have values, calculate diff and replace the "empty" bucket
if|if
condition|(
operator|!
name|Double
operator|.
name|isNaN
argument_list|(
name|thisBucketValue
argument_list|)
operator|&&
operator|!
name|Double
operator|.
name|isNaN
argument_list|(
name|lagValue
argument_list|)
condition|)
block|{
name|double
name|diff
init|=
name|thisBucketValue
operator|-
name|lagValue
decl_stmt|;
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
name|diff
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
name|newBucket
operator|=
name|factory
operator|.
name|createBucket
argument_list|(
name|factory
operator|.
name|getKey
argument_list|(
name|bucket
argument_list|)
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
argument_list|)
expr_stmt|;
block|}
name|newBuckets
operator|.
name|add
argument_list|(
name|newBucket
argument_list|)
expr_stmt|;
name|lagWindow
operator|.
name|add
argument_list|(
name|thisBucketValue
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
operator|.
name|createAggregation
argument_list|(
name|newBuckets
argument_list|)
return|;
block|}
block|}
end_class

end_unit

