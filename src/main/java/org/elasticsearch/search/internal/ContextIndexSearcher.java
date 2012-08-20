begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
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
name|ImmutableList
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
name|Lists
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
name|Maps
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
name|ExtendedIndexSearcher
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
name|IndexReader
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
name|*
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
name|MultiCollector
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
name|AndFilter
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
name|index
operator|.
name|engine
operator|.
name|Engine
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
name|dfs
operator|.
name|CachedDfSource
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
DECL|class|ContextIndexSearcher
specifier|public
class|class
name|ContextIndexSearcher
extends|extends
name|ExtendedIndexSearcher
block|{
DECL|class|Scopes
specifier|public
specifier|static
specifier|final
class|class
name|Scopes
block|{
DECL|field|MAIN
specifier|public
specifier|static
specifier|final
name|String
name|MAIN
init|=
literal|"_main_"
decl_stmt|;
DECL|field|GLOBAL
specifier|public
specifier|static
specifier|final
name|String
name|GLOBAL
init|=
literal|"_global_"
decl_stmt|;
DECL|field|NA
specifier|public
specifier|static
specifier|final
name|String
name|NA
init|=
literal|"_na_"
decl_stmt|;
block|}
DECL|field|searchContext
specifier|private
specifier|final
name|SearchContext
name|searchContext
decl_stmt|;
DECL|field|reader
specifier|private
specifier|final
name|IndexReader
name|reader
decl_stmt|;
DECL|field|dfSource
specifier|private
name|CachedDfSource
name|dfSource
decl_stmt|;
DECL|field|scopeCollectors
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Collector
argument_list|>
argument_list|>
name|scopeCollectors
decl_stmt|;
DECL|field|processingScope
specifier|private
name|String
name|processingScope
decl_stmt|;
DECL|method|ContextIndexSearcher
specifier|public
name|ContextIndexSearcher
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|,
name|Engine
operator|.
name|Searcher
name|searcher
parameter_list|)
block|{
name|super
argument_list|(
name|searcher
operator|.
name|searcher
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchContext
operator|=
name|searchContext
expr_stmt|;
name|this
operator|.
name|reader
operator|=
name|searcher
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
expr_stmt|;
block|}
DECL|method|dfSource
specifier|public
name|void
name|dfSource
parameter_list|(
name|CachedDfSource
name|dfSource
parameter_list|)
block|{
name|this
operator|.
name|dfSource
operator|=
name|dfSource
expr_stmt|;
block|}
DECL|method|addCollector
specifier|public
name|void
name|addCollector
parameter_list|(
name|String
name|scope
parameter_list|,
name|Collector
name|collector
parameter_list|)
block|{
if|if
condition|(
name|scopeCollectors
operator|==
literal|null
condition|)
block|{
name|scopeCollectors
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|Collector
argument_list|>
name|collectors
init|=
name|scopeCollectors
operator|.
name|get
argument_list|(
name|scope
argument_list|)
decl_stmt|;
if|if
condition|(
name|collectors
operator|==
literal|null
condition|)
block|{
name|collectors
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|scopeCollectors
operator|.
name|put
argument_list|(
name|scope
argument_list|,
name|collectors
argument_list|)
expr_stmt|;
block|}
name|collectors
operator|.
name|add
argument_list|(
name|collector
argument_list|)
expr_stmt|;
block|}
DECL|method|removeCollectors
specifier|public
name|List
argument_list|<
name|Collector
argument_list|>
name|removeCollectors
parameter_list|(
name|String
name|scope
parameter_list|)
block|{
if|if
condition|(
name|scopeCollectors
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|scopeCollectors
operator|.
name|remove
argument_list|(
name|scope
argument_list|)
return|;
block|}
DECL|method|hasCollectors
specifier|public
name|boolean
name|hasCollectors
parameter_list|(
name|String
name|scope
parameter_list|)
block|{
if|if
condition|(
name|scopeCollectors
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|!
name|scopeCollectors
operator|.
name|containsKey
argument_list|(
name|scope
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
operator|!
name|scopeCollectors
operator|.
name|get
argument_list|(
name|scope
argument_list|)
operator|.
name|isEmpty
argument_list|()
return|;
block|}
DECL|method|processingScope
specifier|public
name|void
name|processingScope
parameter_list|(
name|String
name|scope
parameter_list|)
block|{
name|this
operator|.
name|processingScope
operator|=
name|scope
expr_stmt|;
block|}
DECL|method|processedScope
specifier|public
name|void
name|processedScope
parameter_list|()
block|{
comment|// clean the current scope (we processed it, also handles scrolling since we don't want to
comment|// do it again)
if|if
condition|(
name|scopeCollectors
operator|!=
literal|null
condition|)
block|{
name|scopeCollectors
operator|.
name|remove
argument_list|(
name|processingScope
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|processingScope
operator|=
name|Scopes
operator|.
name|NA
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|Query
name|original
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|original
operator|==
name|searchContext
operator|.
name|query
argument_list|()
operator|||
name|original
operator|==
name|searchContext
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
condition|)
block|{
comment|// optimize in case its the top level search query and we already rewrote it...
if|if
condition|(
name|searchContext
operator|.
name|queryRewritten
argument_list|()
condition|)
block|{
return|return
name|searchContext
operator|.
name|query
argument_list|()
return|;
block|}
name|Query
name|rewriteQuery
init|=
name|super
operator|.
name|rewrite
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|searchContext
operator|.
name|updateRewriteQuery
argument_list|(
name|rewriteQuery
argument_list|)
expr_stmt|;
return|return
name|rewriteQuery
return|;
block|}
else|else
block|{
return|return
name|super
operator|.
name|rewrite
argument_list|(
name|original
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|createNormalizedWeight
specifier|public
name|Weight
name|createNormalizedWeight
parameter_list|(
name|Query
name|query
parameter_list|)
throws|throws
name|IOException
block|{
comment|// if its the main query, use we have dfs data, only then do it
if|if
condition|(
name|dfSource
operator|!=
literal|null
operator|&&
operator|(
name|query
operator|==
name|searchContext
operator|.
name|query
argument_list|()
operator|||
name|query
operator|==
name|searchContext
operator|.
name|parsedQuery
argument_list|()
operator|.
name|query
argument_list|()
operator|)
condition|)
block|{
return|return
name|dfSource
operator|.
name|createNormalizedWeight
argument_list|(
name|query
argument_list|)
return|;
block|}
return|return
name|super
operator|.
name|createNormalizedWeight
argument_list|(
name|query
argument_list|)
return|;
block|}
comment|// override from the Searcher to allow to control if scores will be tracked or not
comment|// LUCENE MONITOR - We override the logic here to apply our own flags for track scores
annotation|@
name|Override
DECL|method|search
specifier|public
name|TopFieldDocs
name|search
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|int
name|nDocs
parameter_list|,
name|Sort
name|sort
parameter_list|,
name|boolean
name|fillFields
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|limit
init|=
name|reader
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
if|if
condition|(
name|limit
operator|==
literal|0
condition|)
block|{
name|limit
operator|=
literal|1
expr_stmt|;
block|}
name|nDocs
operator|=
name|Math
operator|.
name|min
argument_list|(
name|nDocs
argument_list|,
name|limit
argument_list|)
expr_stmt|;
name|TopFieldCollector
name|collector
init|=
name|TopFieldCollector
operator|.
name|create
argument_list|(
name|sort
argument_list|,
name|nDocs
argument_list|,
name|fillFields
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
argument_list|,
operator|!
name|weight
operator|.
name|scoresDocsOutOfOrder
argument_list|()
argument_list|)
decl_stmt|;
name|search
argument_list|(
name|weight
argument_list|,
name|filter
argument_list|,
name|collector
argument_list|)
expr_stmt|;
return|return
operator|(
name|TopFieldDocs
operator|)
name|collector
operator|.
name|topDocs
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|public
name|void
name|search
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|Filter
name|filter
parameter_list|,
name|Collector
name|collector
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|searchContext
operator|.
name|parsedFilter
argument_list|()
operator|!=
literal|null
operator|&&
name|Scopes
operator|.
name|MAIN
operator|.
name|equals
argument_list|(
name|processingScope
argument_list|)
condition|)
block|{
comment|// this will only get applied to the actual search collector and not
comment|// to any scoped collectors, also, it will only be applied to the main collector
comment|// since that is where the filter should only work
name|collector
operator|=
operator|new
name|FilteredCollector
argument_list|(
name|collector
argument_list|,
name|searchContext
operator|.
name|parsedFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchContext
operator|.
name|timeoutInMillis
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// TODO: change to use our own counter that uses the scheduler in ThreadPool
name|collector
operator|=
operator|new
name|TimeLimitingCollector
argument_list|(
name|collector
argument_list|,
name|TimeLimitingCollector
operator|.
name|getGlobalCounter
argument_list|()
argument_list|,
name|searchContext
operator|.
name|timeoutInMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scopeCollectors
operator|!=
literal|null
condition|)
block|{
name|List
argument_list|<
name|Collector
argument_list|>
name|collectors
init|=
name|scopeCollectors
operator|.
name|get
argument_list|(
name|processingScope
argument_list|)
decl_stmt|;
if|if
condition|(
name|collectors
operator|!=
literal|null
operator|&&
operator|!
name|collectors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|collector
operator|=
operator|new
name|MultiCollector
argument_list|(
name|collector
argument_list|,
name|collectors
operator|.
name|toArray
argument_list|(
operator|new
name|Collector
index|[
name|collectors
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// apply the minimum score after multi collector so we filter facets as well
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
block|}
name|Filter
name|combinedFilter
decl_stmt|;
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
name|combinedFilter
operator|=
name|searchContext
operator|.
name|aliasFilter
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|searchContext
operator|.
name|aliasFilter
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|combinedFilter
operator|=
operator|new
name|AndFilter
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|filter
argument_list|,
name|searchContext
operator|.
name|aliasFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|combinedFilter
operator|=
name|filter
expr_stmt|;
block|}
block|}
comment|// we only compute the doc id set once since within a context, we execute the same query always...
if|if
condition|(
name|searchContext
operator|.
name|timeoutInMillis
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
try|try
block|{
name|super
operator|.
name|search
argument_list|(
name|weight
argument_list|,
name|combinedFilter
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TimeLimitingCollector
operator|.
name|TimeExceededException
name|e
parameter_list|)
block|{
name|searchContext
operator|.
name|queryResult
argument_list|()
operator|.
name|searchTimedOut
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|super
operator|.
name|search
argument_list|(
name|weight
argument_list|,
name|combinedFilter
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|Explanation
name|explain
parameter_list|(
name|Query
name|query
parameter_list|,
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|searchContext
operator|.
name|aliasFilter
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|explain
argument_list|(
name|query
argument_list|,
name|doc
argument_list|)
return|;
block|}
name|FilteredQuery
name|filteredQuery
init|=
operator|new
name|FilteredQuery
argument_list|(
name|query
argument_list|,
name|searchContext
operator|.
name|aliasFilter
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|super
operator|.
name|explain
argument_list|(
name|filteredQuery
argument_list|,
name|doc
argument_list|)
return|;
block|}
block|}
end_class

end_unit

