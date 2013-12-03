begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.terms
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
name|terms
package|;
end_package

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
name|AtomicReaderContext
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
name|BytesRef
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
name|lucene
operator|.
name|ReaderContextAware
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
name|LongArray
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
name|fielddata
operator|.
name|BytesValues
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
name|fielddata
operator|.
name|ordinals
operator|.
name|Ordinals
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
name|bucket
operator|.
name|BytesRefHash
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
name|terms
operator|.
name|support
operator|.
name|BucketPriorityQueue
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
name|terms
operator|.
name|support
operator|.
name|IncludeExclude
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
name|bytes
operator|.
name|BytesValuesSource
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
name|Collections
import|;
end_import

begin_comment
comment|/**  * An aggregator of string values.  */
end_comment

begin_class
DECL|class|StringTermsAggregator
specifier|public
class|class
name|StringTermsAggregator
extends|extends
name|BucketsAggregator
block|{
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
name|valuesSource
decl_stmt|;
DECL|field|order
specifier|private
specifier|final
name|InternalOrder
name|order
decl_stmt|;
DECL|field|requiredSize
specifier|private
specifier|final
name|int
name|requiredSize
decl_stmt|;
DECL|field|shardSize
specifier|private
specifier|final
name|int
name|shardSize
decl_stmt|;
DECL|field|bucketOrds
specifier|protected
specifier|final
name|BytesRefHash
name|bucketOrds
decl_stmt|;
DECL|field|includeExclude
specifier|private
specifier|final
name|IncludeExclude
name|includeExclude
decl_stmt|;
DECL|method|StringTermsAggregator
specifier|public
name|StringTermsAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|estimatedBucketCount
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|BucketAggregationMode
operator|.
name|PER_BUCKET
argument_list|,
name|factories
argument_list|,
name|estimatedBucketCount
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
name|this
operator|.
name|requiredSize
operator|=
name|requiredSize
expr_stmt|;
name|this
operator|.
name|shardSize
operator|=
name|shardSize
expr_stmt|;
name|this
operator|.
name|includeExclude
operator|=
name|includeExclude
expr_stmt|;
name|bucketOrds
operator|=
operator|new
name|BytesRefHash
argument_list|(
name|estimatedBucketCount
argument_list|,
name|aggregationContext
operator|.
name|pageCacheRecycler
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|shouldCollect
specifier|public
name|boolean
name|shouldCollect
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|owningBucketOrdinal
operator|==
literal|0
assert|;
specifier|final
name|BytesValues
name|values
init|=
name|valuesSource
operator|.
name|bytesValues
argument_list|()
decl_stmt|;
specifier|final
name|int
name|valuesCount
init|=
name|values
operator|.
name|setDocument
argument_list|(
name|doc
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
name|valuesCount
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|BytesRef
name|bytes
init|=
name|values
operator|.
name|nextValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|includeExclude
operator|!=
literal|null
operator|&&
operator|!
name|includeExclude
operator|.
name|accept
argument_list|(
name|bytes
argument_list|)
condition|)
block|{
continue|continue;
block|}
specifier|final
name|int
name|hash
init|=
name|values
operator|.
name|currentValueHash
argument_list|()
decl_stmt|;
assert|assert
name|hash
operator|==
name|bytes
operator|.
name|hashCode
argument_list|()
assert|;
name|long
name|bucketOrdinal
init|=
name|bucketOrds
operator|.
name|add
argument_list|(
name|bytes
argument_list|,
name|hash
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketOrdinal
operator|<
literal|0
condition|)
block|{
comment|// already seen
name|bucketOrdinal
operator|=
operator|-
literal|1
operator|-
name|bucketOrdinal
expr_stmt|;
block|}
name|collectBucket
argument_list|(
name|doc
argument_list|,
name|bucketOrdinal
argument_list|)
expr_stmt|;
block|}
block|}
comment|// private impl that stores a bucket ord. This allows for computing the aggregations lazily.
DECL|class|OrdinalBucket
specifier|static
class|class
name|OrdinalBucket
extends|extends
name|StringTerms
operator|.
name|Bucket
block|{
DECL|field|bucketOrd
name|int
name|bucketOrd
decl_stmt|;
DECL|method|OrdinalBucket
specifier|public
name|OrdinalBucket
parameter_list|()
block|{
name|super
argument_list|(
operator|new
name|BytesRef
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
name|StringTerms
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
block|{
assert|assert
name|owningBucketOrdinal
operator|==
literal|0
assert|;
specifier|final
name|int
name|size
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|bucketOrds
operator|.
name|size
argument_list|()
argument_list|,
name|shardSize
argument_list|)
decl_stmt|;
name|BucketPriorityQueue
name|ordered
init|=
operator|new
name|BucketPriorityQueue
argument_list|(
name|size
argument_list|,
name|order
operator|.
name|comparator
argument_list|()
argument_list|)
decl_stmt|;
name|OrdinalBucket
name|spare
init|=
literal|null
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
name|bucketOrds
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|spare
operator|==
literal|null
condition|)
block|{
name|spare
operator|=
operator|new
name|OrdinalBucket
argument_list|()
expr_stmt|;
block|}
name|bucketOrds
operator|.
name|get
argument_list|(
name|i
argument_list|,
name|spare
operator|.
name|termBytes
argument_list|)
expr_stmt|;
name|spare
operator|.
name|docCount
operator|=
name|bucketDocCount
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|spare
operator|.
name|bucketOrd
operator|=
name|i
expr_stmt|;
name|spare
operator|=
operator|(
name|OrdinalBucket
operator|)
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|spare
argument_list|)
expr_stmt|;
block|}
specifier|final
name|InternalTerms
operator|.
name|Bucket
index|[]
name|list
init|=
operator|new
name|InternalTerms
operator|.
name|Bucket
index|[
name|ordered
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|ordered
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
operator|--
name|i
control|)
block|{
specifier|final
name|OrdinalBucket
name|bucket
init|=
operator|(
name|OrdinalBucket
operator|)
name|ordered
operator|.
name|pop
argument_list|()
decl_stmt|;
name|bucket
operator|.
name|aggregations
operator|=
name|bucketAggregations
argument_list|(
name|bucket
operator|.
name|bucketOrd
argument_list|)
expr_stmt|;
name|list
index|[
name|i
index|]
operator|=
name|bucket
expr_stmt|;
block|}
return|return
operator|new
name|StringTerms
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|list
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|StringTerms
name|buildEmptyAggregation
parameter_list|()
block|{
return|return
operator|new
name|StringTerms
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|Collections
operator|.
expr|<
name|InternalTerms
operator|.
name|Bucket
operator|>
name|emptyList
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doRelease
specifier|public
name|void
name|doRelease
parameter_list|()
block|{
name|Releasables
operator|.
name|release
argument_list|(
name|bucketOrds
argument_list|)
expr_stmt|;
block|}
comment|/**      * Extension of StringTermsAggregator that caches bucket ords using terms ordinals.      */
DECL|class|WithOrdinals
specifier|public
specifier|static
class|class
name|WithOrdinals
extends|extends
name|StringTermsAggregator
implements|implements
name|ReaderContextAware
block|{
DECL|field|valuesSource
specifier|private
specifier|final
name|BytesValuesSource
operator|.
name|WithOrdinals
name|valuesSource
decl_stmt|;
DECL|field|bytesValues
specifier|private
name|BytesValues
operator|.
name|WithOrdinals
name|bytesValues
decl_stmt|;
DECL|field|ordinals
specifier|private
name|Ordinals
operator|.
name|Docs
name|ordinals
decl_stmt|;
DECL|field|ordinalToBucket
specifier|private
name|LongArray
name|ordinalToBucket
decl_stmt|;
DECL|method|WithOrdinals
specifier|public
name|WithOrdinals
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|BytesValuesSource
operator|.
name|WithOrdinals
name|valuesSource
parameter_list|,
name|long
name|esitmatedBucketCount
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|valuesSource
argument_list|,
name|esitmatedBucketCount
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
literal|null
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|reader
parameter_list|)
block|{
name|bytesValues
operator|=
name|valuesSource
operator|.
name|bytesValues
argument_list|()
expr_stmt|;
name|ordinals
operator|=
name|bytesValues
operator|.
name|ordinals
argument_list|()
expr_stmt|;
specifier|final
name|long
name|maxOrd
init|=
name|ordinals
operator|.
name|getMaxOrd
argument_list|()
decl_stmt|;
if|if
condition|(
name|ordinalToBucket
operator|==
literal|null
operator|||
name|ordinalToBucket
operator|.
name|size
argument_list|()
operator|<
name|maxOrd
condition|)
block|{
if|if
condition|(
name|ordinalToBucket
operator|!=
literal|null
condition|)
block|{
name|ordinalToBucket
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
name|ordinalToBucket
operator|=
name|BigArrays
operator|.
name|newLongArray
argument_list|(
name|BigArrays
operator|.
name|overSize
argument_list|(
name|maxOrd
argument_list|)
argument_list|,
name|context
argument_list|()
operator|.
name|pageCacheRecycler
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|ordinalToBucket
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|maxOrd
argument_list|,
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|owningBucketOrdinal
operator|==
literal|0
operator|:
literal|"this is a per_bucket aggregator"
assert|;
specifier|final
name|int
name|valuesCount
init|=
name|ordinals
operator|.
name|setDocument
argument_list|(
name|doc
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
name|valuesCount
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|long
name|ord
init|=
name|ordinals
operator|.
name|nextOrd
argument_list|()
decl_stmt|;
name|long
name|bucketOrd
init|=
name|ordinalToBucket
operator|.
name|get
argument_list|(
name|ord
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketOrd
operator|<
literal|0
condition|)
block|{
comment|// unlikely condition on a low-cardinality field
specifier|final
name|BytesRef
name|bytes
init|=
name|bytesValues
operator|.
name|getValueByOrd
argument_list|(
name|ord
argument_list|)
decl_stmt|;
specifier|final
name|int
name|hash
init|=
name|bytesValues
operator|.
name|currentValueHash
argument_list|()
decl_stmt|;
assert|assert
name|hash
operator|==
name|bytes
operator|.
name|hashCode
argument_list|()
assert|;
name|bucketOrd
operator|=
name|bucketOrds
operator|.
name|add
argument_list|(
name|bytes
argument_list|,
name|hash
argument_list|)
expr_stmt|;
if|if
condition|(
name|bucketOrd
operator|<
literal|0
condition|)
block|{
comment|// already seen in another segment
name|bucketOrd
operator|=
operator|-
literal|1
operator|-
name|bucketOrd
expr_stmt|;
block|}
name|ordinalToBucket
operator|.
name|set
argument_list|(
name|ord
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
name|collectBucket
argument_list|(
name|doc
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doRelease
specifier|public
name|void
name|doRelease
parameter_list|()
block|{
name|Releasables
operator|.
name|release
argument_list|(
name|bucketOrds
argument_list|,
name|ordinalToBucket
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

