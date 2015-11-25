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
name|IndexReaderContext
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
name|index
operator|.
name|ReaderUtil
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
name|IndexSearcher
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
name|Weight
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
DECL|class|NestedAggregator
specifier|public
class|class
name|NestedAggregator
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
name|BitSetProducer
name|parentFilter
decl_stmt|;
DECL|field|childFilter
specifier|private
specifier|final
name|Query
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
name|parentAggregator
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|childFilter
operator|=
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
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
specifier|final
name|IndexReaderContext
name|topLevelContext
init|=
name|ReaderUtil
operator|.
name|getTopLevelContext
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
specifier|final
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|topLevelContext
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|setQueryCache
argument_list|(
literal|null
argument_list|)
expr_stmt|;
specifier|final
name|Weight
name|weight
init|=
name|searcher
operator|.
name|createNormalizedWeight
argument_list|(
name|childFilter
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|childDocs
operator|=
name|weight
operator|.
name|scorer
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
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
name|Query
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
name|getBitSetProducer
argument_list|(
name|parentFilterNotCached
argument_list|)
expr_stmt|;
name|parentDocs
operator|=
name|parentFilter
operator|.
name|getBitSet
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
if|if
condition|(
name|parentDocs
operator|==
literal|null
condition|)
block|{
comment|// There are no parentDocs in the segment, so return and set childDocs to null, so we exit early for future invocations.
name|childDocs
operator|=
literal|null
expr_stmt|;
return|return;
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
name|InternalNested
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
DECL|method|findClosestNestedPath
specifier|private
specifier|static
name|Query
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
comment|/**          * @param name          *            the name of this aggregation          * @param path          *            the path to use for this nested aggregation. The path must          *            match the path to a nested object in the mappings.          */
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
name|InternalNested
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
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
name|ObjectMapper
name|objectMapper
init|=
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|getObjectMapper
argument_list|(
name|path
argument_list|)
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
name|pipelineAggregators
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
name|pipelineAggregators
argument_list|,
name|metaData
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
name|AggregatorFactory
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
name|String
name|path
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|Factory
name|factory
init|=
operator|new
name|Factory
argument_list|(
name|name
argument_list|,
name|path
argument_list|)
decl_stmt|;
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
name|writeString
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
name|Factory
name|other
init|=
operator|(
name|Factory
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
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
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
name|pipelineAggregators
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

