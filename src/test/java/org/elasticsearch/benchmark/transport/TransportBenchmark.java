begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
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
name|StopWatch
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
name|unit
operator|.
name|ByteSizeUnit
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
name|ByteSizeValue
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
name|local
operator|.
name|LocalTransport
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportBenchmark
specifier|public
class|class
name|TransportBenchmark
block|{
DECL|enum|Type
specifier|static
enum|enum
name|Type
block|{
DECL|enum constant|LOCAL
name|LOCAL
block|{
annotation|@
name|Override
specifier|public
name|Transport
name|newTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
return|return
operator|new
name|LocalTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|NETTY
name|NETTY
block|{
annotation|@
name|Override
specifier|public
name|Transport
name|newTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
return|return
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
name|Settings
operator|.
name|EMPTY
argument_list|)
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
return|;
block|}
block|}
block|;
DECL|method|newTransport
specifier|public
specifier|abstract
name|Transport
name|newTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
function_decl|;
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
specifier|final
name|String
name|executor
init|=
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
decl_stmt|;
specifier|final
name|boolean
name|waitForRequest
init|=
literal|true
decl_stmt|;
specifier|final
name|ByteSizeValue
name|payloadSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|100
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_CLIENTS
init|=
literal|10
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_ITERATIONS
init|=
literal|100000
decl_stmt|;
specifier|final
name|byte
index|[]
name|payload
init|=
operator|new
name|byte
index|[
operator|(
name|int
operator|)
name|payloadSize
operator|.
name|bytes
argument_list|()
index|]
decl_stmt|;
specifier|final
name|AtomicLong
name|idGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|Type
name|type
init|=
name|Type
operator|.
name|NETTY
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ThreadPool
name|serverThreadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"server"
argument_list|)
decl_stmt|;
specifier|final
name|TransportService
name|serverTransportService
init|=
operator|new
name|TransportService
argument_list|(
name|type
operator|.
name|newTransport
argument_list|(
name|settings
argument_list|,
name|serverThreadPool
argument_list|)
argument_list|,
name|serverThreadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|ThreadPool
name|clientThreadPool
init|=
operator|new
name|ThreadPool
argument_list|(
literal|"client"
argument_list|)
decl_stmt|;
specifier|final
name|TransportService
name|clientTransportService
init|=
operator|new
name|TransportService
argument_list|(
name|type
operator|.
name|newTransport
argument_list|(
name|settings
argument_list|,
name|clientThreadPool
argument_list|)
argument_list|,
name|clientThreadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"server"
argument_list|,
name|serverTransportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|serverTransportService
operator|.
name|registerRequestHandler
argument_list|(
literal|"benchmark"
argument_list|,
name|BenchmarkMessageRequest
operator|.
name|class
argument_list|,
name|executor
argument_list|,
operator|new
name|TransportRequestHandler
argument_list|<
name|BenchmarkMessageRequest
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|BenchmarkMessageRequest
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|BenchmarkMessageResponse
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|clientTransportService
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10000
condition|;
name|i
operator|++
control|)
block|{
name|BenchmarkMessageRequest
name|message
init|=
operator|new
name|BenchmarkMessageRequest
argument_list|(
literal|1
argument_list|,
name|payload
argument_list|)
decl_stmt|;
name|clientTransportService
operator|.
name|submitRequest
argument_list|(
name|node
argument_list|,
literal|"benchmark"
argument_list|,
name|message
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|BenchmarkMessageResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BenchmarkMessageResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BenchmarkMessageResponse
argument_list|()
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
name|SAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|BenchmarkMessageResponse
name|response
parameter_list|)
block|{                 }
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
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
name|Thread
index|[]
name|clients
init|=
operator|new
name|Thread
index|[
name|NUMBER_OF_CLIENTS
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|NUMBER_OF_CLIENTS
operator|*
name|NUMBER_OF_ITERATIONS
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
name|NUMBER_OF_CLIENTS
condition|;
name|i
operator|++
control|)
block|{
name|clients
index|[
name|i
index|]
operator|=
operator|new
name|Thread
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
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|NUMBER_OF_ITERATIONS
condition|;
name|j
operator|++
control|)
block|{
specifier|final
name|long
name|id
init|=
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|BenchmarkMessageRequest
name|request
init|=
operator|new
name|BenchmarkMessageRequest
argument_list|(
name|id
argument_list|,
name|payload
argument_list|)
decl_stmt|;
name|BaseTransportResponseHandler
argument_list|<
name|BenchmarkMessageResponse
argument_list|>
name|handler
init|=
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|BenchmarkMessageResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BenchmarkMessageResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BenchmarkMessageResponse
argument_list|()
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
name|executor
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|BenchmarkMessageResponse
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|.
name|id
argument_list|()
operator|!=
name|id
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"NO ID MATCH ["
operator|+
name|response
operator|.
name|id
argument_list|()
operator|+
literal|"] and ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
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
name|exp
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
if|if
condition|(
name|waitForRequest
condition|)
block|{
name|clientTransportService
operator|.
name|submitRequest
argument_list|(
name|node
argument_list|,
literal|"benchmark"
argument_list|,
name|request
argument_list|,
name|handler
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|clientTransportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
literal|"benchmark"
argument_list|,
name|request
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
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
name|NUMBER_OF_CLIENTS
condition|;
name|i
operator|++
control|)
block|{
name|clients
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Ran ["
operator|+
name|NUMBER_OF_CLIENTS
operator|+
literal|"], each with ["
operator|+
name|NUMBER_OF_ITERATIONS
operator|+
literal|"] iterations, payload ["
operator|+
name|payloadSize
operator|+
literal|"]: took ["
operator|+
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|+
literal|"], TPS: "
operator|+
operator|(
name|NUMBER_OF_CLIENTS
operator|*
name|NUMBER_OF_ITERATIONS
operator|)
operator|/
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|secondsFrac
argument_list|()
argument_list|)
expr_stmt|;
name|clientTransportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|clientThreadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|serverTransportService
operator|.
name|close
argument_list|()
expr_stmt|;
name|serverThreadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

