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
name|Query
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
name|BitSetProducer
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
name|ParseField
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|AggregatorBuilder
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
DECL|field|PATH_FIELD
specifier|static
specifier|final
name|ParseField
name|PATH_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"path"
argument_list|)
decl_stmt|;
DECL|field|parentFilter
specifier|private
specifier|final
name|Query
name|parentFilter
decl_stmt|;
DECL|field|parentBitsetProducer
specifier|private
specifier|final
name|BitSetProducer
name|parentBitsetProducer
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
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
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
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|parentFilter
operator|=
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
expr_stmt|;
block|}
name|parentBitsetProducer
operator|=
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|bitsetFilterCache
argument_list|()
operator|.
name|getBitSetProducer
argument_list|(
name|parentFilter
argument_list|)
expr_stmt|;
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
specifier|final
name|BitSet
name|parentDocs
init|=
name|parentBitsetProducer
operator|.
name|getBitSet
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
if|if
condition|(
name|parentDocs
operator|==
literal|null
condition|)
block|{
return|return
name|LeafBucketCollector
operator|.
name|NO_OP_COLLECTOR
return|;
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
name|pipelineAggregators
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
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getParentFilter
name|Query
name|getParentFilter
parameter_list|()
block|{
return|return
name|parentFilter
return|;
block|}
DECL|class|ReverseNestedAggregatorBuilder
specifier|public
specifier|static
class|class
name|ReverseNestedAggregatorBuilder
extends|extends
name|AggregatorBuilder
argument_list|<
name|ReverseNestedAggregatorBuilder
argument_list|>
block|{
DECL|field|path
specifier|private
name|String
name|path
decl_stmt|;
DECL|method|ReverseNestedAggregatorBuilder
specifier|public
name|ReverseNestedAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalReverseNested
operator|.
name|TYPE
argument_list|)
expr_stmt|;
block|}
comment|/**          * Set the path to use for this nested aggregation. The path must match          * the path to a nested object in the mappings. If it is not specified          * then this aggregation will go back to the root document.          */
DECL|method|path
specifier|public
name|ReverseNestedAggregatorBuilder
name|path
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Get the path to use for this nested aggregation.          */
DECL|method|path
specifier|public
name|String
name|path
parameter_list|()
block|{
return|return
name|path
return|;
block|}
annotation|@
name|Override
DECL|method|doBuild
specifier|protected
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|doBuild
parameter_list|(
name|AggregationContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ReverseNestedAggregatorFactory
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|path
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|PATH_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|ReverseNestedAggregatorBuilder
name|doReadFrom
parameter_list|(
name|String
name|name
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ReverseNestedAggregatorBuilder
name|factory
init|=
operator|new
name|ReverseNestedAggregatorBuilder
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|factory
operator|.
name|path
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
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
name|writeOptionalString
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|path
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|ReverseNestedAggregatorBuilder
name|other
init|=
operator|(
name|ReverseNestedAggregatorBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|path
argument_list|,
name|other
operator|.
name|path
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

