begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport
package|package
name|org
operator|.
name|elasticsearch
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
name|Version
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
name|CheckedBiConsumer
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
name|breaker
operator|.
name|CircuitBreaker
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
name|breaker
operator|.
name|NoopCircuitBreaker
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
name|LifecycleComponent
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_interface
DECL|interface|Transport
specifier|public
interface|interface
name|Transport
extends|extends
name|LifecycleComponent
block|{
DECL|field|TRANSPORT_TCP_COMPRESS
name|Setting
argument_list|<
name|Boolean
argument_list|>
name|TRANSPORT_TCP_COMPRESS
init|=
name|Setting
operator|.
name|boolSetting
argument_list|(
literal|"transport.tcp.compress"
argument_list|,
literal|false
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|method|transportServiceAdapter
name|void
name|transportServiceAdapter
parameter_list|(
name|TransportServiceAdapter
name|service
parameter_list|)
function_decl|;
comment|/**      * The address the transport is bound on.      */
DECL|method|boundAddress
name|BoundTransportAddress
name|boundAddress
parameter_list|()
function_decl|;
comment|/**      * Further profile bound addresses      * @return<code>null</code> iff profiles are unsupported, otherwise a map with name of profile and its bound transport address      */
DECL|method|profileBoundAddresses
name|Map
argument_list|<
name|String
argument_list|,
name|BoundTransportAddress
argument_list|>
name|profileBoundAddresses
parameter_list|()
function_decl|;
comment|/**      * Returns an address from its string representation.      */
DECL|method|addressesFromString
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
function_decl|;
comment|/**      * Returns<tt>true</tt> if the node is connected.      */
DECL|method|nodeConnected
name|boolean
name|nodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
function_decl|;
comment|/**      * Connects to a node with the given connection profile. If the node is already connected this method has no effect.      * Once a successful is established, it can be validated before being exposed.      */
DECL|method|connectToNode
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|ConnectionProfile
name|connectionProfile
parameter_list|,
name|CheckedBiConsumer
argument_list|<
name|Connection
argument_list|,
name|ConnectionProfile
argument_list|,
name|IOException
argument_list|>
name|connectionValidator
parameter_list|)
throws|throws
name|ConnectTransportException
function_decl|;
comment|/**      * Disconnected from the given node, if not connected, will do nothing.      */
DECL|method|disconnectFromNode
name|void
name|disconnectFromNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
function_decl|;
DECL|method|getLocalAddresses
name|List
argument_list|<
name|String
argument_list|>
name|getLocalAddresses
parameter_list|()
function_decl|;
DECL|method|getInFlightRequestBreaker
specifier|default
name|CircuitBreaker
name|getInFlightRequestBreaker
parameter_list|()
block|{
return|return
operator|new
name|NoopCircuitBreaker
argument_list|(
literal|"in-flight-noop"
argument_list|)
return|;
block|}
comment|/**      * Returns a new request ID to use when sending a message via {@link Connection#sendRequest(long, String,      * TransportRequest, TransportRequestOptions)}      */
DECL|method|newRequestId
name|long
name|newRequestId
parameter_list|()
function_decl|;
comment|/**      * Returns a connection for the given node if the node is connected.      * Connections returned from this method must not be closed. The lifecycle of this connection is maintained by the Transport      * implementation.      *      * @throws NodeNotConnectedException if the node is not connected      * @see #connectToNode(DiscoveryNode, ConnectionProfile, CheckedBiConsumer)      */
DECL|method|getConnection
name|Connection
name|getConnection
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
function_decl|;
comment|/**      * Opens a new connection to the given node and returns it. In contrast to      * {@link #connectToNode(DiscoveryNode, ConnectionProfile, CheckedBiConsumer)} the returned connection is not managed by      * the transport implementation. This connection must be closed once it's not needed anymore.      * This connection type can be used to execute a handshake between two nodes before the node will be published via      * {@link #connectToNode(DiscoveryNode, ConnectionProfile, CheckedBiConsumer)}.      */
DECL|method|openConnection
name|Connection
name|openConnection
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|ConnectionProfile
name|profile
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|getStats
name|TransportStats
name|getStats
parameter_list|()
function_decl|;
comment|/**      * A unidirectional connection to a {@link DiscoveryNode}      */
DECL|interface|Connection
interface|interface
name|Connection
extends|extends
name|Closeable
block|{
comment|/**          * The node this connection is associated with          */
DECL|method|getNode
name|DiscoveryNode
name|getNode
parameter_list|()
function_decl|;
comment|/**          * Sends the request to the node this connection is associated with          * @throws NodeNotConnectedException if the given node is not connected          */
DECL|method|sendRequest
name|void
name|sendRequest
parameter_list|(
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
function_decl|;
comment|/**          * Returns the version of the node this connection was established with.          */
DECL|method|getVersion
specifier|default
name|Version
name|getVersion
parameter_list|()
block|{
return|return
name|getNode
argument_list|()
operator|.
name|getVersion
argument_list|()
return|;
block|}
comment|/**          * Returns a key that this connection can be cached on. Delegating subclasses must delegate method call to          * the original connection.          */
DECL|method|getCacheKey
specifier|default
name|Object
name|getCacheKey
parameter_list|()
block|{
return|return
name|this
return|;
block|}
block|}
block|}
end_interface

end_unit

