begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
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
name|TotalHitCountCollector
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
name|inject
operator|.
name|Inject
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
name|search
operator|.
name|SearchParseElement
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
name|facet
operator|.
name|FacetPhase
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
name|ContextIndexSearcher
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
name|ScopePhase
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
name|sort
operator|.
name|SortParseElement
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
name|TrackScoresParseElement
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
DECL|class|QueryPhase
specifier|public
class|class
name|QueryPhase
implements|implements
name|SearchPhase
block|{
DECL|field|facetPhase
specifier|private
specifier|final
name|FacetPhase
name|facetPhase
decl_stmt|;
annotation|@
name|Inject
DECL|method|QueryPhase
specifier|public
name|QueryPhase
parameter_list|(
name|FacetPhase
name|facetPhase
parameter_list|)
block|{
name|this
operator|.
name|facetPhase
operator|=
name|facetPhase
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parseElements
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|SearchParseElement
argument_list|>
name|parseElements
parameter_list|()
block|{
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|SearchParseElement
argument_list|>
name|parseElements
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|parseElements
operator|.
name|put
argument_list|(
literal|"from"
argument_list|,
operator|new
name|FromParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"size"
argument_list|,
operator|new
name|SizeParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"indices_boost"
argument_list|,
operator|new
name|IndicesBoostParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"indicesBoost"
argument_list|,
operator|new
name|IndicesBoostParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"query"
argument_list|,
operator|new
name|QueryParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"queryBinary"
argument_list|,
operator|new
name|QueryBinaryParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"query_binary"
argument_list|,
operator|new
name|QueryBinaryParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"filter"
argument_list|,
operator|new
name|FilterParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"filterBinary"
argument_list|,
operator|new
name|FilterBinaryParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"filter_binary"
argument_list|,
operator|new
name|FilterBinaryParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"sort"
argument_list|,
operator|new
name|SortParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"trackScores"
argument_list|,
operator|new
name|TrackScoresParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"track_scores"
argument_list|,
operator|new
name|TrackScoresParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"min_score"
argument_list|,
operator|new
name|MinScoreParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"minScore"
argument_list|,
operator|new
name|MinScoreParseElement
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"timeout"
argument_list|,
operator|new
name|TimeoutParseElement
argument_list|()
argument_list|)
operator|.
name|putAll
argument_list|(
name|facetPhase
operator|.
name|parseElements
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|parseElements
operator|.
name|build
argument_list|()
return|;
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
name|facetPhase
operator|.
name|preProcess
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
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
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|searchTimedOut
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// set the filter on the searcher
if|if
condition|(
name|searchContext
operator|.
name|scopePhases
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// we have scoped queries, refresh the id cache
try|try
block|{
name|searchContext
operator|.
name|idCache
argument_list|()
operator|.
name|refresh
argument_list|(
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|getTopReaderContext
argument_list|()
operator|.
name|leaves
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryPhaseExecutionException
argument_list|(
name|searchContext
argument_list|,
literal|"Failed to refresh id cache for child queries"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// the first scope level is the most nested child
for|for
control|(
name|ScopePhase
name|scopePhase
range|:
name|searchContext
operator|.
name|scopePhases
argument_list|()
control|)
block|{
if|if
condition|(
name|scopePhase
operator|instanceof
name|ScopePhase
operator|.
name|TopDocsPhase
condition|)
block|{
name|ScopePhase
operator|.
name|TopDocsPhase
name|topDocsPhase
init|=
operator|(
name|ScopePhase
operator|.
name|TopDocsPhase
operator|)
name|scopePhase
decl_stmt|;
name|int
name|numDocs
init|=
operator|(
name|searchContext
operator|.
name|from
argument_list|()
operator|+
name|searchContext
operator|.
name|size
argument_list|()
operator|)
decl_stmt|;
if|if
condition|(
name|numDocs
operator|==
literal|0
condition|)
block|{
name|numDocs
operator|=
literal|1
expr_stmt|;
block|}
try|try
block|{
name|numDocs
operator|*=
name|topDocsPhase
operator|.
name|factor
argument_list|()
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|topDocsPhase
operator|.
name|clear
argument_list|()
expr_stmt|;
if|if
condition|(
name|topDocsPhase
operator|.
name|scope
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|processingScope
argument_list|(
name|topDocsPhase
operator|.
name|scope
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|TopDocs
name|topDocs
init|=
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|topDocsPhase
operator|.
name|query
argument_list|()
argument_list|,
name|numDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|topDocsPhase
operator|.
name|scope
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// we mark the scope as processed, so we don't process it again, even if we need to rerun the query...
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|processedScope
argument_list|()
expr_stmt|;
block|}
name|topDocsPhase
operator|.
name|processResults
argument_list|(
name|topDocs
argument_list|,
name|searchContext
argument_list|)
expr_stmt|;
comment|// check if we found enough docs, if so, break
if|if
condition|(
name|topDocsPhase
operator|.
name|numHits
argument_list|()
operator|>=
operator|(
name|searchContext
operator|.
name|from
argument_list|()
operator|+
name|searchContext
operator|.
name|size
argument_list|()
operator|)
condition|)
block|{
break|break;
block|}
comment|// if we did not find enough docs, check if it make sense to search further
if|if
condition|(
name|topDocs
operator|.
name|totalHits
operator|<=
name|numDocs
condition|)
block|{
break|break;
block|}
comment|// if not, update numDocs, and search again
name|numDocs
operator|*=
name|topDocsPhase
operator|.
name|incrementalFactor
argument_list|()
expr_stmt|;
if|if
condition|(
name|numDocs
operator|>
name|topDocs
operator|.
name|totalHits
condition|)
block|{
name|numDocs
operator|=
name|topDocs
operator|.
name|totalHits
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryPhaseExecutionException
argument_list|(
name|searchContext
argument_list|,
literal|"Failed to execute child query ["
operator|+
name|scopePhase
operator|.
name|query
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|scopePhase
operator|instanceof
name|ScopePhase
operator|.
name|CollectorPhase
condition|)
block|{
try|try
block|{
name|ScopePhase
operator|.
name|CollectorPhase
name|collectorPhase
init|=
operator|(
name|ScopePhase
operator|.
name|CollectorPhase
operator|)
name|scopePhase
decl_stmt|;
comment|// collector phase might not require extra processing, for example, when scrolling
if|if
condition|(
operator|!
name|collectorPhase
operator|.
name|requiresProcessing
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|scopePhase
operator|.
name|scope
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|processingScope
argument_list|(
name|scopePhase
operator|.
name|scope
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Collector
name|collector
init|=
name|collectorPhase
operator|.
name|collector
argument_list|()
decl_stmt|;
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|collectorPhase
operator|.
name|query
argument_list|()
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|collectorPhase
operator|.
name|processCollector
argument_list|(
name|collector
argument_list|)
expr_stmt|;
if|if
condition|(
name|collectorPhase
operator|.
name|scope
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// we mark the scope as processed, so we don't process it again, even if we need to rerun the query...
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|processedScope
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryPhaseExecutionException
argument_list|(
name|searchContext
argument_list|,
literal|"Failed to execute child query ["
operator|+
name|scopePhase
operator|.
name|query
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
block|}
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|processingScope
argument_list|(
name|ContextIndexSearcher
operator|.
name|Scopes
operator|.
name|MAIN
argument_list|)
expr_stmt|;
try|try
block|{
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|from
argument_list|(
name|searchContext
operator|.
name|from
argument_list|()
argument_list|)
expr_stmt|;
name|searchContext
operator|.
name|queryResult
argument_list|()
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
name|TopDocs
name|topDocs
decl_stmt|;
name|int
name|numDocs
init|=
name|searchContext
operator|.
name|from
argument_list|()
operator|+
name|searchContext
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|numDocs
operator|==
literal|0
condition|)
block|{
comment|// if 0 was asked, change it to 1 since 0 is not allowed
name|numDocs
operator|=
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|searchContext
operator|.
name|searchType
argument_list|()
operator|==
name|SearchType
operator|.
name|COUNT
condition|)
block|{
name|TotalHitCountCollector
name|collector
init|=
operator|new
name|TotalHitCountCollector
argument_list|()
decl_stmt|;
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|collector
argument_list|)
expr_stmt|;
name|topDocs
operator|=
operator|new
name|TopDocs
argument_list|(
name|collector
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
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|searchContext
operator|.
name|searchType
argument_list|()
operator|==
name|SearchType
operator|.
name|SCAN
condition|)
block|{
name|topDocs
operator|=
name|searchContext
operator|.
name|scanContext
argument_list|()
operator|.
name|execute
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|topDocs
operator|=
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|null
argument_list|,
name|numDocs
argument_list|,
name|searchContext
operator|.
name|sort
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topDocs
operator|=
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
block|}
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|topDocs
argument_list|(
name|topDocs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
finally|finally
block|{
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|processedScope
argument_list|()
expr_stmt|;
block|}
name|facetPhase
operator|.
name|execute
argument_list|(
name|searchContext
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

