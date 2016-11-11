begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
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
name|support
operator|.
name|ActionFilters
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
name|HandledTransportAction
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
name|TransportAction
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
name|ClusterState
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
name|block
operator|.
name|ClusterBlockLevel
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|service
operator|.
name|ClusterService
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|AtomicArray
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
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
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
name|TransportService
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
name|ConcurrentLinkedQueue
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|TransportMultiSearchAction
specifier|public
class|class
name|TransportMultiSearchAction
extends|extends
name|HandledTransportAction
argument_list|<
name|MultiSearchRequest
argument_list|,
name|MultiSearchResponse
argument_list|>
block|{
DECL|field|availableProcessors
specifier|private
specifier|final
name|int
name|availableProcessors
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|searchAction
specifier|private
specifier|final
name|TransportAction
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|>
name|searchAction
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportMultiSearchAction
specifier|public
name|TransportMultiSearchAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportSearchAction
name|searchAction
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|MultiSearchAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|MultiSearchRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|searchAction
operator|=
name|searchAction
expr_stmt|;
name|this
operator|.
name|availableProcessors
operator|=
name|EsExecutors
operator|.
name|numberOfProcessors
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
comment|// For testing only:
DECL|method|TransportMultiSearchAction
name|TransportMultiSearchAction
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportAction
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|>
name|searchAction
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|int
name|availableProcessors
parameter_list|)
block|{
name|super
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|MultiSearchAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|MultiSearchRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|searchAction
operator|=
name|searchAction
expr_stmt|;
name|this
operator|.
name|availableProcessors
operator|=
name|availableProcessors
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|MultiSearchRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|clusterState
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedRaiseException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|)
expr_stmt|;
name|int
name|maxConcurrentSearches
init|=
name|request
operator|.
name|maxConcurrentSearchRequests
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxConcurrentSearches
operator|==
literal|0
condition|)
block|{
name|maxConcurrentSearches
operator|=
name|defaultMaxConcurrentSearches
argument_list|(
name|availableProcessors
argument_list|,
name|clusterState
argument_list|)
expr_stmt|;
block|}
name|Queue
argument_list|<
name|SearchRequestSlot
argument_list|>
name|searchRequestSlots
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<>
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
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|SearchRequest
name|searchRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|searchRequestSlots
operator|.
name|add
argument_list|(
operator|new
name|SearchRequestSlot
argument_list|(
name|searchRequest
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|numRequests
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|AtomicArray
argument_list|<
name|MultiSearchResponse
operator|.
name|Item
argument_list|>
name|responses
init|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|numRequests
argument_list|)
decl_stmt|;
specifier|final
name|AtomicInteger
name|responseCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|numRequests
argument_list|)
decl_stmt|;
name|int
name|numConcurrentSearches
init|=
name|Math
operator|.
name|min
argument_list|(
name|numRequests
argument_list|,
name|maxConcurrentSearches
argument_list|)
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
name|numConcurrentSearches
condition|;
name|i
operator|++
control|)
block|{
name|executeSearch
argument_list|(
name|searchRequestSlots
argument_list|,
name|responses
argument_list|,
name|responseCounter
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
comment|/*      * This is not perfect and makes a big assumption, that all nodes have the same thread pool size / have the number      * of processors and that shard of the indices the search requests go to are more or less evenly distributed across      * all nodes in the cluster. But I think it is a good enough default for most cases, if not then the default should be      * overwritten in the request itself.      */
DECL|method|defaultMaxConcurrentSearches
specifier|static
name|int
name|defaultMaxConcurrentSearches
parameter_list|(
name|int
name|availableProcessors
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|int
name|numDateNodes
init|=
name|state
operator|.
name|getNodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// availableProcessors will never be larger than 32, so max defaultMaxConcurrentSearches will never be larger than 49,
comment|// but we don't know about about other search requests that are being executed so lets cap at 10 per node
name|int
name|defaultSearchThreadPoolSize
init|=
name|Math
operator|.
name|min
argument_list|(
name|ThreadPool
operator|.
name|searchThreadPoolSize
argument_list|(
name|availableProcessors
argument_list|)
argument_list|,
literal|10
argument_list|)
decl_stmt|;
return|return
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|numDateNodes
operator|*
name|defaultSearchThreadPoolSize
argument_list|)
return|;
block|}
DECL|method|executeSearch
name|void
name|executeSearch
parameter_list|(
name|Queue
argument_list|<
name|SearchRequestSlot
argument_list|>
name|requests
parameter_list|,
name|AtomicArray
argument_list|<
name|MultiSearchResponse
operator|.
name|Item
argument_list|>
name|responses
parameter_list|,
name|AtomicInteger
name|responseCounter
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|SearchRequestSlot
name|request
init|=
name|requests
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|==
literal|null
condition|)
block|{
comment|// Ok... so there're no more requests then this is ok, we're then waiting for running requests to complete
return|return;
block|}
name|searchAction
operator|.
name|execute
argument_list|(
name|request
operator|.
name|request
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchResponse
name|searchResponse
parameter_list|)
block|{
name|responses
operator|.
name|set
argument_list|(
name|request
operator|.
name|responseSlot
argument_list|,
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
name|searchResponse
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|handleResponse
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|responses
operator|.
name|set
argument_list|(
name|request
operator|.
name|responseSlot
argument_list|,
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
name|handleResponse
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|handleResponse
parameter_list|()
block|{
if|if
condition|(
name|responseCounter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|MultiSearchResponse
argument_list|(
name|responses
operator|.
name|toArray
argument_list|(
operator|new
name|MultiSearchResponse
operator|.
name|Item
index|[
name|responses
operator|.
name|length
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|executeSearch
argument_list|(
name|requests
argument_list|,
name|responses
argument_list|,
name|responseCounter
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|SearchRequestSlot
specifier|static
specifier|final
class|class
name|SearchRequestSlot
block|{
DECL|field|request
specifier|final
name|SearchRequest
name|request
decl_stmt|;
DECL|field|responseSlot
specifier|final
name|int
name|responseSlot
decl_stmt|;
DECL|method|SearchRequestSlot
name|SearchRequestSlot
parameter_list|(
name|SearchRequest
name|request
parameter_list|,
name|int
name|responseSlot
parameter_list|)
block|{
name|this
operator|.
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|responseSlot
operator|=
name|responseSlot
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

