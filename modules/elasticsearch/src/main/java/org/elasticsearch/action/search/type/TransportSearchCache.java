begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search.type
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|type
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchShardTarget
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
name|DfsSearchResult
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
name|QueryFetchSearchResult
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
name|QuerySearchResultProvider
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
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
name|ConcurrentHashMap
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
name|ConcurrentLinkedQueue
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|TransportSearchCache
specifier|public
class|class
name|TransportSearchCache
block|{
DECL|field|cacheDfsResults
specifier|private
specifier|final
name|Queue
argument_list|<
name|Collection
argument_list|<
name|DfsSearchResult
argument_list|>
argument_list|>
name|cacheDfsResults
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|Collection
argument_list|<
name|DfsSearchResult
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|cacheQueryResults
specifier|private
specifier|final
name|Queue
argument_list|<
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
argument_list|>
name|cacheQueryResults
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|cacheFetchResults
specifier|private
specifier|final
name|Queue
argument_list|<
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
argument_list|>
name|cacheFetchResults
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|cacheQueryFetchResults
specifier|private
specifier|final
name|Queue
argument_list|<
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QueryFetchSearchResult
argument_list|>
argument_list|>
name|cacheQueryFetchResults
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QueryFetchSearchResult
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|obtainDfsResults
specifier|public
name|Collection
argument_list|<
name|DfsSearchResult
argument_list|>
name|obtainDfsResults
parameter_list|()
block|{
name|Collection
argument_list|<
name|DfsSearchResult
argument_list|>
name|dfsSearchResults
decl_stmt|;
while|while
condition|(
operator|(
name|dfsSearchResults
operator|=
name|cacheDfsResults
operator|.
name|poll
argument_list|()
operator|)
operator|==
literal|null
condition|)
block|{
name|cacheDfsResults
operator|.
name|offer
argument_list|(
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|DfsSearchResult
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|dfsSearchResults
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|dfsSearchResults
return|;
block|}
DECL|method|releaseDfsResults
specifier|public
name|void
name|releaseDfsResults
parameter_list|(
name|Collection
argument_list|<
name|DfsSearchResult
argument_list|>
name|dfsResults
parameter_list|)
block|{
name|dfsResults
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cacheDfsResults
operator|.
name|offer
argument_list|(
name|dfsResults
argument_list|)
expr_stmt|;
block|}
DECL|method|obtainQueryResults
specifier|public
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
name|obtainQueryResults
parameter_list|()
block|{
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
name|queryResults
decl_stmt|;
while|while
condition|(
operator|(
name|queryResults
operator|=
name|cacheQueryResults
operator|.
name|poll
argument_list|()
operator|)
operator|==
literal|null
condition|)
block|{
name|cacheQueryResults
operator|.
name|offer
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|queryResults
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|queryResults
return|;
block|}
DECL|method|releaseQueryResults
specifier|public
name|void
name|releaseQueryResults
parameter_list|(
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QuerySearchResultProvider
argument_list|>
name|queryResults
parameter_list|)
block|{
name|queryResults
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cacheQueryResults
operator|.
name|offer
argument_list|(
name|queryResults
argument_list|)
expr_stmt|;
block|}
DECL|method|obtainFetchResults
specifier|public
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
name|obtainFetchResults
parameter_list|()
block|{
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
name|fetchResults
decl_stmt|;
while|while
condition|(
operator|(
name|fetchResults
operator|=
name|cacheFetchResults
operator|.
name|poll
argument_list|()
operator|)
operator|==
literal|null
condition|)
block|{
name|cacheFetchResults
operator|.
name|offer
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fetchResults
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|fetchResults
return|;
block|}
DECL|method|releaseFetchResults
specifier|public
name|void
name|releaseFetchResults
parameter_list|(
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|FetchSearchResult
argument_list|>
name|fetchResults
parameter_list|)
block|{
name|fetchResults
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cacheFetchResults
operator|.
name|offer
argument_list|(
name|fetchResults
argument_list|)
expr_stmt|;
block|}
DECL|method|obtainQueryFetchResults
specifier|public
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QueryFetchSearchResult
argument_list|>
name|obtainQueryFetchResults
parameter_list|()
block|{
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QueryFetchSearchResult
argument_list|>
name|fetchResults
decl_stmt|;
while|while
condition|(
operator|(
name|fetchResults
operator|=
name|cacheQueryFetchResults
operator|.
name|poll
argument_list|()
operator|)
operator|==
literal|null
condition|)
block|{
name|cacheQueryFetchResults
operator|.
name|offer
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<
name|SearchShardTarget
argument_list|,
name|QueryFetchSearchResult
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fetchResults
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|fetchResults
return|;
block|}
DECL|method|releaseQueryFetchResults
specifier|public
name|void
name|releaseQueryFetchResults
parameter_list|(
name|Map
argument_list|<
name|SearchShardTarget
argument_list|,
name|QueryFetchSearchResult
argument_list|>
name|fetchResults
parameter_list|)
block|{
name|fetchResults
operator|.
name|clear
argument_list|()
expr_stmt|;
name|cacheQueryFetchResults
operator|.
name|offer
argument_list|(
name|fetchResults
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

