begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|liveness
operator|.
name|LivenessResponse
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|liveness
operator|.
name|TransportLivenessAction
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
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|ClusterStateAction
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
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|ClusterStateResponse
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
name|ClusterName
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
name|Lifecycle
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
name|LifecycleListener
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
name|transport
operator|.
name|BoundTransportAddress
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
name|transport
operator|.
name|TransportAddress
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
name|ConnectTransportException
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
name|ConnectionProfile
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
name|Transport
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
name|TransportException
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
name|TransportRequestOptions
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
name|TransportResponseHandler
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
name|TransportServiceAdapter
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
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|CopyOnWriteArraySet
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
DECL|class|FailAndRetryMockTransport
specifier|abstract
class|class
name|FailAndRetryMockTransport
parameter_list|<
name|Response
extends|extends
name|TransportResponse
parameter_list|>
implements|implements
name|Transport
block|{
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|connectMode
specifier|private
name|boolean
name|connectMode
init|=
literal|true
decl_stmt|;
DECL|field|transportServiceAdapter
specifier|private
name|TransportServiceAdapter
name|transportServiceAdapter
decl_stmt|;
DECL|field|connectTransportExceptions
specifier|private
specifier|final
name|AtomicInteger
name|connectTransportExceptions
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|failures
specifier|private
specifier|final
name|AtomicInteger
name|failures
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|successes
specifier|private
specifier|final
name|AtomicInteger
name|successes
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|triedNodes
specifier|private
specifier|final
name|Set
argument_list|<
name|DiscoveryNode
argument_list|>
name|triedNodes
init|=
operator|new
name|CopyOnWriteArraySet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|FailAndRetryMockTransport
name|FailAndRetryMockTransport
parameter_list|(
name|Random
name|random
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|)
block|{
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|(
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
block|}
DECL|method|getMockClusterState
specifier|protected
specifier|abstract
name|ClusterState
name|getMockClusterState
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
function_decl|;
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|sendRequest
specifier|public
name|void
name|sendRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|)
throws|throws
name|IOException
throws|,
name|TransportException
block|{
comment|//we make sure that nodes get added to the connected ones when calling addTransportAddress, by returning proper nodes info
if|if
condition|(
name|connectMode
condition|)
block|{
if|if
condition|(
name|TransportLivenessAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|TransportResponseHandler
name|transportResponseHandler
init|=
name|transportServiceAdapter
operator|.
name|onResponseReceived
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
name|transportResponseHandler
operator|.
name|handleResponse
argument_list|(
operator|new
name|LivenessResponse
argument_list|(
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|getDefault
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|node
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ClusterStateAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|TransportResponseHandler
name|transportResponseHandler
init|=
name|transportServiceAdapter
operator|.
name|onResponseReceived
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
name|ClusterState
name|clusterState
init|=
name|getMockClusterState
argument_list|(
name|node
argument_list|)
decl_stmt|;
name|transportResponseHandler
operator|.
name|handleResponse
argument_list|(
operator|new
name|ClusterStateResponse
argument_list|(
name|clusterName
argument_list|,
name|clusterState
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Mock transport does not understand action "
operator|+
name|action
argument_list|)
throw|;
block|}
return|return;
block|}
comment|//once nodes are connected we'll just return errors for each sendRequest call
name|triedNodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|>
literal|10
condition|)
block|{
name|connectTransportExceptions
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"node not available"
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
comment|//throw whatever exception that is not a subclass of ConnectTransportException
throw|throw
operator|new
name|IllegalStateException
argument_list|()
throw|;
block|}
else|else
block|{
name|TransportResponseHandler
name|transportResponseHandler
init|=
name|transportServiceAdapter
operator|.
name|onResponseReceived
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|successes
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|transportResponseHandler
operator|.
name|handleResponse
argument_list|(
name|newResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|failures
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|transportResponseHandler
operator|.
name|handleException
argument_list|(
operator|new
name|TransportException
argument_list|(
literal|"transport exception"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|newResponse
specifier|protected
specifier|abstract
name|Response
name|newResponse
parameter_list|()
function_decl|;
DECL|method|endConnectMode
specifier|public
name|void
name|endConnectMode
parameter_list|()
block|{
name|this
operator|.
name|connectMode
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|connectTransportExceptions
specifier|public
name|int
name|connectTransportExceptions
parameter_list|()
block|{
return|return
name|connectTransportExceptions
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|failures
specifier|public
name|int
name|failures
parameter_list|()
block|{
return|return
name|failures
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|successes
specifier|public
name|int
name|successes
parameter_list|()
block|{
return|return
name|successes
operator|.
name|get
argument_list|()
return|;
block|}
DECL|method|triedNodes
specifier|public
name|Set
argument_list|<
name|DiscoveryNode
argument_list|>
name|triedNodes
parameter_list|()
block|{
return|return
name|triedNodes
return|;
block|}
annotation|@
name|Override
DECL|method|transportServiceAdapter
specifier|public
name|void
name|transportServiceAdapter
parameter_list|(
name|TransportServiceAdapter
name|transportServiceAdapter
parameter_list|)
block|{
name|this
operator|.
name|transportServiceAdapter
operator|=
name|transportServiceAdapter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|boundAddress
specifier|public
name|BoundTransportAddress
name|boundAddress
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addressesFromString
specifier|public
name|TransportAddress
index|[]
name|addressesFromString
parameter_list|(
name|String
name|address
parameter_list|,
name|int
name|perAddressLimit
parameter_list|)
throws|throws
name|UnknownHostException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|nodeConnected
specifier|public
name|boolean
name|nodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|connectToNode
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|ConnectionProfile
name|connectionProfile
parameter_list|)
throws|throws
name|ConnectTransportException
block|{      }
annotation|@
name|Override
DECL|method|disconnectFromNode
specifier|public
name|void
name|disconnectFromNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|serverOpen
specifier|public
name|long
name|serverOpen
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|lifecycleState
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addLifecycleListener
specifier|public
name|void
name|addLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|removeLifecycleListener
specifier|public
name|void
name|removeLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|start
specifier|public
name|void
name|start
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|profileBoundAddresses
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BoundTransportAddress
argument_list|>
name|profileBoundAddresses
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyMap
argument_list|()
return|;
block|}
block|}
end_class

end_unit

