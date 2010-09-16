begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indexer.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indexer
operator|.
name|cluster
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|VoidStreamable
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|PublishIndexerClusterStateAction
specifier|public
class|class
name|PublishIndexerClusterStateAction
extends|extends
name|AbstractComponent
block|{
DECL|interface|NewClusterStateListener
specifier|public
specifier|static
interface|interface
name|NewClusterStateListener
block|{
DECL|method|onNewClusterState
name|void
name|onNewClusterState
parameter_list|(
name|IndexerClusterState
name|clusterState
parameter_list|)
function_decl|;
block|}
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
DECL|field|listener
specifier|private
specifier|final
name|NewClusterStateListener
name|listener
decl_stmt|;
DECL|method|PublishIndexerClusterStateAction
specifier|public
name|PublishIndexerClusterStateAction
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
name|NewClusterStateListener
name|listener
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
name|listener
operator|=
name|listener
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|PublishClusterStateRequestHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|PublishClusterStateRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|transportService
operator|.
name|removeHandler
argument_list|(
name|PublishClusterStateRequestHandler
operator|.
name|ACTION
argument_list|)
expr_stmt|;
block|}
DECL|method|publish
specifier|public
name|void
name|publish
parameter_list|(
name|IndexerClusterState
name|clusterState
parameter_list|)
block|{
specifier|final
name|DiscoveryNodes
name|discoNodes
init|=
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|DiscoveryNode
name|node
range|:
name|discoNodes
control|)
block|{
if|if
condition|(
name|node
operator|.
name|equals
argument_list|(
name|discoNodes
operator|.
name|localNode
argument_list|()
argument_list|)
condition|)
block|{
comment|// no need to send to our self
continue|continue;
block|}
comment|// we only want to send nodes that are either possible master nodes or indexer nodes
comment|// master nodes because they will handle the state and the allocation of indexers
comment|// and indexer nodes since they will end up creating indexes
if|if
condition|(
name|node
operator|.
name|clientNode
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
operator|!
name|node
operator|.
name|masterNode
argument_list|()
operator|&&
operator|!
name|IndexerNodeHelper
operator|.
name|isIndexerNode
argument_list|(
name|node
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|PublishClusterStateRequestHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|PublishClusterStateRequest
argument_list|(
name|clusterState
argument_list|)
argument_list|,
operator|new
name|VoidTransportResponseHandler
argument_list|(
literal|false
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to send cluster state to [{}], should be detected as failed soon..."
argument_list|,
name|exp
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
DECL|class|PublishClusterStateRequest
specifier|private
class|class
name|PublishClusterStateRequest
implements|implements
name|Streamable
block|{
DECL|field|clusterState
specifier|private
name|IndexerClusterState
name|clusterState
decl_stmt|;
DECL|method|PublishClusterStateRequest
specifier|private
name|PublishClusterStateRequest
parameter_list|()
block|{         }
DECL|method|PublishClusterStateRequest
specifier|private
name|PublishClusterStateRequest
parameter_list|(
name|IndexerClusterState
name|clusterState
parameter_list|)
block|{
name|this
operator|.
name|clusterState
operator|=
name|clusterState
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
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
name|clusterState
operator|=
name|IndexerClusterState
operator|.
name|Builder
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|IndexerClusterState
operator|.
name|Builder
operator|.
name|writeTo
argument_list|(
name|clusterState
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|PublishClusterStateRequestHandler
specifier|private
class|class
name|PublishClusterStateRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|PublishClusterStateRequest
argument_list|>
block|{
DECL|field|ACTION
specifier|static
specifier|final
name|String
name|ACTION
init|=
literal|"indexer/state/publish"
decl_stmt|;
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|PublishClusterStateRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|PublishClusterStateRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|PublishClusterStateRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|listener
operator|.
name|onNewClusterState
argument_list|(
name|request
operator|.
name|clusterState
argument_list|)
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
comment|/**          * No need to spawn, we add submit a new cluster state directly. This allows for faster application.          */
DECL|method|spawn
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

