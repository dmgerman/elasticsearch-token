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
name|com
operator|.
name|google
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
name|Node
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
name|util
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
name|util
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
name|util
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
name|util
operator|.
name|concurrent
operator|.
name|highscalelib
operator|.
name|NonBlockingHashMapLong
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
name|util
operator|.
name|transport
operator|.
name|BoundTransportAddress
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMaps
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|AbstractComponent
implements|implements
name|LifecycleComponent
argument_list|<
name|TransportService
argument_list|>
block|{
DECL|field|lifecycle
specifier|private
specifier|final
name|Lifecycle
name|lifecycle
init|=
operator|new
name|Lifecycle
argument_list|()
decl_stmt|;
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
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|TransportRequestHandler
argument_list|>
name|serverHandlers
init|=
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|clientHandlers
specifier|private
specifier|final
name|NonBlockingHashMapLong
argument_list|<
name|TransportResponseHandler
argument_list|>
name|clientHandlers
init|=
operator|new
name|NonBlockingHashMapLong
argument_list|<
name|TransportResponseHandler
argument_list|>
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
DECL|field|throwConnectException
specifier|private
name|boolean
name|throwConnectException
init|=
literal|false
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
DECL|method|lifecycleState
annotation|@
name|Override
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
name|this
operator|.
name|lifecycle
operator|.
name|state
argument_list|()
return|;
block|}
DECL|method|start
specifier|public
name|TransportService
name|start
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStarted
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
comment|// register us as an adapter for the transport service
name|transport
operator|.
name|transportServiceAdapter
argument_list|(
operator|new
name|TransportServiceAdapter
argument_list|()
block|{
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
return|return
name|clientHandlers
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
return|;
block|}
block|}
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
return|return
name|this
return|;
block|}
DECL|method|stop
specifier|public
name|TransportService
name|stop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStopped
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
name|transport
operator|.
name|stop
argument_list|()
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
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToClosed
argument_list|()
condition|)
block|{
return|return;
block|}
name|transport
operator|.
name|close
argument_list|()
expr_stmt|;
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
DECL|method|nodesAdded
specifier|public
name|void
name|nodesAdded
parameter_list|(
name|Iterable
argument_list|<
name|Node
argument_list|>
name|nodes
parameter_list|)
block|{
try|try
block|{
name|transport
operator|.
name|nodesAdded
argument_list|(
name|nodes
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
name|warn
argument_list|(
literal|"Failed add nodes ["
operator|+
name|nodes
operator|+
literal|"] to transport"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|nodesRemoved
specifier|public
name|void
name|nodesRemoved
parameter_list|(
name|Iterable
argument_list|<
name|Node
argument_list|>
name|nodes
parameter_list|)
block|{
try|try
block|{
name|transport
operator|.
name|nodesRemoved
argument_list|(
name|nodes
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
name|warn
argument_list|(
literal|"Failed to remove nodes["
operator|+
name|nodes
operator|+
literal|"] from transport"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
name|Node
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
name|Node
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
specifier|final
name|long
name|requestId
init|=
name|newRequestId
argument_list|()
decl_stmt|;
try|try
block|{
name|clientHandlers
operator|.
name|put
argument_list|(
name|requestId
argument_list|,
name|handler
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
name|handler
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
name|handler
operator|.
name|handleException
argument_list|(
operator|new
name|SendRequestTransportException
argument_list|(
name|node
argument_list|,
name|action
argument_list|,
name|e
argument_list|)
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
name|serverHandlers
operator|.
name|put
argument_list|(
name|action
argument_list|,
name|handler
argument_list|)
expr_stmt|;
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
name|serverHandlers
operator|.
name|remove
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

