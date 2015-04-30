begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.filters
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
name|filters
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
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Bits
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
name|AggregationExecutionException
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
name|LeafBucketCollectorBase
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
name|BucketsAggregator
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
name|reducers
operator|.
name|Reducer
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
comment|/**  *  */
end_comment

begin_class
DECL|class|FiltersAggregator
specifier|public
class|class
name|FiltersAggregator
extends|extends
name|BucketsAggregator
block|{
DECL|class|KeyedFilter
specifier|static
class|class
name|KeyedFilter
block|{
DECL|field|key
specifier|final
name|String
name|key
decl_stmt|;
DECL|field|filter
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|method|KeyedFilter
name|KeyedFilter
parameter_list|(
name|String
name|key
parameter_list|,
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
block|}
DECL|field|filters
specifier|private
specifier|final
name|KeyedFilter
index|[]
name|filters
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|method|FiltersAggregator
specifier|public
name|FiltersAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|List
argument_list|<
name|KeyedFilter
argument_list|>
name|filters
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|List
argument_list|<
name|Reducer
argument_list|>
name|reducers
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
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|reducers
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|filters
operator|.
name|toArray
argument_list|(
operator|new
name|KeyedFilter
index|[
name|filters
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
name|LeafBucketCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|,
specifier|final
name|LeafBucketCollector
name|sub
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO: use the iterator if the filter does not support random access
comment|// no need to provide deleted docs to the filter
specifier|final
name|Bits
index|[]
name|bits
init|=
operator|new
name|Bits
index|[
name|filters
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
name|filters
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|bits
index|[
name|i
index|]
operator|=
name|DocIdSets
operator|.
name|asSequentialAccessBits
argument_list|(
name|ctx
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|filters
index|[
name|i
index|]
operator|.
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|ctx
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|bucket
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
name|bits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|bits
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|doc
argument_list|,
name|bucketOrd
argument_list|(
name|bucket
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|InternalFilters
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|filters
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
name|filters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|KeyedFilter
name|filter
init|=
name|filters
index|[
name|i
index|]
decl_stmt|;
name|long
name|bucketOrd
init|=
name|bucketOrd
argument_list|(
name|owningBucketOrdinal
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|InternalFilters
operator|.
name|Bucket
name|bucket
init|=
operator|new
name|InternalFilters
operator|.
name|Bucket
argument_list|(
name|filter
operator|.
name|key
argument_list|,
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|bucketAggregations
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|keyed
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalFilters
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|keyed
argument_list|,
name|reducers
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
name|InternalAggregations
name|subAggs
init|=
name|buildEmptySubAggregations
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|InternalFilters
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|filters
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
name|filters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|InternalFilters
operator|.
name|Bucket
name|bucket
init|=
operator|new
name|InternalFilters
operator|.
name|Bucket
argument_list|(
name|filters
index|[
name|i
index|]
operator|.
name|key
argument_list|,
literal|0
argument_list|,
name|subAggs
argument_list|,
name|keyed
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalFilters
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|keyed
argument_list|,
name|reducers
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|bucketOrd
specifier|final
name|long
name|bucketOrd
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|,
name|int
name|filterOrd
parameter_list|)
block|{
return|return
name|owningBucketOrdinal
operator|*
name|filters
operator|.
name|length
operator|+
name|filterOrd
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|AggregatorFactory
block|{
DECL|field|filters
specifier|private
specifier|final
name|List
argument_list|<
name|KeyedFilter
argument_list|>
name|filters
decl_stmt|;
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|KeyedFilter
argument_list|>
name|filters
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalFilters
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createInternal
specifier|public
name|Aggregator
name|createInternal
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
parameter_list|,
name|List
argument_list|<
name|Reducer
argument_list|>
name|reducers
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
name|FiltersAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|filters
argument_list|,
name|keyed
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|reducers
argument_list|,
name|metaData
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

