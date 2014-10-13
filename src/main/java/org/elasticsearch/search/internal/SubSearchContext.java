begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|util
operator|.
name|Counter
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
name|index
operator|.
name|query
operator|.
name|ParsedFilter
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
name|Scroll
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
name|SearchContextAggregations
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
name|fetch
operator|.
name|fielddata
operator|.
name|FieldDataFieldsContext
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
name|innerhits
operator|.
name|InnerHitsContext
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
name|script
operator|.
name|ScriptFieldsContext
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
name|source
operator|.
name|FetchSourceContext
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
name|highlight
operator|.
name|SearchContextHighlight
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
name|lookup
operator|.
name|SearchLookup
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
name|QuerySearchResult
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
name|suggest
operator|.
name|SuggestionSearchContext
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SubSearchContext
specifier|public
class|class
name|SubSearchContext
extends|extends
name|FilteredSearchContext
block|{
comment|// By default return 3 hits per bucket. A higher default would make the response really large by default, since
comment|// the to hits are returned per bucket.
DECL|field|DEFAULT_SIZE
specifier|private
specifier|final
specifier|static
name|int
name|DEFAULT_SIZE
init|=
literal|3
decl_stmt|;
DECL|field|from
specifier|private
name|int
name|from
decl_stmt|;
DECL|field|size
specifier|private
name|int
name|size
init|=
name|DEFAULT_SIZE
decl_stmt|;
DECL|field|sort
specifier|private
name|Sort
name|sort
decl_stmt|;
DECL|field|fetchSearchResult
specifier|private
specifier|final
name|FetchSearchResult
name|fetchSearchResult
decl_stmt|;
DECL|field|querySearchResult
specifier|private
specifier|final
name|QuerySearchResult
name|querySearchResult
decl_stmt|;
DECL|field|docIdsToLoad
specifier|private
name|int
index|[]
name|docIdsToLoad
decl_stmt|;
DECL|field|docsIdsToLoadFrom
specifier|private
name|int
name|docsIdsToLoadFrom
decl_stmt|;
DECL|field|docsIdsToLoadSize
specifier|private
name|int
name|docsIdsToLoadSize
decl_stmt|;
DECL|field|fieldNames
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
decl_stmt|;
DECL|field|fieldDataFields
specifier|private
name|FieldDataFieldsContext
name|fieldDataFields
decl_stmt|;
DECL|field|scriptFields
specifier|private
name|ScriptFieldsContext
name|scriptFields
decl_stmt|;
DECL|field|fetchSourceContext
specifier|private
name|FetchSourceContext
name|fetchSourceContext
decl_stmt|;
DECL|field|highlight
specifier|private
name|SearchContextHighlight
name|highlight
decl_stmt|;
DECL|field|explain
specifier|private
name|boolean
name|explain
decl_stmt|;
DECL|field|trackScores
specifier|private
name|boolean
name|trackScores
decl_stmt|;
DECL|field|version
specifier|private
name|boolean
name|version
decl_stmt|;
DECL|method|SubSearchContext
specifier|public
name|SubSearchContext
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|this
operator|.
name|fetchSearchResult
operator|=
operator|new
name|FetchSearchResult
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySearchResult
operator|=
operator|new
name|QuerySearchResult
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|preProcess
specifier|public
name|void
name|preProcess
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|searchFilter
specifier|public
name|Filter
name|searchFilter
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this context should be read only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|searchType
specifier|public
name|SearchContext
name|searchType
parameter_list|(
name|SearchType
name|searchType
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this context should be read only"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|queryBoost
specifier|public
name|SearchContext
name|queryBoost
parameter_list|(
name|float
name|queryBoost
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|scroll
specifier|public
name|SearchContext
name|scroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|aggregations
specifier|public
name|SearchContext
name|aggregations
parameter_list|(
name|SearchContextAggregations
name|aggregations
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
DECL|method|highlight
specifier|public
name|SearchContextHighlight
name|highlight
parameter_list|()
block|{
return|return
name|highlight
return|;
block|}
DECL|method|highlight
specifier|public
name|void
name|highlight
parameter_list|(
name|SearchContextHighlight
name|highlight
parameter_list|)
block|{
name|this
operator|.
name|highlight
operator|=
name|highlight
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|suggest
specifier|public
name|void
name|suggest
parameter_list|(
name|SuggestionSearchContext
name|suggest
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|addRescore
specifier|public
name|void
name|addRescore
parameter_list|(
name|RescoreSearchContext
name|rescore
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|hasFieldDataFields
specifier|public
name|boolean
name|hasFieldDataFields
parameter_list|()
block|{
return|return
name|fieldDataFields
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|fieldDataFields
specifier|public
name|FieldDataFieldsContext
name|fieldDataFields
parameter_list|()
block|{
if|if
condition|(
name|fieldDataFields
operator|==
literal|null
condition|)
block|{
name|fieldDataFields
operator|=
operator|new
name|FieldDataFieldsContext
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|fieldDataFields
return|;
block|}
annotation|@
name|Override
DECL|method|hasScriptFields
specifier|public
name|boolean
name|hasScriptFields
parameter_list|()
block|{
return|return
name|scriptFields
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|scriptFields
specifier|public
name|ScriptFieldsContext
name|scriptFields
parameter_list|()
block|{
if|if
condition|(
name|scriptFields
operator|==
literal|null
condition|)
block|{
name|scriptFields
operator|=
operator|new
name|ScriptFieldsContext
argument_list|()
expr_stmt|;
block|}
return|return
name|this
operator|.
name|scriptFields
return|;
block|}
annotation|@
name|Override
DECL|method|sourceRequested
specifier|public
name|boolean
name|sourceRequested
parameter_list|()
block|{
return|return
name|fetchSourceContext
operator|!=
literal|null
operator|&&
name|fetchSourceContext
operator|.
name|fetchSource
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|hasFetchSourceContext
specifier|public
name|boolean
name|hasFetchSourceContext
parameter_list|()
block|{
return|return
name|fetchSourceContext
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|fetchSourceContext
specifier|public
name|FetchSourceContext
name|fetchSourceContext
parameter_list|()
block|{
return|return
name|fetchSourceContext
return|;
block|}
annotation|@
name|Override
DECL|method|fetchSourceContext
specifier|public
name|SearchContext
name|fetchSourceContext
parameter_list|(
name|FetchSourceContext
name|fetchSourceContext
parameter_list|)
block|{
name|this
operator|.
name|fetchSourceContext
operator|=
name|fetchSourceContext
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|timeoutInMillis
specifier|public
name|void
name|timeoutInMillis
parameter_list|(
name|long
name|timeoutInMillis
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|terminateAfter
specifier|public
name|void
name|terminateAfter
parameter_list|(
name|int
name|terminateAfter
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|minimumScore
specifier|public
name|SearchContext
name|minimumScore
parameter_list|(
name|float
name|minimumScore
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|sort
specifier|public
name|SearchContext
name|sort
parameter_list|(
name|Sort
name|sort
parameter_list|)
block|{
name|this
operator|.
name|sort
operator|=
name|sort
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|sort
specifier|public
name|Sort
name|sort
parameter_list|()
block|{
return|return
name|sort
return|;
block|}
annotation|@
name|Override
DECL|method|trackScores
specifier|public
name|SearchContext
name|trackScores
parameter_list|(
name|boolean
name|trackScores
parameter_list|)
block|{
name|this
operator|.
name|trackScores
operator|=
name|trackScores
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|trackScores
specifier|public
name|boolean
name|trackScores
parameter_list|()
block|{
return|return
name|trackScores
return|;
block|}
annotation|@
name|Override
DECL|method|parsedPostFilter
specifier|public
name|SearchContext
name|parsedPostFilter
parameter_list|(
name|ParsedFilter
name|postFilter
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|updateRewriteQuery
specifier|public
name|SearchContext
name|updateRewriteQuery
parameter_list|(
name|Query
name|rewriteQuery
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|from
specifier|public
name|int
name|from
parameter_list|()
block|{
return|return
name|from
return|;
block|}
annotation|@
name|Override
DECL|method|from
specifier|public
name|SearchContext
name|from
parameter_list|(
name|int
name|from
parameter_list|)
block|{
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
annotation|@
name|Override
DECL|method|size
specifier|public
name|SearchContext
name|size
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|hasFieldNames
specifier|public
name|boolean
name|hasFieldNames
parameter_list|()
block|{
return|return
name|fieldNames
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|fieldNames
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|fieldNames
parameter_list|()
block|{
if|if
condition|(
name|fieldNames
operator|==
literal|null
condition|)
block|{
name|fieldNames
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
return|return
name|fieldNames
return|;
block|}
annotation|@
name|Override
DECL|method|emptyFieldNames
specifier|public
name|void
name|emptyFieldNames
parameter_list|()
block|{
name|this
operator|.
name|fieldNames
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|boolean
name|explain
parameter_list|()
block|{
return|return
name|explain
return|;
block|}
annotation|@
name|Override
DECL|method|explain
specifier|public
name|void
name|explain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|this
operator|.
name|explain
operator|=
name|explain
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|groupStats
specifier|public
name|void
name|groupStats
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|groupStats
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|version
specifier|public
name|boolean
name|version
parameter_list|()
block|{
return|return
name|version
return|;
block|}
annotation|@
name|Override
DECL|method|version
specifier|public
name|void
name|version
parameter_list|(
name|boolean
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoad
specifier|public
name|int
index|[]
name|docIdsToLoad
parameter_list|()
block|{
return|return
name|docIdsToLoad
return|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoadFrom
specifier|public
name|int
name|docIdsToLoadFrom
parameter_list|()
block|{
return|return
name|docsIdsToLoadFrom
return|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoadSize
specifier|public
name|int
name|docIdsToLoadSize
parameter_list|()
block|{
return|return
name|docsIdsToLoadSize
return|;
block|}
annotation|@
name|Override
DECL|method|docIdsToLoad
specifier|public
name|SearchContext
name|docIdsToLoad
parameter_list|(
name|int
index|[]
name|docIdsToLoad
parameter_list|,
name|int
name|docsIdsToLoadFrom
parameter_list|,
name|int
name|docsIdsToLoadSize
parameter_list|)
block|{
name|this
operator|.
name|docIdsToLoad
operator|=
name|docIdsToLoad
expr_stmt|;
name|this
operator|.
name|docsIdsToLoadFrom
operator|=
name|docsIdsToLoadFrom
expr_stmt|;
name|this
operator|.
name|docsIdsToLoadSize
operator|=
name|docsIdsToLoadSize
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|accessed
specifier|public
name|void
name|accessed
parameter_list|(
name|long
name|accessTime
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|keepAlive
specifier|public
name|void
name|keepAlive
parameter_list|(
name|long
name|keepAlive
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|lastEmittedDoc
specifier|public
name|void
name|lastEmittedDoc
parameter_list|(
name|ScoreDoc
name|doc
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|queryResult
specifier|public
name|QuerySearchResult
name|queryResult
parameter_list|()
block|{
return|return
name|querySearchResult
return|;
block|}
annotation|@
name|Override
DECL|method|fetchResult
specifier|public
name|FetchSearchResult
name|fetchResult
parameter_list|()
block|{
return|return
name|fetchSearchResult
return|;
block|}
DECL|field|searchLookup
specifier|private
name|SearchLookup
name|searchLookup
decl_stmt|;
annotation|@
name|Override
DECL|method|lookup
specifier|public
name|SearchLookup
name|lookup
parameter_list|()
block|{
if|if
condition|(
name|searchLookup
operator|==
literal|null
condition|)
block|{
name|searchLookup
operator|=
operator|new
name|SearchLookup
argument_list|(
name|mapperService
argument_list|()
argument_list|,
name|fieldData
argument_list|()
argument_list|,
name|request
argument_list|()
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|searchLookup
return|;
block|}
annotation|@
name|Override
DECL|method|useSlowScroll
specifier|public
name|SearchContext
name|useSlowScroll
parameter_list|(
name|boolean
name|useSlowScroll
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|timeEstimateCounter
specifier|public
name|Counter
name|timeEstimateCounter
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Not supported"
argument_list|)
throw|;
block|}
DECL|field|innerHitsContext
specifier|private
name|InnerHitsContext
name|innerHitsContext
decl_stmt|;
annotation|@
name|Override
DECL|method|innerHits
specifier|public
name|void
name|innerHits
parameter_list|(
name|InnerHitsContext
name|innerHitsContext
parameter_list|)
block|{
name|this
operator|.
name|innerHitsContext
operator|=
name|innerHitsContext
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerHits
specifier|public
name|InnerHitsContext
name|innerHits
parameter_list|()
block|{
return|return
name|innerHitsContext
return|;
block|}
block|}
end_class

end_unit

