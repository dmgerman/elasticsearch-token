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
name|bytes
operator|.
name|ReleasablePagedBytesReference
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
name|ThrowableObjectOutputStream
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
name|ReleasableBytesStreamOutput
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
name|StreamOutput
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
name|netty
operator|.
name|ReleaseChannelFutureListener
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
name|ChannelFuture
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
name|NotSerializableException
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NettyTransportChannel
specifier|public
class|class
name|NettyTransportChannel
implements|implements
name|TransportChannel
block|{
DECL|field|transport
specifier|private
specifier|final
name|NettyTransport
name|transport
decl_stmt|;
DECL|field|transportServiceAdapter
specifier|private
specifier|final
name|TransportServiceAdapter
name|transportServiceAdapter
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|Version
name|version
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
DECL|field|channel
specifier|private
specifier|final
name|Channel
name|channel
decl_stmt|;
DECL|field|requestId
specifier|private
specifier|final
name|long
name|requestId
decl_stmt|;
DECL|field|profileName
specifier|private
specifier|final
name|String
name|profileName
decl_stmt|;
DECL|method|NettyTransportChannel
specifier|public
name|NettyTransportChannel
parameter_list|(
name|NettyTransport
name|transport
parameter_list|,
name|TransportServiceAdapter
name|transportServiceAdapter
parameter_list|,
name|String
name|action
parameter_list|,
name|Channel
name|channel
parameter_list|,
name|long
name|requestId
parameter_list|,
name|Version
name|version
parameter_list|,
name|String
name|profileName
parameter_list|)
block|{
name|this
operator|.
name|transportServiceAdapter
operator|=
name|transportServiceAdapter
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|transport
operator|=
name|transport
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
name|this
operator|.
name|requestId
operator|=
name|requestId
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
DECL|method|getProfileName
specifier|public
name|String
name|getProfileName
parameter_list|()
block|{
return|return
name|profileName
return|;
block|}
annotation|@
name|Override
DECL|method|action
specifier|public
name|String
name|action
parameter_list|()
block|{
return|return
name|this
operator|.
name|action
return|;
block|}
annotation|@
name|Override
DECL|method|sendResponse
specifier|public
name|void
name|sendResponse
parameter_list|(
name|TransportResponse
name|response
parameter_list|)
throws|throws
name|IOException
block|{
name|sendResponse
argument_list|(
name|response
argument_list|,
name|TransportResponseOptions
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|sendResponse
specifier|public
name|void
name|sendResponse
parameter_list|(
name|TransportResponse
name|response
parameter_list|,
name|TransportResponseOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|transport
operator|.
name|compress
condition|)
block|{
name|options
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
name|byte
name|status
init|=
literal|0
decl_stmt|;
name|status
operator|=
name|TransportStatus
operator|.
name|setResponse
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|ReleasableBytesStreamOutput
name|bStream
init|=
operator|new
name|ReleasableBytesStreamOutput
argument_list|(
name|transport
operator|.
name|bigArrays
argument_list|)
decl_stmt|;
name|boolean
name|addedReleaseListener
init|=
literal|false
decl_stmt|;
try|try
block|{
name|bStream
operator|.
name|skip
argument_list|(
name|NettyHeader
operator|.
name|HEADER_SIZE
argument_list|)
expr_stmt|;
name|StreamOutput
name|stream
init|=
name|bStream
decl_stmt|;
if|if
condition|(
name|options
operator|.
name|compress
argument_list|()
condition|)
block|{
name|status
operator|=
name|TransportStatus
operator|.
name|setCompress
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|stream
operator|=
name|CompressorFactory
operator|.
name|defaultCompressor
argument_list|()
operator|.
name|streamOutput
argument_list|(
name|stream
argument_list|)
expr_stmt|;
block|}
name|stream
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|response
operator|.
name|writeTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
name|ReleasablePagedBytesReference
name|bytes
init|=
name|bStream
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|ChannelBuffer
name|buffer
init|=
name|bytes
operator|.
name|toChannelBuffer
argument_list|()
decl_stmt|;
name|NettyHeader
operator|.
name|writeHeader
argument_list|(
name|buffer
argument_list|,
name|requestId
argument_list|,
name|status
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|ChannelFuture
name|future
init|=
name|channel
operator|.
name|write
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|ReleaseChannelFutureListener
name|listener
init|=
operator|new
name|ReleaseChannelFutureListener
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
name|future
operator|.
name|addListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
name|addedReleaseListener
operator|=
literal|true
expr_stmt|;
name|transportServiceAdapter
operator|.
name|onResponseSent
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|,
name|response
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|addedReleaseListener
condition|)
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|bStream
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|sendResponse
specifier|public
name|void
name|sendResponse
parameter_list|(
name|Throwable
name|error
parameter_list|)
throws|throws
name|IOException
block|{
name|BytesStreamOutput
name|stream
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
try|try
block|{
name|stream
operator|.
name|skip
argument_list|(
name|NettyHeader
operator|.
name|HEADER_SIZE
argument_list|)
expr_stmt|;
name|RemoteTransportException
name|tx
init|=
operator|new
name|RemoteTransportException
argument_list|(
name|transport
operator|.
name|nodeName
argument_list|()
argument_list|,
name|transport
operator|.
name|wrapAddress
argument_list|(
name|channel
operator|.
name|getLocalAddress
argument_list|()
argument_list|)
argument_list|,
name|action
argument_list|,
name|error
argument_list|)
decl_stmt|;
name|ThrowableObjectOutputStream
name|too
init|=
operator|new
name|ThrowableObjectOutputStream
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|too
operator|.
name|writeObject
argument_list|(
name|tx
argument_list|)
expr_stmt|;
name|too
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NotSerializableException
name|e
parameter_list|)
block|{
name|stream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|stream
operator|.
name|skip
argument_list|(
name|NettyHeader
operator|.
name|HEADER_SIZE
argument_list|)
expr_stmt|;
name|RemoteTransportException
name|tx
init|=
operator|new
name|RemoteTransportException
argument_list|(
name|transport
operator|.
name|nodeName
argument_list|()
argument_list|,
name|transport
operator|.
name|wrapAddress
argument_list|(
name|channel
operator|.
name|getLocalAddress
argument_list|()
argument_list|)
argument_list|,
name|action
argument_list|,
operator|new
name|NotSerializableTransportException
argument_list|(
name|error
argument_list|)
argument_list|)
decl_stmt|;
name|ThrowableObjectOutputStream
name|too
init|=
operator|new
name|ThrowableObjectOutputStream
argument_list|(
name|stream
argument_list|)
decl_stmt|;
name|too
operator|.
name|writeObject
argument_list|(
name|tx
argument_list|)
expr_stmt|;
name|too
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|byte
name|status
init|=
literal|0
decl_stmt|;
name|status
operator|=
name|TransportStatus
operator|.
name|setResponse
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|status
operator|=
name|TransportStatus
operator|.
name|setError
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|BytesReference
name|bytes
init|=
name|stream
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|ChannelBuffer
name|buffer
init|=
name|bytes
operator|.
name|toChannelBuffer
argument_list|()
decl_stmt|;
name|NettyHeader
operator|.
name|writeHeader
argument_list|(
name|buffer
argument_list|,
name|requestId
argument_list|,
name|status
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|channel
operator|.
name|write
argument_list|(
name|buffer
argument_list|)
expr_stmt|;
name|transportServiceAdapter
operator|.
name|onResponseSent
argument_list|(
name|requestId
argument_list|,
name|action
argument_list|,
name|error
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the underlying netty channel. This method is intended be used for access to netty to get additional      * details when processing the request and may be used by plugins. Responses should be sent using the methods      * defined in this class and not directly on the channel.      * @return underlying netty channel      */
DECL|method|getChannel
specifier|public
name|Channel
name|getChannel
parameter_list|()
block|{
return|return
name|channel
return|;
block|}
block|}
end_class

end_unit

