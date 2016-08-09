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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodes
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
name|CountDown
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
name|action
operator|.
name|SearchTransportService
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
name|util
operator|.
name|ArrayList
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|AtomicReference
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|TransportSearchHelper
operator|.
name|parseScrollId
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|TransportClearScrollAction
specifier|public
class|class
name|TransportClearScrollAction
extends|extends
name|HandledTransportAction
argument_list|<
name|ClearScrollRequest
argument_list|,
name|ClearScrollResponse
argument_list|>
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|searchTransportService
specifier|private
specifier|final
name|SearchTransportService
name|searchTransportService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportClearScrollAction
specifier|public
name|TransportClearScrollAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|SearchTransportService
name|searchTransportService
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
name|ClearScrollAction
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
name|ClearScrollRequest
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
name|searchTransportService
operator|=
name|searchTransportService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ClearScrollRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ClearScrollResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|new
name|Async
argument_list|(
name|request
argument_list|,
name|listener
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
DECL|class|Async
specifier|private
class|class
name|Async
block|{
DECL|field|nodes
specifier|final
name|DiscoveryNodes
name|nodes
decl_stmt|;
DECL|field|expectedOps
specifier|final
name|CountDown
name|expectedOps
decl_stmt|;
DECL|field|contexts
specifier|final
name|List
argument_list|<
name|ScrollIdForNode
index|[]
argument_list|>
name|contexts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|listener
specifier|final
name|ActionListener
argument_list|<
name|ClearScrollResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|expHolder
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|expHolder
decl_stmt|;
DECL|field|numberOfFreedSearchContexts
specifier|final
name|AtomicInteger
name|numberOfFreedSearchContexts
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|method|Async
specifier|private
name|Async
parameter_list|(
name|ClearScrollRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|ClearScrollResponse
argument_list|>
name|listener
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|int
name|expectedOps
init|=
literal|0
decl_stmt|;
name|this
operator|.
name|nodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|getScrollIds
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
literal|"_all"
operator|.
name|equals
argument_list|(
name|request
operator|.
name|getScrollIds
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
condition|)
block|{
name|expectedOps
operator|=
name|nodes
operator|.
name|getSize
argument_list|()
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|String
name|parsedScrollId
range|:
name|request
operator|.
name|getScrollIds
argument_list|()
control|)
block|{
name|ScrollIdForNode
index|[]
name|context
init|=
name|parseScrollId
argument_list|(
name|parsedScrollId
argument_list|)
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|expectedOps
operator|+=
name|context
operator|.
name|length
expr_stmt|;
name|this
operator|.
name|contexts
operator|.
name|add
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|expHolder
operator|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|expectedOps
operator|=
operator|new
name|CountDown
argument_list|(
name|expectedOps
argument_list|)
expr_stmt|;
block|}
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|expectedOps
operator|.
name|isCountedDown
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClearScrollResponse
argument_list|(
literal|true
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|contexts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
specifier|final
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
name|searchTransportService
operator|.
name|sendClearAllScrollContexts
argument_list|(
name|node
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|TransportResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|TransportResponse
name|response
parameter_list|)
block|{
name|onFreedContext
argument_list|(
literal|true
argument_list|)
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
name|onFailedFreedContext
argument_list|(
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|ScrollIdForNode
index|[]
name|context
range|:
name|contexts
control|)
block|{
for|for
control|(
name|ScrollIdForNode
name|target
range|:
name|context
control|)
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
name|target
operator|.
name|getNode
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|node
operator|==
literal|null
condition|)
block|{
name|onFreedContext
argument_list|(
literal|false
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|searchTransportService
operator|.
name|sendFreeContext
argument_list|(
name|node
argument_list|,
name|target
operator|.
name|getScrollId
argument_list|()
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|SearchTransportService
operator|.
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
name|SearchTransportService
operator|.
name|SearchFreeContextResponse
name|freed
parameter_list|)
block|{
name|onFreedContext
argument_list|(
name|freed
operator|.
name|isFreed
argument_list|()
argument_list|)
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
name|onFailedFreedContext
argument_list|(
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|onFreedContext
name|void
name|onFreedContext
parameter_list|(
name|boolean
name|freed
parameter_list|)
block|{
if|if
condition|(
name|freed
condition|)
block|{
name|numberOfFreedSearchContexts
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|expectedOps
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|boolean
name|succeeded
init|=
name|expHolder
operator|.
name|get
argument_list|()
operator|==
literal|null
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClearScrollResponse
argument_list|(
name|succeeded
argument_list|,
name|numberOfFreedSearchContexts
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onFailedFreedContext
name|void
name|onFailedFreedContext
parameter_list|(
name|Throwable
name|e
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
operator|new
name|ParameterizedMessage
argument_list|(
literal|"Clear SC failed on node[{}]"
argument_list|,
name|node
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|expectedOps
operator|.
name|countDown
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClearScrollResponse
argument_list|(
literal|false
argument_list|,
name|numberOfFreedSearchContexts
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|expHolder
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

