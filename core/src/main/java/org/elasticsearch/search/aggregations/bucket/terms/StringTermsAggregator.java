begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|util
operator|.
name|BytesRef
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
name|BytesRefBuilder
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
name|util
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
name|index
operator|.
name|fielddata
operator|.
name|SortedBinaryDocValues
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

begin_comment
comment|/**  * An aggregator of string values.  */
end_comment

begin_class
DECL|class|StringTermsAggregator
specifier|public
class|class
name|StringTermsAggregator
extends|extends
name|AbstractStringTermsAggregator
block|{
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
name|valuesSource
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
operator|.
name|StringFilter
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
name|Terms
operator|.
name|Order
name|order
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|BucketCountThresholds
name|bucketCountThresholds
parameter_list|,
name|IncludeExclude
operator|.
name|StringFilter
name|includeExclude
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|SubAggCollectionMode
name|collectionMode
parameter_list|,
name|boolean
name|showTermDocCountError
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
name|order
argument_list|,
name|format
argument_list|,
name|bucketCountThresholds
argument_list|,
name|collectionMode
argument_list|,
name|showTermDocCountError
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
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
name|includeExclude
operator|=
name|includeExclude
expr_stmt|;
name|bucketOrds
operator|=
operator|new
name|BytesRefHash
argument_list|(
literal|1
argument_list|,
name|context
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
operator|(
name|valuesSource
operator|!=
literal|null
operator|&&
name|valuesSource
operator|.
name|needsScores
argument_list|()
operator|)
operator|||
name|super
operator|.
name|needsScores
argument_list|()
return|;
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
specifier|final
name|SortedBinaryDocValues
name|values
init|=
name|valuesSource
operator|.
name|bytesValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
name|values
argument_list|)
block|{
specifier|final
name|BytesRefBuilder
name|previous
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
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
assert|assert
name|bucket
operator|==
literal|0
assert|;
if|if
condition|(
name|values
operator|.
name|advanceExact
argument_list|(
name|doc
argument_list|)
condition|)
block|{
specifier|final
name|int
name|valuesCount
init|=
name|values
operator|.
name|docValueCount
argument_list|()
decl_stmt|;
comment|// SortedBinaryDocValues don't guarantee uniqueness so we
comment|// need to take care of dups
name|previous
operator|.
name|clear
argument_list|()
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
if|if
condition|(
name|previous
operator|.
name|get
argument_list|()
operator|.
name|equals
argument_list|(
name|bytes
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|long
name|bucketOrdinal
init|=
name|bucketOrds
operator|.
name|add
argument_list|(
name|bytes
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
name|collectExistingBucket
argument_list|(
name|sub
argument_list|,
name|doc
argument_list|,
name|bucketOrdinal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|doc
argument_list|,
name|bucketOrdinal
argument_list|)
expr_stmt|;
block|}
name|previous
operator|.
name|copyBytes
argument_list|(
name|bytes
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
assert|assert
name|owningBucketOrdinal
operator|==
literal|0
assert|;
if|if
condition|(
name|bucketCountThresholds
operator|.
name|getMinDocCount
argument_list|()
operator|==
literal|0
operator|&&
operator|(
name|order
operator|!=
name|InternalOrder
operator|.
name|COUNT_DESC
operator|||
name|bucketOrds
operator|.
name|size
argument_list|()
operator|<
name|bucketCountThresholds
operator|.
name|getRequiredSize
argument_list|()
operator|)
condition|)
block|{
comment|// we need to fill-in the blanks
for|for
control|(
name|LeafReaderContext
name|ctx
range|:
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getTopReaderContext
argument_list|()
operator|.
name|leaves
argument_list|()
control|)
block|{
specifier|final
name|SortedBinaryDocValues
name|values
init|=
name|valuesSource
operator|.
name|bytesValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
comment|// brute force
for|for
control|(
name|int
name|docId
init|=
literal|0
init|;
name|docId
operator|<
name|ctx
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
condition|;
operator|++
name|docId
control|)
block|{
if|if
condition|(
name|values
operator|.
name|advanceExact
argument_list|(
name|docId
argument_list|)
condition|)
block|{
specifier|final
name|int
name|valueCount
init|=
name|values
operator|.
name|docValueCount
argument_list|()
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
name|valueCount
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|BytesRef
name|term
init|=
name|values
operator|.
name|nextValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|includeExclude
operator|==
literal|null
operator|||
name|includeExclude
operator|.
name|accept
argument_list|(
name|term
argument_list|)
condition|)
block|{
name|bucketOrds
operator|.
name|add
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
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
name|bucketCountThresholds
operator|.
name|getShardSize
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|otherDocCount
init|=
literal|0
decl_stmt|;
name|BucketPriorityQueue
argument_list|<
name|StringTerms
operator|.
name|Bucket
argument_list|>
name|ordered
init|=
operator|new
name|BucketPriorityQueue
argument_list|<>
argument_list|(
name|size
argument_list|,
name|order
operator|.
name|comparator
argument_list|(
name|this
argument_list|)
argument_list|)
decl_stmt|;
name|StringTerms
operator|.
name|Bucket
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
name|i
operator|++
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
name|StringTerms
operator|.
name|Bucket
argument_list|(
operator|new
name|BytesRef
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
name|showTermDocCountError
argument_list|,
literal|0
argument_list|,
name|format
argument_list|)
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
name|otherDocCount
operator|+=
name|spare
operator|.
name|docCount
expr_stmt|;
name|spare
operator|.
name|bucketOrd
operator|=
name|i
expr_stmt|;
if|if
condition|(
name|bucketCountThresholds
operator|.
name|getShardMinDocCount
argument_list|()
operator|<=
name|spare
operator|.
name|docCount
condition|)
block|{
name|spare
operator|=
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|spare
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Get the top buckets
specifier|final
name|StringTerms
operator|.
name|Bucket
index|[]
name|list
init|=
operator|new
name|StringTerms
operator|.
name|Bucket
index|[
name|ordered
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|long
name|survivingBucketOrds
index|[]
init|=
operator|new
name|long
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
name|StringTerms
operator|.
name|Bucket
name|bucket
init|=
operator|(
name|StringTerms
operator|.
name|Bucket
operator|)
name|ordered
operator|.
name|pop
argument_list|()
decl_stmt|;
name|survivingBucketOrds
index|[
name|i
index|]
operator|=
name|bucket
operator|.
name|bucketOrd
expr_stmt|;
name|list
index|[
name|i
index|]
operator|=
name|bucket
expr_stmt|;
name|otherDocCount
operator|-=
name|bucket
operator|.
name|docCount
expr_stmt|;
block|}
comment|// replay any deferred collections
name|runDeferredCollections
argument_list|(
name|survivingBucketOrds
argument_list|)
expr_stmt|;
comment|// Now build the aggs
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|list
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|StringTerms
operator|.
name|Bucket
name|bucket
init|=
operator|(
name|StringTerms
operator|.
name|Bucket
operator|)
name|list
index|[
name|i
index|]
decl_stmt|;
name|bucket
operator|.
name|termBytes
operator|=
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|bucket
operator|.
name|termBytes
argument_list|)
expr_stmt|;
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
name|bucket
operator|.
name|docCountError
operator|=
literal|0
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
name|bucketCountThresholds
operator|.
name|getRequiredSize
argument_list|()
argument_list|,
name|bucketCountThresholds
operator|.
name|getMinDocCount
argument_list|()
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|,
name|format
argument_list|,
name|bucketCountThresholds
operator|.
name|getShardSize
argument_list|()
argument_list|,
name|showTermDocCountError
argument_list|,
name|otherDocCount
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|list
argument_list|)
argument_list|,
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|public
name|void
name|doClose
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|bucketOrds
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

