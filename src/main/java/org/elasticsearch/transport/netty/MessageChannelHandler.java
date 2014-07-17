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
name|ElasticsearchIllegalStateException
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
name|compress
operator|.
name|Compressor
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
name|compress
operator|.
name|CompressorFactory
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
name|ThrowableObjectInputStream
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
name|CachedStreamInput
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
name|support
operator|.
name|TransportStatus
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
name|buffer
operator|.
name|ChannelBuffer
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
comment|/**  * A handler (must be the last one!) that does size based frame decoding and forwards the actual message  * to the relevant action.  */
end_comment

begin_class
DECL|class|MessageChannelHandler
specifier|public
class|class
name|MessageChannelHandler
extends|extends
name|SimpleChannelUpstreamHandler
block|{
DECL|field|logger
specifier|protected
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportServiceAdapter
specifier|protected
specifier|final
name|TransportServiceAdapter
name|transportServiceAdapter
decl_stmt|;
DECL|field|transport
specifier|protected
specifier|final
name|NettyTransport
name|transport
decl_stmt|;
DECL|method|MessageChannelHandler
specifier|public
name|MessageChannelHandler
parameter_list|(
name|NettyTransport
name|transport
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|threadPool
operator|=
name|transport
operator|.
name|threadPool
argument_list|()
expr_stmt|;
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
name|logger
operator|=
name|logger
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeComplete
specifier|public
name|void
name|writeComplete
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|WriteCompletionEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
name|transportServiceAdapter
operator|.
name|sent
argument_list|(
name|e
operator|.
name|getWrittenAmount
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|writeComplete
argument_list|(
name|ctx
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|MessageEvent
name|e
parameter_list|)
throws|throws
name|Exception
block|{
name|Object
name|m
init|=
name|e
operator|.
name|getMessage
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|m
operator|instanceof
name|ChannelBuffer
operator|)
condition|)
block|{
name|ctx
operator|.
name|sendUpstream
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|ChannelBuffer
name|buffer
init|=
operator|(
name|ChannelBuffer
operator|)
name|m
decl_stmt|;
name|int
name|size
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
literal|4
argument_list|)
decl_stmt|;
name|transportServiceAdapter
operator|.
name|received
argument_list|(
name|size
operator|+
literal|6
argument_list|)
expr_stmt|;
comment|// we have additional bytes to read, outside of the header
name|boolean
name|hasMessageBytesToRead
init|=
operator|(
name|size
operator|-
operator|(
name|NettyHeader
operator|.
name|HEADER_SIZE
operator|-
literal|6
operator|)
operator|)
operator|!=
literal|0
decl_stmt|;
name|int
name|markedReaderIndex
init|=
name|buffer
operator|.
name|readerIndex
argument_list|()
decl_stmt|;
name|int
name|expectedIndexReader
init|=
name|markedReaderIndex
operator|+
name|size
decl_stmt|;
comment|// netty always copies a buffer, either in NioWorker in its read handler, where it copies to a fresh
comment|// buffer, or in the cumlation buffer, which is cleaned each time
name|StreamInput
name|streamIn
init|=
name|ChannelBufferStreamInputFactory
operator|.
name|create
argument_list|(
name|buffer
argument_list|,
name|size
argument_list|)
decl_stmt|;
name|long
name|requestId
init|=
name|buffer
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|byte
name|status
init|=
name|buffer
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|Version
name|version
init|=
name|Version
operator|.
name|fromId
argument_list|(
name|buffer
operator|.
name|readInt
argument_list|()
argument_list|)
decl_stmt|;
name|StreamInput
name|wrappedStream
decl_stmt|;
if|if
condition|(
name|TransportStatus
operator|.
name|isCompress
argument_list|(
name|status
argument_list|)
operator|&&
name|hasMessageBytesToRead
operator|&&
name|buffer
operator|.
name|readable
argument_list|()
condition|)
block|{
name|Compressor
name|compressor
init|=
name|CompressorFactory
operator|.
name|compressor
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
if|if
condition|(
name|compressor
operator|==
literal|null
condition|)
block|{
name|int
name|maxToRead
init|=
name|Math
operator|.
name|min
argument_list|(
name|buffer
operator|.
name|readableBytes
argument_list|()
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|int
name|offset
init|=
name|buffer
operator|.
name|readerIndex
argument_list|()
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"stream marked as compressed, but no compressor found, first ["
argument_list|)
operator|.
name|append
argument_list|(
name|maxToRead
argument_list|)
operator|.
name|append
argument_list|(
literal|"] content bytes out of ["
argument_list|)
operator|.
name|append
argument_list|(
name|buffer
operator|.
name|readableBytes
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"] readable bytes with message size ["
argument_list|)
operator|.
name|append
argument_list|(
name|size
argument_list|)
operator|.
name|append
argument_list|(
literal|"] "
argument_list|)
operator|.
name|append
argument_list|(
literal|"] are ["
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
name|maxToRead
condition|;
name|i
operator|++
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|buffer
operator|.
name|getByte
argument_list|(
name|offset
operator|+
name|i
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|","
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
name|wrappedStream
operator|=
name|CachedStreamInput
operator|.
name|cachedHandlesCompressed
argument_list|(
name|compressor
argument_list|,
name|streamIn
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|wrappedStream
operator|=
name|CachedStreamInput
operator|.
name|cachedHandles
argument_list|(
name|streamIn
argument_list|)
expr_stmt|;
block|}
name|wrappedStream
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
if|if
condition|(
name|TransportStatus
operator|.
name|isRequest
argument_list|(
name|status
argument_list|)
condition|)
block|{
name|String
name|action
init|=
name|handleRequest
argument_list|(
name|ctx
operator|.
name|getChannel
argument_list|()
argument_list|,
name|wrappedStream
argument_list|,
name|requestId
argument_list|,
name|version
argument_list|)
decl_stmt|;
if|if
condition|(
name|buffer
operator|.
name|readerIndex
argument_list|()
operator|!=
name|expectedIndexReader
condition|)
block|{
if|if
condition|(
name|buffer
operator|.
name|readerIndex
argument_list|()
operator|<
name|expectedIndexReader
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Message not fully read (request) for [{}] and action [{}], resetting"
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Message read past expected size (request) for [{}] and action [{}], resetting"
argument_list|,
name|requestId
argument_list|,
name|action
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|readerIndex
argument_list|(
name|expectedIndexReader
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|TransportResponseHandler
name|handler
init|=
name|transportServiceAdapter
operator|.
name|remove
argument_list|(
name|requestId
argument_list|)
decl_stmt|;
comment|// ignore if its null, the adapter logs it
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|TransportStatus
operator|.
name|isError
argument_list|(
name|status
argument_list|)
condition|)
block|{
name|handlerResponseError
argument_list|(
name|wrappedStream
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|handleResponse
argument_list|(
name|ctx
operator|.
name|getChannel
argument_list|()
argument_list|,
name|wrappedStream
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// if its null, skip those bytes
name|buffer
operator|.
name|readerIndex
argument_list|(
name|markedReaderIndex
operator|+
name|size
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|buffer
operator|.
name|readerIndex
argument_list|()
operator|!=
name|expectedIndexReader
condition|)
block|{
if|if
condition|(
name|buffer
operator|.
name|readerIndex
argument_list|()
operator|<
name|expectedIndexReader
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Message not fully read (response) for [{}] handler {}, error [{}], resetting"
argument_list|,
name|requestId
argument_list|,
name|handler
argument_list|,
name|TransportStatus
operator|.
name|isError
argument_list|(
name|status
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Message read past expected size (response) for [{}] handler {}, error [{}], resetting"
argument_list|,
name|requestId
argument_list|,
name|handler
argument_list|,
name|TransportStatus
operator|.
name|isError
argument_list|(
name|status
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|readerIndex
argument_list|(
name|expectedIndexReader
argument_list|)
expr_stmt|;
block|}
block|}
name|wrappedStream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|handleResponse
specifier|protected
name|void
name|handleResponse
parameter_list|(
name|Channel
name|channel
parameter_list|,
name|StreamInput
name|buffer
parameter_list|,
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|)
block|{
specifier|final
name|TransportResponse
name|response
init|=
name|handler
operator|.
name|newInstance
argument_list|()
decl_stmt|;
name|response
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
name|response
operator|.
name|remoteAddress
argument_list|()
expr_stmt|;
try|try
block|{
name|response
operator|.
name|readFrom
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|handleException
argument_list|(
name|handler
argument_list|,
operator|new
name|TransportSerializationException
argument_list|(
literal|"Failed to deserialize response of type ["
operator|+
name|response
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
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
name|handleResponse
argument_list|(
name|response
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
name|ResponseHandler
argument_list|(
name|handler
argument_list|,
name|response
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
name|handleException
argument_list|(
name|handler
argument_list|,
operator|new
name|ResponseHandlerFailureTransportException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|handlerResponseError
specifier|private
name|void
name|handlerResponseError
parameter_list|(
name|StreamInput
name|buffer
parameter_list|,
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|)
block|{
name|Throwable
name|error
decl_stmt|;
try|try
block|{
name|ThrowableObjectInputStream
name|ois
init|=
operator|new
name|ThrowableObjectInputStream
argument_list|(
name|buffer
argument_list|,
name|transport
operator|.
name|settings
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
decl_stmt|;
name|error
operator|=
operator|(
name|Throwable
operator|)
name|ois
operator|.
name|readObject
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|error
operator|=
operator|new
name|TransportSerializationException
argument_list|(
literal|"Failed to deserialize exception response from stream"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|handleException
argument_list|(
name|handler
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
DECL|method|handleException
specifier|private
name|void
name|handleException
parameter_list|(
specifier|final
name|TransportResponseHandler
name|handler
parameter_list|,
name|Throwable
name|error
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|error
operator|instanceof
name|RemoteTransportException
operator|)
condition|)
block|{
name|error
operator|=
operator|new
name|RemoteTransportException
argument_list|(
name|error
operator|.
name|getMessage
argument_list|()
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
specifier|final
name|RemoteTransportException
name|rtx
init|=
operator|(
name|RemoteTransportException
operator|)
name|error
decl_stmt|;
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
try|try
block|{
name|handler
operator|.
name|handleException
argument_list|(
name|rtx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed to handle exception response [{}]"
argument_list|,
name|e
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
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
try|try
block|{
name|handler
operator|.
name|handleException
argument_list|(
name|rtx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed to handle exception response [{}]"
argument_list|,
name|e
argument_list|,
name|handler
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|handleRequest
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
name|ExceptionEvent
name|e
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
name|e
argument_list|)
expr_stmt|;
block|}
DECL|class|ResponseHandler
class|class
name|ResponseHandler
implements|implements
name|Runnable
block|{
DECL|field|handler
specifier|private
specifier|final
name|TransportResponseHandler
name|handler
decl_stmt|;
DECL|field|response
specifier|private
specifier|final
name|TransportResponse
name|response
decl_stmt|;
DECL|method|ResponseHandler
specifier|public
name|ResponseHandler
parameter_list|(
name|TransportResponseHandler
name|handler
parameter_list|,
name|TransportResponse
name|response
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
name|response
operator|=
name|response
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
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|handler
operator|.
name|handleResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|handleException
argument_list|(
name|handler
argument_list|,
operator|new
name|ResponseHandlerFailureTransportException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|RequestHandler
class|class
name|RequestHandler
extends|extends
name|AbstractRunnable
block|{
DECL|field|handler
specifier|private
specifier|final
name|TransportRequestHandler
name|handler
decl_stmt|;
DECL|field|request
specifier|private
specifier|final
name|TransportRequest
name|request
decl_stmt|;
DECL|field|transportChannel
specifier|private
specifier|final
name|NettyTransportChannel
name|transportChannel
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
DECL|method|RequestHandler
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
DECL|method|run
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
DECL|method|isForceExecution
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
end_class

end_unit

