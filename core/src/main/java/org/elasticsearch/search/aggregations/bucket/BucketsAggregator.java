begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
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
name|lease
operator|.
name|Releasable
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
name|util
operator|.
name|BigArrays
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
name|util
operator|.
name|IntArray
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
name|AggregatorBase
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
name|LeafBucketCollector
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
name|Arrays
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
DECL|class|BucketsAggregator
specifier|public
specifier|abstract
class|class
name|BucketsAggregator
extends|extends
name|AggregatorBase
block|{
DECL|field|bigArrays
specifier|private
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
DECL|field|docCounts
specifier|private
name|IntArray
name|docCounts
decl_stmt|;
DECL|method|BucketsAggregator
specifier|public
name|BucketsAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|SearchContext
name|context
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
name|super
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|bigArrays
operator|=
name|context
operator|.
name|bigArrays
argument_list|()
expr_stmt|;
name|docCounts
operator|=
name|bigArrays
operator|.
name|newIntArray
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**      * Return an upper bound of the maximum bucket ordinal seen so far.      */
DECL|method|maxBucketOrd
specifier|public
specifier|final
name|long
name|maxBucketOrd
parameter_list|()
block|{
return|return
name|docCounts
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * Ensure there are at least<code>maxBucketOrd</code> buckets available.      */
DECL|method|grow
specifier|public
specifier|final
name|void
name|grow
parameter_list|(
name|long
name|maxBucketOrd
parameter_list|)
block|{
name|docCounts
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|docCounts
argument_list|,
name|maxBucketOrd
argument_list|)
expr_stmt|;
block|}
comment|/**      * Utility method to collect the given doc in the given bucket (identified by the bucket ordinal)      */
DECL|method|collectBucket
specifier|public
specifier|final
name|void
name|collectBucket
parameter_list|(
name|LeafBucketCollector
name|subCollector
parameter_list|,
name|int
name|doc
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
throws|throws
name|IOException
block|{
name|grow
argument_list|(
name|bucketOrd
operator|+
literal|1
argument_list|)
expr_stmt|;
name|collectExistingBucket
argument_list|(
name|subCollector
argument_list|,
name|doc
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
comment|/**      * Same as {@link #collectBucket(LeafBucketCollector, int, long)}, but doesn't check if the docCounts needs to be re-sized.      */
DECL|method|collectExistingBucket
specifier|public
specifier|final
name|void
name|collectExistingBucket
parameter_list|(
name|LeafBucketCollector
name|subCollector
parameter_list|,
name|int
name|doc
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
throws|throws
name|IOException
block|{
name|docCounts
operator|.
name|increment
argument_list|(
name|bucketOrd
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|subCollector
operator|.
name|collect
argument_list|(
name|doc
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
DECL|method|getDocCounts
specifier|public
name|IntArray
name|getDocCounts
parameter_list|()
block|{
return|return
name|docCounts
return|;
block|}
comment|/**      * Utility method to increment the doc counts of the given bucket (identified by the bucket ordinal)      */
DECL|method|incrementBucketDocCount
specifier|public
specifier|final
name|void
name|incrementBucketDocCount
parameter_list|(
name|long
name|bucketOrd
parameter_list|,
name|int
name|inc
parameter_list|)
block|{
name|docCounts
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|docCounts
argument_list|,
name|bucketOrd
operator|+
literal|1
argument_list|)
expr_stmt|;
name|docCounts
operator|.
name|increment
argument_list|(
name|bucketOrd
argument_list|,
name|inc
argument_list|)
expr_stmt|;
block|}
comment|/**      * Utility method to return the number of documents that fell in the given bucket (identified by the bucket ordinal)      */
DECL|method|bucketDocCount
specifier|public
specifier|final
name|int
name|bucketDocCount
parameter_list|(
name|long
name|bucketOrd
parameter_list|)
block|{
if|if
condition|(
name|bucketOrd
operator|>=
name|docCounts
operator|.
name|size
argument_list|()
condition|)
block|{
comment|// This may happen eg. if no document in the highest buckets is accepted by a sub aggregator.
comment|// For example, if there is a long terms agg on 3 terms 1,2,3 with a sub filter aggregator and if no document with 3 as a value
comment|// matches the filter, then the filter will never collect bucket ord 3. However, the long terms agg will call bucketAggregations(3)
comment|// on the filter aggregator anyway to build sub-aggregations.
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|docCounts
operator|.
name|get
argument_list|(
name|bucketOrd
argument_list|)
return|;
block|}
block|}
comment|/**      * Required method to build the child aggregations of the given bucket (identified by the bucket ordinal).      */
DECL|method|bucketAggregations
specifier|protected
specifier|final
name|InternalAggregations
name|bucketAggregations
parameter_list|(
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|InternalAggregation
index|[]
name|aggregations
init|=
operator|new
name|InternalAggregation
index|[
name|subAggregators
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|subAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|aggregations
index|[
name|i
index|]
operator|=
name|subAggregators
index|[
name|i
index|]
operator|.
name|buildAggregation
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalAggregations
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|aggregations
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Utility method to build empty aggregations of the sub aggregators.      */
DECL|method|bucketEmptyAggregations
specifier|protected
specifier|final
name|InternalAggregations
name|bucketEmptyAggregations
parameter_list|()
block|{
specifier|final
name|InternalAggregation
index|[]
name|aggregations
init|=
operator|new
name|InternalAggregation
index|[
name|subAggregators
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|subAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|aggregations
index|[
name|i
index|]
operator|=
name|subAggregators
index|[
name|i
index|]
operator|.
name|buildEmptyAggregation
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|InternalAggregations
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|aggregations
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|final
name|void
name|close
parameter_list|()
block|{
try|try
init|(
name|Releasable
name|releasable
init|=
name|docCounts
init|)
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

