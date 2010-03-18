begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
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
name|index
operator|.
name|mapper
operator|.
name|DocumentMapper
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
name|facets
operator|.
name|FacetsPhase
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
name|util
operator|.
name|lucene
operator|.
name|search
operator|.
name|TermFilter
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|QueryPhase
specifier|public
class|class
name|QueryPhase
implements|implements
name|SearchPhase
block|{
DECL|field|facetsPhase
specifier|private
specifier|final
name|FacetsPhase
name|facetsPhase
decl_stmt|;
DECL|method|QueryPhase
annotation|@
name|Inject
specifier|public
name|QueryPhase
parameter_list|(
name|FacetsPhase
name|facetsPhase
parameter_list|)
block|{
name|this
operator|.
name|facetsPhase
operator|=
name|facetsPhase
expr_stmt|;
block|}
DECL|method|parseElements
annotation|@
name|Override
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
literal|"queryParserName"
argument_list|,
operator|new
name|QueryParserNameParseElement
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
literal|"sort"
argument_list|,
operator|new
name|SortParseElement
argument_list|()
argument_list|)
operator|.
name|putAll
argument_list|(
name|facetsPhase
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
DECL|method|preProcess
annotation|@
name|Override
specifier|public
name|void
name|preProcess
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|query
argument_list|()
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"No query specified in search request"
argument_list|)
throw|;
block|}
name|context
operator|.
name|query
argument_list|()
operator|.
name|setBoost
argument_list|(
name|context
operator|.
name|query
argument_list|()
operator|.
name|getBoost
argument_list|()
operator|*
name|context
operator|.
name|queryBoost
argument_list|()
argument_list|)
expr_stmt|;
name|facetsPhase
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
if|if
condition|(
name|searchContext
operator|.
name|types
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
if|if
condition|(
name|searchContext
operator|.
name|types
argument_list|()
operator|.
name|length
operator|==
literal|1
condition|)
block|{
name|String
name|type
init|=
name|searchContext
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|searchContext
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|Filter
name|typeFilter
init|=
operator|new
name|TermFilter
argument_list|(
name|docMapper
operator|.
name|typeMapper
argument_list|()
operator|.
name|term
argument_list|(
name|docMapper
operator|.
name|type
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|typeFilter
operator|=
name|searchContext
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|typeFilter
argument_list|)
expr_stmt|;
name|query
operator|=
operator|new
name|FilteredQuery
argument_list|(
name|query
argument_list|,
name|typeFilter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|BooleanFilter
name|booleanFilter
init|=
operator|new
name|BooleanFilter
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|type
range|:
name|searchContext
operator|.
name|types
argument_list|()
control|)
block|{
name|DocumentMapper
name|docMapper
init|=
name|searchContext
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|type
argument_list|)
decl_stmt|;
name|Filter
name|typeFilter
init|=
operator|new
name|TermFilter
argument_list|(
name|docMapper
operator|.
name|typeMapper
argument_list|()
operator|.
name|term
argument_list|(
name|docMapper
operator|.
name|type
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|typeFilter
operator|=
name|searchContext
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|typeFilter
argument_list|)
expr_stmt|;
name|booleanFilter
operator|.
name|add
argument_list|(
operator|new
name|FilterClause
argument_list|(
name|typeFilter
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|query
operator|=
operator|new
name|FilteredQuery
argument_list|(
name|query
argument_list|,
name|booleanFilter
argument_list|)
expr_stmt|;
block|}
block|}
name|TopDocs
name|topDocs
decl_stmt|;
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
name|searchContext
operator|.
name|from
argument_list|()
operator|+
name|searchContext
operator|.
name|size
argument_list|()
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
literal|""
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|facetsPhase
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

