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
name|buffer
operator|.
name|ByteBuf
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelDuplexHandler
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelHandlerContext
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|channel
operator|.
name|ChannelPromise
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|ReferenceCountUtil
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
name|transport
operator|.
name|TcpHeader
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
name|TransportServiceAdapter
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
name|Transports
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
comment|/**  * A handler (must be the last one!) that does size based frame decoding and forwards the actual message  * to the relevant action.  */
end_comment

begin_class
DECL|class|Netty4MessageChannelHandler
specifier|final
class|class
name|Netty4MessageChannelHandler
extends|extends
name|ChannelDuplexHandler
block|{
DECL|field|transportServiceAdapter
specifier|private
specifier|final
name|TransportServiceAdapter
name|transportServiceAdapter
decl_stmt|;
DECL|field|transport
specifier|private
specifier|final
name|Netty4Transport
name|transport
decl_stmt|;
DECL|field|profileName
specifier|private
specifier|final
name|String
name|profileName
decl_stmt|;
DECL|method|Netty4MessageChannelHandler
name|Netty4MessageChannelHandler
parameter_list|(
name|Netty4Transport
name|transport
parameter_list|,
name|String
name|profileName
parameter_list|)
block|{
name|this
operator|.
name|transportServiceAdapter
operator|=
name|transport
operator|.
name|transportServiceAdapter
argument_list|()
expr_stmt|;
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
name|this
operator|.
name|profileName
operator|=
name|profileName
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|,
name|ChannelPromise
name|promise
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|msg
operator|instanceof
name|ByteBuf
operator|&&
name|transportServiceAdapter
operator|!=
literal|null
condition|)
block|{
comment|// record the number of bytes send on the channel
name|promise
operator|.
name|addListener
argument_list|(
name|f
lambda|->
name|transportServiceAdapter
operator|.
name|addBytesSent
argument_list|(
operator|(
operator|(
name|ByteBuf
operator|)
name|msg
operator|)
operator|.
name|readableBytes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|write
argument_list|(
name|msg
argument_list|,
name|promise
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|channelRead
specifier|public
name|void
name|channelRead
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Object
name|msg
parameter_list|)
throws|throws
name|Exception
block|{
name|Transports
operator|.
name|assertTransportThread
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
operator|(
name|msg
operator|instanceof
name|ByteBuf
operator|)
condition|)
block|{
name|ctx
operator|.
name|fireChannelRead
argument_list|(
name|msg
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|ByteBuf
name|buffer
init|=
operator|(
name|ByteBuf
operator|)
name|msg
decl_stmt|;
specifier|final
name|int
name|remainingMessageSize
init|=
name|buffer
operator|.
name|getInt
argument_list|(
name|buffer
operator|.
name|readerIndex
argument_list|()
operator|-
name|TcpHeader
operator|.
name|MESSAGE_LENGTH_SIZE
argument_list|)
decl_stmt|;
specifier|final
name|int
name|expectedReaderIndex
init|=
name|buffer
operator|.
name|readerIndex
argument_list|()
operator|+
name|remainingMessageSize
decl_stmt|;
name|InetSocketAddress
name|remoteAddress
init|=
operator|(
name|InetSocketAddress
operator|)
name|ctx
operator|.
name|channel
argument_list|()
operator|.
name|remoteAddress
argument_list|()
decl_stmt|;
try|try
block|{
comment|// netty always copies a buffer, either in NioWorker in its read handler, where it copies to a fresh
comment|// buffer, or in the cumulation buffer, which is cleaned each time so it could be bigger than the actual size
name|BytesReference
name|reference
init|=
name|Netty4Utils
operator|.
name|toBytesReference
argument_list|(
name|buffer
argument_list|,
name|remainingMessageSize
argument_list|)
decl_stmt|;
name|transport
operator|.
name|messageReceived
argument_list|(
name|reference
argument_list|,
name|ctx
operator|.
name|channel
argument_list|()
argument_list|,
name|profileName
argument_list|,
name|remoteAddress
argument_list|,
name|remainingMessageSize
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// Set the expected position of the buffer, no matter what happened
name|buffer
operator|.
name|readerIndex
argument_list|(
name|expectedReaderIndex
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|exceptionCaught
specifier|public
name|void
name|exceptionCaught
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Throwable
name|cause
parameter_list|)
throws|throws
name|Exception
block|{
name|transport
operator|.
name|exceptionCaught
argument_list|(
name|ctx
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
