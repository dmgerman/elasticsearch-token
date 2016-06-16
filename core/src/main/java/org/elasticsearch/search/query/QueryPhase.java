begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|query
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
name|Term
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
name|queries
operator|.
name|MinDocQuery
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
name|BooleanClause
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
name|BooleanQuery
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
name|Collector
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
name|ConstantScoreQuery
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
name|MatchAllDocsQuery
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
name|MultiCollector
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
name|TermQuery
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
name|TimeLimitingCollector
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
name|TopScoreDocCollector
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
name|TotalHitCountCollector
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
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|lucene
operator|.
name|MinimumScoreCollector
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
name|FilteredCollector
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
name|settings
operator|.
name|Settings
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
name|SearchPhase
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
name|SearchService
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
name|AggregationPhase
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
name|ScrollContext
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
name|profile
operator|.
name|ProfileShardResult
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
name|profile
operator|.
name|SearchProfileShardResults
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
name|profile
operator|.
name|query
operator|.
name|CollectorResult
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
name|profile
operator|.
name|query
operator|.
name|InternalProfileCollector
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
name|rescore
operator|.
name|RescorePhase
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
name|rescore
operator|.
name|RescoreSearchContext
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
name|sort
operator|.
name|SortAndFormats
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
name|suggest
operator|.
name|SuggestPhase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|AbstractList
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
name|Collections
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
name|concurrent
operator|.
name|Callable
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|QueryPhase
specifier|public
class|class
name|QueryPhase
implements|implements
name|SearchPhase
block|{
DECL|field|aggregationPhase
specifier|private
specifier|final
name|AggregationPhase
name|aggregationPhase
decl_stmt|;
DECL|field|suggestPhase
specifier|private
specifier|final
name|SuggestPhase
name|suggestPhase
decl_stmt|;
DECL|field|rescorePhase
specifier|private
name|RescorePhase
name|rescorePhase
decl_stmt|;
DECL|method|QueryPhase
specifier|public
name|QueryPhase
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|aggregationPhase
operator|=
operator|new
name|AggregationPhase
argument_list|()
expr_stmt|;
name|this
operator|.
name|suggestPhase
operator|=
operator|new
name|SuggestPhase
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|rescorePhase
operator|=
operator|new
name|RescorePhase
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|preProcess
specifier|public
name|void
name|preProcess
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
name|context
operator|.
name|preProcess
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|)
throws|throws
name|QueryPhaseExecutionException
block|{
if|if
condition|(
name|searchContext
operator|.
name|hasOnlySuggest
argument_list|()
condition|)
block|{
name|suggestPhase
operator|.
name|execute
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
comment|// TODO: fix this once we can fetch docs for suggestions
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|(
operator|new
name|TopDocs
argument_list|(
literal|0
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
argument_list|,
operator|new
name|DocValueFormat
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// Pre-process aggregations as late as possible. In the case of a DFS_Q_T_F
comment|// request, preProcess is called on the DFS phase phase, this is why we pre-process them
comment|// here to make sure it happens during the QUERY phase
name|aggregationPhase
operator|.
name|preProcess
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
name|boolean
name|rescore
init|=
name|execute
argument_list|(
name|searchContext
argument_list|,
name|searchContext
operator|.
name|searcher
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|rescore
condition|)
block|{
comment|// only if we do a regular search
name|rescorePhase
operator|.
name|execute
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
block|}
name|suggestPhase
operator|.
name|execute
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
name|aggregationPhase
operator|.
name|execute
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchContext
operator|.
name|getProfilers
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ProfileShardResult
name|shardResults
init|=
name|SearchProfileShardResults
operator|.
name|buildShardResults
argument_list|(
name|searchContext
operator|.
name|getProfilers
argument_list|()
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|profileResults
argument_list|(
name|shardResults
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|returnsDocsInOrder
specifier|private
specifier|static
name|boolean
name|returnsDocsInOrder
parameter_list|(
name|Query
name|query
parameter_list|,
name|SortAndFormats
name|sf
parameter_list|)
block|{
if|if
condition|(
name|sf
operator|==
literal|null
operator|||
name|Sort
operator|.
name|RELEVANCE
operator|.
name|equals
argument_list|(
name|sf
operator|.
name|sort
argument_list|)
condition|)
block|{
comment|// sort by score
comment|// queries that return constant scores will return docs in index
comment|// order since Lucene tie-breaks on the doc id
return|return
name|query
operator|.
name|getClass
argument_list|()
operator|==
name|ConstantScoreQuery
operator|.
name|class
operator|||
name|query
operator|.
name|getClass
argument_list|()
operator|==
name|MatchAllDocsQuery
operator|.
name|class
return|;
block|}
else|else
block|{
return|return
name|Sort
operator|.
name|INDEXORDER
operator|.
name|equals
argument_list|(
name|sf
operator|.
name|sort
argument_list|)
return|;
block|}
block|}
comment|/**      * In a package-private method so that it can be tested without having to      * wire everything (mapperService, etc.)      * @return whether the rescoring phase should be executed      */
DECL|method|execute
specifier|static
name|boolean
name|execute
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|,
specifier|final
name|IndexSearcher
name|searcher
parameter_list|)
throws|throws
name|QueryPhaseExecutionException
block|{
name|QuerySearchResult
name|queryResult
init|=
name|searchContext
operator|.
name|queryResult
argument_list|()
decl_stmt|;
name|queryResult
operator|.
name|searchTimedOut
argument_list|(
literal|false
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|doProfile
init|=
name|searchContext
operator|.
name|getProfilers
argument_list|()
operator|!=
literal|null
decl_stmt|;
specifier|final
name|SearchType
name|searchType
init|=
name|searchContext
operator|.
name|searchType
argument_list|()
decl_stmt|;
name|boolean
name|rescore
init|=
literal|false
decl_stmt|;
try|try
block|{
name|queryResult
operator|.
name|from
argument_list|(
name|searchContext
operator|.
name|from
argument_list|()
argument_list|)
expr_stmt|;
name|queryResult
operator|.
name|size
argument_list|(
name|searchContext
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|searchContext
operator|.
name|query
argument_list|()
decl_stmt|;
specifier|final
name|int
name|totalNumDocs
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|numDocs
argument_list|()
decl_stmt|;
name|int
name|numDocs
init|=
name|Math
operator|.
name|min
argument_list|(
name|searchContext
operator|.
name|from
argument_list|()
operator|+
name|searchContext
operator|.
name|size
argument_list|()
argument_list|,
name|totalNumDocs
argument_list|)
decl_stmt|;
name|Collector
name|collector
decl_stmt|;
name|Callable
argument_list|<
name|TopDocs
argument_list|>
name|topDocsCallable
decl_stmt|;
name|DocValueFormat
index|[]
name|sortValueFormats
init|=
operator|new
name|DocValueFormat
index|[
literal|0
index|]
decl_stmt|;
assert|assert
name|query
operator|==
name|searcher
operator|.
name|rewrite
argument_list|(
name|query
argument_list|)
assert|;
comment|// already rewritten
if|if
condition|(
name|searchContext
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
comment|// no matter what the value of from is
specifier|final
name|TotalHitCountCollector
name|totalHitCountCollector
init|=
operator|new
name|TotalHitCountCollector
argument_list|()
decl_stmt|;
name|collector
operator|=
name|totalHitCountCollector
expr_stmt|;
if|if
condition|(
name|searchContext
operator|.
name|getProfilers
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_COUNT
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|topDocsCallable
operator|=
operator|new
name|Callable
argument_list|<
name|TopDocs
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TopDocs
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
operator|new
name|TopDocs
argument_list|(
name|totalHitCountCollector
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
else|else
block|{
comment|// Perhaps have a dedicated scroll phase?
specifier|final
name|ScrollContext
name|scrollContext
init|=
name|searchContext
operator|.
name|scrollContext
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|scrollContext
operator|!=
literal|null
operator|)
operator|==
operator|(
name|searchContext
operator|.
name|request
argument_list|()
operator|.
name|scroll
argument_list|()
operator|!=
literal|null
operator|)
assert|;
specifier|final
name|TopDocsCollector
argument_list|<
name|?
argument_list|>
name|topDocsCollector
decl_stmt|;
name|ScoreDoc
name|after
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|searchContext
operator|.
name|request
argument_list|()
operator|.
name|scroll
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|numDocs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|searchContext
operator|.
name|size
argument_list|()
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|after
operator|=
name|scrollContext
operator|.
name|lastEmittedDoc
expr_stmt|;
if|if
condition|(
name|returnsDocsInOrder
argument_list|(
name|query
argument_list|,
name|searchContext
operator|.
name|sort
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|scrollContext
operator|.
name|totalHits
operator|==
operator|-
literal|1
condition|)
block|{
comment|// first round
assert|assert
name|scrollContext
operator|.
name|lastEmittedDoc
operator|==
literal|null
assert|;
comment|// there is not much that we can optimize here since we want to collect all
comment|// documents in order to get the total number of hits
block|}
else|else
block|{
comment|// now this gets interesting: since we sort in index-order, we can directly
comment|// skip to the desired doc and stop collecting after ${size} matches
if|if
condition|(
name|scrollContext
operator|.
name|lastEmittedDoc
operator|!=
literal|null
condition|)
block|{
name|BooleanQuery
name|bq
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
name|query
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|MinDocQuery
argument_list|(
name|after
operator|.
name|doc
operator|+
literal|1
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|FILTER
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|query
operator|=
name|bq
expr_stmt|;
block|}
name|searchContext
operator|.
name|terminateAfter
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|after
operator|=
name|searchContext
operator|.
name|searchAfter
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|totalNumDocs
operator|==
literal|0
condition|)
block|{
comment|// top collectors don't like a size of 0
name|numDocs
operator|=
literal|1
expr_stmt|;
block|}
assert|assert
name|numDocs
operator|>
literal|0
assert|;
if|if
condition|(
name|searchContext
operator|.
name|sort
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|SortAndFormats
name|sf
init|=
name|searchContext
operator|.
name|sort
argument_list|()
decl_stmt|;
name|topDocsCollector
operator|=
name|TopFieldCollector
operator|.
name|create
argument_list|(
name|sf
operator|.
name|sort
argument_list|,
name|numDocs
argument_list|,
operator|(
name|FieldDoc
operator|)
name|after
argument_list|,
literal|true
argument_list|,
name|searchContext
operator|.
name|trackScores
argument_list|()
argument_list|,
name|searchContext
operator|.
name|trackScores
argument_list|()
argument_list|)
expr_stmt|;
name|sortValueFormats
operator|=
name|sf
operator|.
name|formats
expr_stmt|;
block|}
else|else
block|{
name|rescore
operator|=
operator|!
name|searchContext
operator|.
name|rescore
argument_list|()
operator|.
name|isEmpty
argument_list|()
expr_stmt|;
for|for
control|(
name|RescoreSearchContext
name|rescoreContext
range|:
name|searchContext
operator|.
name|rescore
argument_list|()
control|)
block|{
name|numDocs
operator|=
name|Math
operator|.
name|max
argument_list|(
name|rescoreContext
operator|.
name|window
argument_list|()
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
block|}
name|topDocsCollector
operator|=
name|TopScoreDocCollector
operator|.
name|create
argument_list|(
name|numDocs
argument_list|,
name|after
argument_list|)
expr_stmt|;
block|}
name|collector
operator|=
name|topDocsCollector
expr_stmt|;
if|if
condition|(
name|doProfile
condition|)
block|{
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_TOP_HITS
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|topDocsCallable
operator|=
operator|new
name|Callable
argument_list|<
name|TopDocs
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TopDocs
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|TopDocs
name|topDocs
init|=
name|topDocsCollector
operator|.
name|topDocs
argument_list|()
decl_stmt|;
if|if
condition|(
name|scrollContext
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|scrollContext
operator|.
name|totalHits
operator|==
operator|-
literal|1
condition|)
block|{
comment|// first round
name|scrollContext
operator|.
name|totalHits
operator|=
name|topDocs
operator|.
name|totalHits
expr_stmt|;
name|scrollContext
operator|.
name|maxScore
operator|=
name|topDocs
operator|.
name|getMaxScore
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// subsequent round: the total number of hits and
comment|// the maximum score were computed on the first round
name|topDocs
operator|.
name|totalHits
operator|=
name|scrollContext
operator|.
name|totalHits
expr_stmt|;
name|topDocs
operator|.
name|setMaxScore
argument_list|(
name|scrollContext
operator|.
name|maxScore
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
name|searchType
condition|)
block|{
case|case
name|QUERY_AND_FETCH
case|:
case|case
name|DFS_QUERY_AND_FETCH
case|:
comment|// for (DFS_)QUERY_AND_FETCH, we already know the last emitted doc
if|if
condition|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
comment|// set the last emitted doc
name|scrollContext
operator|.
name|lastEmittedDoc
operator|=
name|topDocs
operator|.
name|scoreDocs
index|[
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|-
literal|1
index|]
expr_stmt|;
block|}
break|break;
default|default:
break|break;
block|}
block|}
return|return
name|topDocs
return|;
block|}
block|}
expr_stmt|;
block|}
specifier|final
name|boolean
name|terminateAfterSet
init|=
name|searchContext
operator|.
name|terminateAfter
argument_list|()
operator|!=
name|SearchContext
operator|.
name|DEFAULT_TERMINATE_AFTER
decl_stmt|;
if|if
condition|(
name|terminateAfterSet
condition|)
block|{
specifier|final
name|Collector
name|child
init|=
name|collector
decl_stmt|;
comment|// throws Lucene.EarlyTerminationException when given count is reached
name|collector
operator|=
name|Lucene
operator|.
name|wrapCountBasedEarlyTerminatingCollector
argument_list|(
name|collector
argument_list|,
name|searchContext
operator|.
name|terminateAfter
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|doProfile
condition|)
block|{
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_TERMINATE_AFTER_COUNT
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|(
name|InternalProfileCollector
operator|)
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|searchContext
operator|.
name|parsedPostFilter
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Collector
name|child
init|=
name|collector
decl_stmt|;
comment|// this will only get applied to the actual search collector and not
comment|// to any scoped collectors, also, it will only be applied to the main collector
comment|// since that is where the filter should only work
specifier|final
name|Weight
name|filterWeight
init|=
name|searcher
operator|.
name|createNormalizedWeight
argument_list|(
name|searchContext
operator|.
name|parsedPostFilter
argument_list|()
operator|.
name|query
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|collector
operator|=
operator|new
name|FilteredCollector
argument_list|(
name|collector
argument_list|,
name|filterWeight
argument_list|)
expr_stmt|;
if|if
condition|(
name|doProfile
condition|)
block|{
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_POST_FILTER
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|(
name|InternalProfileCollector
operator|)
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// plug in additional collectors, like aggregations
specifier|final
name|List
argument_list|<
name|Collector
argument_list|>
name|subCollectors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|subCollectors
operator|.
name|add
argument_list|(
name|collector
argument_list|)
expr_stmt|;
name|subCollectors
operator|.
name|addAll
argument_list|(
name|searchContext
operator|.
name|queryCollectors
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|collector
operator|=
name|MultiCollector
operator|.
name|wrap
argument_list|(
name|subCollectors
argument_list|)
expr_stmt|;
if|if
condition|(
name|doProfile
operator|&&
name|collector
operator|instanceof
name|InternalProfileCollector
operator|==
literal|false
condition|)
block|{
comment|// When there is a single collector to wrap, MultiCollector returns it
comment|// directly, so only wrap in the case that there are several sub collectors
specifier|final
name|List
argument_list|<
name|InternalProfileCollector
argument_list|>
name|children
init|=
operator|new
name|AbstractList
argument_list|<
name|InternalProfileCollector
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InternalProfileCollector
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
operator|(
name|InternalProfileCollector
operator|)
name|subCollectors
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|subCollectors
operator|.
name|size
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_MULTI
argument_list|,
name|children
argument_list|)
expr_stmt|;
block|}
comment|// apply the minimum score after multi collector so we filter aggs as well
if|if
condition|(
name|searchContext
operator|.
name|minimumScore
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Collector
name|child
init|=
name|collector
decl_stmt|;
name|collector
operator|=
operator|new
name|MinimumScoreCollector
argument_list|(
name|collector
argument_list|,
name|searchContext
operator|.
name|minimumScore
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|doProfile
condition|)
block|{
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_MIN_SCORE
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|(
name|InternalProfileCollector
operator|)
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|collector
operator|.
name|getClass
argument_list|()
operator|==
name|TotalHitCountCollector
operator|.
name|class
condition|)
block|{
comment|// Optimize counts in simple cases to return in constant time
comment|// instead of using a collector
while|while
condition|(
literal|true
condition|)
block|{
comment|// remove wrappers that don't matter for counts
comment|// this is necessary so that we don't only optimize match_all
comment|// queries but also match_all queries that are nested in
comment|// a constant_score query
if|if
condition|(
name|query
operator|instanceof
name|ConstantScoreQuery
condition|)
block|{
name|query
operator|=
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|query
operator|)
operator|.
name|getQuery
argument_list|()
expr_stmt|;
block|}
else|else
block|{
break|break;
block|}
block|}
if|if
condition|(
name|query
operator|.
name|getClass
argument_list|()
operator|==
name|MatchAllDocsQuery
operator|.
name|class
condition|)
block|{
name|collector
operator|=
literal|null
expr_stmt|;
name|topDocsCallable
operator|=
operator|new
name|Callable
argument_list|<
name|TopDocs
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TopDocs
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|count
init|=
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|numDocs
argument_list|()
decl_stmt|;
return|return
operator|new
name|TopDocs
argument_list|(
name|count
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|query
operator|.
name|getClass
argument_list|()
operator|==
name|TermQuery
operator|.
name|class
operator|&&
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|hasDeletions
argument_list|()
operator|==
literal|false
condition|)
block|{
specifier|final
name|Term
name|term
init|=
operator|(
operator|(
name|TermQuery
operator|)
name|query
operator|)
operator|.
name|getTerm
argument_list|()
decl_stmt|;
name|collector
operator|=
literal|null
expr_stmt|;
name|topDocsCallable
operator|=
operator|new
name|Callable
argument_list|<
name|TopDocs
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TopDocs
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|searcher
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
control|)
block|{
name|count
operator|+=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|docFreq
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TopDocs
argument_list|(
name|count
argument_list|,
name|Lucene
operator|.
name|EMPTY_SCORE_DOCS
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
block|}
specifier|final
name|boolean
name|timeoutSet
init|=
name|searchContext
operator|.
name|timeoutInMillis
argument_list|()
operator|!=
name|SearchService
operator|.
name|NO_TIMEOUT
operator|.
name|millis
argument_list|()
decl_stmt|;
if|if
condition|(
name|timeoutSet
operator|&&
name|collector
operator|!=
literal|null
condition|)
block|{
comment|// collector might be null if no collection is actually needed
specifier|final
name|Collector
name|child
init|=
name|collector
decl_stmt|;
comment|// TODO: change to use our own counter that uses the scheduler in ThreadPool
comment|// throws TimeLimitingCollector.TimeExceededException when timeout has reached
name|collector
operator|=
name|Lucene
operator|.
name|wrapTimeLimitingCollector
argument_list|(
name|collector
argument_list|,
name|searchContext
operator|.
name|timeEstimateCounter
argument_list|()
argument_list|,
name|searchContext
operator|.
name|timeoutInMillis
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|doProfile
condition|)
block|{
name|collector
operator|=
operator|new
name|InternalProfileCollector
argument_list|(
name|collector
argument_list|,
name|CollectorResult
operator|.
name|REASON_SEARCH_TIMEOUT
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
operator|(
name|InternalProfileCollector
operator|)
name|child
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
try|try
block|{
if|if
condition|(
name|collector
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|doProfile
condition|)
block|{
name|searchContext
operator|.
name|getProfilers
argument_list|()
operator|.
name|getCurrentQueryProfiler
argument_list|()
operator|.
name|setCollector
argument_list|(
operator|(
name|InternalProfileCollector
operator|)
name|collector
argument_list|)
expr_stmt|;
block|}
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|TimeLimitingCollector
operator|.
name|TimeExceededException
name|e
parameter_list|)
block|{
assert|assert
name|timeoutSet
operator|:
literal|"TimeExceededException thrown even though timeout wasn't set"
assert|;
name|queryResult
operator|.
name|searchTimedOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Lucene
operator|.
name|EarlyTerminationException
name|e
parameter_list|)
block|{
assert|assert
name|terminateAfterSet
operator|:
literal|"EarlyTerminationException thrown even though terminateAfter wasn't set"
assert|;
name|queryResult
operator|.
name|terminatedEarly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|searchContext
operator|.
name|clearReleasables
argument_list|(
name|SearchContext
operator|.
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|terminateAfterSet
operator|&&
name|queryResult
operator|.
name|terminatedEarly
argument_list|()
operator|==
literal|null
condition|)
block|{
name|queryResult
operator|.
name|terminatedEarly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|queryResult
operator|.
name|topDocs
argument_list|(
name|topDocsCallable
operator|.
name|call
argument_list|()
argument_list|,
name|sortValueFormats
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchContext
operator|.
name|getProfilers
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|ProfileShardResult
name|shardResults
init|=
name|SearchProfileShardResults
operator|.
name|buildShardResults
argument_list|(
name|searchContext
operator|.
name|getProfilers
argument_list|()
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|profileResults
argument_list|(
name|shardResults
argument_list|)
expr_stmt|;
block|}
return|return
name|rescore
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryPhaseExecutionException
argument_list|(
name|searchContext
argument_list|,
literal|"Failed to execute main query"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

