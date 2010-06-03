begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|search
operator|.
name|dfs
operator|.
name|CachedDfSource
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
name|collect
operator|.
name|Lists
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
name|MultiCollector
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ContextIndexSearcher
specifier|public
class|class
name|ContextIndexSearcher
extends|extends
name|IndexSearcher
block|{
DECL|field|searchContext
specifier|private
name|SearchContext
name|searchContext
decl_stmt|;
DECL|field|dfSource
specifier|private
name|CachedDfSource
name|dfSource
decl_stmt|;
DECL|field|collectors
specifier|private
name|List
argument_list|<
name|Collector
argument_list|>
name|collectors
decl_stmt|;
DECL|field|globalCollectors
specifier|private
name|List
argument_list|<
name|Collector
argument_list|>
name|globalCollectors
decl_stmt|;
DECL|field|useGlobalCollectors
specifier|private
name|boolean
name|useGlobalCollectors
init|=
literal|false
decl_stmt|;
DECL|method|ContextIndexSearcher
specifier|public
name|ContextIndexSearcher
parameter_list|(
name|SearchContext
name|searchContext
parameter_list|,
name|IndexReader
name|r
parameter_list|)
block|{
name|super
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchContext
operator|=
name|searchContext
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
name|Collector
name|collector
parameter_list|)
block|{
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
block|}
name|collectors
operator|.
name|add
argument_list|(
name|collector
argument_list|)
expr_stmt|;
block|}
DECL|method|collectors
specifier|public
name|List
argument_list|<
name|Collector
argument_list|>
name|collectors
parameter_list|()
block|{
return|return
name|collectors
return|;
block|}
DECL|method|addGlobalCollector
specifier|public
name|void
name|addGlobalCollector
parameter_list|(
name|Collector
name|collector
parameter_list|)
block|{
if|if
condition|(
name|globalCollectors
operator|==
literal|null
condition|)
block|{
name|globalCollectors
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|globalCollectors
operator|.
name|add
argument_list|(
name|collector
argument_list|)
expr_stmt|;
block|}
DECL|method|globalCollectors
specifier|public
name|List
argument_list|<
name|Collector
argument_list|>
name|globalCollectors
parameter_list|()
block|{
return|return
name|globalCollectors
return|;
block|}
DECL|method|useGlobalCollectors
specifier|public
name|void
name|useGlobalCollectors
parameter_list|(
name|boolean
name|useGlobalCollectors
parameter_list|)
block|{
name|this
operator|.
name|useGlobalCollectors
operator|=
name|useGlobalCollectors
expr_stmt|;
block|}
DECL|method|rewrite
annotation|@
name|Override
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
name|originalQuery
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
DECL|method|createWeight
annotation|@
name|Override
specifier|protected
name|Weight
name|createWeight
parameter_list|(
name|Query
name|query
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|dfSource
operator|==
literal|null
condition|)
block|{
return|return
name|super
operator|.
name|createWeight
argument_list|(
name|query
argument_list|)
return|;
block|}
return|return
name|query
operator|.
name|weight
argument_list|(
name|dfSource
argument_list|)
return|;
block|}
DECL|method|search
annotation|@
name|Override
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
name|timeout
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|collector
operator|=
operator|new
name|TimeLimitingCollector
argument_list|(
name|collector
argument_list|,
name|searchContext
operator|.
name|timeout
argument_list|()
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|useGlobalCollectors
condition|)
block|{
if|if
condition|(
name|globalCollectors
operator|!=
literal|null
condition|)
block|{
name|collector
operator|=
operator|new
name|MultiCollector
argument_list|(
name|collector
argument_list|,
name|globalCollectors
operator|.
name|toArray
argument_list|(
operator|new
name|Collector
index|[
name|globalCollectors
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|collectors
operator|!=
literal|null
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
comment|// we only compute the doc id set once since within a context, we execute the same query always...
if|if
condition|(
name|searchContext
operator|.
name|timeout
argument_list|()
operator|!=
literal|null
condition|)
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
try|try
block|{
name|super
operator|.
name|search
argument_list|(
name|weight
argument_list|,
name|filter
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
name|filter
argument_list|,
name|collector
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

