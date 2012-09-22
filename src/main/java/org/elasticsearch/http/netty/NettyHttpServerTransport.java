begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.http.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|http
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
name|ElasticSearchException
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
name|netty
operator|.
name|NettyStaticSetup
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
name|netty
operator|.
name|OpenChannelsHandler
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
name|network
operator|.
name|NetworkUtils
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
name|transport
operator|.
name|NetworkExceptionHelper
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
name|PortsRange
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
name|http
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
name|http
operator|.
name|HttpRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
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
name|BindTransportException
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
name|bootstrap
operator|.
name|ServerBootstrap
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
name|*
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
name|socket
operator|.
name|nio
operator|.
name|NioServerSocketChannelFactory
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
name|socket
operator|.
name|oio
operator|.
name|OioServerSocketChannelFactory
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
name|handler
operator|.
name|codec
operator|.
name|http
operator|.
name|*
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
name|handler
operator|.
name|timeout
operator|.
name|ReadTimeoutException
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
name|InetAddress
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
name|atomic
operator|.
name|AtomicReference
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
name|network
operator|.
name|NetworkService
operator|.
name|TcpSettings
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
operator|.
name|daemonThreadFactory
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NettyHttpServerTransport
specifier|public
class|class
name|NettyHttpServerTransport
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|HttpServerTransport
argument_list|>
implements|implements
name|HttpServerTransport
block|{
static|static
block|{
name|NettyStaticSetup
operator|.
name|setup
argument_list|()
expr_stmt|;
block|}
DECL|field|networkService
specifier|private
specifier|final
name|NetworkService
name|networkService
decl_stmt|;
DECL|field|maxContentLength
specifier|final
name|ByteSizeValue
name|maxContentLength
decl_stmt|;
DECL|field|maxInitialLineLength
specifier|final
name|ByteSizeValue
name|maxInitialLineLength
decl_stmt|;
DECL|field|maxHeaderSize
specifier|final
name|ByteSizeValue
name|maxHeaderSize
decl_stmt|;
DECL|field|maxChunkSize
specifier|final
name|ByteSizeValue
name|maxChunkSize
decl_stmt|;
DECL|field|workerCount
specifier|private
specifier|final
name|int
name|workerCount
decl_stmt|;
DECL|field|blockingServer
specifier|private
specifier|final
name|boolean
name|blockingServer
decl_stmt|;
DECL|field|compression
specifier|final
name|boolean
name|compression
decl_stmt|;
DECL|field|compressionLevel
specifier|private
specifier|final
name|int
name|compressionLevel
decl_stmt|;
DECL|field|resetCookies
specifier|final
name|boolean
name|resetCookies
decl_stmt|;
DECL|field|port
specifier|private
specifier|final
name|String
name|port
decl_stmt|;
DECL|field|bindHost
specifier|private
specifier|final
name|String
name|bindHost
decl_stmt|;
DECL|field|publishHost
specifier|private
specifier|final
name|String
name|publishHost
decl_stmt|;
DECL|field|tcpNoDelay
specifier|private
specifier|final
name|Boolean
name|tcpNoDelay
decl_stmt|;
DECL|field|tcpKeepAlive
specifier|private
specifier|final
name|Boolean
name|tcpKeepAlive
decl_stmt|;
DECL|field|reuseAddress
specifier|private
specifier|final
name|Boolean
name|reuseAddress
decl_stmt|;
DECL|field|tcpSendBufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|tcpSendBufferSize
decl_stmt|;
DECL|field|tcpReceiveBufferSize
specifier|private
specifier|final
name|ByteSizeValue
name|tcpReceiveBufferSize
decl_stmt|;
DECL|field|receiveBufferSizePredictorFactory
specifier|private
specifier|final
name|ReceiveBufferSizePredictorFactory
name|receiveBufferSizePredictorFactory
decl_stmt|;
DECL|field|maxCumulationBufferCapacity
specifier|final
name|ByteSizeValue
name|maxCumulationBufferCapacity
decl_stmt|;
DECL|field|maxCompositeBufferComponents
specifier|final
name|int
name|maxCompositeBufferComponents
decl_stmt|;
DECL|field|serverBootstrap
specifier|private
specifier|volatile
name|ServerBootstrap
name|serverBootstrap
decl_stmt|;
DECL|field|boundAddress
specifier|private
specifier|volatile
name|BoundTransportAddress
name|boundAddress
decl_stmt|;
DECL|field|serverChannel
specifier|private
specifier|volatile
name|Channel
name|serverChannel
decl_stmt|;
DECL|field|serverOpenChannels
name|OpenChannelsHandler
name|serverOpenChannels
decl_stmt|;
DECL|field|httpServerAdapter
specifier|private
specifier|volatile
name|HttpServerAdapter
name|httpServerAdapter
decl_stmt|;
annotation|@
name|Inject
DECL|method|NettyHttpServerTransport
specifier|public
name|NettyHttpServerTransport
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|networkService
operator|=
name|networkService
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"netty.epollBugWorkaround"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"org.jboss.netty.epollBugWorkaround"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
block|}
name|ByteSizeValue
name|maxContentLength
init|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_content_length"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
literal|"http.max_content_length"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|100
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|this
operator|.
name|maxChunkSize
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_chunk_size"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
literal|"http.max_chunk_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|8
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxHeaderSize
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_header_size"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
literal|"http.max_header_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|8
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxInitialLineLength
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_initial_line_length"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
literal|"http.max_initial_line_length"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|4
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// don't reset cookies by default, since I don't think we really need to
comment|// note, parsing cookies was fixed in netty 3.5.1 regarding stack allocation, but still, currently, we don't need cookies
name|this
operator|.
name|resetCookies
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"reset_cookies"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.reset_cookies"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxCumulationBufferCapacity
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"max_cumulation_buffer_capacity"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxCompositeBufferComponents
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"max_composite_buffer_components"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|workerCount
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"worker_count"
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|*
literal|2
argument_list|)
expr_stmt|;
name|this
operator|.
name|blockingServer
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.blocking_server"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|TCP_BLOCKING_SERVER
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|TCP_BLOCKING
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"port"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"http.port"
argument_list|,
literal|"9200-9300"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|bindHost
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"bind_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"http.bind_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"http.host"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|publishHost
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"publish_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"http.publish_host"
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"http.host"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|tcpNoDelay
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"tcp_no_delay"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|TCP_NO_DELAY
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|tcpKeepAlive
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"tcp_keep_alive"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|TCP_KEEP_ALIVE
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|reuseAddress
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"reuse_address"
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
name|TCP_REUSE_ADDRESS
argument_list|,
name|NetworkUtils
operator|.
name|defaultReuseAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|tcpSendBufferSize
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"tcp_send_buffer_size"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|TCP_SEND_BUFFER_SIZE
argument_list|,
name|TCP_DEFAULT_SEND_BUFFER_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|tcpReceiveBufferSize
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"tcp_receive_buffer_size"
argument_list|,
name|settings
operator|.
name|getAsBytesSize
argument_list|(
name|TCP_RECEIVE_BUFFER_SIZE
argument_list|,
name|TCP_DEFAULT_RECEIVE_BUFFER_SIZE
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|defaultReceiverPredictor
init|=
literal|512
operator|*
literal|1024
decl_stmt|;
if|if
condition|(
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|directMemoryMax
argument_list|()
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// we can guess a better default...
name|long
name|l
init|=
call|(
name|long
call|)
argument_list|(
operator|(
literal|0.3
operator|*
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|directMemoryMax
argument_list|()
operator|.
name|bytes
argument_list|()
operator|)
operator|/
name|workerCount
argument_list|)
decl_stmt|;
name|defaultReceiverPredictor
operator|=
name|Math
operator|.
name|min
argument_list|(
name|defaultReceiverPredictor
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|l
argument_list|,
literal|64
operator|*
literal|1024
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// See AdaptiveReceiveBufferSizePredictor#DEFAULT_XXX for default values in netty..., we can use higher ones for us, even fixed one
name|ByteSizeValue
name|receivePredictorMin
init|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"receive_predictor_min"
argument_list|,
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"receive_predictor_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|defaultReceiverPredictor
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|ByteSizeValue
name|receivePredictorMax
init|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"receive_predictor_max"
argument_list|,
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"receive_predictor_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|defaultReceiverPredictor
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|receivePredictorMax
operator|.
name|bytes
argument_list|()
operator|==
name|receivePredictorMin
operator|.
name|bytes
argument_list|()
condition|)
block|{
name|receiveBufferSizePredictorFactory
operator|=
operator|new
name|FixedReceiveBufferSizePredictorFactory
argument_list|(
operator|(
name|int
operator|)
name|receivePredictorMax
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|receiveBufferSizePredictorFactory
operator|=
operator|new
name|AdaptiveReceiveBufferSizePredictorFactory
argument_list|(
operator|(
name|int
operator|)
name|receivePredictorMin
operator|.
name|bytes
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|receivePredictorMin
operator|.
name|bytes
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|receivePredictorMax
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|compression
operator|=
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.compression"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|compressionLevel
operator|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"http.compression_level"
argument_list|,
literal|6
argument_list|)
expr_stmt|;
comment|// validate max content length
if|if
condition|(
name|maxContentLength
operator|.
name|bytes
argument_list|()
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"maxContentLength["
operator|+
name|maxContentLength
operator|+
literal|"] set to high value, resetting it to [100mb]"
argument_list|)
expr_stmt|;
name|maxContentLength
operator|=
operator|new
name|ByteSizeValue
argument_list|(
literal|100
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|maxContentLength
operator|=
name|maxContentLength
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"using max_chunk_size[{}], max_header_size[{}], max_initial_line_length[{}], max_content_length[{}], receive_predictor[{}->{}]"
argument_list|,
name|maxChunkSize
argument_list|,
name|maxHeaderSize
argument_list|,
name|maxInitialLineLength
argument_list|,
name|this
operator|.
name|maxContentLength
argument_list|,
name|receivePredictorMin
argument_list|,
name|receivePredictorMax
argument_list|)
expr_stmt|;
block|}
DECL|method|settings
specifier|public
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|this
operator|.
name|settings
return|;
block|}
DECL|method|httpServerAdapter
specifier|public
name|void
name|httpServerAdapter
parameter_list|(
name|HttpServerAdapter
name|httpServerAdapter
parameter_list|)
block|{
name|this
operator|.
name|httpServerAdapter
operator|=
name|httpServerAdapter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|this
operator|.
name|serverOpenChannels
operator|=
operator|new
name|OpenChannelsHandler
argument_list|(
name|logger
argument_list|)
expr_stmt|;
if|if
condition|(
name|blockingServer
condition|)
block|{
name|serverBootstrap
operator|=
operator|new
name|ServerBootstrap
argument_list|(
operator|new
name|OioServerSocketChannelFactory
argument_list|(
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"http_server_boss"
argument_list|)
argument_list|)
argument_list|,
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"http_server_worker"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|serverBootstrap
operator|=
operator|new
name|ServerBootstrap
argument_list|(
operator|new
name|NioServerSocketChannelFactory
argument_list|(
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"http_server_boss"
argument_list|)
argument_list|)
argument_list|,
name|Executors
operator|.
name|newCachedThreadPool
argument_list|(
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"http_server_worker"
argument_list|)
argument_list|)
argument_list|,
name|workerCount
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|serverBootstrap
operator|.
name|setPipelineFactory
argument_list|(
operator|new
name|MyChannelPipelineFactory
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|tcpNoDelay
operator|!=
literal|null
condition|)
block|{
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"child.tcpNoDelay"
argument_list|,
name|tcpNoDelay
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tcpKeepAlive
operator|!=
literal|null
condition|)
block|{
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"child.keepAlive"
argument_list|,
name|tcpKeepAlive
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tcpSendBufferSize
operator|!=
literal|null
operator|&&
name|tcpSendBufferSize
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"child.sendBufferSize"
argument_list|,
name|tcpSendBufferSize
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tcpReceiveBufferSize
operator|!=
literal|null
operator|&&
name|tcpReceiveBufferSize
operator|.
name|bytes
argument_list|()
operator|>
literal|0
condition|)
block|{
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"child.receiveBufferSize"
argument_list|,
name|tcpReceiveBufferSize
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"receiveBufferSizePredictorFactory"
argument_list|,
name|receiveBufferSizePredictorFactory
argument_list|)
expr_stmt|;
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"child.receiveBufferSizePredictorFactory"
argument_list|,
name|receiveBufferSizePredictorFactory
argument_list|)
expr_stmt|;
if|if
condition|(
name|reuseAddress
operator|!=
literal|null
condition|)
block|{
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"reuseAddress"
argument_list|,
name|reuseAddress
argument_list|)
expr_stmt|;
name|serverBootstrap
operator|.
name|setOption
argument_list|(
literal|"child.reuseAddress"
argument_list|,
name|reuseAddress
argument_list|)
expr_stmt|;
block|}
comment|// Bind and start to accept incoming connections.
name|InetAddress
name|hostAddressX
decl_stmt|;
try|try
block|{
name|hostAddressX
operator|=
name|networkService
operator|.
name|resolveBindHostAddress
argument_list|(
name|bindHost
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BindHttpException
argument_list|(
literal|"Failed to resolve host ["
operator|+
name|bindHost
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
specifier|final
name|InetAddress
name|hostAddress
init|=
name|hostAddressX
decl_stmt|;
name|PortsRange
name|portsRange
init|=
operator|new
name|PortsRange
argument_list|(
name|port
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|lastException
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|success
init|=
name|portsRange
operator|.
name|iterate
argument_list|(
operator|new
name|PortsRange
operator|.
name|PortCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|onPortNumber
parameter_list|(
name|int
name|portNumber
parameter_list|)
block|{
try|try
block|{
name|serverChannel
operator|=
name|serverBootstrap
operator|.
name|bind
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|hostAddress
argument_list|,
name|portNumber
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
throw|throw
operator|new
name|BindHttpException
argument_list|(
literal|"Failed to bind to ["
operator|+
name|port
operator|+
literal|"]"
argument_list|,
name|lastException
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
name|InetSocketAddress
name|boundAddress
init|=
operator|(
name|InetSocketAddress
operator|)
name|serverChannel
operator|.
name|getLocalAddress
argument_list|()
decl_stmt|;
name|InetSocketAddress
name|publishAddress
decl_stmt|;
try|try
block|{
name|publishAddress
operator|=
operator|new
name|InetSocketAddress
argument_list|(
name|networkService
operator|.
name|resolvePublishHostAddress
argument_list|(
name|publishHost
argument_list|)
argument_list|,
name|boundAddress
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|BindTransportException
argument_list|(
literal|"Failed to resolve publish address"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|boundAddress
operator|=
operator|new
name|BoundTransportAddress
argument_list|(
operator|new
name|InetSocketTransportAddress
argument_list|(
name|boundAddress
argument_list|)
argument_list|,
operator|new
name|InetSocketTransportAddress
argument_list|(
name|publishAddress
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|serverChannel
operator|!=
literal|null
condition|)
block|{
name|serverChannel
operator|.
name|close
argument_list|()
operator|.
name|awaitUninterruptibly
argument_list|()
expr_stmt|;
name|serverChannel
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|serverOpenChannels
operator|!=
literal|null
condition|)
block|{
name|serverOpenChannels
operator|.
name|close
argument_list|()
expr_stmt|;
name|serverOpenChannels
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|serverBootstrap
operator|!=
literal|null
condition|)
block|{
name|serverBootstrap
operator|.
name|releaseExternalResources
argument_list|()
expr_stmt|;
name|serverBootstrap
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|boundAddress
specifier|public
name|BoundTransportAddress
name|boundAddress
parameter_list|()
block|{
return|return
name|this
operator|.
name|boundAddress
return|;
block|}
annotation|@
name|Override
DECL|method|stats
specifier|public
name|HttpStats
name|stats
parameter_list|()
block|{
name|OpenChannelsHandler
name|channels
init|=
name|serverOpenChannels
decl_stmt|;
return|return
operator|new
name|HttpStats
argument_list|(
name|channels
operator|==
literal|null
condition|?
literal|0
else|:
name|channels
operator|.
name|numberOfOpenChannels
argument_list|()
argument_list|,
name|channels
operator|==
literal|null
condition|?
literal|0
else|:
name|channels
operator|.
name|totalChannels
argument_list|()
argument_list|)
return|;
block|}
DECL|method|dispatchRequest
name|void
name|dispatchRequest
parameter_list|(
name|HttpRequest
name|request
parameter_list|,
name|HttpChannel
name|channel
parameter_list|)
block|{
name|httpServerAdapter
operator|.
name|dispatchRequest
argument_list|(
name|request
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
DECL|method|exceptionCaught
name|void
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|ExceptionEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|e
operator|.
name|getCause
argument_list|()
operator|instanceof
name|ReadTimeoutException
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Connection timeout [{}]"
argument_list|,
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|getRemoteAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
comment|// ignore
return|return;
block|}
if|if
condition|(
operator|!
name|NetworkExceptionHelper
operator|.
name|isCloseConnectionException
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Caught exception while handling client http traffic, closing connection {}"
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|ctx
operator|.
name|getChannel
argument_list|()
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Caught exception while handling client http traffic, closing connection {}"
argument_list|,
name|e
operator|.
name|getCause
argument_list|()
argument_list|,
name|ctx
operator|.
name|getChannel
argument_list|()
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|MyChannelPipelineFactory
specifier|static
class|class
name|MyChannelPipelineFactory
implements|implements
name|ChannelPipelineFactory
block|{
DECL|field|transport
specifier|private
specifier|final
name|NettyHttpServerTransport
name|transport
decl_stmt|;
DECL|field|requestHandler
specifier|private
specifier|final
name|HttpRequestHandler
name|requestHandler
decl_stmt|;
DECL|method|MyChannelPipelineFactory
name|MyChannelPipelineFactory
parameter_list|(
name|NettyHttpServerTransport
name|transport
parameter_list|)
block|{
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
name|this
operator|.
name|requestHandler
operator|=
operator|new
name|HttpRequestHandler
argument_list|(
name|transport
argument_list|)
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
name|Channels
operator|.
name|pipeline
argument_list|()
decl_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"openChannels"
argument_list|,
name|transport
operator|.
name|serverOpenChannels
argument_list|)
expr_stmt|;
name|HttpRequestDecoder
name|requestDecoder
init|=
operator|new
name|HttpRequestDecoder
argument_list|(
operator|(
name|int
operator|)
name|transport
operator|.
name|maxInitialLineLength
operator|.
name|bytes
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|transport
operator|.
name|maxHeaderSize
operator|.
name|bytes
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|transport
operator|.
name|maxChunkSize
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|transport
operator|.
name|maxCumulationBufferCapacity
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|transport
operator|.
name|maxCumulationBufferCapacity
operator|.
name|bytes
argument_list|()
operator|>
name|Integer
operator|.
name|MAX_VALUE
condition|)
block|{
name|requestDecoder
operator|.
name|setMaxCumulationBufferCapacity
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|requestDecoder
operator|.
name|setMaxCumulationBufferCapacity
argument_list|(
operator|(
name|int
operator|)
name|transport
operator|.
name|maxCumulationBufferCapacity
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|transport
operator|.
name|maxCompositeBufferComponents
operator|!=
operator|-
literal|1
condition|)
block|{
name|requestDecoder
operator|.
name|setMaxCumulationBufferComponents
argument_list|(
name|transport
operator|.
name|maxCompositeBufferComponents
argument_list|)
expr_stmt|;
block|}
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"decoder"
argument_list|,
name|requestDecoder
argument_list|)
expr_stmt|;
if|if
condition|(
name|transport
operator|.
name|compression
condition|)
block|{
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"decoder_compress"
argument_list|,
operator|new
name|HttpContentDecompressor
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|HttpChunkAggregator
name|httpChunkAggregator
init|=
operator|new
name|HttpChunkAggregator
argument_list|(
operator|(
name|int
operator|)
name|transport
operator|.
name|maxContentLength
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|transport
operator|.
name|maxCompositeBufferComponents
operator|!=
operator|-
literal|1
condition|)
block|{
name|httpChunkAggregator
operator|.
name|setMaxCumulationBufferComponents
argument_list|(
name|transport
operator|.
name|maxCompositeBufferComponents
argument_list|)
expr_stmt|;
block|}
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"aggregator"
argument_list|,
name|httpChunkAggregator
argument_list|)
expr_stmt|;
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"encoder"
argument_list|,
operator|new
name|HttpResponseEncoder
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|transport
operator|.
name|compression
condition|)
block|{
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"encoder_compress"
argument_list|,
operator|new
name|HttpContentCompressor
argument_list|(
name|transport
operator|.
name|compressionLevel
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|pipeline
operator|.
name|addLast
argument_list|(
literal|"handler"
argument_list|,
name|requestHandler
argument_list|)
expr_stmt|;
return|return
name|pipeline
return|;
block|}
block|}
block|}
end_class

end_unit

