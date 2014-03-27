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
name|Releasables
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
name|LongArray
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
name|support
operator|.
name|AggregationContext
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|BucketsAggregator
specifier|public
specifier|abstract
class|class
name|BucketsAggregator
extends|extends
name|Aggregator
block|{
DECL|field|docCounts
specifier|private
name|LongArray
name|docCounts
decl_stmt|;
DECL|field|collectableSugAggregators
specifier|private
specifier|final
name|Aggregator
index|[]
name|collectableSugAggregators
decl_stmt|;
DECL|method|BucketsAggregator
specifier|public
name|BucketsAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|BucketAggregationMode
name|bucketAggregationMode
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|long
name|estimatedBucketsCount
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|bucketAggregationMode
argument_list|,
name|factories
argument_list|,
name|estimatedBucketsCount
argument_list|,
name|context
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|docCounts
operator|=
name|bigArrays
operator|.
name|newLongArray
argument_list|(
name|estimatedBucketsCount
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Aggregator
argument_list|>
name|collectables
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|subAggregators
operator|.
name|length
argument_list|)
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
if|if
condition|(
name|subAggregators
index|[
name|i
index|]
operator|.
name|shouldCollect
argument_list|()
condition|)
block|{
name|collectables
operator|.
name|add
argument_list|(
operator|(
name|subAggregators
index|[
name|i
index|]
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
name|collectableSugAggregators
operator|=
name|collectables
operator|.
name|toArray
argument_list|(
operator|new
name|Aggregator
index|[
name|collectables
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**      * Utility method to collect the given doc in the given bucket (identified by the bucket ordinal)      */
DECL|method|collectBucket
specifier|protected
specifier|final
name|void
name|collectBucket
parameter_list|(
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
literal|1
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|collectableSugAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|collectableSugAggregators
index|[
name|i
index|]
operator|.
name|collect
argument_list|(
name|doc
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Utility method to collect the given doc in the given bucket but not to update the doc counts of the bucket      */
DECL|method|collectBucketNoCounts
specifier|protected
specifier|final
name|void
name|collectBucketNoCounts
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
throws|throws
name|IOException
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|collectableSugAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|collectableSugAggregators
index|[
name|i
index|]
operator|.
name|collect
argument_list|(
name|doc
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Utility method to increment the doc counts of the given bucket (identified by the bucket ordinal)      */
DECL|method|incrementBucketDocCount
specifier|protected
specifier|final
name|void
name|incrementBucketDocCount
parameter_list|(
name|int
name|inc
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
throws|throws
name|IOException
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
name|long
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
literal|0L
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
comment|/**      * Utility method to build the aggregations of the given bucket (identified by the bucket ordinal)      */
DECL|method|bucketAggregations
specifier|protected
specifier|final
name|InternalAggregations
name|bucketAggregations
parameter_list|(
name|long
name|bucketOrd
parameter_list|)
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
specifier|final
name|long
name|bucketDocCount
init|=
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
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
name|bucketDocCount
operator|==
literal|0L
condition|?
name|subAggregators
index|[
name|i
index|]
operator|.
name|buildEmptyAggregation
argument_list|()
else|:
name|subAggregators
index|[
name|i
index|]
operator|.
name|buildAggregation
argument_list|(
name|bucketOrd
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
annotation|@
name|Override
DECL|method|release
specifier|public
specifier|final
name|boolean
name|release
parameter_list|()
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|super
operator|.
name|release
argument_list|()
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
name|Releasables
operator|.
name|release
argument_list|(
name|success
argument_list|,
name|docCounts
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

