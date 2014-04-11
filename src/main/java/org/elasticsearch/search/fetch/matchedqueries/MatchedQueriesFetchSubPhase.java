begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.matchedqueries
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|matchedqueries
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
name|DocIdSet
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
name|util
operator|.
name|Bits
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|docset
operator|.
name|DocIdSets
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
name|fetch
operator|.
name|FetchSubPhase
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
DECL|class|MatchedQueriesFetchSubPhase
specifier|public
class|class
name|MatchedQueriesFetchSubPhase
implements|implements
name|FetchSubPhase
block|{
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
DECL|method|hitsExecutionNeeded
specifier|public
name|boolean
name|hitsExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|hitsExecute
specifier|public
name|void
name|hitsExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|InternalSearchHit
index|[]
name|hits
parameter_list|)
throws|throws
name|ElasticsearchException
block|{     }
annotation|@
name|Override
DECL|method|hitExecutionNeeded
specifier|public
name|boolean
name|hitExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
operator|!
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|namedFilters
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|||
operator|(
name|context
operator|.
name|parsedPostFilter
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|context
operator|.
name|parsedPostFilter
argument_list|()
operator|.
name|namedFilters
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|hitExecute
specifier|public
name|void
name|hitExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|matchedQueries
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|addMatchedQueries
argument_list|(
name|hitContext
argument_list|,
name|context
operator|.
name|parsedQuery
argument_list|()
operator|.
name|namedFilters
argument_list|()
argument_list|,
name|matchedQueries
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|parsedPostFilter
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|addMatchedQueries
argument_list|(
name|hitContext
argument_list|,
name|context
operator|.
name|parsedPostFilter
argument_list|()
operator|.
name|namedFilters
argument_list|()
argument_list|,
name|matchedQueries
argument_list|)
expr_stmt|;
block|}
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|matchedQueries
argument_list|(
name|matchedQueries
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|matchedQueries
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|addMatchedQueries
specifier|private
name|void
name|addMatchedQueries
parameter_list|(
name|HitContext
name|hitContext
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Filter
argument_list|>
name|namedFiltersAndQueries
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|matchedQueries
parameter_list|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Filter
argument_list|>
name|entry
range|:
name|namedFiltersAndQueries
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Filter
name|filter
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
try|try
block|{
name|DocIdSet
name|docIdSet
init|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|hitContext
operator|.
name|readerContext
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// null is fine, since we filter by hitContext.docId()
if|if
condition|(
operator|!
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
name|Bits
name|bits
init|=
name|docIdSet
operator|.
name|bits
argument_list|()
decl_stmt|;
if|if
condition|(
name|bits
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|bits
operator|.
name|get
argument_list|(
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|)
condition|)
block|{
name|matchedQueries
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|DocIdSetIterator
name|iterator
init|=
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|iterator
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|iterator
operator|.
name|advance
argument_list|(
name|hitContext
operator|.
name|docId
argument_list|()
argument_list|)
operator|==
name|hitContext
operator|.
name|docId
argument_list|()
condition|)
block|{
name|matchedQueries
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
finally|finally
block|{
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|clearReleasables
argument_list|(
name|Lifetime
operator|.
name|COLLECTION
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

