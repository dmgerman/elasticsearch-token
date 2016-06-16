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
name|lease
operator|.
name|Releasables
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
name|indices
operator|.
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|ESTestCase
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
name|transport
operator|.
name|MockTransportService
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
name|TestThreadPool
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
name|BaseTransportResponseHandler
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
name|TransportChannel
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
name|TransportResponseOptions
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
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
name|equalTo
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
name|greaterThan
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|NettyScheduledPingTests
specifier|public
class|class
name|NettyScheduledPingTests
extends|extends
name|ESTestCase
block|{
DECL|method|testScheduledPing
specifier|public
name|void
name|testScheduledPing
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|TestThreadPool
argument_list|(
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|NettyTransport
operator|.
name|PING_SCHEDULE
operator|.
name|getKey
argument_list|()
argument_list|,
literal|"5ms"
argument_list|)
operator|.
name|put
argument_list|(
name|TransportSettings
operator|.
name|PORT
operator|.
name|getKey
argument_list|()
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|CircuitBreakerService
name|circuitBreakerService
init|=
operator|new
name|NoneCircuitBreakerService
argument_list|()
decl_stmt|;
name|NamedWriteableRegistry
name|registryA
init|=
operator|new
name|NamedWriteableRegistry
argument_list|()
decl_stmt|;
specifier|final
name|NettyTransport
name|nettyA
init|=
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|registryA
argument_list|,
name|circuitBreakerService
argument_list|)
decl_stmt|;
name|ClusterName
name|test
init|=
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|MockTransportService
name|serviceA
init|=
operator|new
name|MockTransportService
argument_list|(
name|settings
argument_list|,
name|nettyA
argument_list|,
name|threadPool
argument_list|,
name|test
argument_list|)
decl_stmt|;
name|serviceA
operator|.
name|start
argument_list|()
expr_stmt|;
name|serviceA
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
name|NamedWriteableRegistry
name|registryB
init|=
operator|new
name|NamedWriteableRegistry
argument_list|()
decl_stmt|;
specifier|final
name|NettyTransport
name|nettyB
init|=
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
operator|new
name|NetworkService
argument_list|(
name|settings
argument_list|)
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|registryB
argument_list|,
name|circuitBreakerService
argument_list|)
decl_stmt|;
name|MockTransportService
name|serviceB
init|=
operator|new
name|MockTransportService
argument_list|(
name|settings
argument_list|,
name|nettyB
argument_list|,
name|threadPool
argument_list|,
name|test
argument_list|)
decl_stmt|;
name|serviceB
operator|.
name|start
argument_list|()
expr_stmt|;
name|serviceB
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
name|DiscoveryNode
name|nodeA
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"TS_A"
argument_list|,
literal|"TS_A"
argument_list|,
name|serviceA
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|nodeB
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"TS_B"
argument_list|,
literal|"TS_B"
argument_list|,
name|serviceB
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|serviceA
operator|.
name|connectToNode
argument_list|(
name|nodeB
argument_list|)
expr_stmt|;
name|serviceB
operator|.
name|connectToNode
argument_list|(
name|nodeA
argument_list|)
expr_stmt|;
name|assertBusy
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
name|assertThat
argument_list|(
name|nettyA
operator|.
name|scheduledPing
operator|.
name|successfulPings
operator|.
name|count
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nettyB
operator|.
name|scheduledPing
operator|.
name|successfulPings
operator|.
name|count
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|100L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nettyA
operator|.
name|scheduledPing
operator|.
name|failedPings
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nettyB
operator|.
name|scheduledPing
operator|.
name|failedPings
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|serviceA
operator|.
name|registerRequestHandler
argument_list|(
literal|"sayHello"
argument_list|,
name|TransportRequest
operator|.
name|Empty
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
operator|new
name|TransportRequestHandler
argument_list|<
name|TransportRequest
operator|.
name|Empty
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|TransportRequest
operator|.
name|Empty
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|,
name|TransportResponseOptions
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Unexpected failure"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
comment|// send some messages while ping requests are going around
name|int
name|rounds
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|rounds
condition|;
name|i
operator|++
control|)
block|{
name|serviceB
operator|.
name|submitRequest
argument_list|(
name|nodeA
argument_list|,
literal|"sayHello"
argument_list|,
name|TransportRequest
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|,
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withCompress
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|TransportResponse
operator|.
name|Empty
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TransportResponse
operator|.
name|Empty
name|newInstance
parameter_list|()
block|{
return|return
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
return|;
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TransportResponse
operator|.
name|Empty
name|response
parameter_list|)
block|{                         }
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
name|logger
operator|.
name|error
argument_list|(
literal|"Unexpected failure"
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"got exception instead of a response: "
operator|+
name|exp
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
name|assertBusy
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
name|assertThat
argument_list|(
name|nettyA
operator|.
name|scheduledPing
operator|.
name|successfulPings
operator|.
name|count
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|200L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nettyB
operator|.
name|scheduledPing
operator|.
name|successfulPings
operator|.
name|count
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|200L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nettyA
operator|.
name|scheduledPing
operator|.
name|failedPings
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nettyB
operator|.
name|scheduledPing
operator|.
name|failedPings
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|Releasables
operator|.
name|close
argument_list|(
name|serviceA
argument_list|,
name|serviceB
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

