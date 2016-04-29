begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|action
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
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
name|ActionListenerResponseHandler
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
name|IndicesRequest
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
name|OriginalIndices
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
name|SearchRequest
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
name|support
operator|.
name|IndicesOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|component
operator|.
name|AbstractComponent
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|fetch
operator|.
name|ScrollQueryFetchSearchResult
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
name|ShardFetchRequest
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
name|ShardFetchSearchRequest
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
name|InternalScrollSearchRequest
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
name|ShardSearchTransportRequest
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
name|QuerySearchRequest
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
name|query
operator|.
name|QuerySearchResultProvider
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
name|ScrollQuerySearchResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequestHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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

begin_comment
comment|/**  * An encapsulation of {@link org.elasticsearch.search.SearchService} operations exposed through  * transport.  */
end_comment

begin_class
DECL|class|SearchTransportService
specifier|public
class|class
name|SearchTransportService
extends|extends
name|AbstractComponent
block|{
DECL|field|FREE_CONTEXT_SCROLL_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|FREE_CONTEXT_SCROLL_ACTION_NAME
init|=
literal|"indices:data/read/search[free_context/scroll]"
decl_stmt|;
DECL|field|FREE_CONTEXT_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|FREE_CONTEXT_ACTION_NAME
init|=
literal|"indices:data/read/search[free_context]"
decl_stmt|;
DECL|field|CLEAR_SCROLL_CONTEXTS_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|CLEAR_SCROLL_CONTEXTS_ACTION_NAME
init|=
literal|"indices:data/read/search[clear_scroll_contexts]"
decl_stmt|;
DECL|field|DFS_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|DFS_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/dfs]"
decl_stmt|;
DECL|field|QUERY_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/query]"
decl_stmt|;
DECL|field|QUERY_ID_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_ID_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/query/id]"
decl_stmt|;
DECL|field|QUERY_SCROLL_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_SCROLL_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/query/scroll]"
decl_stmt|;
DECL|field|QUERY_FETCH_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_FETCH_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/query+fetch]"
decl_stmt|;
DECL|field|QUERY_QUERY_FETCH_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_QUERY_FETCH_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/query/query+fetch]"
decl_stmt|;
DECL|field|QUERY_FETCH_SCROLL_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|QUERY_FETCH_SCROLL_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/query+fetch/scroll]"
decl_stmt|;
DECL|field|FETCH_ID_SCROLL_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|FETCH_ID_SCROLL_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/fetch/id/scroll]"
decl_stmt|;
DECL|field|FETCH_ID_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|FETCH_ID_ACTION_NAME
init|=
literal|"indices:data/read/search[phase/fetch/id]"
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|searchService
specifier|private
specifier|final
name|SearchService
name|searchService
decl_stmt|;
annotation|@
name|Inject
DECL|method|SearchTransportService
specifier|public
name|SearchTransportService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|SearchService
name|searchService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|searchService
operator|=
name|searchService
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|FREE_CONTEXT_SCROLL_ACTION_NAME
argument_list|,
name|ScrollFreeContextRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|FreeContextTransportHandler
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|FREE_CONTEXT_ACTION_NAME
argument_list|,
name|SearchFreeContextRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|FreeContextTransportHandler
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|CLEAR_SCROLL_CONTEXTS_ACTION_NAME
argument_list|,
name|ClearScrollContextsRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|ClearScrollContextsTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|DFS_ACTION_NAME
argument_list|,
name|ShardSearchTransportRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchDfsTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|QUERY_ACTION_NAME
argument_list|,
name|ShardSearchTransportRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchQueryTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|QUERY_ID_ACTION_NAME
argument_list|,
name|QuerySearchRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchQueryByIdTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|QUERY_SCROLL_ACTION_NAME
argument_list|,
name|InternalScrollSearchRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchQueryScrollTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|QUERY_FETCH_ACTION_NAME
argument_list|,
name|ShardSearchTransportRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchQueryFetchTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|QUERY_QUERY_FETCH_ACTION_NAME
argument_list|,
name|QuerySearchRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchQueryQueryFetchTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|QUERY_FETCH_SCROLL_ACTION_NAME
argument_list|,
name|InternalScrollSearchRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|SearchQueryFetchScrollTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|FETCH_ID_SCROLL_ACTION_NAME
argument_list|,
name|ShardFetchRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|FetchByIdTransportHandler
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|FETCH_ID_ACTION_NAME
argument_list|,
name|ShardFetchSearchRequest
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SEARCH
argument_list|,
operator|new
name|FetchByIdTransportHandler
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|sendFreeContext
specifier|public
name|void
name|sendFreeContext
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|long
name|contextId
parameter_list|,
name|SearchRequest
name|request
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|FREE_CONTEXT_ACTION_NAME
argument_list|,
operator|new
name|SearchFreeContextRequest
argument_list|(
name|request
argument_list|,
name|contextId
argument_list|)
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|SearchFreeContextResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchFreeContextResponse
name|response
parameter_list|)
block|{
comment|// no need to respond if it was freed or not
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{              }
block|}
argument_list|,
name|SearchFreeContextResponse
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendFreeContext
specifier|public
name|void
name|sendFreeContext
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|contextId
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|SearchFreeContextResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|FREE_CONTEXT_SCROLL_ACTION_NAME
argument_list|,
operator|new
name|ScrollFreeContextRequest
argument_list|(
name|contextId
argument_list|)
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|SearchFreeContextResponse
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendClearAllScrollContexts
specifier|public
name|void
name|sendClearAllScrollContexts
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|TransportResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|CLEAR_SCROLL_CONTEXTS_ACTION_NAME
argument_list|,
operator|new
name|ClearScrollContextsRequest
argument_list|()
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
parameter_list|()
lambda|->
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteDfs
specifier|public
name|void
name|sendExecuteDfs
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ShardSearchTransportRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|DfsSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|DFS_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|DfsSearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteQuery
specifier|public
name|void
name|sendExecuteQuery
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ShardSearchTransportRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|QuerySearchResultProvider
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|QUERY_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|QuerySearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteQuery
specifier|public
name|void
name|sendExecuteQuery
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|QuerySearchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|QuerySearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|QUERY_ID_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|QuerySearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteQuery
specifier|public
name|void
name|sendExecuteQuery
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|InternalScrollSearchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ScrollQuerySearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|QUERY_SCROLL_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|ScrollQuerySearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteFetch
specifier|public
name|void
name|sendExecuteFetch
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ShardSearchTransportRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|QueryFetchSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|QUERY_FETCH_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|QueryFetchSearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteFetch
specifier|public
name|void
name|sendExecuteFetch
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|QuerySearchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|QueryFetchSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|QUERY_QUERY_FETCH_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|QueryFetchSearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteFetch
specifier|public
name|void
name|sendExecuteFetch
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|InternalScrollSearchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ScrollQueryFetchSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|QUERY_FETCH_SCROLL_ACTION_NAME
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|ScrollQueryFetchSearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteFetch
specifier|public
name|void
name|sendExecuteFetch
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ShardFetchSearchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|FetchSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|sendExecuteFetch
argument_list|(
name|node
argument_list|,
name|FETCH_ID_ACTION_NAME
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteFetchScroll
specifier|public
name|void
name|sendExecuteFetchScroll
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|ShardFetchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|FetchSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|sendExecuteFetch
argument_list|(
name|node
argument_list|,
name|FETCH_ID_SCROLL_ACTION_NAME
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|sendExecuteFetch
specifier|private
name|void
name|sendExecuteFetch
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|action
parameter_list|,
specifier|final
name|ShardFetchRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|FetchSearchResult
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
operator|new
name|ActionListenerResponseHandler
argument_list|<>
argument_list|(
name|listener
argument_list|,
name|FetchSearchResult
operator|::
operator|new
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|ScrollFreeContextRequest
specifier|static
class|class
name|ScrollFreeContextRequest
extends|extends
name|TransportRequest
block|{
DECL|field|id
specifier|private
name|long
name|id
decl_stmt|;
DECL|method|ScrollFreeContextRequest
name|ScrollFreeContextRequest
parameter_list|()
block|{         }
DECL|method|ScrollFreeContextRequest
name|ScrollFreeContextRequest
parameter_list|(
name|long
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|long
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchFreeContextRequest
specifier|static
class|class
name|SearchFreeContextRequest
extends|extends
name|ScrollFreeContextRequest
implements|implements
name|IndicesRequest
block|{
DECL|field|originalIndices
specifier|private
name|OriginalIndices
name|originalIndices
decl_stmt|;
DECL|method|SearchFreeContextRequest
specifier|public
name|SearchFreeContextRequest
parameter_list|()
block|{         }
DECL|method|SearchFreeContextRequest
name|SearchFreeContextRequest
parameter_list|(
name|SearchRequest
name|request
parameter_list|,
name|long
name|id
parameter_list|)
block|{
name|super
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|this
operator|.
name|originalIndices
operator|=
operator|new
name|OriginalIndices
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
if|if
condition|(
name|originalIndices
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|originalIndices
operator|.
name|indices
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
if|if
condition|(
name|originalIndices
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|originalIndices
operator|.
name|indicesOptions
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|originalIndices
operator|=
name|OriginalIndices
operator|.
name|readOriginalIndices
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|OriginalIndices
operator|.
name|writeOriginalIndices
argument_list|(
name|originalIndices
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchFreeContextResponse
specifier|public
specifier|static
class|class
name|SearchFreeContextResponse
extends|extends
name|TransportResponse
block|{
DECL|field|freed
specifier|private
name|boolean
name|freed
decl_stmt|;
DECL|method|SearchFreeContextResponse
name|SearchFreeContextResponse
parameter_list|()
block|{         }
DECL|method|SearchFreeContextResponse
name|SearchFreeContextResponse
parameter_list|(
name|boolean
name|freed
parameter_list|)
block|{
name|this
operator|.
name|freed
operator|=
name|freed
expr_stmt|;
block|}
DECL|method|isFreed
specifier|public
name|boolean
name|isFreed
parameter_list|()
block|{
return|return
name|freed
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|freed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|freed
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FreeContextTransportHandler
class|class
name|FreeContextTransportHandler
parameter_list|<
name|FreeContextRequest
extends|extends
name|ScrollFreeContextRequest
parameter_list|>
implements|implements
name|TransportRequestHandler
argument_list|<
name|FreeContextRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|FreeContextRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|boolean
name|freed
init|=
name|searchService
operator|.
name|freeContext
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|SearchFreeContextResponse
argument_list|(
name|freed
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ClearScrollContextsRequest
specifier|static
class|class
name|ClearScrollContextsRequest
extends|extends
name|TransportRequest
block|{     }
DECL|class|ClearScrollContextsTransportHandler
class|class
name|ClearScrollContextsTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|ClearScrollContextsRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ClearScrollContextsRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|searchService
operator|.
name|freeAllScrollContexts
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchDfsTransportHandler
class|class
name|SearchDfsTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|ShardSearchTransportRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardSearchTransportRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|DfsSearchResult
name|result
init|=
name|searchService
operator|.
name|executeDfsPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchQueryTransportHandler
class|class
name|SearchQueryTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|ShardSearchTransportRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardSearchTransportRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|QuerySearchResultProvider
name|result
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchQueryByIdTransportHandler
class|class
name|SearchQueryByIdTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|QuerySearchRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|QuerySearchRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|QuerySearchResult
name|result
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchQueryScrollTransportHandler
class|class
name|SearchQueryScrollTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|InternalScrollSearchRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|InternalScrollSearchRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|ScrollQuerySearchResult
name|result
init|=
name|searchService
operator|.
name|executeQueryPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchQueryFetchTransportHandler
class|class
name|SearchQueryFetchTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|ShardSearchTransportRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardSearchTransportRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|QueryFetchSearchResult
name|result
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchQueryQueryFetchTransportHandler
class|class
name|SearchQueryQueryFetchTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|QuerySearchRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|QuerySearchRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|QueryFetchSearchResult
name|result
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|FetchByIdTransportHandler
class|class
name|FetchByIdTransportHandler
parameter_list|<
name|Request
extends|extends
name|ShardFetchRequest
parameter_list|>
implements|implements
name|TransportRequestHandler
argument_list|<
name|Request
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|Request
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|FetchSearchResult
name|result
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SearchQueryFetchScrollTransportHandler
class|class
name|SearchQueryFetchScrollTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|InternalScrollSearchRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|InternalScrollSearchRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|ScrollQueryFetchSearchResult
name|result
init|=
name|searchService
operator|.
name|executeFetchPhase
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

