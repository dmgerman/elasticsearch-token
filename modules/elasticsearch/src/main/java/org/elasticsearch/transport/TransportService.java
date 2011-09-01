begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|collect
operator|.
name|ImmutableMap
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
name|collect
operator|.
name|MapBuilder
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
name|metrics
operator|.
name|MeanMetric
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMapLong
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
name|LinkedHashMap
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
name|CopyOnWriteArrayList
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
name|ScheduledFuture
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportService
specifier|public
class|class
name|TransportService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|TransportService
argument_list|>
block|{
DECL|field|transport
specifier|private
specifier|final
name|Transport
name|transport
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|serverHandlers
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|TransportRequestHandler
argument_list|>
name|serverHandlers
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|serverHandlersMutex
specifier|final
name|Object
name|serverHandlersMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|clientHandlers
specifier|final
name|ConcurrentMapLong
argument_list|<
name|RequestHolder
argument_list|>
name|clientHandlers
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMapLong
argument_list|()
decl_stmt|;
DECL|field|requestIds
specifier|final
name|AtomicLong
name|requestIds
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|connectionListeners
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|TransportConnectionListener
argument_list|>
name|connectionListeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|TransportConnectionListener
argument_list|>
argument_list|()
decl_stmt|;
comment|// An LRU (don't really care about concurrency here) that holds the latest timed out requests so if they
comment|// do show up, we can print more descriptive information about them
DECL|field|timeoutInfoHandlers
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|TimeoutInfoHolder
argument_list|>
name|timeoutInfoHandlers
init|=
name|Collections
operator|.
name|synchronizedMap
argument_list|(
operator|new
name|LinkedHashMap
argument_list|<
name|Long
argument_list|,
name|TimeoutInfoHolder
argument_list|>
argument_list|(
literal|100
argument_list|,
literal|.75F
argument_list|,
literal|true
argument_list|)
block|{
specifier|protected
name|boolean
name|removeEldestEntry
parameter_list|(
name|Map
operator|.
name|Entry
name|eldest
parameter_list|)
block|{
return|return
name|size
argument_list|()
operator|>
literal|100
return|;
block|}
block|}
argument_list|)
decl_stmt|;
DECL|field|throwConnectException
specifier|private
name|boolean
name|throwConnectException
init|=
literal|false
decl_stmt|;
DECL|field|adapter
specifier|private
specifier|final
name|TransportService
operator|.
name|Adapter
name|adapter
init|=
operator|new
name|Adapter
argument_list|()
decl_stmt|;
DECL|method|TransportService
specifier|public
name|TransportService
parameter_list|(
name|Transport
name|transport
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|this
argument_list|(
name|EMPTY_SETTINGS
argument_list|,
name|transport
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|TransportService
annotation|@
name|Inject
specifier|public
name|TransportService
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
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
name|adapter
operator|.
name|rxMetric
operator|.
name|clear
argument_list|()
expr_stmt|;
name|adapter
operator|.
name|txMetric
operator|.
name|clear
argument_list|()
expr_stmt|;
name|transport
operator|.
name|transportServiceAdapter
argument_list|(
name|adapter
argument_list|)
expr_stmt|;
name|transport
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
name|transport
operator|.
name|boundAddress
argument_list|()
operator|!=
literal|null
operator|&&
name|logger
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"{}"
argument_list|,
name|transport
operator|.
name|boundAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|transport
operator|.
name|stop
argument_list|()
expr_stmt|;
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
block|{
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
DECL|method|info
specifier|public
name|TransportInfo
name|info
parameter_list|()
block|{
return|return
operator|new
name|TransportInfo
argument_list|(
name|boundAddress
argument_list|()
argument_list|)
return|;
block|}
DECL|method|stats
specifier|public
name|TransportStats
name|stats
parameter_list|()
block|{
return|return
operator|new
name|TransportStats
argument_list|(
name|transport
operator|.
name|serverOpen
argument_list|()
argument_list|,
name|adapter
operator|.
name|rxMetric
operator|.
name|count
argument_list|()
argument_list|,
name|adapter
operator|.
name|rxMetric
operator|.
name|sum
argument_list|()
argument_list|,
name|adapter
operator|.
name|txMetric
operator|.
name|count
argument_list|()
argument_list|,
name|adapter
operator|.
name|txMetric
operator|.
name|sum
argument_list|()
argument_list|)
return|;
block|}
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
DECL|method|addConnectionListener
specifier|public
name|void
name|addConnectionListener
parameter_list|(
name|TransportConnectionListener
name|listener
parameter_list|)
block|{
name|connectionListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|removeConnectionListener
specifier|public
name|void
name|removeConnectionListener
parameter_list|(
name|TransportConnectionListener
name|listener
parameter_list|)
block|{
name|connectionListeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
comment|/**      * Set to<tt>true</tt> to indicate that a {@link ConnectTransportException} should be thrown when      * sending a message (otherwise, it will be passed to the response handler). Defaults to<tt>false</tt>.      *      *<p>This is useful when logic based on connect failure is needed without having to wrap the handler,      * for example, in case of retries across several nodes.      */
DECL|method|throwConnectException
specifier|public
name|void
name|throwConnectException
parameter_list|(
name|boolean
name|throwConnectException
parameter_list|)
block|{
name|this
operator|.
name|throwConnectException
operator|=
name|throwConnectException
expr_stmt|;
block|}
DECL|method|submitRequest
specifier|public
parameter_list|<
name|T
extends|extends
name|Streamable
parameter_list|>
name|TransportFuture
argument_list|<
name|T
argument_list|>
name|submitRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|action
parameter_list|,
name|Streamable
name|message
parameter_list|,
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|)
throws|throws
name|TransportException
block|{
return|return
name|submitRequest
argument_list|(
name|node
argument_list|,
name|action
argument_list|,
name|message
argument_list|,
name|TransportRequestOptions
operator|.
name|EMPTY
argument_list|,
name|handler
argument_list|)
return|;
block|}
DECL|method|submitRequest
specifier|public
parameter_list|<
name|T
extends|extends
name|Streamable
parameter_list|>
name|TransportFuture
argument_list|<
name|T
argument_list|>
name|submitRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|action
parameter_list|,
name|Streamable
name|message
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|,
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|)
throws|throws
name|TransportException
block|{
name|PlainTransportFuture
argument_list|<
name|T
argument_list|>
name|futureHandler
init|=
operator|new
name|PlainTransportFuture
argument_list|<
name|T
argument_list|>
argument_list|(
name|handler
argument_list|)
decl_stmt|;
name|sendRequest
argument_list|(
name|node
argument_list|,
name|action
argument_list|,
name|message
argument_list|,
name|options
argument_list|,
name|futureHandler
argument_list|)
expr_stmt|;
return|return
name|futureHandler
return|;
block|}
DECL|method|sendRequest
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
name|String
name|action
parameter_list|,
specifier|final
name|Streamable
name|message
parameter_list|,
specifier|final
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|)
throws|throws
name|TransportException
block|{
name|sendRequest
argument_list|(
name|node
argument_list|,
name|action
argument_list|,
name|message
argument_list|,
name|TransportRequestOptions
operator|.
name|EMPTY
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
DECL|method|sendRequest
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
name|String
name|action
parameter_list|,
specifier|final
name|Streamable
name|message
parameter_list|,
specifier|final
name|TransportRequestOptions
name|options
parameter_list|,
specifier|final
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|)
throws|throws
name|TransportException
block|{
specifier|final
name|long
name|requestId
init|=
name|newRequestId
argument_list|()
decl_stmt|;
name|TimeoutHandler
name|timeoutHandler
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|options
operator|.
name|timeout
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|timeoutHandler
operator|=
operator|new
name|TimeoutHandler
argument_list|(
name|requestId
argument_list|)
expr_stmt|;
name|timeoutHandler
operator|.
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|options
operator|.
name|timeout
argument_list|()
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
argument_list|,
name|timeoutHandler
argument_list|)
expr_stmt|;
block|}
name|clientHandlers
operator|.
name|put
argument_list|(
name|requestId
argument_list|,
operator|new
name|RequestHolder
argument_list|<
name|T
argument_list|>
argument_list|(
name|handler
argument_list|,
name|node
argument_list|,
name|action
argument_list|,
name|timeoutHandler
argument_list|)
argument_list|)
expr_stmt|;
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
name|message
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|Exception
name|e
parameter_list|)
block|{
comment|// usually happen either because we failed to connect to the node
comment|// or because we failed serializing the message
name|clientHandlers
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
expr_stmt|;
if|if
condition|(
name|timeoutHandler
operator|!=
literal|null
condition|)
block|{
name|timeoutHandler
operator|.
name|future
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|throwConnectException
condition|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ConnectTransportException
condition|)
block|{
throw|throw
operator|(
name|ConnectTransportException
operator|)
name|e
throw|;
block|}
block|}
comment|// callback that an exception happened, but on a different thread since we don't
comment|// want handlers to worry about stack overflows
specifier|final
name|SendRequestTransportException
name|sendRequestException
init|=
operator|new
name|SendRequestTransportException
argument_list|(
name|node
argument_list|,
name|action
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|CACHED
argument_list|)
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
name|handler
operator|.
name|handleException
argument_list|(
name|sendRequestException
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|newRequestId
specifier|private
name|long
name|newRequestId
parameter_list|()
block|{
return|return
name|requestIds
operator|.
name|getAndIncrement
argument_list|()
return|;
block|}
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
DECL|method|registerHandler
specifier|public
name|void
name|registerHandler
parameter_list|(
name|ActionTransportRequestHandler
name|handler
parameter_list|)
block|{
name|registerHandler
argument_list|(
name|handler
operator|.
name|action
argument_list|()
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
DECL|method|registerHandler
specifier|public
name|void
name|registerHandler
parameter_list|(
name|String
name|action
parameter_list|,
name|TransportRequestHandler
name|handler
parameter_list|)
block|{
synchronized|synchronized
init|(
name|serverHandlersMutex
init|)
block|{
name|TransportRequestHandler
name|handlerReplaced
init|=
name|serverHandlers
operator|.
name|get
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|serverHandlers
operator|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|(
name|serverHandlers
argument_list|)
operator|.
name|put
argument_list|(
name|action
argument_list|,
name|handler
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
if|if
condition|(
name|handlerReplaced
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Registered two transport handlers for action {}, handlers: {}, {}"
argument_list|,
name|action
argument_list|,
name|handler
argument_list|,
name|handlerReplaced
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|removeHandler
specifier|public
name|void
name|removeHandler
parameter_list|(
name|String
name|action
parameter_list|)
block|{
synchronized|synchronized
init|(
name|serverHandlersMutex
init|)
block|{
name|serverHandlers
operator|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|(
name|serverHandlers
argument_list|)
operator|.
name|remove
argument_list|(
name|action
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|Adapter
class|class
name|Adapter
implements|implements
name|TransportServiceAdapter
block|{
DECL|field|rxMetric
specifier|final
name|MeanMetric
name|rxMetric
init|=
operator|new
name|MeanMetric
argument_list|()
decl_stmt|;
DECL|field|txMetric
specifier|final
name|MeanMetric
name|txMetric
init|=
operator|new
name|MeanMetric
argument_list|()
decl_stmt|;
DECL|method|received
annotation|@
name|Override
specifier|public
name|void
name|received
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|rxMetric
operator|.
name|inc
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
DECL|method|sent
annotation|@
name|Override
specifier|public
name|void
name|sent
parameter_list|(
name|long
name|size
parameter_list|)
block|{
name|txMetric
operator|.
name|inc
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
DECL|method|handler
annotation|@
name|Override
specifier|public
name|TransportRequestHandler
name|handler
parameter_list|(
name|String
name|action
parameter_list|)
block|{
return|return
name|serverHandlers
operator|.
name|get
argument_list|(
name|action
argument_list|)
return|;
block|}
DECL|method|remove
annotation|@
name|Override
specifier|public
name|TransportResponseHandler
name|remove
parameter_list|(
name|long
name|requestId
parameter_list|)
block|{
name|RequestHolder
name|holder
init|=
name|clientHandlers
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
if|if
condition|(
name|holder
operator|==
literal|null
condition|)
block|{
comment|// lets see if its in the timeout holder
name|TimeoutInfoHolder
name|timeoutInfoHolder
init|=
name|timeoutInfoHandlers
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeoutInfoHolder
operator|!=
literal|null
condition|)
block|{
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"Received response for a request that has timed out, sent [{}ms] ago, timed out [{}ms] ago, action [{}], node [{}], id [{}]"
argument_list|,
name|time
operator|-
name|timeoutInfoHolder
operator|.
name|sentTime
argument_list|()
argument_list|,
name|time
operator|-
name|timeoutInfoHolder
operator|.
name|timeoutTime
argument_list|()
argument_list|,
name|timeoutInfoHolder
operator|.
name|action
argument_list|()
argument_list|,
name|timeoutInfoHolder
operator|.
name|node
argument_list|()
argument_list|,
name|requestId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Transport response handler not found of id [{}]"
argument_list|,
name|requestId
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
name|holder
operator|.
name|cancel
argument_list|()
expr_stmt|;
return|return
name|holder
operator|.
name|handler
argument_list|()
return|;
block|}
DECL|method|raiseNodeConnected
annotation|@
name|Override
specifier|public
name|void
name|raiseNodeConnected
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|threadPool
operator|.
name|cached
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
name|TransportConnectionListener
name|connectionListener
range|:
name|connectionListeners
control|)
block|{
name|connectionListener
operator|.
name|onNodeConnected
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|raiseNodeDisconnected
annotation|@
name|Override
specifier|public
name|void
name|raiseNodeDisconnected
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|threadPool
operator|.
name|cached
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
name|TransportConnectionListener
name|connectionListener
range|:
name|connectionListeners
control|)
block|{
name|connectionListener
operator|.
name|onNodeDisconnected
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
comment|// node got disconnected, raise disconnection on possible ongoing handlers
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|RequestHolder
argument_list|>
name|entry
range|:
name|clientHandlers
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|RequestHolder
name|holder
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|holder
operator|.
name|node
argument_list|()
operator|.
name|equals
argument_list|(
name|node
argument_list|)
condition|)
block|{
specifier|final
name|RequestHolder
name|holderToNotify
init|=
name|clientHandlers
operator|.
name|remove
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|holderToNotify
operator|!=
literal|null
condition|)
block|{
comment|// callback that an exception happened, but on a different thread since we don't
comment|// want handlers to worry about stack overflows
name|threadPool
operator|.
name|cached
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
name|holderToNotify
operator|.
name|handler
argument_list|()
operator|.
name|handleException
argument_list|(
operator|new
name|NodeDisconnectedException
argument_list|(
name|node
argument_list|,
name|holderToNotify
operator|.
name|action
argument_list|()
argument_list|)
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
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TimeoutHandler
class|class
name|TimeoutHandler
implements|implements
name|Runnable
block|{
DECL|field|requestId
specifier|private
specifier|final
name|long
name|requestId
decl_stmt|;
DECL|field|sentTime
specifier|private
specifier|final
name|long
name|sentTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
DECL|field|future
name|ScheduledFuture
name|future
decl_stmt|;
DECL|method|TimeoutHandler
name|TimeoutHandler
parameter_list|(
name|long
name|requestId
parameter_list|)
block|{
name|this
operator|.
name|requestId
operator|=
name|requestId
expr_stmt|;
block|}
DECL|method|sentTime
specifier|public
name|long
name|sentTime
parameter_list|()
block|{
return|return
name|sentTime
return|;
block|}
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|future
operator|.
name|isCancelled
argument_list|()
condition|)
block|{
return|return;
block|}
specifier|final
name|RequestHolder
name|holder
init|=
name|clientHandlers
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
if|if
condition|(
name|holder
operator|!=
literal|null
condition|)
block|{
comment|// add it to the timeout information holder, in case we are going to get a response later
name|long
name|timeoutTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|timeoutInfoHandlers
operator|.
name|put
argument_list|(
name|requestId
argument_list|,
operator|new
name|TimeoutInfoHolder
argument_list|(
name|holder
operator|.
name|node
argument_list|()
argument_list|,
name|holder
operator|.
name|action
argument_list|()
argument_list|,
name|sentTime
argument_list|,
name|timeoutTime
argument_list|)
argument_list|)
expr_stmt|;
name|holder
operator|.
name|handler
argument_list|()
operator|.
name|handleException
argument_list|(
operator|new
name|ReceiveTimeoutTransportException
argument_list|(
name|holder
operator|.
name|node
argument_list|()
argument_list|,
name|holder
operator|.
name|action
argument_list|()
argument_list|,
literal|"request_id ["
operator|+
name|requestId
operator|+
literal|"] timed out after ["
operator|+
operator|(
name|timeoutTime
operator|-
name|sentTime
operator|)
operator|+
literal|"ms]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|TimeoutInfoHolder
specifier|static
class|class
name|TimeoutInfoHolder
block|{
DECL|field|node
specifier|private
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
DECL|field|sentTime
specifier|private
specifier|final
name|long
name|sentTime
decl_stmt|;
DECL|field|timeoutTime
specifier|private
specifier|final
name|long
name|timeoutTime
decl_stmt|;
DECL|method|TimeoutInfoHolder
name|TimeoutInfoHolder
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|action
parameter_list|,
name|long
name|sentTime
parameter_list|,
name|long
name|timeoutTime
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|sentTime
operator|=
name|sentTime
expr_stmt|;
name|this
operator|.
name|timeoutTime
operator|=
name|timeoutTime
expr_stmt|;
block|}
DECL|method|node
specifier|public
name|DiscoveryNode
name|node
parameter_list|()
block|{
return|return
name|node
return|;
block|}
DECL|method|action
specifier|public
name|String
name|action
parameter_list|()
block|{
return|return
name|action
return|;
block|}
DECL|method|sentTime
specifier|public
name|long
name|sentTime
parameter_list|()
block|{
return|return
name|sentTime
return|;
block|}
DECL|method|timeoutTime
specifier|public
name|long
name|timeoutTime
parameter_list|()
block|{
return|return
name|timeoutTime
return|;
block|}
block|}
DECL|class|RequestHolder
specifier|static
class|class
name|RequestHolder
parameter_list|<
name|T
extends|extends
name|Streamable
parameter_list|>
block|{
DECL|field|handler
specifier|private
specifier|final
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
decl_stmt|;
DECL|field|node
specifier|private
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
DECL|field|timeout
specifier|private
specifier|final
name|TimeoutHandler
name|timeout
decl_stmt|;
DECL|method|RequestHolder
name|RequestHolder
parameter_list|(
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|,
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|action
parameter_list|,
name|TimeoutHandler
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
DECL|method|handler
specifier|public
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|()
block|{
return|return
name|handler
return|;
block|}
DECL|method|node
specifier|public
name|DiscoveryNode
name|node
parameter_list|()
block|{
return|return
name|this
operator|.
name|node
return|;
block|}
DECL|method|action
specifier|public
name|String
name|action
parameter_list|()
block|{
return|return
name|this
operator|.
name|action
return|;
block|}
DECL|method|cancel
specifier|public
name|void
name|cancel
parameter_list|()
block|{
if|if
condition|(
name|timeout
operator|!=
literal|null
condition|)
block|{
name|timeout
operator|.
name|future
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

