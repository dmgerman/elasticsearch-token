begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty4
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty4
package|;
end_package

begin_import
import|import
name|io
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
name|elasticsearch
operator|.
name|ESNetty4IntegTestCase
import|;
end_import

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
name|indices
operator|.
name|breaker
operator|.
name|CircuitBreakerService
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
name|TransportSettings
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
name|ArrayList
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
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|supportsDedicatedMasters
operator|=
literal|false
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|Netty4TransportIT
specifier|public
class|class
name|Netty4TransportIT
extends|extends
name|ESNetty4IntegTestCase
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
name|Settings
operator|.
name|builder
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
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|list
operator|.
name|add
argument_list|(
name|ExceptionThrowingNetty4Transport
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
name|list
operator|.
name|addAll
argument_list|(
name|super
operator|.
name|nodePlugins
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|Collections
operator|.
name|unmodifiableCollection
argument_list|(
name|list
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
DECL|class|ExceptionThrowingNetty4Transport
specifier|public
specifier|static
specifier|final
class|class
name|ExceptionThrowingNetty4Transport
extends|extends
name|Netty4Transport
block|{
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
block|{
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
name|ExceptionThrowingNetty4Transport
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Inject
DECL|method|ExceptionThrowingNetty4Transport
specifier|public
name|ExceptionThrowingNetty4Transport
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
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
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
name|namedWriteableRegistry
argument_list|,
name|circuitBreakerService
argument_list|)
expr_stmt|;
block|}
DECL|method|handleRequest
specifier|protected
name|String
name|handleRequest
parameter_list|(
name|Channel
name|channel
parameter_list|,
name|String
name|profileName
parameter_list|,
name|StreamInput
name|stream
parameter_list|,
name|long
name|requestId
parameter_list|,
name|int
name|messageLengthBytes
parameter_list|,
name|Version
name|version
parameter_list|,
name|InetSocketAddress
name|remoteAddress
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|action
init|=
name|super
operator|.
name|handleRequest
argument_list|(
name|channel
argument_list|,
name|profileName
argument_list|,
name|stream
argument_list|,
name|requestId
argument_list|,
name|messageLengthBytes
argument_list|,
name|version
argument_list|,
name|remoteAddress
argument_list|)
decl_stmt|;
name|channelProfileName
operator|=
name|TransportSettings
operator|.
name|DEFAULT_PROFILE
expr_stmt|;
return|return
name|action
return|;
block|}
annotation|@
name|Override
DECL|method|validateRequest
specifier|protected
name|void
name|validateRequest
parameter_list|(
name|StreamInput
name|buffer
parameter_list|,
name|long
name|requestId
parameter_list|,
name|String
name|action
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|validateRequest
argument_list|(
name|buffer
argument_list|,
name|requestId
argument_list|,
name|action
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
block|}
block|}
block|}
end_class

end_unit

