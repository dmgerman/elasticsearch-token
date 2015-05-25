begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|BytesStreamOutput
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
name|network
operator|.
name|NetworkService
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|AbstractRunnable
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
name|ConcurrentCollections
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
name|*
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

begin_comment
comment|/**  * A mock transport service that allows to simulate different network topology failures.  */
end_comment

begin_class
DECL|class|MockTransportService
specifier|public
class|class
name|MockTransportService
extends|extends
name|TransportService
block|{
DECL|field|original
specifier|private
specifier|final
name|Transport
name|original
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockTransportService
specifier|public
name|MockTransportService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Transport
name|transport
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
operator|new
name|LookupTestTransport
argument_list|(
name|transport
argument_list|)
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|original
operator|=
name|transport
expr_stmt|;
block|}
comment|/**      * Clears all the registered rules.      */
DECL|method|clearAllRules
specifier|public
name|void
name|clearAllRules
parameter_list|()
block|{
name|transport
argument_list|()
operator|.
name|transports
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
comment|/**      * Clears the rule associated with the provided node.      */
DECL|method|clearRule
specifier|public
name|void
name|clearRule
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|transport
argument_list|()
operator|.
name|transports
operator|.
name|remove
argument_list|(
name|node
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the original Transport service wrapped by this mock transport service.      */
DECL|method|original
specifier|public
name|Transport
name|original
parameter_list|()
block|{
return|return
name|original
return|;
block|}
comment|/**      * Adds a rule that will cause every send request to fail, and each new connect since the rule      * is added to fail as well.      */
DECL|method|addFailToSendNoConnectRule
specifier|public
name|void
name|addFailToSendNoConnectRule
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|addDelegate
argument_list|(
name|node
argument_list|,
operator|new
name|DelegateTransport
argument_list|(
name|original
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"DISCONNECT: simulated"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"DISCONNECT: simulated"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"DISCONNECT: simulated"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a rule that will cause matching operations to throw ConnectTransportExceptions      */
DECL|method|addFailToSendNoConnectRule
specifier|public
name|void
name|addFailToSendNoConnectRule
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|String
modifier|...
name|blockedActions
parameter_list|)
block|{
name|addFailToSendNoConnectRule
argument_list|(
name|node
argument_list|,
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|blockedActions
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a rule that will cause matching operations to throw ConnectTransportExceptions      */
DECL|method|addFailToSendNoConnectRule
specifier|public
name|void
name|addFailToSendNoConnectRule
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|blockedActions
parameter_list|)
block|{
name|addDelegate
argument_list|(
name|node
argument_list|,
operator|new
name|DelegateTransport
argument_list|(
name|original
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|original
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|original
operator|.
name|connectToNodeLight
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
if|if
condition|(
name|blockedActions
operator|.
name|contains
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> preventing {} request"
argument_list|,
name|action
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"DISCONNECT: prevented "
operator|+
name|action
operator|+
literal|" request"
argument_list|)
throw|;
block|}
name|original
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a rule that will cause ignores each send request, simulating an unresponsive node      * and failing to connect once the rule was added.      */
DECL|method|addUnresponsiveRule
specifier|public
name|void
name|addUnresponsiveRule
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|addDelegate
argument_list|(
name|node
argument_list|,
operator|new
name|DelegateTransport
argument_list|(
name|original
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"UNRESPONSIVE: simulated"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"UNRESPONSIVE: simulated"
argument_list|)
throw|;
block|}
annotation|@
name|Override
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
comment|// don't send anything, the receiving node is unresponsive
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a rule that will cause ignores each send request, simulating an unresponsive node      * and failing to connect once the rule was added.      *      * @param duration the amount of time to delay sending and connecting.      */
DECL|method|addUnresponsiveRule
specifier|public
name|void
name|addUnresponsiveRule
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|TimeValue
name|duration
parameter_list|)
block|{
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|addDelegate
argument_list|(
name|node
argument_list|,
operator|new
name|DelegateTransport
argument_list|(
name|original
argument_list|)
block|{
name|TimeValue
name|getDelay
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|duration
operator|.
name|millis
argument_list|()
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|startTime
operator|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|TimeValue
name|delay
init|=
name|getDelay
argument_list|()
decl_stmt|;
if|if
condition|(
name|delay
operator|.
name|millis
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|original
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// TODO: Replace with proper setting
name|TimeValue
name|connectingTimeout
init|=
name|NetworkService
operator|.
name|TcpSettings
operator|.
name|TCP_DEFAULT_CONNECT_TIMEOUT
decl_stmt|;
try|try
block|{
if|if
condition|(
name|delay
operator|.
name|millis
argument_list|()
operator|<
name|connectingTimeout
operator|.
name|millis
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|delay
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|original
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|connectingTimeout
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"UNRESPONSIVE: simulated"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"UNRESPONSIVE: interrupted while sleeping"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|TimeValue
name|delay
init|=
name|getDelay
argument_list|()
decl_stmt|;
if|if
condition|(
name|delay
operator|.
name|millis
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|original
operator|.
name|connectToNodeLight
argument_list|(
name|node
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// TODO: Replace with proper setting
name|TimeValue
name|connectingTimeout
init|=
name|NetworkService
operator|.
name|TcpSettings
operator|.
name|TCP_DEFAULT_CONNECT_TIMEOUT
decl_stmt|;
try|try
block|{
if|if
condition|(
name|delay
operator|.
name|millis
argument_list|()
operator|<
name|connectingTimeout
operator|.
name|millis
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|delay
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|original
operator|.
name|connectToNodeLight
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|connectingTimeout
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"UNRESPONSIVE: simulated"
argument_list|)
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"UNRESPONSIVE: interrupted while sleeping"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|sendRequest
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|long
name|requestId
parameter_list|,
specifier|final
name|String
name|action
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
specifier|final
name|TransportRequestOptions
name|options
parameter_list|)
throws|throws
name|IOException
throws|,
name|TransportException
block|{
comment|// delayed sending - even if larger then the request timeout to simulated a potential late response from target node
name|TimeValue
name|delay
init|=
name|getDelay
argument_list|()
decl_stmt|;
if|if
condition|(
name|delay
operator|.
name|millis
argument_list|()
operator|<=
literal|0
condition|)
block|{
name|original
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|options
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// poor mans request cloning...
name|RequestHandlerRegistry
name|reg
init|=
name|MockTransportService
operator|.
name|this
operator|.
name|getRequestHandler
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|bStream
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|request
operator|.
name|writeTo
argument_list|(
name|bStream
argument_list|)
expr_stmt|;
specifier|final
name|TransportRequest
name|clonedRequest
init|=
name|reg
operator|.
name|newRequest
argument_list|()
decl_stmt|;
name|clonedRequest
operator|.
name|readFrom
argument_list|(
name|StreamInput
operator|.
name|wrap
argument_list|(
name|bStream
operator|.
name|bytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|delay
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
operator|new
name|AbstractRunnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to send delayed request"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|IOException
block|{
name|original
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|clonedRequest
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a new delegate transport that is used for communication with the given node.      *      * @return<tt>true</tt> iff no other delegate was registered for this node before, otherwise<tt>false</tt>      */
DECL|method|addDelegate
specifier|public
name|boolean
name|addDelegate
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|DelegateTransport
name|transport
parameter_list|)
block|{
return|return
name|transport
argument_list|()
operator|.
name|transports
operator|.
name|put
argument_list|(
name|node
operator|.
name|getAddress
argument_list|()
argument_list|,
name|transport
argument_list|)
operator|==
literal|null
return|;
block|}
DECL|method|transport
specifier|private
name|LookupTestTransport
name|transport
parameter_list|()
block|{
return|return
operator|(
name|LookupTestTransport
operator|)
name|transport
return|;
block|}
comment|/**      * A lookup transport that has a list of potential Transport implementations to delegate to for node operations,      * if none is registered, then the default one is used.      */
DECL|class|LookupTestTransport
specifier|private
specifier|static
class|class
name|LookupTestTransport
extends|extends
name|DelegateTransport
block|{
DECL|field|transports
specifier|final
name|ConcurrentMap
argument_list|<
name|TransportAddress
argument_list|,
name|Transport
argument_list|>
name|transports
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|method|LookupTestTransport
name|LookupTestTransport
parameter_list|(
name|Transport
name|transport
parameter_list|)
block|{
name|super
argument_list|(
name|transport
argument_list|)
expr_stmt|;
block|}
DECL|method|getTransport
specifier|private
name|Transport
name|getTransport
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|Transport
name|transport
init|=
name|transports
operator|.
name|get
argument_list|(
name|node
operator|.
name|getAddress
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|transport
operator|!=
literal|null
condition|)
block|{
return|return
name|transport
return|;
block|}
return|return
name|this
operator|.
name|transport
return|;
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
name|getTransport
argument_list|(
name|node
argument_list|)
operator|.
name|nodeConnected
argument_list|(
name|node
argument_list|)
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
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|getTransport
argument_list|(
name|node
argument_list|)
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|connectToNodeLight
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|getTransport
argument_list|(
name|node
argument_list|)
operator|.
name|connectToNodeLight
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
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
block|{
name|getTransport
argument_list|(
name|node
argument_list|)
operator|.
name|disconnectFromNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|getTransport
argument_list|(
name|node
argument_list|)
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * A pure delegate transport.      * Can be extracted to a common class if needed in other places in the codebase.      */
DECL|class|DelegateTransport
specifier|public
specifier|static
class|class
name|DelegateTransport
implements|implements
name|Transport
block|{
DECL|field|transport
specifier|protected
specifier|final
name|Transport
name|transport
decl_stmt|;
DECL|method|DelegateTransport
specifier|public
name|DelegateTransport
parameter_list|(
name|Transport
name|transport
parameter_list|)
block|{
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|transportServiceAdapter
specifier|public
name|void
name|transportServiceAdapter
parameter_list|(
name|TransportServiceAdapter
name|service
parameter_list|)
block|{
name|transport
operator|.
name|transportServiceAdapter
argument_list|(
name|service
argument_list|)
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
name|transport
operator|.
name|boundAddress
argument_list|()
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
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|transport
operator|.
name|addressesFromString
argument_list|(
name|address
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|addressSupported
specifier|public
name|boolean
name|addressSupported
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|TransportAddress
argument_list|>
name|address
parameter_list|)
block|{
return|return
name|transport
operator|.
name|addressSupported
argument_list|(
name|address
argument_list|)
return|;
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
name|transport
operator|.
name|nodeConnected
argument_list|(
name|node
argument_list|)
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
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|transport
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|connectToNodeLight
specifier|public
name|void
name|connectToNodeLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|transport
operator|.
name|connectToNodeLight
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
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
block|{
name|transport
operator|.
name|disconnectFromNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|transport
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|serverOpen
specifier|public
name|long
name|serverOpen
parameter_list|()
block|{
return|return
name|transport
operator|.
name|serverOpen
argument_list|()
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
name|transport
operator|.
name|lifecycleState
argument_list|()
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
name|transport
operator|.
name|addLifecycleListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
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
name|transport
operator|.
name|removeLifecycleListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|start
specifier|public
name|Transport
name|start
parameter_list|()
block|{
name|transport
operator|.
name|start
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|Transport
name|stop
parameter_list|()
block|{
name|transport
operator|.
name|stop
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
name|transport
operator|.
name|profileBoundAddresses
argument_list|()
return|;
block|}
block|}
DECL|field|activeTracers
name|List
argument_list|<
name|Tracer
argument_list|>
name|activeTracers
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|class|Tracer
specifier|public
specifier|static
class|class
name|Tracer
block|{
DECL|method|receivedRequest
specifier|public
name|void
name|receivedRequest
parameter_list|(
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|)
block|{         }
DECL|method|responseSent
specifier|public
name|void
name|responseSent
parameter_list|(
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|)
block|{         }
DECL|method|responseSent
specifier|public
name|void
name|responseSent
parameter_list|(
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{         }
DECL|method|receivedResponse
specifier|public
name|void
name|receivedResponse
parameter_list|(
name|long
name|requestId
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|String
name|action
parameter_list|)
block|{         }
DECL|method|requestSent
specifier|public
name|void
name|requestSent
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
name|TransportRequestOptions
name|options
parameter_list|)
block|{         }
block|}
DECL|method|addTracer
specifier|public
name|void
name|addTracer
parameter_list|(
name|Tracer
name|tracer
parameter_list|)
block|{
name|activeTracers
operator|.
name|add
argument_list|(
name|tracer
argument_list|)
expr_stmt|;
block|}
DECL|method|removeTracer
specifier|public
name|boolean
name|removeTracer
parameter_list|(
name|Tracer
name|tracer
parameter_list|)
block|{
return|return
name|activeTracers
operator|.
name|remove
argument_list|(
name|tracer
argument_list|)
return|;
block|}
DECL|method|clearTracers
specifier|public
name|void
name|clearTracers
parameter_list|()
block|{
name|activeTracers
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createAdapter
specifier|protected
name|Adapter
name|createAdapter
parameter_list|()
block|{
return|return
operator|new
name|MockAdapter
argument_list|()
return|;
block|}
DECL|class|MockAdapter
class|class
name|MockAdapter
extends|extends
name|Adapter
block|{
annotation|@
name|Override
DECL|method|traceEnabled
specifier|protected
name|boolean
name|traceEnabled
parameter_list|()
block|{
return|return
name|super
operator|.
name|traceEnabled
argument_list|()
operator|||
name|activeTracers
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|traceReceivedRequest
specifier|protected
name|void
name|traceReceivedRequest
parameter_list|(
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|)
block|{
name|super
operator|.
name|traceReceivedRequest
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|)
expr_stmt|;
for|for
control|(
name|Tracer
name|tracer
range|:
name|activeTracers
control|)
block|{
name|tracer
operator|.
name|receivedRequest
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|traceResponseSent
specifier|protected
name|void
name|traceResponseSent
parameter_list|(
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|)
block|{
name|super
operator|.
name|traceResponseSent
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|)
expr_stmt|;
for|for
control|(
name|Tracer
name|tracer
range|:
name|activeTracers
control|)
block|{
name|tracer
operator|.
name|responseSent
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|traceResponseSent
specifier|protected
name|void
name|traceResponseSent
parameter_list|(
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|super
operator|.
name|traceResponseSent
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|,
name|t
argument_list|)
expr_stmt|;
for|for
control|(
name|Tracer
name|tracer
range|:
name|activeTracers
control|)
block|{
name|tracer
operator|.
name|responseSent
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|traceReceivedResponse
specifier|protected
name|void
name|traceReceivedResponse
parameter_list|(
name|long
name|requestId
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|String
name|action
parameter_list|)
block|{
name|super
operator|.
name|traceReceivedResponse
argument_list|(
name|requestId
argument_list|,
name|sourceNode
argument_list|,
name|action
argument_list|)
expr_stmt|;
for|for
control|(
name|Tracer
name|tracer
range|:
name|activeTracers
control|)
block|{
name|tracer
operator|.
name|receivedResponse
argument_list|(
name|requestId
argument_list|,
name|sourceNode
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|traceRequestSent
specifier|protected
name|void
name|traceRequestSent
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
name|TransportRequestOptions
name|options
parameter_list|)
block|{
name|super
operator|.
name|traceRequestSent
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|options
argument_list|)
expr_stmt|;
for|for
control|(
name|Tracer
name|tracer
range|:
name|activeTracers
control|)
block|{
name|tracer
operator|.
name|requestSent
argument_list|(
name|node
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

