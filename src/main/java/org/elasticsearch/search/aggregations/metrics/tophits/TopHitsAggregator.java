begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.tophits
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|tophits
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
name|FieldDoc
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
name|LeafCollector
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
name|ScoreDoc
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Sort
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
name|TopDocs
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
name|TopDocsCollector
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
name|TopFieldCollector
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
name|TopFieldDocs
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
name|TopScoreDocCollector
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
name|Lucene
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationInitializationException
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
name|metrics
operator|.
name|MetricsAggregator
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|FetchPhase
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
name|fetch
operator|.
name|FetchSearchResult
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
name|InternalSearchHit
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
name|InternalSearchHits
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
name|SubSearchContext
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
comment|/**  */
end_comment

begin_class
DECL|class|TopHitsAggregator
specifier|public
class|class
name|TopHitsAggregator
extends|extends
name|MetricsAggregator
block|{
comment|/** Simple wrapper around a top-level collector and the current leaf collector. */
DECL|class|TopDocsAndLeafCollector
specifier|private
specifier|static
class|class
name|TopDocsAndLeafCollector
block|{
DECL|field|topLevelCollector
specifier|final
name|TopDocsCollector
argument_list|<
name|?
argument_list|>
name|topLevelCollector
decl_stmt|;
DECL|field|leafCollector
name|LeafCollector
name|leafCollector
decl_stmt|;
DECL|method|TopDocsAndLeafCollector
name|TopDocsAndLeafCollector
parameter_list|(
name|TopDocsCollector
argument_list|<
name|?
argument_list|>
name|topLevelCollector
parameter_list|)
block|{
name|this
operator|.
name|topLevelCollector
operator|=
name|topLevelCollector
expr_stmt|;
block|}
block|}
DECL|field|fetchPhase
specifier|final
name|FetchPhase
name|fetchPhase
decl_stmt|;
DECL|field|subSearchContext
specifier|final
name|SubSearchContext
name|subSearchContext
decl_stmt|;
DECL|field|topDocsCollectors
specifier|final
name|LongObjectPagedHashMap
argument_list|<
name|TopDocsAndLeafCollector
argument_list|>
name|topDocsCollectors
decl_stmt|;
DECL|method|TopHitsAggregator
specifier|public
name|TopHitsAggregator
parameter_list|(
name|FetchPhase
name|fetchPhase
parameter_list|,
name|SubSearchContext
name|subSearchContext
parameter_list|,
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
name|this
operator|.
name|fetchPhase
operator|=
name|fetchPhase
expr_stmt|;
name|topDocsCollectors
operator|=
operator|new
name|LongObjectPagedHashMap
argument_list|<>
argument_list|(
literal|1
argument_list|,
name|context
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|subSearchContext
operator|=
name|subSearchContext
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
name|Sort
name|sort
init|=
name|subSearchContext
operator|.
name|sort
argument_list|()
decl_stmt|;
if|if
condition|(
name|sort
operator|!=
literal|null
condition|)
block|{
return|return
name|sort
operator|.
name|needsScores
argument_list|()
operator|||
name|subSearchContext
operator|.
name|trackScores
argument_list|()
return|;
block|}
else|else
block|{
comment|// sort by score
return|return
literal|true
return|;
block|}
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
for|for
control|(
name|LongObjectPagedHashMap
operator|.
name|Cursor
argument_list|<
name|TopDocsAndLeafCollector
argument_list|>
name|cursor
range|:
name|topDocsCollectors
control|)
block|{
name|cursor
operator|.
name|value
operator|.
name|leafCollector
operator|=
name|cursor
operator|.
name|value
operator|.
name|topLevelCollector
operator|.
name|getLeafCollector
argument_list|(
name|ctx
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
name|Scorer
name|scorer
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
for|for
control|(
name|LongObjectPagedHashMap
operator|.
name|Cursor
argument_list|<
name|TopDocsAndLeafCollector
argument_list|>
name|cursor
range|:
name|topDocsCollectors
control|)
block|{
name|cursor
operator|.
name|value
operator|.
name|leafCollector
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
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
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
name|TopDocsAndLeafCollector
name|collectors
init|=
name|topDocsCollectors
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|collectors
operator|==
literal|null
condition|)
block|{
name|Sort
name|sort
init|=
name|subSearchContext
operator|.
name|sort
argument_list|()
decl_stmt|;
name|int
name|topN
init|=
name|subSearchContext
operator|.
name|from
argument_list|()
operator|+
name|subSearchContext
operator|.
name|size
argument_list|()
decl_stmt|;
name|TopDocsCollector
argument_list|<
name|?
argument_list|>
name|topLevelCollector
init|=
name|sort
operator|!=
literal|null
condition|?
name|TopFieldCollector
operator|.
name|create
argument_list|(
name|sort
argument_list|,
name|topN
argument_list|,
literal|true
argument_list|,
name|subSearchContext
operator|.
name|trackScores
argument_list|()
argument_list|,
name|subSearchContext
operator|.
name|trackScores
argument_list|()
argument_list|)
else|:
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|topN
argument_list|)
decl_stmt|;
name|collectors
operator|=
operator|new
name|TopDocsAndLeafCollector
argument_list|(
name|topLevelCollector
argument_list|)
expr_stmt|;
name|collectors
operator|.
name|leafCollector
operator|=
name|collectors
operator|.
name|topLevelCollector
operator|.
name|getLeafCollector
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
name|collectors
operator|.
name|leafCollector
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
name|topDocsCollectors
operator|.
name|put
argument_list|(
name|bucket
argument_list|,
name|collectors
argument_list|)
expr_stmt|;
block|}
name|collectors
operator|.
name|leafCollector
operator|.
name|collect
argument_list|(
name|docId
argument_list|)
expr_stmt|;
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
block|{
name|TopDocsAndLeafCollector
name|topDocsCollector
init|=
name|topDocsCollectors
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
specifier|final
name|InternalTopHits
name|topHits
decl_stmt|;
if|if
condition|(
name|topDocsCollector
operator|==
literal|null
condition|)
block|{
name|topHits
operator|=
name|buildEmptyAggregation
argument_list|()
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|TopDocs
name|topDocs
init|=
name|topDocsCollector
operator|.
name|topLevelCollector
operator|.
name|topDocs
argument_list|()
decl_stmt|;
name|subSearchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|(
name|topDocs
argument_list|)
expr_stmt|;
name|int
index|[]
name|docIdsToLoad
init|=
operator|new
name|int
index|[
name|topDocs
operator|.
name|scoreDocs
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
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|docIdsToLoad
index|[
name|i
index|]
operator|=
name|topDocs
operator|.
name|scoreDocs
index|[
name|i
index|]
operator|.
name|doc
expr_stmt|;
block|}
name|subSearchContext
operator|.
name|docIdsToLoad
argument_list|(
name|docIdsToLoad
argument_list|,
literal|0
argument_list|,
name|docIdsToLoad
operator|.
name|length
argument_list|)
expr_stmt|;
name|fetchPhase
operator|.
name|execute
argument_list|(
name|subSearchContext
argument_list|)
expr_stmt|;
name|FetchSearchResult
name|fetchResult
init|=
name|subSearchContext
operator|.
name|fetchResult
argument_list|()
decl_stmt|;
name|InternalSearchHit
index|[]
name|internalHits
init|=
name|fetchResult
operator|.
name|fetchResult
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|internalHits
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
name|internalHits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ScoreDoc
name|scoreDoc
init|=
name|topDocs
operator|.
name|scoreDocs
index|[
name|i
index|]
decl_stmt|;
name|InternalSearchHit
name|searchHitFields
init|=
name|internalHits
index|[
name|i
index|]
decl_stmt|;
name|searchHitFields
operator|.
name|shard
argument_list|(
name|subSearchContext
operator|.
name|shardTarget
argument_list|()
argument_list|)
expr_stmt|;
name|searchHitFields
operator|.
name|score
argument_list|(
name|scoreDoc
operator|.
name|score
argument_list|)
expr_stmt|;
if|if
condition|(
name|scoreDoc
operator|instanceof
name|FieldDoc
condition|)
block|{
name|FieldDoc
name|fieldDoc
init|=
operator|(
name|FieldDoc
operator|)
name|scoreDoc
decl_stmt|;
name|searchHitFields
operator|.
name|sortValues
argument_list|(
name|fieldDoc
operator|.
name|fields
argument_list|)
expr_stmt|;
block|}
block|}
name|topHits
operator|=
operator|new
name|InternalTopHits
argument_list|(
name|name
argument_list|,
name|subSearchContext
operator|.
name|from
argument_list|()
argument_list|,
name|subSearchContext
operator|.
name|size
argument_list|()
argument_list|,
name|topDocs
argument_list|,
name|fetchResult
operator|.
name|hits
argument_list|()
argument_list|,
name|reducers
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|topHits
return|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalTopHits
name|buildEmptyAggregation
parameter_list|()
block|{
name|TopDocs
name|topDocs
decl_stmt|;
if|if
condition|(
name|subSearchContext
operator|.
name|sort
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|topDocs
operator|=
operator|new
name|TopFieldDocs
argument_list|(
literal|0
argument_list|,
operator|new
name|FieldDoc
index|[
literal|0
index|]
argument_list|,
name|subSearchContext
operator|.
name|sort
argument_list|()
operator|.
name|getSort
argument_list|()
argument_list|,
name|Float
operator|.
name|NaN
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topDocs
operator|=
name|Lucene
operator|.
name|EMPTY_TOP_DOCS
expr_stmt|;
block|}
return|return
operator|new
name|InternalTopHits
argument_list|(
name|name
argument_list|,
name|subSearchContext
operator|.
name|from
argument_list|()
argument_list|,
name|subSearchContext
operator|.
name|size
argument_list|()
argument_list|,
name|topDocs
argument_list|,
name|InternalSearchHits
operator|.
name|empty
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
name|topDocsCollectors
argument_list|)
expr_stmt|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|AggregatorFactory
block|{
DECL|field|fetchPhase
specifier|private
specifier|final
name|FetchPhase
name|fetchPhase
decl_stmt|;
DECL|field|subSearchContext
specifier|private
specifier|final
name|SubSearchContext
name|subSearchContext
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|FetchPhase
name|fetchPhase
parameter_list|,
name|SubSearchContext
name|subSearchContext
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalTopHits
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchPhase
operator|=
name|fetchPhase
expr_stmt|;
name|this
operator|.
name|subSearchContext
operator|=
name|subSearchContext
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
name|aggregationContext
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
name|TopHitsAggregator
argument_list|(
name|fetchPhase
argument_list|,
name|subSearchContext
argument_list|,
name|name
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|reducers
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|subFactories
specifier|public
name|AggregatorFactory
name|subFactories
parameter_list|(
name|AggregatorFactories
name|subFactories
parameter_list|)
block|{
throw|throw
operator|new
name|AggregationInitializationException
argument_list|(
literal|"Aggregator ["
operator|+
name|name
operator|+
literal|"] of type ["
operator|+
name|type
operator|+
literal|"] cannot accept sub-aggregations"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

