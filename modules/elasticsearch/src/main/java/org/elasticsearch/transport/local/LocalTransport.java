begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.local
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|local
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|AbstractLifecycleComponent
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
name|ThrowableObjectInputStream
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
name|*
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
name|ImmutableSettings
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
name|LocalTransportAddress
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
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|support
operator|.
name|TransportStreams
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|Map
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
name|atomic
operator|.
name|AtomicLong
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|LocalTransport
specifier|public
class|class
name|LocalTransport
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|Transport
argument_list|>
implements|implements
name|Transport
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportServiceAdapter
specifier|private
specifier|volatile
name|TransportServiceAdapter
name|transportServiceAdapter
decl_stmt|;
DECL|field|boundAddress
specifier|private
specifier|volatile
name|BoundTransportAddress
name|boundAddress
decl_stmt|;
DECL|field|localAddress
specifier|private
specifier|volatile
name|LocalTransportAddress
name|localAddress
decl_stmt|;
DECL|field|transports
specifier|private
specifier|final
specifier|static
name|ConcurrentMap
argument_list|<
name|TransportAddress
argument_list|,
name|LocalTransport
argument_list|>
name|transports
init|=
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|transportAddressIdGenerator
specifier|private
specifier|static
specifier|final
name|AtomicLong
name|transportAddressIdGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|connectedNodes
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|DiscoveryNode
argument_list|,
name|LocalTransport
argument_list|>
name|connectedNodes
init|=
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|method|LocalTransport
specifier|public
name|LocalTransport
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|LocalTransport
annotation|@
name|Inject
specifier|public
name|LocalTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
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
block|}
DECL|method|addressesFromString
annotation|@
name|Override
specifier|public
name|TransportAddress
index|[]
name|addressesFromString
parameter_list|(
name|String
name|address
parameter_list|)
block|{
return|return
operator|new
name|TransportAddress
index|[]
block|{
operator|new
name|LocalTransportAddress
argument_list|(
name|address
argument_list|)
block|}
return|;
block|}
DECL|method|addressSupported
annotation|@
name|Override
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
name|LocalTransportAddress
operator|.
name|class
operator|.
name|equals
argument_list|(
name|address
argument_list|)
return|;
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|localAddress
operator|=
operator|new
name|LocalTransportAddress
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|transportAddressIdGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|transports
operator|.
name|put
argument_list|(
name|localAddress
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|boundAddress
operator|=
operator|new
name|BoundTransportAddress
argument_list|(
name|localAddress
argument_list|,
name|localAddress
argument_list|)
expr_stmt|;
block|}
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|transports
operator|.
name|remove
argument_list|(
name|localAddress
argument_list|)
expr_stmt|;
comment|// now, go over all the transports connected to me, and raise disconnected event
for|for
control|(
specifier|final
name|LocalTransport
name|targetTransport
range|:
name|transports
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
specifier|final
name|Map
operator|.
name|Entry
argument_list|<
name|DiscoveryNode
argument_list|,
name|LocalTransport
argument_list|>
name|entry
range|:
name|targetTransport
operator|.
name|connectedNodes
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|==
name|this
condition|)
block|{
name|targetTransport
operator|.
name|disconnectFromNode
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|transportServiceAdapter
annotation|@
name|Override
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
DECL|method|boundAddress
annotation|@
name|Override
specifier|public
name|BoundTransportAddress
name|boundAddress
parameter_list|()
block|{
return|return
name|boundAddress
return|;
block|}
DECL|method|nodeConnected
annotation|@
name|Override
specifier|public
name|boolean
name|nodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
return|return
name|connectedNodes
operator|.
name|containsKey
argument_list|(
name|node
argument_list|)
return|;
block|}
DECL|method|connectToNode
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
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|connectedNodes
operator|.
name|containsKey
argument_list|(
name|node
argument_list|)
condition|)
block|{
return|return;
block|}
specifier|final
name|LocalTransport
name|targetTransport
init|=
name|transports
operator|.
name|get
argument_list|(
name|node
operator|.
name|address
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|targetTransport
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ConnectTransportException
argument_list|(
name|node
argument_list|,
literal|"Failed to connect"
argument_list|)
throw|;
block|}
name|connectedNodes
operator|.
name|put
argument_list|(
name|node
argument_list|,
name|targetTransport
argument_list|)
expr_stmt|;
name|transportServiceAdapter
operator|.
name|raiseNodeConnected
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|disconnectFromNode
annotation|@
name|Override
specifier|public
name|void
name|disconnectFromNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|LocalTransport
name|removed
init|=
name|connectedNodes
operator|.
name|remove
argument_list|(
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
block|{
name|transportServiceAdapter
operator|.
name|raiseNodeDisconnected
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|sendRequest
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|Streamable
parameter_list|>
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
specifier|final
name|Streamable
name|message
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|)
throws|throws
name|IOException
throws|,
name|TransportException
block|{
name|HandlesStreamOutput
name|stream
init|=
name|CachedStreamOutput
operator|.
name|cachedHandlesBytes
argument_list|()
decl_stmt|;
name|stream
operator|.
name|writeLong
argument_list|(
name|requestId
argument_list|)
expr_stmt|;
name|byte
name|status
init|=
literal|0
decl_stmt|;
name|status
operator|=
name|TransportStreams
operator|.
name|statusSetRequest
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|stream
operator|.
name|writeByte
argument_list|(
name|status
argument_list|)
expr_stmt|;
comment|// 0 for request, 1 for response.
name|stream
operator|.
name|writeUTF
argument_list|(
name|action
argument_list|)
expr_stmt|;
name|message
operator|.
name|writeTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
specifier|final
name|LocalTransport
name|targetTransport
init|=
name|connectedNodes
operator|.
name|get
argument_list|(
name|node
argument_list|)
decl_stmt|;
if|if
condition|(
name|targetTransport
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NodeNotConnectedException
argument_list|(
name|node
argument_list|,
literal|"Node not connected"
argument_list|)
throw|;
block|}
specifier|final
name|byte
index|[]
name|data
init|=
operator|(
operator|(
name|BytesStreamOutput
operator|)
name|stream
operator|.
name|wrappedOut
argument_list|()
operator|)
operator|.
name|copiedByteArray
argument_list|()
decl_stmt|;
name|transportServiceAdapter
operator|.
name|sent
argument_list|(
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|threadPool
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
name|targetTransport
operator|.
name|messageReceived
argument_list|(
name|data
argument_list|,
name|action
argument_list|,
name|LocalTransport
operator|.
name|this
argument_list|,
name|requestId
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|threadPool
name|ThreadPool
name|threadPool
parameter_list|()
block|{
return|return
name|this
operator|.
name|threadPool
return|;
block|}
DECL|method|messageReceived
name|void
name|messageReceived
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|String
name|action
parameter_list|,
name|LocalTransport
name|sourceTransport
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Long
name|sendRequestId
parameter_list|)
block|{
name|transportServiceAdapter
operator|.
name|received
argument_list|(
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|StreamInput
name|stream
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|data
argument_list|)
decl_stmt|;
name|stream
operator|=
name|CachedStreamInput
operator|.
name|cachedHandles
argument_list|(
name|stream
argument_list|)
expr_stmt|;
try|try
block|{
name|long
name|requestId
init|=
name|stream
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|byte
name|status
init|=
name|stream
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|boolean
name|isRequest
init|=
name|TransportStreams
operator|.
name|statusIsRequest
argument_list|(
name|status
argument_list|)
decl_stmt|;
if|if
condition|(
name|isRequest
condition|)
block|{
name|handleRequest
argument_list|(
name|stream
argument_list|,
name|requestId
argument_list|,
name|sourceTransport
argument_list|)
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|TransportResponseHandler
name|handler
init|=
name|transportServiceAdapter
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
comment|// ignore if its null, the adapter logs it
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|TransportStreams
operator|.
name|statusIsError
argument_list|(
name|status
argument_list|)
condition|)
block|{
name|handlerResponseError
argument_list|(
name|stream
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|handleResponse
argument_list|(
name|stream
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|sendRequestId
operator|!=
literal|null
condition|)
block|{
name|TransportResponseHandler
name|handler
init|=
name|transportServiceAdapter
operator|.
name|remove
argument_list|(
name|sendRequestId
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|handler
operator|.
name|handleException
argument_list|(
operator|new
name|RemoteTransportException
argument_list|(
name|nodeName
argument_list|()
argument_list|,
name|localAddress
argument_list|,
name|action
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to receive message for action ["
operator|+
name|action
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|handleRequest
specifier|private
name|void
name|handleRequest
parameter_list|(
name|StreamInput
name|stream
parameter_list|,
name|long
name|requestId
parameter_list|,
name|LocalTransport
name|sourceTransport
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|String
name|action
init|=
name|stream
operator|.
name|readUTF
argument_list|()
decl_stmt|;
specifier|final
name|LocalTransportChannel
name|transportChannel
init|=
operator|new
name|LocalTransportChannel
argument_list|(
name|this
argument_list|,
name|sourceTransport
argument_list|,
name|action
argument_list|,
name|requestId
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|TransportRequestHandler
name|handler
init|=
name|transportServiceAdapter
operator|.
name|handler
argument_list|(
name|action
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ActionNotFoundTransportException
argument_list|(
literal|"Action ["
operator|+
name|action
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
specifier|final
name|Streamable
name|streamable
init|=
name|handler
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|streamable
operator|.
name|readFrom
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|handler
operator|.
name|messageReceived
argument_list|(
name|streamable
argument_list|,
name|transportChannel
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|transportChannel
operator|.
name|sendResponse
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to send error message back to client for action ["
operator|+
name|action
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"Actual Exception"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|handleResponse
specifier|private
name|void
name|handleResponse
parameter_list|(
name|StreamInput
name|buffer
parameter_list|,
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|)
block|{
specifier|final
name|Streamable
name|streamable
init|=
name|handler
operator|.
name|newInstance
argument_list|()
decl_stmt|;
try|try
block|{
name|streamable
operator|.
name|readFrom
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|handleException
argument_list|(
name|handler
argument_list|,
operator|new
name|TransportSerializationException
argument_list|(
literal|"Failed to deserialize response of type ["
operator|+
name|streamable
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|handler
operator|.
name|spawn
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|handler
operator|.
name|handleResponse
argument_list|(
name|streamable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|handleException
argument_list|(
name|handler
argument_list|,
operator|new
name|ResponseHandlerFailureTransportException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
comment|//noinspection unchecked
name|handler
operator|.
name|handleResponse
argument_list|(
name|streamable
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|handleException
argument_list|(
name|handler
argument_list|,
operator|new
name|ResponseHandlerFailureTransportException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|handlerResponseError
specifier|private
name|void
name|handlerResponseError
parameter_list|(
name|StreamInput
name|buffer
parameter_list|,
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|)
block|{
name|Throwable
name|error
decl_stmt|;
try|try
block|{
name|ThrowableObjectInputStream
name|ois
init|=
operator|new
name|ThrowableObjectInputStream
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|error
operator|=
operator|(
name|Throwable
operator|)
name|ois
operator|.
name|readObject
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|error
operator|=
operator|new
name|TransportSerializationException
argument_list|(
literal|"Failed to deserialize exception response from stream"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|handleException
argument_list|(
name|handler
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
DECL|method|handleException
specifier|private
name|void
name|handleException
parameter_list|(
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|error
operator|instanceof
name|RemoteTransportException
operator|)
condition|)
block|{
name|error
operator|=
operator|new
name|RemoteTransportException
argument_list|(
literal|"None remote transport exception"
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
specifier|final
name|RemoteTransportException
name|rtx
init|=
operator|(
name|RemoteTransportException
operator|)
name|error
decl_stmt|;
name|handler
operator|.
name|handleException
argument_list|(
name|rtx
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

