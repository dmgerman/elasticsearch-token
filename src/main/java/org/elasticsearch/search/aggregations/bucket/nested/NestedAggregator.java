begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.nested
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
name|nested
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
name|search
operator|.
name|QueryCachingPolicy
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
name|join
operator|.
name|BitDocIdSetFilter
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
name|BitDocIdSet
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
name|BitSet
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
name|lucene
operator|.
name|search
operator|.
name|Queries
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
name|mapper
operator|.
name|MapperService
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
name|mapper
operator|.
name|object
operator|.
name|ObjectMapper
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
name|NonCollectingAggregator
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
name|Map
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NestedAggregator
specifier|public
class|class
name|NestedAggregator
extends|extends
name|SingleBucketAggregator
block|{
DECL|field|parentFilter
specifier|private
name|BitDocIdSetFilter
name|parentFilter
decl_stmt|;
DECL|field|childFilter
specifier|private
specifier|final
name|Filter
name|childFilter
decl_stmt|;
DECL|field|childDocs
specifier|private
name|DocIdSetIterator
name|childDocs
decl_stmt|;
DECL|field|parentDocs
specifier|private
name|BitSet
name|parentDocs
decl_stmt|;
DECL|method|NestedAggregator
specifier|public
name|NestedAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ObjectMapper
name|objectMapper
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parentAggregator
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|,
name|QueryCachingPolicy
name|filterCachingPolicy
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
name|parentAggregator
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
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
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
argument_list|,
literal|null
argument_list|,
name|filterCachingPolicy
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
specifier|final
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
comment|// Reset parentFilter, so we resolve the parentDocs for each new segment being searched
name|this
operator|.
name|parentFilter
operator|=
literal|null
expr_stmt|;
comment|// In ES if parent is deleted, then also the children are deleted. Therefore acceptedDocs can also null here.
name|DocIdSet
name|childDocIdSet
init|=
name|childFilter
operator|.
name|getDocIdSet
argument_list|(
name|ctx
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|childDocIdSet
argument_list|)
condition|)
block|{
name|childDocs
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|childDocs
operator|=
name|childDocIdSet
operator|.
name|iterator
argument_list|()
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
name|parentDoc
parameter_list|,
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
comment|// here we translate the parent doc to a list of its nested docs, and then call super.collect for evey one of them so they'll be collected
comment|// if parentDoc is 0 then this means that this parent doesn't have child docs (b/c these appear always before the parent doc), so we can skip:
if|if
condition|(
name|parentDoc
operator|==
literal|0
operator|||
name|childDocs
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|parentFilter
operator|==
literal|null
condition|)
block|{
comment|// The aggs are instantiated in reverse, first the most inner nested aggs and lastly the top level aggs
comment|// So at the time a nested 'nested' aggs is parsed its closest parent nested aggs hasn't been constructed.
comment|// So the trick is to set at the last moment just before needed and we can use its child filter as the
comment|// parent filter.
comment|// Additional NOTE: Before this logic was performed in the setNextReader(...) method, but the the assumption
comment|// that aggs instances are constructed in reverse doesn't hold when buckets are constructed lazily during
comment|// aggs execution
name|Filter
name|parentFilterNotCached
init|=
name|findClosestNestedPath
argument_list|(
name|parent
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentFilterNotCached
operator|==
literal|null
condition|)
block|{
name|parentFilterNotCached
operator|=
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
expr_stmt|;
block|}
name|parentFilter
operator|=
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitDocIdSetFilter
argument_list|(
name|parentFilterNotCached
argument_list|)
expr_stmt|;
name|BitDocIdSet
name|parentSet
init|=
name|parentFilter
operator|.
name|getDocIdSet
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|parentSet
argument_list|)
condition|)
block|{
comment|// There are no parentDocs in the segment, so return and set childDocs to null, so we exit early for future invocations.
name|childDocs
operator|=
literal|null
expr_stmt|;
return|return;
block|}
else|else
block|{
name|parentDocs
operator|=
name|parentSet
operator|.
name|bits
argument_list|()
expr_stmt|;
block|}
block|}
specifier|final
name|int
name|prevParentDoc
init|=
name|parentDocs
operator|.
name|prevSetBit
argument_list|(
name|parentDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|childDocId
init|=
name|childDocs
operator|.
name|docID
argument_list|()
decl_stmt|;
if|if
condition|(
name|childDocId
operator|<=
name|prevParentDoc
condition|)
block|{
name|childDocId
operator|=
name|childDocs
operator|.
name|advance
argument_list|(
name|prevParentDoc
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
for|for
control|(
init|;
name|childDocId
operator|<
name|parentDoc
condition|;
name|childDocId
operator|=
name|childDocs
operator|.
name|nextDoc
argument_list|()
control|)
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|childDocId
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
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
return|return
operator|new
name|InternalNested
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
return|return
operator|new
name|InternalNested
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
name|buildEmptySubAggregations
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|findClosestNestedPath
specifier|private
specifier|static
name|Filter
name|findClosestNestedPath
parameter_list|(
name|Aggregator
name|parent
parameter_list|)
block|{
for|for
control|(
init|;
name|parent
operator|!=
literal|null
condition|;
name|parent
operator|=
name|parent
operator|.
name|parent
argument_list|()
control|)
block|{
if|if
condition|(
name|parent
operator|instanceof
name|NestedAggregator
condition|)
block|{
return|return
operator|(
operator|(
name|NestedAggregator
operator|)
name|parent
operator|)
operator|.
name|childFilter
return|;
block|}
elseif|else
if|if
condition|(
name|parent
operator|instanceof
name|ReverseNestedAggregator
condition|)
block|{
return|return
operator|(
operator|(
name|ReverseNestedAggregator
operator|)
name|parent
operator|)
operator|.
name|getParentFilter
argument_list|()
return|;
block|}
block|}
return|return
literal|null
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
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|field|queryCachingPolicy
specifier|private
specifier|final
name|QueryCachingPolicy
name|queryCachingPolicy
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|path
parameter_list|,
name|QueryCachingPolicy
name|queryCachingPolicy
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalNested
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
name|this
operator|.
name|queryCachingPolicy
operator|=
name|queryCachingPolicy
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
if|if
condition|(
name|collectsFromSingleBucket
operator|==
literal|false
condition|)
block|{
return|return
name|asMultiBucketAggregator
argument_list|(
name|this
argument_list|,
name|context
argument_list|,
name|parent
argument_list|)
return|;
block|}
name|MapperService
operator|.
name|SmartNameObjectMapper
name|mapper
init|=
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|smartNameObjectMapper
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Unmapped
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
return|;
block|}
name|ObjectMapper
name|objectMapper
init|=
name|mapper
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|objectMapper
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|Unmapped
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
return|;
block|}
if|if
condition|(
operator|!
name|objectMapper
operator|.
name|nested
argument_list|()
operator|.
name|isNested
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"[nested] nested path ["
operator|+
name|path
operator|+
literal|"] is not nested"
argument_list|)
throw|;
block|}
return|return
operator|new
name|NestedAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|objectMapper
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|,
name|queryCachingPolicy
argument_list|)
return|;
block|}
DECL|class|Unmapped
specifier|private
specifier|final
specifier|static
class|class
name|Unmapped
extends|extends
name|NonCollectingAggregator
block|{
DECL|method|Unmapped
specifier|public
name|Unmapped
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregationContext
name|context
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
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
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
name|InternalNested
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
name|buildEmptySubAggregations
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

