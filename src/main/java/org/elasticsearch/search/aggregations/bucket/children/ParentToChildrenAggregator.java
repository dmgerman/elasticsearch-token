begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.children
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
name|children
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
name|index
operator|.
name|SortedDocValues
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
name|DocIdSet
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
name|DocIdSetIterator
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
name|ElasticsearchIllegalStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|common
operator|.
name|util
operator|.
name|LongObjectPagedHashMap
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
name|search
operator|.
name|child
operator|.
name|ConstantScorer
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
name|bucket
operator|.
name|SingleBucketAggregator
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
comment|// The RecordingPerReaderBucketCollector assumes per segment recording which isn't the case for this
end_comment

begin_comment
comment|// aggregation, for this reason that collector can't be used
end_comment

begin_class
DECL|class|ParentToChildrenAggregator
specifier|public
class|class
name|ParentToChildrenAggregator
extends|extends
name|SingleBucketAggregator
implements|implements
name|ReaderContextAware
block|{
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|childFilter
specifier|private
specifier|final
name|Filter
name|childFilter
decl_stmt|;
DECL|field|parentFilter
specifier|private
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|ParentChild
name|valuesSource
decl_stmt|;
comment|// Maybe use PagedGrowableWriter? This will be less wasteful than LongArray, but then we don't have the reuse feature of BigArrays.
comment|// Also if we know the highest possible value that a parent agg will create then we store multiple values into one slot
DECL|field|parentOrdToBuckets
specifier|private
specifier|final
name|LongArray
name|parentOrdToBuckets
decl_stmt|;
comment|// Only pay the extra storage price if the a parentOrd has multiple buckets
comment|// Most of the times a parent doesn't have multiple buckets, since there is only one document per parent ord,
comment|// only in the case of terms agg if a parent doc has multiple terms per field this is needed:
DECL|field|parentOrdToOtherBuckets
specifier|private
specifier|final
name|LongObjectPagedHashMap
argument_list|<
name|long
index|[]
argument_list|>
name|parentOrdToOtherBuckets
decl_stmt|;
DECL|field|multipleBucketsPerParentOrd
specifier|private
name|boolean
name|multipleBucketsPerParentOrd
init|=
literal|false
decl_stmt|;
DECL|field|replay
specifier|private
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|replay
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|globalOrdinals
specifier|private
name|SortedDocValues
name|globalOrdinals
decl_stmt|;
DECL|field|parentDocs
specifier|private
name|Bits
name|parentDocs
decl_stmt|;
DECL|method|ParentToChildrenAggregator
specifier|public
name|ParentToChildrenAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|String
name|parentType
parameter_list|,
name|Filter
name|childFilter
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|ParentChild
name|valuesSource
parameter_list|,
name|long
name|maxOrd
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
name|factories
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
comment|// The child filter doesn't rely on random access it just used to iterate over all docs with a specific type,
comment|// so use the filter cache instead. When the filter cache is smarter with what filter impl to pick we can benefit
comment|// from it here
name|this
operator|.
name|childFilter
operator|=
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|childFilter
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentFilter
operator|=
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|parentFilter
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentOrdToBuckets
operator|=
name|aggregationContext
operator|.
name|bigArrays
argument_list|()
operator|.
name|newLongArray
argument_list|(
name|maxOrd
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentOrdToBuckets
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|maxOrd
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentOrdToOtherBuckets
operator|=
operator|new
name|LongObjectPagedHashMap
argument_list|<>
argument_list|(
name|aggregationContext
operator|.
name|bigArrays
argument_list|()
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
DECL|method|buildAggregation
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
block|{
return|return
operator|new
name|InternalChildren
argument_list|(
name|name
argument_list|,
name|bucketDocCount
argument_list|(
name|owningBucketOrdinal
argument_list|)
argument_list|,
name|bucketAggregations
argument_list|(
name|owningBucketOrdinal
argument_list|)
argument_list|,
name|getMetaData
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
return|return
operator|new
name|InternalChildren
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
name|buildEmptySubAggregations
argument_list|()
argument_list|,
name|getMetaData
argument_list|()
argument_list|)
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
name|docId
parameter_list|,
name|long
name|bucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|parentDocs
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|)
block|{
name|long
name|globalOrdinal
init|=
name|globalOrdinals
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrdinal
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|parentOrdToBuckets
operator|.
name|get
argument_list|(
name|globalOrdinal
argument_list|)
operator|==
operator|-
literal|1
condition|)
block|{
name|parentOrdToBuckets
operator|.
name|set
argument_list|(
name|globalOrdinal
argument_list|,
name|bucketOrdinal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
index|[]
name|bucketOrds
init|=
name|parentOrdToOtherBuckets
operator|.
name|get
argument_list|(
name|globalOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketOrds
operator|!=
literal|null
condition|)
block|{
name|bucketOrds
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|bucketOrds
argument_list|,
name|bucketOrds
operator|.
name|length
operator|+
literal|1
argument_list|)
expr_stmt|;
name|bucketOrds
index|[
name|bucketOrds
operator|.
name|length
operator|-
literal|1
index|]
operator|=
name|bucketOrdinal
expr_stmt|;
name|parentOrdToOtherBuckets
operator|.
name|put
argument_list|(
name|globalOrdinal
argument_list|,
name|bucketOrds
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|parentOrdToOtherBuckets
operator|.
name|put
argument_list|(
name|globalOrdinal
argument_list|,
operator|new
name|long
index|[]
block|{
name|bucketOrdinal
block|}
argument_list|)
expr_stmt|;
block|}
name|multipleBucketsPerParentOrd
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|LeafReaderContext
name|reader
parameter_list|)
block|{
if|if
condition|(
name|replay
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|globalOrdinals
operator|=
name|valuesSource
operator|.
name|globalOrdinalsValues
argument_list|(
name|parentType
argument_list|)
expr_stmt|;
assert|assert
name|globalOrdinals
operator|!=
literal|null
assert|;
try|try
block|{
name|DocIdSet
name|parentDocIdSet
init|=
name|parentFilter
operator|.
name|getDocIdSet
argument_list|(
name|reader
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// The DocIdSets.toSafeBits(...) can convert to FixedBitSet, but this
comment|// will only happen if the none filter cache is used. (which only happens in tests)
comment|// Otherwise the filter cache will produce a bitset based filter.
name|parentDocs
operator|=
name|DocIdSets
operator|.
name|toSafeBits
argument_list|(
name|reader
operator|.
name|reader
argument_list|()
argument_list|,
name|parentDocIdSet
argument_list|)
expr_stmt|;
name|DocIdSet
name|childDocIdSet
init|=
name|childFilter
operator|.
name|getDocIdSet
argument_list|(
name|reader
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrdinals
operator|!=
literal|null
operator|&&
operator|!
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|childDocIdSet
argument_list|)
condition|)
block|{
name|replay
operator|.
name|add
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
name|ExceptionsHelper
operator|.
name|convertToElastic
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|doPostCollection
specifier|protected
name|void
name|doPostCollection
parameter_list|()
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|replay
init|=
name|this
operator|.
name|replay
decl_stmt|;
name|this
operator|.
name|replay
operator|=
literal|null
expr_stmt|;
for|for
control|(
name|LeafReaderContext
name|atomicReaderContext
range|:
name|replay
control|)
block|{
name|context
operator|.
name|setNextReader
argument_list|(
name|atomicReaderContext
argument_list|)
expr_stmt|;
name|SortedDocValues
name|globalOrdinals
init|=
name|valuesSource
operator|.
name|globalOrdinalsValues
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
name|DocIdSet
name|childDocIdSet
init|=
name|childFilter
operator|.
name|getDocIdSet
argument_list|(
name|atomicReaderContext
argument_list|,
name|atomicReaderContext
operator|.
name|reader
argument_list|()
operator|.
name|getLiveDocs
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|childDocIdSet
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|DocIdSetIterator
name|childDocsIter
init|=
name|childDocIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|childDocsIter
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
comment|// Set the scorer, since we now replay only the child docIds
name|context
operator|.
name|setScorer
argument_list|(
name|ConstantScorer
operator|.
name|create
argument_list|(
name|childDocsIter
argument_list|,
literal|null
argument_list|,
literal|1f
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|docId
init|=
name|childDocsIter
operator|.
name|nextDoc
argument_list|()
init|;
name|docId
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|;
name|docId
operator|=
name|childDocsIter
operator|.
name|nextDoc
argument_list|()
control|)
block|{
name|long
name|globalOrdinal
init|=
name|globalOrdinals
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
name|globalOrdinal
operator|!=
operator|-
literal|1
condition|)
block|{
name|long
name|bucketOrd
init|=
name|parentOrdToBuckets
operator|.
name|get
argument_list|(
name|globalOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketOrd
operator|!=
operator|-
literal|1
condition|)
block|{
name|collectBucket
argument_list|(
name|docId
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
if|if
condition|(
name|multipleBucketsPerParentOrd
condition|)
block|{
name|long
index|[]
name|otherBucketOrds
init|=
name|parentOrdToOtherBuckets
operator|.
name|get
argument_list|(
name|globalOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|otherBucketOrds
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|long
name|otherBucketOrd
range|:
name|otherBucketOrds
control|)
block|{
name|collectBucket
argument_list|(
name|docId
argument_list|,
name|otherBucketOrd
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|parentOrdToBuckets
argument_list|,
name|parentOrdToOtherBuckets
argument_list|)
expr_stmt|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|ValuesSourceAggregatorFactory
argument_list|<
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|ParentChild
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
block|{
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
decl_stmt|;
DECL|field|parentFilter
specifier|private
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|childFilter
specifier|private
specifier|final
name|Filter
name|childFilter
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|ParentChild
argument_list|>
name|config
parameter_list|,
name|String
name|parentType
parameter_list|,
name|Filter
name|parentFilter
parameter_list|,
name|Filter
name|childFilter
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalChildren
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|this
operator|.
name|parentType
operator|=
name|parentType
expr_stmt|;
name|this
operator|.
name|parentFilter
operator|=
name|parentFilter
expr_stmt|;
name|this
operator|.
name|childFilter
operator|=
name|childFilter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
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
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"[children] aggregation doesn't support unmapped"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|protected
name|Aggregator
name|create
parameter_list|(
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|ParentChild
name|valuesSource
parameter_list|,
name|long
name|expectedBucketsCount
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
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
name|long
name|maxOrd
init|=
name|valuesSource
operator|.
name|globalMaxOrd
argument_list|(
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|searcher
argument_list|()
argument_list|,
name|parentType
argument_list|)
decl_stmt|;
return|return
operator|new
name|ParentToChildrenAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|parentType
argument_list|,
name|childFilter
argument_list|,
name|parentFilter
argument_list|,
name|valuesSource
argument_list|,
name|maxOrd
argument_list|,
name|metaData
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

