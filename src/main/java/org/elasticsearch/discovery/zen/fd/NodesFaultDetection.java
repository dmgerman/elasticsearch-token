begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen.fd
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
operator|.
name|fd
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|unit
operator|.
name|TimeValue
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
name|concurrent
operator|.
name|ConcurrentMap
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
name|CopyOnWriteArrayList
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
name|node
operator|.
name|DiscoveryNodes
operator|.
name|EMPTY_NODES
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
import|;
end_import

begin_import
import|import static
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
name|ConcurrentCollections
operator|.
name|newConcurrentMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequestOptions
operator|.
name|options
import|;
end_import

begin_comment
comment|/**  * A fault detection of multiple nodes.  */
end_comment

begin_class
DECL|class|NodesFaultDetection
specifier|public
class|class
name|NodesFaultDetection
extends|extends
name|AbstractComponent
block|{
DECL|interface|Listener
specifier|public
specifier|static
interface|interface
name|Listener
block|{
DECL|method|onNodeFailure
name|void
name|onNodeFailure
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|reason
parameter_list|)
function_decl|;
block|}
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|connectOnNetworkDisconnect
specifier|private
specifier|final
name|boolean
name|connectOnNetworkDisconnect
decl_stmt|;
DECL|field|pingInterval
specifier|private
specifier|final
name|TimeValue
name|pingInterval
decl_stmt|;
DECL|field|pingRetryTimeout
specifier|private
specifier|final
name|TimeValue
name|pingRetryTimeout
decl_stmt|;
DECL|field|pingRetryCount
specifier|private
specifier|final
name|int
name|pingRetryCount
decl_stmt|;
comment|// used mainly for testing, should always be true
DECL|field|registerConnectionListener
specifier|private
specifier|final
name|boolean
name|registerConnectionListener
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|Listener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|Listener
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|nodesFD
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeFD
argument_list|>
name|nodesFD
init|=
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|connectionListener
specifier|private
specifier|final
name|FDConnectionListener
name|connectionListener
decl_stmt|;
DECL|field|latestNodes
specifier|private
specifier|volatile
name|DiscoveryNodes
name|latestNodes
init|=
name|EMPTY_NODES
decl_stmt|;
DECL|field|running
specifier|private
specifier|volatile
name|boolean
name|running
init|=
literal|false
decl_stmt|;
DECL|method|NodesFaultDetection
specifier|public
name|NodesFaultDetection
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|connectOnNetworkDisconnect
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"connect_on_network_disconnect"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"ping_interval"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingRetryTimeout
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"ping_timeout"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingRetryCount
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"ping_retries"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|this
operator|.
name|registerConnectionListener
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"register_connection_listener"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"[node  ] uses ping_interval [{}], ping_timeout [{}], ping_retries [{}]"
argument_list|,
name|pingInterval
argument_list|,
name|pingRetryTimeout
argument_list|,
name|pingRetryCount
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|PingRequestHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|PingRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|connectionListener
operator|=
operator|new
name|FDConnectionListener
argument_list|()
expr_stmt|;
if|if
condition|(
name|registerConnectionListener
condition|)
block|{
name|transportService
operator|.
name|addConnectionListener
argument_list|(
name|connectionListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addListener
specifier|public
name|void
name|addListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|removeListener
specifier|public
name|void
name|removeListener
parameter_list|(
name|Listener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|updateNodes
specifier|public
name|void
name|updateNodes
parameter_list|(
name|DiscoveryNodes
name|nodes
parameter_list|)
block|{
name|DiscoveryNodes
name|prevNodes
init|=
name|latestNodes
decl_stmt|;
name|this
operator|.
name|latestNodes
operator|=
name|nodes
expr_stmt|;
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return;
block|}
name|DiscoveryNodes
operator|.
name|Delta
name|delta
init|=
name|nodes
operator|.
name|delta
argument_list|(
name|prevNodes
argument_list|)
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|newNode
range|:
name|delta
operator|.
name|addedNodes
argument_list|()
control|)
block|{
if|if
condition|(
name|newNode
operator|.
name|id
argument_list|()
operator|.
name|equals
argument_list|(
name|nodes
operator|.
name|localNodeId
argument_list|()
argument_list|)
condition|)
block|{
comment|// no need to monitor the local node
continue|continue;
block|}
if|if
condition|(
operator|!
name|nodesFD
operator|.
name|containsKey
argument_list|(
name|newNode
argument_list|)
condition|)
block|{
name|nodesFD
operator|.
name|put
argument_list|(
name|newNode
argument_list|,
operator|new
name|NodeFD
argument_list|()
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|pingInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|SendPingRequest
argument_list|(
name|newNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|DiscoveryNode
name|removedNode
range|:
name|delta
operator|.
name|removedNodes
argument_list|()
control|)
block|{
name|nodesFD
operator|.
name|remove
argument_list|(
name|removedNode
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|start
specifier|public
name|NodesFaultDetection
name|start
parameter_list|()
block|{
if|if
condition|(
name|running
condition|)
block|{
return|return
name|this
return|;
block|}
name|running
operator|=
literal|true
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|stop
specifier|public
name|NodesFaultDetection
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return
name|this
return|;
block|}
name|running
operator|=
literal|false
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|stop
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|removeHandler
argument_list|(
name|PingRequestHandler
operator|.
name|ACTION
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|removeConnectionListener
argument_list|(
name|connectionListener
argument_list|)
expr_stmt|;
block|}
DECL|method|handleTransportDisconnect
specifier|private
name|void
name|handleTransportDisconnect
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
if|if
condition|(
operator|!
name|latestNodes
operator|.
name|nodeExists
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
return|return;
block|}
name|NodeFD
name|nodeFD
init|=
name|nodesFD
operator|.
name|remove
argument_list|(
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeFD
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return;
block|}
name|nodeFD
operator|.
name|running
operator|=
literal|false
expr_stmt|;
if|if
condition|(
name|connectOnNetworkDisconnect
condition|)
block|{
try|try
block|{
name|transportService
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|nodesFD
operator|.
name|put
argument_list|(
name|node
argument_list|,
operator|new
name|NodeFD
argument_list|()
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|pingInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|SendPingRequest
argument_list|(
name|node
argument_list|)
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
name|trace
argument_list|(
literal|"[node  ] [{}] transport disconnected (with verified connect)"
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|notifyNodeFailure
argument_list|(
name|node
argument_list|,
literal|"transport disconnected (with verified connect)"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[node  ] [{}] transport disconnected"
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|notifyNodeFailure
argument_list|(
name|node
argument_list|,
literal|"transport disconnected"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|notifyNodeFailure
specifier|private
name|void
name|notifyNodeFailure
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|String
name|reason
parameter_list|)
block|{
name|threadPool
operator|.
name|generic
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
for|for
control|(
name|Listener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|onNodeFailure
argument_list|(
name|node
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|SendPingRequest
specifier|private
class|class
name|SendPingRequest
implements|implements
name|Runnable
block|{
DECL|field|node
specifier|private
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|method|SendPingRequest
specifier|private
name|SendPingRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return;
block|}
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|PingRequestHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|PingRequest
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|options
argument_list|()
operator|.
name|withHighType
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|pingRetryTimeout
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|PingResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|PingResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|PingResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|PingResponse
name|response
parameter_list|)
block|{
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return;
block|}
name|NodeFD
name|nodeFD
init|=
name|nodesFD
operator|.
name|get
argument_list|(
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeFD
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|nodeFD
operator|.
name|running
condition|)
block|{
return|return;
block|}
name|nodeFD
operator|.
name|retryCount
operator|=
literal|0
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|pingInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|SendPingRequest
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
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
comment|// check if the master node did not get switched on us...
if|if
condition|(
operator|!
name|running
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|exp
operator|instanceof
name|ConnectTransportException
condition|)
block|{
comment|// ignore this one, we already handle it by registering a connection listener
return|return;
block|}
name|NodeFD
name|nodeFD
init|=
name|nodesFD
operator|.
name|get
argument_list|(
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeFD
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|nodeFD
operator|.
name|running
condition|)
block|{
return|return;
block|}
name|int
name|retryCount
init|=
operator|++
name|nodeFD
operator|.
name|retryCount
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[node  ] failed to ping [{}], retry [{}] out of [{}]"
argument_list|,
name|exp
argument_list|,
name|node
argument_list|,
name|retryCount
argument_list|,
name|pingRetryCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|retryCount
operator|>=
name|pingRetryCount
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[node  ] failed to ping [{}], tried [{}] times, each with  maximum [{}] timeout"
argument_list|,
name|node
argument_list|,
name|pingRetryCount
argument_list|,
name|pingRetryTimeout
argument_list|)
expr_stmt|;
comment|// not good, failure
if|if
condition|(
name|nodesFD
operator|.
name|remove
argument_list|(
name|node
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|notifyNodeFailure
argument_list|(
name|node
argument_list|,
literal|"failed to ping, tried ["
operator|+
name|pingRetryCount
operator|+
literal|"] times, each with maximum ["
operator|+
name|pingRetryTimeout
operator|+
literal|"] timeout"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// resend the request, not reschedule, rely on send timeout
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|PingRequestHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|PingRequest
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|options
argument_list|()
operator|.
name|withHighType
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|pingRetryTimeout
argument_list|)
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
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
block|}
DECL|class|NodeFD
specifier|static
class|class
name|NodeFD
block|{
DECL|field|retryCount
specifier|volatile
name|int
name|retryCount
decl_stmt|;
DECL|field|running
specifier|volatile
name|boolean
name|running
init|=
literal|true
decl_stmt|;
block|}
DECL|class|FDConnectionListener
specifier|private
class|class
name|FDConnectionListener
implements|implements
name|TransportConnectionListener
block|{
annotation|@
name|Override
DECL|method|onNodeConnected
specifier|public
name|void
name|onNodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|onNodeDisconnected
specifier|public
name|void
name|onNodeDisconnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|handleTransportDisconnect
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|PingRequestHandler
class|class
name|PingRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|PingRequest
argument_list|>
block|{
DECL|field|ACTION
specifier|public
specifier|static
specifier|final
name|String
name|ACTION
init|=
literal|"discovery/zen/fd/ping"
decl_stmt|;
annotation|@
name|Override
DECL|method|newInstance
specifier|public
name|PingRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|PingRequest
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
name|PingRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// if we are not the node we are supposed to be pinged, send an exception
comment|// this can happen when a kill -9 is sent, and another node is started using the same port
if|if
condition|(
operator|!
name|latestNodes
operator|.
name|localNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|nodeId
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Got pinged as node ["
operator|+
name|request
operator|.
name|nodeId
operator|+
literal|"], but I am node ["
operator|+
name|latestNodes
operator|.
name|localNodeId
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|PingResponse
argument_list|()
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
DECL|class|PingRequest
specifier|static
class|class
name|PingRequest
extends|extends
name|TransportRequest
block|{
comment|// the (assumed) node id we are pinging
DECL|field|nodeId
specifier|private
name|String
name|nodeId
decl_stmt|;
DECL|method|PingRequest
name|PingRequest
parameter_list|()
block|{         }
DECL|method|PingRequest
name|PingRequest
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|nodeId
operator|=
name|nodeId
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
name|nodeId
operator|=
name|in
operator|.
name|readString
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
name|writeString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|PingResponse
specifier|private
specifier|static
class|class
name|PingResponse
extends|extends
name|TransportResponse
block|{
DECL|method|PingResponse
specifier|private
name|PingResponse
parameter_list|()
block|{         }
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
block|}
block|}
block|}
end_class

end_unit

