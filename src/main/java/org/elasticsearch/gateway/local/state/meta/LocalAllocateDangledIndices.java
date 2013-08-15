begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway.local.state.meta
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|local
operator|.
name|state
operator|.
name|meta
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
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
name|ProcessedClusterStateUpdateTask
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
name|ClusterBlocks
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
name|IndexMetaData
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
name|MetaData
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
name|routing
operator|.
name|RoutingTable
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
name|routing
operator|.
name|allocation
operator|.
name|AllocationService
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
name|routing
operator|.
name|allocation
operator|.
name|RoutingAllocation
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
name|discovery
operator|.
name|MasterNotDiscoveredException
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
name|*
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
name|Arrays
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
operator|.
name|newClusterStateBuilder
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|LocalAllocateDangledIndices
specifier|public
class|class
name|LocalAllocateDangledIndices
extends|extends
name|AbstractComponent
block|{
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
annotation|@
name|Inject
DECL|method|LocalAllocateDangledIndices
specifier|public
name|LocalAllocateDangledIndices
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|AllocationService
name|allocationService
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
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|allocationService
operator|=
name|allocationService
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|AllocateDangledRequestHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|AllocateDangledRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|allocateDangled
specifier|public
name|void
name|allocateDangled
parameter_list|(
name|IndexMetaData
index|[]
name|indices
parameter_list|,
specifier|final
name|Listener
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
name|DiscoveryNode
name|masterNode
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
decl_stmt|;
if|if
condition|(
name|masterNode
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|MasterNotDiscoveredException
argument_list|(
literal|"no master to send allocate dangled request"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|AllocateDangledRequest
name|request
init|=
operator|new
name|AllocateDangledRequest
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
argument_list|,
name|indices
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|masterNode
argument_list|,
name|AllocateDangledRequestHandler
operator|.
name|ACTION
argument_list|,
name|request
argument_list|,
operator|new
name|TransportResponseHandler
argument_list|<
name|AllocateDangledResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|AllocateDangledResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|AllocateDangledResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|AllocateDangledResponse
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|interface|Listener
specifier|public
specifier|static
interface|interface
name|Listener
block|{
DECL|method|onResponse
name|void
name|onResponse
parameter_list|(
name|AllocateDangledResponse
name|response
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
function_decl|;
block|}
DECL|class|AllocateDangledRequestHandler
class|class
name|AllocateDangledRequestHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|AllocateDangledRequest
argument_list|>
block|{
DECL|field|ACTION
specifier|public
specifier|static
specifier|final
name|String
name|ACTION
init|=
literal|"/gateway/local/allocate_dangled"
decl_stmt|;
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|AllocateDangledRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|AllocateDangledRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|AllocateDangledRequest
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|String
index|[]
name|indexNames
init|=
operator|new
name|String
index|[
name|request
operator|.
name|indices
operator|.
name|length
index|]
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
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indexNames
index|[
name|i
index|]
operator|=
name|request
operator|.
name|indices
index|[
name|i
index|]
operator|.
name|index
argument_list|()
expr_stmt|;
block|}
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"allocation dangled indices "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|indexNames
argument_list|)
argument_list|,
operator|new
name|ProcessedClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
if|if
condition|(
name|currentState
operator|.
name|blocks
argument_list|()
operator|.
name|disableStatePersistence
argument_list|()
condition|)
block|{
return|return
name|currentState
return|;
block|}
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|()
operator|.
name|metaData
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterBlocks
operator|.
name|Builder
name|blocks
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
decl_stmt|;
name|RoutingTable
operator|.
name|Builder
name|routingTableBuilder
init|=
name|RoutingTable
operator|.
name|builder
argument_list|()
operator|.
name|routingTable
argument_list|(
name|currentState
operator|.
name|routingTable
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|importNeeded
init|=
literal|false
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexMetaData
name|indexMetaData
range|:
name|request
operator|.
name|indices
control|)
block|{
if|if
condition|(
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|importNeeded
operator|=
literal|true
expr_stmt|;
name|metaData
operator|.
name|put
argument_list|(
name|indexMetaData
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|blocks
operator|.
name|addBlocks
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
name|routingTableBuilder
operator|.
name|addAsRecovery
argument_list|(
name|indexMetaData
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"/"
argument_list|)
operator|.
name|append
argument_list|(
name|indexMetaData
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|importNeeded
condition|)
block|{
return|return
name|currentState
return|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"auto importing dangled indices {} from [{}]"
argument_list|,
name|sb
operator|.
name|toString
argument_list|()
argument_list|,
name|request
operator|.
name|fromNode
argument_list|)
expr_stmt|;
name|ClusterState
name|updatedState
init|=
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocks
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTableBuilder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// now, reroute
name|RoutingAllocation
operator|.
name|Result
name|routingResult
init|=
name|allocationService
operator|.
name|reroute
argument_list|(
name|newClusterStateBuilder
argument_list|()
operator|.
name|state
argument_list|(
name|updatedState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTableBuilder
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|updatedState
argument_list|)
operator|.
name|routingResult
argument_list|(
name|routingResult
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"unexpected failure during [{}]"
argument_list|,
name|t
argument_list|,
name|source
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed send response for allocating dangled"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|AllocateDangledResponse
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed send response for allocating dangled"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
block|}
DECL|class|AllocateDangledRequest
specifier|static
class|class
name|AllocateDangledRequest
extends|extends
name|TransportRequest
block|{
DECL|field|fromNode
name|DiscoveryNode
name|fromNode
decl_stmt|;
DECL|field|indices
name|IndexMetaData
index|[]
name|indices
decl_stmt|;
DECL|method|AllocateDangledRequest
name|AllocateDangledRequest
parameter_list|()
block|{         }
DECL|method|AllocateDangledRequest
name|AllocateDangledRequest
parameter_list|(
name|DiscoveryNode
name|fromNode
parameter_list|,
name|IndexMetaData
index|[]
name|indices
parameter_list|)
block|{
name|this
operator|.
name|fromNode
operator|=
name|fromNode
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
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
name|fromNode
operator|=
name|DiscoveryNode
operator|.
name|readNode
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indices
operator|=
operator|new
name|IndexMetaData
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|IndexMetaData
operator|.
name|Builder
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|fromNode
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indices
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexMetaData
name|indexMetaData
range|:
name|indices
control|)
block|{
name|IndexMetaData
operator|.
name|Builder
operator|.
name|writeTo
argument_list|(
name|indexMetaData
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|AllocateDangledResponse
specifier|public
specifier|static
class|class
name|AllocateDangledResponse
extends|extends
name|TransportResponse
block|{
DECL|field|ack
specifier|private
name|boolean
name|ack
decl_stmt|;
DECL|method|AllocateDangledResponse
name|AllocateDangledResponse
parameter_list|()
block|{         }
DECL|method|AllocateDangledResponse
name|AllocateDangledResponse
parameter_list|(
name|boolean
name|ack
parameter_list|)
block|{
name|this
operator|.
name|ack
operator|=
name|ack
expr_stmt|;
block|}
DECL|method|ack
specifier|public
name|boolean
name|ack
parameter_list|()
block|{
return|return
name|ack
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
name|ack
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
name|ack
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

