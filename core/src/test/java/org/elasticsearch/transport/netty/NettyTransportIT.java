begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty
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
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|health
operator|.
name|ClusterHealthStatus
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
name|NamedWriteableRegistry
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
name|logging
operator|.
name|ESLogger
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
name|NetworkModule
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
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|ClusterScope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|Scope
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
name|RequestHandlerRegistry
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
name|TransportSettings
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
name|org
operator|.
name|jboss
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPipelineFactory
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|NettyTransportIT
specifier|public
class|class
name|NettyTransportIT
extends|extends
name|ESIntegTestCase
block|{
comment|// static so we can use it in anonymous classes
DECL|field|channelProfileName
specifier|private
specifier|static
name|String
name|channelProfileName
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MODE_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"network"
argument_list|)
operator|.
name|put
argument_list|(
name|NetworkModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
literal|"exception-throwing"
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|ExceptionThrowingNettyTransport
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|testThatConnectionFailsAsIntended
specifier|public
name|void
name|testThatConnectionFailsAsIntended
parameter_list|()
throws|throws
name|Exception
block|{
name|Client
name|transportClient
init|=
name|internalCluster
argument_list|()
operator|.
name|transportClient
argument_list|()
decl_stmt|;
name|ClusterHealthResponse
name|clusterIndexHealths
init|=
name|transportClient
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterIndexHealths
operator|.
name|getStatus
argument_list|()
argument_list|,
name|is
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|transportClient
operator|.
name|filterWithHeader
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"ERROR"
argument_list|,
literal|"MY MESSAGE"
argument_list|)
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected exception, but didn't happen"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"MY MESSAGE"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|channelProfileName
argument_list|,
name|is
argument_list|(
name|TransportSettings
operator|.
name|DEFAULT_PROFILE
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"exception-throwing-netty-transport"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"an exception throwing transport for testing"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|NetworkModule
name|module
parameter_list|)
block|{
name|module
operator|.
name|registerTransport
argument_list|(
literal|"exception-throwing"
argument_list|,
name|ExceptionThrowingNettyTransport
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
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
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
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
argument_list|,
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configureServerChannelPipelineFactory
specifier|public
name|ChannelPipelineFactory
name|configureServerChannelPipelineFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|Settings
name|groupSettings
parameter_list|)
block|{
return|return
operator|new
name|ErrorPipelineFactory
argument_list|(
name|this
argument_list|,
name|name
argument_list|,
name|groupSettings
argument_list|)
return|;
block|}
DECL|class|ErrorPipelineFactory
specifier|private
specifier|static
class|class
name|ErrorPipelineFactory
extends|extends
name|ServerChannelPipelineFactory
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|ErrorPipelineFactory
specifier|public
name|ErrorPipelineFactory
parameter_list|(
name|ExceptionThrowingNettyTransport
name|exceptionThrowingNettyTransport
parameter_list|,
name|String
name|name
parameter_list|,
name|Settings
name|groupSettings
parameter_list|)
block|{
name|super
argument_list|(
name|exceptionThrowingNettyTransport
argument_list|,
name|name
argument_list|,
name|groupSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|exceptionThrowingNettyTransport
operator|.
name|logger
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPipeline
specifier|public
name|ChannelPipeline
name|getPipeline
parameter_list|()
throws|throws
name|Exception
block|{
name|ChannelPipeline
name|pipeline
init|=
name|super
operator|.
name|getPipeline
argument_list|()
decl_stmt|;
name|pipeline
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
name|nettyTransport
argument_list|,
name|logger
argument_list|,
name|TransportSettings
operator|.
name|DEFAULT_PROFILE
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
name|transport
argument_list|,
name|transportServiceAdapter
argument_list|,
name|action
argument_list|,
name|channel
argument_list|,
name|requestId
argument_list|,
name|version
argument_list|,
name|name
argument_list|)
decl_stmt|;
try|try
block|{
specifier|final
name|RequestHandlerRegistry
name|reg
init|=
name|transportServiceAdapter
operator|.
name|getRequestHandler
argument_list|(
name|action
argument_list|)
decl_stmt|;
if|if
condition|(
name|reg
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
name|reg
operator|.
name|newRequest
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
name|String
name|error
init|=
name|threadPool
operator|.
name|getThreadContext
argument_list|()
operator|.
name|getHeader
argument_list|(
literal|"ERROR"
argument_list|)
decl_stmt|;
if|if
condition|(
name|error
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
name|error
argument_list|)
throw|;
block|}
if|if
condition|(
name|reg
operator|.
name|getExecutor
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
name|reg
operator|.
name|processMessageReceived
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
name|reg
operator|.
name|getExecutor
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|RequestHandler
argument_list|(
name|reg
argument_list|,
name|request
argument_list|,
name|transportChannel
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
literal|"Failed to send error message back to client for action [{}]"
argument_list|,
name|e
argument_list|,
name|action
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
name|channelProfileName
operator|=
name|transportChannel
operator|.
name|getProfileName
argument_list|()
expr_stmt|;
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
name|RequestHandlerRegistry
name|reg
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
specifier|public
name|RequestHandler
parameter_list|(
name|RequestHandlerRegistry
name|reg
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
name|NettyTransportChannel
name|transportChannel
parameter_list|)
block|{
name|this
operator|.
name|reg
operator|=
name|reg
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
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|reg
operator|.
name|processMessageReceived
argument_list|(
name|request
argument_list|,
name|transportChannel
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isForceExecution
parameter_list|()
block|{
return|return
name|reg
operator|.
name|isForceExecution
argument_list|()
return|;
block|}
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
if|if
condition|(
name|transport
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
literal|"Failed to send error message back to client for action [{}]"
argument_list|,
name|e1
argument_list|,
name|reg
operator|.
name|getAction
argument_list|()
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
block|}
argument_list|)
expr_stmt|;
return|return
name|pipeline
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

