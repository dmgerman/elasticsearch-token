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
name|ElasticsearchException
import|;
end_import

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
name|inject
operator|.
name|AbstractModule
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
name|InetSocketTransportAddress
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
name|BigArrays
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
name|ActionNotFoundTransportException
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
name|TransportRequestHandler
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
name|netty
operator|.
name|MessageChannelHandler
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
name|netty
operator|.
name|NettyTransport
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
name|netty
operator|.
name|NettyTransportChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPipeline
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
name|InetSocketAddress
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ConfigurableErrorNettyTransportModule
specifier|public
class|class
name|ConfigurableErrorNettyTransportModule
extends|extends
name|AbstractModule
block|{
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|ExceptionThrowingNettyTransport
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|Transport
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|ExceptionThrowingNettyTransport
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
DECL|class|ExceptionThrowingNettyTransport
specifier|public
specifier|static
specifier|final
class|class
name|ExceptionThrowingNettyTransport
extends|extends
name|NettyTransport
block|{
annotation|@
name|Inject
DECL|method|ExceptionThrowingNettyTransport
specifier|public
name|ExceptionThrowingNettyTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|networkService
argument_list|,
name|bigArrays
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configureServerChannelPipeline
specifier|public
name|void
name|configureServerChannelPipeline
parameter_list|(
name|ChannelPipeline
name|channelPipeline
parameter_list|)
block|{
name|super
operator|.
name|configureServerChannelPipeline
argument_list|(
name|channelPipeline
argument_list|)
expr_stmt|;
name|channelPipeline
operator|.
name|replace
argument_list|(
literal|"dispatcher"
argument_list|,
literal|"dispatcher"
argument_list|,
operator|new
name|MessageChannelHandler
argument_list|(
name|this
argument_list|,
name|logger
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|String
name|handleRequest
parameter_list|(
name|Channel
name|channel
parameter_list|,
name|StreamInput
name|buffer
parameter_list|,
name|long
name|requestId
parameter_list|,
name|Version
name|version
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|action
init|=
name|buffer
operator|.
name|readString
argument_list|()
decl_stmt|;
specifier|final
name|NettyTransportChannel
name|transportChannel
init|=
operator|new
name|NettyTransportChannel
argument_list|(
name|ExceptionThrowingNettyTransport
operator|.
name|this
argument_list|,
name|action
argument_list|,
name|channel
argument_list|,
name|requestId
argument_list|,
name|version
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
name|action
argument_list|)
throw|;
block|}
specifier|final
name|TransportRequest
name|request
init|=
name|handler
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|request
operator|.
name|remoteAddress
argument_list|(
operator|new
name|InetSocketTransportAddress
argument_list|(
operator|(
name|InetSocketAddress
operator|)
name|channel
operator|.
name|getRemoteAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|readFrom
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|getHeaders
argument_list|()
operator|!=
literal|null
operator|&&
name|request
operator|.
name|getHeaders
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"ERROR"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
operator|(
name|String
operator|)
name|request
operator|.
name|getHeaders
argument_list|()
operator|.
name|get
argument_list|(
literal|"ERROR"
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|handler
operator|.
name|executor
argument_list|()
operator|==
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
condition|)
block|{
comment|//noinspection unchecked
name|handler
operator|.
name|messageReceived
argument_list|(
name|request
argument_list|,
name|transportChannel
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|handler
operator|.
name|executor
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|RequestHandler
argument_list|(
name|handler
argument_list|,
name|request
argument_list|,
name|transportChannel
argument_list|,
name|action
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
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
return|return
name|action
return|;
block|}
class|class
name|RequestHandler
extends|extends
name|AbstractRunnable
block|{
specifier|private
specifier|final
name|TransportRequestHandler
name|handler
decl_stmt|;
specifier|private
specifier|final
name|TransportRequest
name|request
decl_stmt|;
specifier|private
specifier|final
name|NettyTransportChannel
name|transportChannel
decl_stmt|;
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
specifier|public
name|RequestHandler
parameter_list|(
name|TransportRequestHandler
name|handler
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
name|NettyTransportChannel
name|transportChannel
parameter_list|,
name|String
name|action
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
name|request
operator|=
name|request
expr_stmt|;
name|this
operator|.
name|transportChannel
operator|=
name|transportChannel
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
block|}
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
name|messageReceived
argument_list|(
name|request
argument_list|,
name|transportChannel
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|ExceptionThrowingNettyTransport
operator|.
name|this
operator|.
name|lifecycleState
argument_list|()
operator|==
name|Lifecycle
operator|.
name|State
operator|.
name|STARTED
condition|)
block|{
comment|// we can only send a response transport is started....
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
name|Throwable
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
name|e1
argument_list|)
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"Actual Exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isForceExecution
parameter_list|()
block|{
return|return
name|handler
operator|.
name|isForceExecution
argument_list|()
return|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

