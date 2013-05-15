begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.dfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|dfs
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
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|hash
operator|.
name|THashSet
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
name|index
operator|.
name|TermContext
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
name|CollectionStatistics
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
name|TermStatistics
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
name|collect
operator|.
name|XMaps
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
name|internal
operator|.
name|SearchContext
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
DECL|class|DfsPhase
specifier|public
class|class
name|DfsPhase
implements|implements
name|SearchPhase
block|{
DECL|field|cachedTermsSet
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|THashSet
argument_list|<
name|Term
argument_list|>
argument_list|>
name|cachedTermsSet
init|=
operator|new
name|ThreadLocal
argument_list|<
name|THashSet
argument_list|<
name|Term
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|THashSet
argument_list|<
name|Term
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|THashSet
argument_list|<
name|Term
argument_list|>
argument_list|()
return|;
block|}
block|}
decl_stmt|;
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
return|return
name|ImmutableMap
operator|.
name|of
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
block|{     }
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
name|THashSet
argument_list|<
name|Term
argument_list|>
name|termsSet
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
operator|!
name|context
operator|.
name|queryRewritten
argument_list|()
condition|)
block|{
name|context
operator|.
name|updateRewriteQuery
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|rewrite
argument_list|(
name|context
operator|.
name|query
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|termsSet
operator|=
name|cachedTermsSet
operator|.
name|get
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|termsSet
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|termsSet
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|context
operator|.
name|query
argument_list|()
operator|.
name|extractTerms
argument_list|(
name|termsSet
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|rescore
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|context
operator|.
name|rescore
argument_list|()
operator|.
name|rescorer
argument_list|()
operator|.
name|extractTerms
argument_list|(
name|context
argument_list|,
name|context
operator|.
name|rescore
argument_list|()
argument_list|,
name|termsSet
argument_list|)
expr_stmt|;
block|}
name|Term
index|[]
name|terms
init|=
name|termsSet
operator|.
name|toArray
argument_list|(
operator|new
name|Term
index|[
name|termsSet
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|TermStatistics
index|[]
name|termStatistics
init|=
operator|new
name|TermStatistics
index|[
name|terms
operator|.
name|length
index|]
decl_stmt|;
name|IndexReaderContext
name|indexReaderContext
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getTopReaderContext
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
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// LUCENE 4 UPGRADE: cache TermContext?
name|TermContext
name|termContext
init|=
name|TermContext
operator|.
name|build
argument_list|(
name|indexReaderContext
argument_list|,
name|terms
index|[
name|i
index|]
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|termStatistics
index|[
name|i
index|]
operator|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|termStatistics
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|termContext
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|CollectionStatistics
argument_list|>
name|fieldStatistics
init|=
name|XMaps
operator|.
name|newNoNullKeysMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Term
name|term
range|:
name|terms
control|)
block|{
assert|assert
name|term
operator|.
name|field
argument_list|()
operator|!=
literal|null
operator|:
literal|"field is null"
assert|;
if|if
condition|(
operator|!
name|fieldStatistics
operator|.
name|containsKey
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
condition|)
block|{
specifier|final
name|CollectionStatistics
name|collectionStatistics
init|=
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|collectionStatistics
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
name|fieldStatistics
operator|.
name|put
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|,
name|collectionStatistics
argument_list|)
expr_stmt|;
block|}
block|}
name|context
operator|.
name|dfsResult
argument_list|()
operator|.
name|termsStatistics
argument_list|(
name|terms
argument_list|,
name|termStatistics
argument_list|)
operator|.
name|fieldStatistics
argument_list|(
name|fieldStatistics
argument_list|)
operator|.
name|maxDoc
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|maxDoc
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
name|DfsPhaseExecutionException
argument_list|(
name|context
argument_list|,
literal|"Exception during dfs phase"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|termsSet
operator|!=
literal|null
condition|)
block|{
name|termsSet
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// don't hold on to terms
block|}
block|}
block|}
block|}
end_class

end_unit

