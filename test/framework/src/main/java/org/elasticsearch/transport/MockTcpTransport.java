begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
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
name|bytes
operator|.
name|BytesReference
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
name|InputStreamStreamInput
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
name|common
operator|.
name|util
operator|.
name|CancellableThreads
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
name|EsExecutors
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|io
operator|.
name|OutputStream
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
name|net
operator|.
name|ServerSocket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Socket
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketException
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
name|ConcurrentHashMap
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
name|Executor
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
name|ExecutorService
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
name|Executors
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
name|RejectedExecutionException
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
name|TimeUnit
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
name|AtomicBoolean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_comment
comment|/**  * This is a socket based blocking TcpTransport implementation that is used for tests  * that need real networking. This implementation is a test only implementation that implements  * the networking layer in the worst possible way since it blocks and uses a thread per request model.  */
end_comment

begin_class
DECL|class|MockTcpTransport
specifier|public
class|class
name|MockTcpTransport
extends|extends
name|TcpTransport
argument_list|<
name|MockTcpTransport
operator|.
name|MockChannel
argument_list|>
block|{
DECL|field|executor
specifier|private
specifier|final
name|ExecutorService
name|executor
decl_stmt|;
DECL|field|mockVersion
specifier|private
specifier|final
name|Version
name|mockVersion
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockTcpTransport
specifier|public
name|MockTcpTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|bigArrays
argument_list|,
name|circuitBreakerService
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|networkService
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
expr_stmt|;
block|}
DECL|method|MockTcpTransport
specifier|public
name|MockTcpTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|Version
name|mockVersion
parameter_list|)
block|{
name|super
argument_list|(
literal|"mock-tcp-transport"
argument_list|,
name|settings
argument_list|,
name|threadPool
argument_list|,
name|bigArrays
argument_list|,
name|circuitBreakerService
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|networkService
argument_list|)
expr_stmt|;
comment|// we have our own crazy cached threadpool this one is not bounded at all...
comment|// using the ES thread factory here is crucial for tests otherwise disruption tests won't block that thread
name|executor
operator|=
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
name|Transports
operator|.
name|TEST_MOCK_TRANSPORT_THREAD_PREFIX
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|mockVersion
operator|=
name|mockVersion
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLocalAddress
specifier|protected
name|InetSocketAddress
name|getLocalAddress
parameter_list|(
name|MockChannel
name|mockChannel
parameter_list|)
block|{
return|return
name|mockChannel
operator|.
name|localAddress
return|;
block|}
annotation|@
name|Override
DECL|method|bind
specifier|protected
name|MockChannel
name|bind
parameter_list|(
specifier|final
name|String
name|name
parameter_list|,
name|InetSocketAddress
name|address
parameter_list|)
throws|throws
name|IOException
block|{
name|ServerSocket
name|socket
init|=
operator|new
name|ServerSocket
argument_list|()
decl_stmt|;
name|socket
operator|.
name|bind
argument_list|(
name|address
argument_list|)
expr_stmt|;
name|socket
operator|.
name|setReuseAddress
argument_list|(
name|TCP_REUSE_ADDRESS
operator|.
name|get
argument_list|(
name|settings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ByteSizeValue
name|tcpReceiveBufferSize
init|=
name|TCP_RECEIVE_BUFFER_SIZE
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|tcpReceiveBufferSize
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
name|socket
operator|.
name|setReceiveBufferSize
argument_list|(
name|tcpReceiveBufferSize
operator|.
name|bytesAsInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MockChannel
name|serverMockChannel
init|=
operator|new
name|MockChannel
argument_list|(
name|socket
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|CountDownLatch
name|started
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|executor
operator|.
name|execute
argument_list|(
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
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|onException
argument_list|(
name|serverMockChannel
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed on handling exception"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|started
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|serverMockChannel
operator|.
name|accept
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
try|try
block|{
name|started
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
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
block|}
return|return
name|serverMockChannel
return|;
block|}
DECL|method|readMessage
specifier|private
name|void
name|readMessage
parameter_list|(
name|MockChannel
name|mockChannel
parameter_list|,
name|StreamInput
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|Socket
name|socket
init|=
name|mockChannel
operator|.
name|activeChannel
decl_stmt|;
name|byte
index|[]
name|minimalHeader
init|=
operator|new
name|byte
index|[
name|TcpHeader
operator|.
name|MARKER_BYTES_SIZE
index|]
decl_stmt|;
name|int
name|firstByte
init|=
name|input
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|firstByte
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Connection reset by peer"
argument_list|)
throw|;
block|}
name|minimalHeader
index|[
literal|0
index|]
operator|=
operator|(
name|byte
operator|)
name|firstByte
expr_stmt|;
name|minimalHeader
index|[
literal|1
index|]
operator|=
operator|(
name|byte
operator|)
name|input
operator|.
name|read
argument_list|()
expr_stmt|;
name|int
name|msgSize
init|=
name|input
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|msgSize
operator|==
operator|-
literal|1
condition|)
block|{
name|socket
operator|.
name|getOutputStream
argument_list|()
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|BytesStreamOutput
name|output
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|msgSize
index|]
decl_stmt|;
name|input
operator|.
name|readFully
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|minimalHeader
argument_list|)
expr_stmt|;
name|output
operator|.
name|writeInt
argument_list|(
name|msgSize
argument_list|)
expr_stmt|;
name|output
operator|.
name|write
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
specifier|final
name|BytesReference
name|bytes
init|=
name|output
operator|.
name|bytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|TcpTransport
operator|.
name|validateMessageHeader
argument_list|(
name|bytes
argument_list|)
condition|)
block|{
name|InetSocketAddress
name|remoteAddress
init|=
operator|(
name|InetSocketAddress
operator|)
name|socket
operator|.
name|getRemoteSocketAddress
argument_list|()
decl_stmt|;
name|messageReceived
argument_list|(
name|bytes
operator|.
name|slice
argument_list|(
name|TcpHeader
operator|.
name|MARKER_BYTES_SIZE
operator|+
name|TcpHeader
operator|.
name|MESSAGE_LENGTH_SIZE
argument_list|,
name|msgSize
argument_list|)
argument_list|,
name|mockChannel
argument_list|,
name|mockChannel
operator|.
name|profile
argument_list|,
name|remoteAddress
argument_list|,
name|msgSize
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// ping message - we just drop all stuff
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|connectToChannelsLight
specifier|protected
name|NodeChannels
name|connectToChannelsLight
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|connectToChannels
argument_list|(
name|node
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|connectToChannels
specifier|protected
name|NodeChannels
name|connectToChannels
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|NodeChannels
name|nodeChannels
init|=
operator|new
name|NodeChannels
argument_list|(
operator|new
name|MockChannel
index|[
literal|1
index|]
argument_list|,
operator|new
name|MockChannel
index|[
literal|1
index|]
argument_list|,
operator|new
name|MockChannel
index|[
literal|1
index|]
argument_list|,
operator|new
name|MockChannel
index|[
literal|1
index|]
argument_list|,
operator|new
name|MockChannel
index|[
literal|1
index|]
argument_list|)
decl_stmt|;
name|boolean
name|success
init|=
literal|false
decl_stmt|;
specifier|final
name|Socket
name|socket
init|=
operator|new
name|Socket
argument_list|()
decl_stmt|;
try|try
block|{
name|Consumer
argument_list|<
name|MockChannel
argument_list|>
name|onClose
init|=
parameter_list|(
name|channel
parameter_list|)
lambda|->
block|{
specifier|final
name|NodeChannels
name|connected
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
name|connected
operator|!=
literal|null
operator|&&
name|connected
operator|.
name|hasChannel
argument_list|(
name|channel
argument_list|)
condition|)
block|{
try|try
block|{
name|executor
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
name|disconnectFromNode
argument_list|(
name|node
argument_list|,
name|channel
argument_list|,
literal|"channel closed event"
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RejectedExecutionException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to run disconnectFromNode - node is shutting down"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|InetSocketAddress
name|address
init|=
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|node
operator|.
name|getAddress
argument_list|()
operator|)
operator|.
name|address
argument_list|()
decl_stmt|;
comment|// we just use a single connections
name|configureSocket
argument_list|(
name|socket
argument_list|)
expr_stmt|;
name|socket
operator|.
name|connect
argument_list|(
name|address
argument_list|,
operator|(
name|int
operator|)
name|TCP_CONNECT_TIMEOUT
operator|.
name|get
argument_list|(
name|settings
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|MockChannel
name|channel
init|=
operator|new
name|MockChannel
argument_list|(
name|socket
argument_list|,
name|address
argument_list|,
literal|"none"
argument_list|,
name|onClose
argument_list|)
decl_stmt|;
name|channel
operator|.
name|loopRead
argument_list|(
name|executor
argument_list|)
expr_stmt|;
for|for
control|(
name|MockChannel
index|[]
name|channels
range|:
name|nodeChannels
operator|.
name|getChannelArrays
argument_list|()
control|)
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
name|channels
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|channels
index|[
name|i
index|]
operator|=
name|channel
expr_stmt|;
block|}
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|success
operator|==
literal|false
condition|)
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|nodeChannels
argument_list|,
name|socket
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|nodeChannels
return|;
block|}
DECL|method|configureSocket
specifier|private
name|void
name|configureSocket
parameter_list|(
name|Socket
name|socket
parameter_list|)
throws|throws
name|SocketException
block|{
name|socket
operator|.
name|setTcpNoDelay
argument_list|(
name|TCP_NO_DELAY
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|ByteSizeValue
name|tcpSendBufferSize
init|=
name|TCP_SEND_BUFFER_SIZE
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|tcpSendBufferSize
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
name|socket
operator|.
name|setSendBufferSize
argument_list|(
name|tcpSendBufferSize
operator|.
name|bytesAsInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ByteSizeValue
name|tcpReceiveBufferSize
init|=
name|TCP_RECEIVE_BUFFER_SIZE
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|tcpReceiveBufferSize
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
name|socket
operator|.
name|setReceiveBufferSize
argument_list|(
name|tcpReceiveBufferSize
operator|.
name|bytesAsInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|socket
operator|.
name|setReuseAddress
argument_list|(
name|TCP_REUSE_ADDRESS
operator|.
name|get
argument_list|(
name|settings
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isOpen
specifier|protected
name|boolean
name|isOpen
parameter_list|(
name|MockChannel
name|mockChannel
parameter_list|)
block|{
return|return
name|mockChannel
operator|.
name|isOpen
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|sendMessage
specifier|protected
name|void
name|sendMessage
parameter_list|(
name|MockChannel
name|mockChannel
parameter_list|,
name|BytesReference
name|reference
parameter_list|,
name|Runnable
name|sendListener
parameter_list|,
name|boolean
name|close
parameter_list|)
throws|throws
name|IOException
block|{
synchronized|synchronized
init|(
name|mockChannel
init|)
block|{
specifier|final
name|Socket
name|socket
init|=
name|mockChannel
operator|.
name|activeChannel
decl_stmt|;
name|OutputStream
name|outputStream
init|=
operator|new
name|BufferedOutputStream
argument_list|(
name|socket
operator|.
name|getOutputStream
argument_list|()
argument_list|)
decl_stmt|;
name|reference
operator|.
name|writeTo
argument_list|(
name|outputStream
argument_list|)
expr_stmt|;
name|outputStream
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|sendListener
operator|!=
literal|null
condition|)
block|{
name|sendListener
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|close
condition|)
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|mockChannel
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|closeChannels
specifier|protected
name|void
name|closeChannels
parameter_list|(
name|List
argument_list|<
name|MockChannel
argument_list|>
name|channel
parameter_list|)
throws|throws
name|IOException
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|channel
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
literal|1
return|;
block|}
DECL|class|MockChannel
specifier|public
specifier|final
class|class
name|MockChannel
implements|implements
name|Closeable
block|{
DECL|field|isOpen
specifier|private
specifier|final
name|AtomicBoolean
name|isOpen
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
DECL|field|localAddress
specifier|private
specifier|final
name|InetSocketAddress
name|localAddress
decl_stmt|;
DECL|field|serverSocket
specifier|private
specifier|final
name|ServerSocket
name|serverSocket
decl_stmt|;
DECL|field|workerChannels
specifier|private
specifier|final
name|ConcurrentHashMap
argument_list|<
name|MockChannel
argument_list|,
name|Boolean
argument_list|>
name|workerChannels
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|activeChannel
specifier|private
specifier|final
name|Socket
name|activeChannel
decl_stmt|;
DECL|field|profile
specifier|private
specifier|final
name|String
name|profile
decl_stmt|;
DECL|field|cancellableThreads
specifier|private
specifier|final
name|CancellableThreads
name|cancellableThreads
init|=
operator|new
name|CancellableThreads
argument_list|()
decl_stmt|;
DECL|field|onClose
specifier|private
specifier|final
name|Closeable
name|onClose
decl_stmt|;
DECL|method|MockChannel
specifier|public
name|MockChannel
parameter_list|(
name|Socket
name|socket
parameter_list|,
name|InetSocketAddress
name|localAddress
parameter_list|,
name|String
name|profile
parameter_list|,
name|Consumer
argument_list|<
name|MockChannel
argument_list|>
name|onClose
parameter_list|)
block|{
name|this
operator|.
name|localAddress
operator|=
name|localAddress
expr_stmt|;
name|this
operator|.
name|activeChannel
operator|=
name|socket
expr_stmt|;
name|this
operator|.
name|serverSocket
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|profile
operator|=
name|profile
expr_stmt|;
name|this
operator|.
name|onClose
operator|=
parameter_list|()
lambda|->
name|onClose
operator|.
name|accept
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|accept
specifier|public
name|void
name|accept
parameter_list|(
name|Executor
name|executor
parameter_list|)
throws|throws
name|IOException
block|{
while|while
condition|(
name|isOpen
operator|.
name|get
argument_list|()
condition|)
block|{
name|Socket
name|accept
init|=
name|serverSocket
operator|.
name|accept
argument_list|()
decl_stmt|;
name|configureSocket
argument_list|(
name|accept
argument_list|)
expr_stmt|;
name|MockChannel
name|mockChannel
init|=
operator|new
name|MockChannel
argument_list|(
name|accept
argument_list|,
name|localAddress
argument_list|,
name|profile
argument_list|,
name|workerChannels
operator|::
name|remove
argument_list|)
decl_stmt|;
name|workerChannels
operator|.
name|put
argument_list|(
name|mockChannel
argument_list|,
name|Boolean
operator|.
name|TRUE
argument_list|)
expr_stmt|;
name|mockChannel
operator|.
name|loopRead
argument_list|(
name|executor
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|loopRead
specifier|public
name|void
name|loopRead
parameter_list|(
name|Executor
name|executor
parameter_list|)
block|{
name|executor
operator|.
name|execute
argument_list|(
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
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|isOpen
operator|.
name|get
argument_list|()
condition|)
block|{
try|try
block|{
name|onException
argument_list|(
name|MockChannel
operator|.
name|this
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed on handling exception"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|StreamInput
name|input
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
operator|new
name|BufferedInputStream
argument_list|(
name|activeChannel
operator|.
name|getInputStream
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
while|while
condition|(
name|isOpen
operator|.
name|get
argument_list|()
condition|)
block|{
name|cancellableThreads
operator|.
name|executeIO
argument_list|(
parameter_list|()
lambda|->
name|readMessage
argument_list|(
name|MockChannel
operator|.
name|this
argument_list|,
name|input
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|MockChannel
specifier|public
name|MockChannel
parameter_list|(
name|ServerSocket
name|serverSocket
parameter_list|,
name|String
name|profile
parameter_list|)
block|{
name|this
operator|.
name|localAddress
operator|=
operator|(
name|InetSocketAddress
operator|)
name|serverSocket
operator|.
name|getLocalSocketAddress
argument_list|()
expr_stmt|;
name|this
operator|.
name|serverSocket
operator|=
name|serverSocket
expr_stmt|;
name|this
operator|.
name|profile
operator|=
name|profile
expr_stmt|;
name|this
operator|.
name|activeChannel
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|onClose
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|isOpen
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|IOUtils
operator|.
name|close
argument_list|(
parameter_list|()
lambda|->
name|cancellableThreads
operator|.
name|cancel
argument_list|(
literal|"channel closed"
argument_list|)
argument_list|,
name|serverSocket
argument_list|,
name|activeChannel
argument_list|,
parameter_list|()
lambda|->
name|IOUtils
operator|.
name|close
argument_list|(
name|workerChannels
operator|.
name|keySet
argument_list|()
argument_list|)
argument_list|,
name|onClose
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
if|if
condition|(
name|NetworkService
operator|.
name|NETWORK_SERVER
operator|.
name|get
argument_list|(
name|settings
argument_list|)
condition|)
block|{
comment|// loop through all profiles and start them up, special handling for default one
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|entry
range|:
name|buildProfileSettings
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
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
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|bindServer
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
block|}
name|super
operator|.
name|doStart
argument_list|()
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|success
operator|==
literal|false
condition|)
block|{
name|doStop
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|stopInternal
specifier|protected
name|void
name|stopInternal
parameter_list|()
block|{
name|ThreadPool
operator|.
name|terminate
argument_list|(
name|executor
argument_list|,
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getCurrentVersion
specifier|protected
name|Version
name|getCurrentVersion
parameter_list|()
block|{
return|return
name|mockVersion
return|;
block|}
block|}
end_class

end_unit

