begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
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
name|base
operator|.
name|Predicate
import|;
end_import

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
name|Iterables
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
name|Scorer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|xcontent
operator|.
name|XContentParser
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
name|DeferringBucketCollector
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
name|internal
operator|.
name|SearchContext
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
operator|.
name|Lifetime
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
name|query
operator|.
name|QueryPhaseExecutionException
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
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
DECL|class|Aggregator
specifier|public
specifier|abstract
class|class
name|Aggregator
extends|extends
name|BucketCollector
implements|implements
name|Releasable
block|{
comment|/**      * Returns whether one of the parents is a {@link BucketsAggregator}.      */
DECL|method|descendsFromBucketAggregator
specifier|public
specifier|static
name|boolean
name|descendsFromBucketAggregator
parameter_list|(
name|Aggregator
name|parent
parameter_list|)
block|{
while|while
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parent
operator|instanceof
name|BucketsAggregator
condition|)
block|{
return|return
literal|true
return|;
block|}
name|parent
operator|=
name|parent
operator|.
name|parent
expr_stmt|;
block|}
return|return
literal|false
return|;
block|}
DECL|field|COLLECTABLE_AGGREGATOR
specifier|private
specifier|static
specifier|final
name|Predicate
argument_list|<
name|Aggregator
argument_list|>
name|COLLECTABLE_AGGREGATOR
init|=
operator|new
name|Predicate
argument_list|<
name|Aggregator
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Aggregator
name|aggregator
parameter_list|)
block|{
return|return
name|aggregator
operator|.
name|shouldCollect
argument_list|()
return|;
block|}
block|}
decl_stmt|;
DECL|field|COLLECT_MODE
specifier|public
specifier|static
specifier|final
name|ParseField
name|COLLECT_MODE
init|=
operator|new
name|ParseField
argument_list|(
literal|"collect_mode"
argument_list|)
decl_stmt|;
DECL|enum|SubAggCollectionMode
specifier|public
enum|enum
name|SubAggCollectionMode
block|{
comment|/**          * Creates buckets and delegates to child aggregators in a single pass over          * the matching documents          */
DECL|enum constant|DEPTH_FIRST
name|DEPTH_FIRST
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"depth_first"
argument_list|)
argument_list|)
block|,
comment|/**          * Creates buckets for all matching docs and then prunes to top-scoring buckets          * before a second pass over the data when child aggregators are called          * but only for docs from the top-scoring buckets          */
DECL|enum constant|BREADTH_FIRST
name|BREADTH_FIRST
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"breadth_first"
argument_list|)
argument_list|)
block|;
DECL|field|parseField
specifier|private
specifier|final
name|ParseField
name|parseField
decl_stmt|;
DECL|method|SubAggCollectionMode
name|SubAggCollectionMode
parameter_list|(
name|ParseField
name|parseField
parameter_list|)
block|{
name|this
operator|.
name|parseField
operator|=
name|parseField
expr_stmt|;
block|}
DECL|method|parseField
specifier|public
name|ParseField
name|parseField
parameter_list|()
block|{
return|return
name|parseField
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|SubAggCollectionMode
name|parse
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|parse
argument_list|(
name|value
argument_list|,
name|ParseField
operator|.
name|EMPTY_FLAGS
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|SubAggCollectionMode
name|parse
parameter_list|(
name|String
name|value
parameter_list|,
name|EnumSet
argument_list|<
name|ParseField
operator|.
name|Flag
argument_list|>
name|flags
parameter_list|)
block|{
name|SubAggCollectionMode
index|[]
name|modes
init|=
name|SubAggCollectionMode
operator|.
name|values
argument_list|()
decl_stmt|;
for|for
control|(
name|SubAggCollectionMode
name|mode
range|:
name|modes
control|)
block|{
if|if
condition|(
name|mode
operator|.
name|parseField
operator|.
name|match
argument_list|(
name|value
argument_list|,
name|flags
argument_list|)
condition|)
block|{
return|return
name|mode
return|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"No "
operator|+
name|COLLECT_MODE
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" found for value ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
comment|// A scorer used for the deferred collection mode to handle any child aggs asking for scores that are not
comment|// recorded.
DECL|field|unavailableScorer
specifier|static
specifier|final
name|Scorer
name|unavailableScorer
init|=
operator|new
name|Scorer
argument_list|(
literal|null
argument_list|)
block|{
specifier|private
specifier|final
name|String
name|MSG
init|=
literal|"A limitation of the "
operator|+
name|SubAggCollectionMode
operator|.
name|BREADTH_FIRST
operator|.
name|parseField
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" collection mode is that scores cannot be buffered along with document IDs"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|float
name|score
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|MSG
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|freq
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|MSG
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|advance
parameter_list|(
name|int
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|MSG
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|cost
parameter_list|()
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|MSG
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|docID
parameter_list|()
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|MSG
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|nextDoc
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
name|MSG
argument_list|)
throw|;
block|}
block|}
decl_stmt|;
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|parent
specifier|protected
specifier|final
name|Aggregator
name|parent
decl_stmt|;
DECL|field|context
specifier|protected
specifier|final
name|AggregationContext
name|context
decl_stmt|;
DECL|field|bigArrays
specifier|protected
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
DECL|field|depth
specifier|protected
specifier|final
name|int
name|depth
decl_stmt|;
DECL|field|metaData
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
decl_stmt|;
DECL|field|factories
specifier|protected
specifier|final
name|AggregatorFactories
name|factories
decl_stmt|;
DECL|field|subAggregators
specifier|protected
specifier|final
name|Aggregator
index|[]
name|subAggregators
decl_stmt|;
DECL|field|collectableSubAggregators
specifier|protected
name|BucketCollector
name|collectableSubAggregators
decl_stmt|;
DECL|field|subAggregatorbyName
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Aggregator
argument_list|>
name|subAggregatorbyName
decl_stmt|;
DECL|field|recordingWrapper
specifier|private
name|DeferringBucketCollector
name|recordingWrapper
decl_stmt|;
comment|/**      * Constructs a new Aggregator.      *      * @param name                  The name of the aggregation      * @param factories             The factories for all the sub-aggregators under this aggregator      * @param context               The aggregation context      * @param parent                The parent aggregator (may be {@code null} for top level aggregators)      * @param metaData              The metaData associated with this aggregator      */
DECL|method|Aggregator
specifier|protected
name|Aggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
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
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
name|context
operator|.
name|bigArrays
argument_list|()
expr_stmt|;
name|this
operator|.
name|depth
operator|=
name|parent
operator|==
literal|null
condition|?
literal|0
else|:
literal|1
operator|+
name|parent
operator|.
name|depth
argument_list|()
expr_stmt|;
assert|assert
name|factories
operator|!=
literal|null
operator|:
literal|"sub-factories provided to BucketAggregator must not be null, use AggragatorFactories.EMPTY instead"
assert|;
name|this
operator|.
name|factories
operator|=
name|factories
expr_stmt|;
name|this
operator|.
name|subAggregators
operator|=
name|factories
operator|.
name|createSubAggregators
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|addReleasable
argument_list|(
name|this
argument_list|,
name|Lifetime
operator|.
name|PHASE
argument_list|)
expr_stmt|;
comment|// Register a safeguard to highlight any invalid construction logic (call to this constructor without subsequent preCollection call)
name|collectableSubAggregators
operator|=
operator|new
name|BucketCollector
argument_list|()
block|{
name|void
name|badState
parameter_list|()
block|{
throw|throw
operator|new
name|QueryPhaseExecutionException
argument_list|(
name|Aggregator
operator|.
name|this
operator|.
name|context
operator|.
name|searchContext
argument_list|()
argument_list|,
literal|"preCollection not called on new Aggregator before use"
argument_list|,
literal|null
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNextReader
parameter_list|(
name|LeafReaderContext
name|reader
parameter_list|)
block|{
name|badState
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCollection
parameter_list|()
throws|throws
name|IOException
block|{
name|badState
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postCollection
parameter_list|()
throws|throws
name|IOException
block|{
name|badState
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
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
name|badState
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|gatherAnalysis
parameter_list|(
name|BucketAnalysisCollector
name|results
parameter_list|,
name|long
name|bucketOrdinal
parameter_list|)
block|{
name|badState
argument_list|()
expr_stmt|;
block|}
block|}
expr_stmt|;
block|}
DECL|method|metaData
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|()
block|{
return|return
name|this
operator|.
name|metaData
return|;
block|}
comment|/**      * Can be overriden by aggregator implementation to be called back when the collection phase starts.      */
DECL|method|doPreCollection
specifier|protected
name|void
name|doPreCollection
parameter_list|()
throws|throws
name|IOException
block|{     }
DECL|method|preCollection
specifier|public
specifier|final
name|void
name|preCollection
parameter_list|()
throws|throws
name|IOException
block|{
name|Iterable
argument_list|<
name|Aggregator
argument_list|>
name|collectables
init|=
name|Iterables
operator|.
name|filter
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|subAggregators
argument_list|)
argument_list|,
name|COLLECTABLE_AGGREGATOR
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|BucketCollector
argument_list|>
name|nextPassCollectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|BucketCollector
argument_list|>
name|thisPassCollectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Aggregator
name|aggregator
range|:
name|collectables
control|)
block|{
if|if
condition|(
name|shouldDefer
argument_list|(
name|aggregator
argument_list|)
condition|)
block|{
name|nextPassCollectors
operator|.
name|add
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|thisPassCollectors
operator|.
name|add
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|nextPassCollectors
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|BucketCollector
name|deferreds
init|=
name|BucketCollector
operator|.
name|wrap
argument_list|(
name|nextPassCollectors
argument_list|)
decl_stmt|;
name|recordingWrapper
operator|=
operator|new
name|DeferringBucketCollector
argument_list|(
name|deferreds
argument_list|,
name|context
argument_list|)
expr_stmt|;
comment|// TODO. Without line below we are dependent on subclass aggs
comment|// delegating setNextReader calls on to child aggs
comment|// which they don't seem to do as a matter of course. Need to move
comment|// to a delegation model rather than broadcast
name|context
operator|.
name|registerReaderContextAware
argument_list|(
name|recordingWrapper
argument_list|)
expr_stmt|;
name|thisPassCollectors
operator|.
name|add
argument_list|(
name|recordingWrapper
argument_list|)
expr_stmt|;
block|}
name|collectableSubAggregators
operator|=
name|BucketCollector
operator|.
name|wrap
argument_list|(
name|thisPassCollectors
argument_list|)
expr_stmt|;
name|collectableSubAggregators
operator|.
name|preCollection
argument_list|()
expr_stmt|;
name|doPreCollection
argument_list|()
expr_stmt|;
block|}
comment|/**      * This method should be overidden by subclasses that want to defer calculation      * of a child aggregation until a first pass is complete and a set of buckets has       * been pruned.      * Deferring collection will require the recording of all doc/bucketIds from the first       * pass and then the sub class should call {@link #runDeferredCollections(long...)}        * for the selected set of buckets that survive the pruning.      * @param aggregator the child aggregator       * @return true if the aggregator should be deferred      * until a first pass at collection has completed      */
DECL|method|shouldDefer
specifier|protected
name|boolean
name|shouldDefer
parameter_list|(
name|Aggregator
name|aggregator
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|runDeferredCollections
specifier|protected
name|void
name|runDeferredCollections
parameter_list|(
name|long
modifier|...
name|bucketOrds
parameter_list|)
block|{
comment|// Being lenient here - ignore calls where there are no deferred collections to playback
if|if
condition|(
name|recordingWrapper
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|setScorer
argument_list|(
name|unavailableScorer
argument_list|)
expr_stmt|;
name|recordingWrapper
operator|.
name|prepareSelectedBuckets
argument_list|(
name|bucketOrds
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * @return  The name of the aggregation.      */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/** Return the depth of this aggregator in the aggregation tree. */
DECL|method|depth
specifier|public
specifier|final
name|int
name|depth
parameter_list|()
block|{
return|return
name|depth
return|;
block|}
comment|/**      * @return  The parent aggregator of this aggregator. The addAggregation are hierarchical in the sense that some can      *          be composed out of others (more specifically, bucket addAggregation can define other addAggregation that will      *          be aggregated per bucket). This method returns the direct parent aggregator that contains this aggregator, or      *          {@code null} if there is none (meaning, this aggregator is a top level one)      */
DECL|method|parent
specifier|public
name|Aggregator
name|parent
parameter_list|()
block|{
return|return
name|parent
return|;
block|}
DECL|method|subAggregators
specifier|public
name|Aggregator
index|[]
name|subAggregators
parameter_list|()
block|{
return|return
name|subAggregators
return|;
block|}
DECL|method|subAggregator
specifier|public
name|Aggregator
name|subAggregator
parameter_list|(
name|String
name|aggName
parameter_list|)
block|{
if|if
condition|(
name|subAggregatorbyName
operator|==
literal|null
condition|)
block|{
name|subAggregatorbyName
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|subAggregators
operator|.
name|length
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
name|subAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|subAggregatorbyName
operator|.
name|put
argument_list|(
name|subAggregators
index|[
name|i
index|]
operator|.
name|name
argument_list|,
name|subAggregators
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|subAggregatorbyName
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
return|;
block|}
comment|/**      * @return  The current aggregation context.      */
DECL|method|context
specifier|public
name|AggregationContext
name|context
parameter_list|()
block|{
return|return
name|context
return|;
block|}
comment|/**      * @return  Whether this aggregator is in the state where it can collect documents. Some aggregators can do their aggregations without      *          actually collecting documents, for example, an aggregator that computes stats over unmapped fields doesn't need to collect      *          anything as it knows to just return "empty" stats as the aggregation result.      */
DECL|method|shouldCollect
specifier|public
specifier|abstract
name|boolean
name|shouldCollect
parameter_list|()
function_decl|;
comment|/**      * Called after collection of all document is done.      */
DECL|method|postCollection
specifier|public
specifier|final
name|void
name|postCollection
parameter_list|()
throws|throws
name|IOException
block|{
name|collectableSubAggregators
operator|.
name|postCollection
argument_list|()
expr_stmt|;
name|doPostCollection
argument_list|()
expr_stmt|;
block|}
comment|/** Called upon release of the aggregator. */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
init|(
name|Releasable
name|_
init|=
name|recordingWrapper
init|)
block|{
name|doClose
argument_list|()
expr_stmt|;
block|}
block|}
comment|/** Release instance-specific data. */
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{}
comment|/**      * Can be overriden by aggregator implementation to be called back when the collection phase ends.      */
DECL|method|doPostCollection
specifier|protected
name|void
name|doPostCollection
parameter_list|()
throws|throws
name|IOException
block|{     }
comment|/**      * @return  The aggregated& built aggregation      */
DECL|method|buildAggregation
specifier|public
specifier|abstract
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|gatherAnalysis
specifier|public
name|void
name|gatherAnalysis
parameter_list|(
name|BucketAnalysisCollector
name|results
parameter_list|,
name|long
name|bucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
name|results
operator|.
name|add
argument_list|(
name|buildAggregation
argument_list|(
name|bucketOrdinal
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildEmptyAggregation
specifier|public
specifier|abstract
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
function_decl|;
DECL|method|buildEmptySubAggregations
specifier|protected
specifier|final
name|InternalAggregations
name|buildEmptySubAggregations
parameter_list|()
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Aggregator
name|aggregator
range|:
name|subAggregators
control|)
block|{
name|aggs
operator|.
name|add
argument_list|(
name|aggregator
operator|.
name|buildEmptyAggregation
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalAggregations
argument_list|(
name|aggs
argument_list|)
return|;
block|}
comment|/**      * Parses the aggregation request and creates the appropriate aggregator factory for it.      *      * @see {@link AggregatorFactory}     */
DECL|interface|Parser
specifier|public
specifier|static
interface|interface
name|Parser
block|{
comment|/**          * @return The aggregation type this parser is associated with.          */
DECL|method|type
name|String
name|type
parameter_list|()
function_decl|;
comment|/**          * Returns the aggregator factory with which this parser is associated, may return {@code null} indicating the          * aggregation should be skipped (e.g. when trying to aggregate on unmapped fields).          *          * @param aggregationName   The name of the aggregation          * @param parser            The xcontent parser          * @param context           The search context          * @return                  The resolved aggregator factory or {@code null} in case the aggregation should be skipped          * @throws java.io.IOException      When parsing fails          */
DECL|method|parse
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

