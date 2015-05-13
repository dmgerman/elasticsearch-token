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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|LongIntHashMap
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
name|SearchParseException
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
DECL|class|ReverseNestedAggregator
specifier|public
class|class
name|ReverseNestedAggregator
extends|extends
name|SingleBucketAggregator
block|{
DECL|field|parentFilter
specifier|private
specifier|final
name|BitDocIdSetFilter
name|parentFilter
decl_stmt|;
DECL|method|ReverseNestedAggregator
specifier|public
name|ReverseNestedAggregator
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
if|if
condition|(
name|objectMapper
operator|==
literal|null
condition|)
block|{
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
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|protected
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
comment|// In ES if parent is deleted, then also the children are deleted, so the child docs this agg receives
comment|// must belong to parent docs that is alive. For this reason acceptedDocs can be null here.
name|BitDocIdSet
name|docIdSet
init|=
name|parentFilter
operator|.
name|getDocIdSet
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
specifier|final
name|BitSet
name|parentDocs
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
return|return
name|LeafBucketCollector
operator|.
name|NO_OP_COLLECTOR
return|;
block|}
else|else
block|{
name|parentDocs
operator|=
name|docIdSet
operator|.
name|bits
argument_list|()
expr_stmt|;
block|}
specifier|final
name|LongIntHashMap
name|bucketOrdToLastCollectedParentDoc
init|=
operator|new
name|LongIntHashMap
argument_list|(
literal|32
argument_list|)
decl_stmt|;
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
name|childDoc
parameter_list|,
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
comment|// fast forward to retrieve the parentDoc this childDoc belongs to
specifier|final
name|int
name|parentDoc
init|=
name|parentDocs
operator|.
name|nextSetBit
argument_list|(
name|childDoc
argument_list|)
decl_stmt|;
assert|assert
name|childDoc
operator|<=
name|parentDoc
operator|&&
name|parentDoc
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
assert|;
name|int
name|keySlot
init|=
name|bucketOrdToLastCollectedParentDoc
operator|.
name|indexOf
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketOrdToLastCollectedParentDoc
operator|.
name|indexExists
argument_list|(
name|keySlot
argument_list|)
condition|)
block|{
name|int
name|lastCollectedParentDoc
init|=
name|bucketOrdToLastCollectedParentDoc
operator|.
name|indexGet
argument_list|(
name|keySlot
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDoc
operator|>
name|lastCollectedParentDoc
condition|)
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|parentDoc
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
name|bucketOrdToLastCollectedParentDoc
operator|.
name|indexReplace
argument_list|(
name|keySlot
argument_list|,
name|parentDoc
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|parentDoc
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
name|bucketOrdToLastCollectedParentDoc
operator|.
name|indexInsert
argument_list|(
name|keySlot
argument_list|,
name|bucket
argument_list|,
name|parentDoc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
DECL|method|findClosestNestedAggregator
specifier|private
specifier|static
name|NestedAggregator
name|findClosestNestedAggregator
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
name|NestedAggregator
operator|)
name|parent
return|;
block|}
block|}
return|return
literal|null
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
name|InternalReverseNested
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
return|return
operator|new
name|InternalReverseNested
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
name|buildEmptySubAggregations
argument_list|()
argument_list|,
name|reducers
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getParentFilter
name|Filter
name|getParentFilter
parameter_list|()
block|{
return|return
name|parentFilter
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
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalReverseNested
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
comment|// Early validation
name|NestedAggregator
name|closestNestedAggregator
init|=
name|findClosestNestedAggregator
argument_list|(
name|parent
argument_list|)
decl_stmt|;
if|if
condition|(
name|closestNestedAggregator
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
operator|.
name|searchContext
argument_list|()
argument_list|,
literal|"Reverse nested aggregation ["
operator|+
name|name
operator|+
literal|"] can only be used inside a [nested] aggregation"
argument_list|,
literal|null
argument_list|)
throw|;
block|}
specifier|final
name|ObjectMapper
name|objectMapper
decl_stmt|;
if|if
condition|(
name|path
operator|!=
literal|null
condition|)
block|{
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
name|reducers
argument_list|,
name|metaData
argument_list|)
return|;
block|}
name|objectMapper
operator|=
name|mapper
operator|.
name|mapper
argument_list|()
expr_stmt|;
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
name|reducers
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
literal|"[reverse_nested] nested path ["
operator|+
name|path
operator|+
literal|"] is not nested"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|objectMapper
operator|=
literal|null
expr_stmt|;
block|}
return|return
operator|new
name|ReverseNestedAggregator
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
name|reducers
argument_list|,
name|metaData
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
name|context
argument_list|,
name|parent
argument_list|,
name|reducers
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
name|InternalReverseNested
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
name|buildEmptySubAggregations
argument_list|()
argument_list|,
name|reducers
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

