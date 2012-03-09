begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|Bytes
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequestOptions
operator|.
name|options
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|BenchmarkNettyLargeMessages
specifier|public
class|class
name|BenchmarkNettyLargeMessages
block|{
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
throws|throws
name|InterruptedException
block|{
specifier|final
name|ByteSizeValue
name|payloadSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|10
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_ITERATIONS
init|=
literal|100000
decl_stmt|;
specifier|final
name|int
name|NUMBER_OF_CLIENTS
init|=
literal|5
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
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|()
decl_stmt|;
specifier|final
name|TransportService
name|transportServiceServer
init|=
operator|new
name|TransportService
argument_list|(
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
argument_list|,
name|threadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|TransportService
name|transportServiceClient
init|=
operator|new
name|TransportService
argument_list|(
operator|new
name|NettyTransport
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
argument_list|,
name|threadPool
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
specifier|final
name|DiscoveryNode
name|bigNode
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"big"
argument_list|,
operator|new
name|InetSocketTransportAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|9300
argument_list|)
argument_list|)
decl_stmt|;
comment|//        final DiscoveryNode smallNode = new DiscoveryNode("small", new InetSocketTransportAddress("localhost", 9300));
specifier|final
name|DiscoveryNode
name|smallNode
init|=
name|bigNode
decl_stmt|;
name|transportServiceClient
operator|.
name|connectToNode
argument_list|(
name|bigNode
argument_list|)
expr_stmt|;
name|transportServiceClient
operator|.
name|connectToNode
argument_list|(
name|smallNode
argument_list|)
expr_stmt|;
name|transportServiceServer
operator|.
name|registerHandler
argument_list|(
literal|"benchmark"
argument_list|,
operator|new
name|BaseTransportRequestHandler
argument_list|<
name|BenchmarkMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BenchmarkMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BenchmarkMessage
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
name|GENERIC
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|BenchmarkMessage
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
name|request
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|NUMBER_OF_CLIENTS
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
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBER_OF_ITERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|BenchmarkMessage
name|message
init|=
operator|new
name|BenchmarkMessage
argument_list|(
literal|1
argument_list|,
name|payload
argument_list|)
decl_stmt|;
name|transportServiceClient
operator|.
name|submitRequest
argument_list|(
name|bigNode
argument_list|,
literal|"benchmark"
argument_list|,
name|message
argument_list|,
name|options
argument_list|()
operator|.
name|withLowType
argument_list|()
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|BenchmarkMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BenchmarkMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BenchmarkMessage
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
name|BenchmarkMessage
name|response
parameter_list|)
block|{                             }
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
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
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
name|i
init|=
literal|0
init|;
name|i
operator|<
name|NUMBER_OF_ITERATIONS
condition|;
name|i
operator|++
control|)
block|{
name|BenchmarkMessage
name|message
init|=
operator|new
name|BenchmarkMessage
argument_list|(
literal|2
argument_list|,
name|Bytes
operator|.
name|EMPTY_ARRAY
argument_list|)
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|transportServiceClient
operator|.
name|submitRequest
argument_list|(
name|smallNode
argument_list|,
literal|"benchmark"
argument_list|,
name|message
argument_list|,
name|options
argument_list|()
operator|.
name|withHighType
argument_list|()
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|BenchmarkMessage
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|BenchmarkMessage
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|BenchmarkMessage
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
name|BenchmarkMessage
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
name|long
name|took
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Took "
operator|+
name|took
operator|+
literal|"ms"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

