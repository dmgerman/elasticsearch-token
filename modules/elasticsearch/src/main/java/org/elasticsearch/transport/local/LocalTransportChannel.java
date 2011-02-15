begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.local
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|local
package|;
end_package

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
name|CachedStreamOutput
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
name|HandlesStreamOutput
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
name|Streamable
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
name|NotSerializableTransportException
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
name|RemoteTransportException
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
name|support
operator|.
name|TransportStreams
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|LocalTransportChannel
specifier|public
class|class
name|LocalTransportChannel
implements|implements
name|TransportChannel
block|{
DECL|field|sourceTransport
specifier|private
specifier|final
name|LocalTransport
name|sourceTransport
decl_stmt|;
comment|// the transport we will *send to*
DECL|field|targetTransport
specifier|private
specifier|final
name|LocalTransport
name|targetTransport
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|String
name|action
decl_stmt|;
DECL|field|requestId
specifier|private
specifier|final
name|long
name|requestId
decl_stmt|;
DECL|method|LocalTransportChannel
specifier|public
name|LocalTransportChannel
parameter_list|(
name|LocalTransport
name|sourceTransport
parameter_list|,
name|LocalTransport
name|targetTransport
parameter_list|,
name|String
name|action
parameter_list|,
name|long
name|requestId
parameter_list|)
block|{
name|this
operator|.
name|sourceTransport
operator|=
name|sourceTransport
expr_stmt|;
name|this
operator|.
name|targetTransport
operator|=
name|targetTransport
expr_stmt|;
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|requestId
operator|=
name|requestId
expr_stmt|;
block|}
DECL|method|action
annotation|@
name|Override
specifier|public
name|String
name|action
parameter_list|()
block|{
return|return
name|action
return|;
block|}
DECL|method|sendResponse
annotation|@
name|Override
specifier|public
name|void
name|sendResponse
parameter_list|(
name|Streamable
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|sendResponse
argument_list|(
name|message
argument_list|,
name|TransportResponseOptions
operator|.
name|EMPTY
argument_list|)
expr_stmt|;
block|}
DECL|method|sendResponse
annotation|@
name|Override
specifier|public
name|void
name|sendResponse
parameter_list|(
name|Streamable
name|message
parameter_list|,
name|TransportResponseOptions
name|options
parameter_list|)
throws|throws
name|IOException
block|{
name|HandlesStreamOutput
name|stream
init|=
name|CachedStreamOutput
operator|.
name|cachedHandlesBytes
argument_list|()
decl_stmt|;
name|stream
operator|.
name|writeLong
argument_list|(
name|requestId
argument_list|)
expr_stmt|;
name|byte
name|status
init|=
literal|0
decl_stmt|;
name|status
operator|=
name|TransportStreams
operator|.
name|statusSetResponse
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|stream
operator|.
name|writeByte
argument_list|(
name|status
argument_list|)
expr_stmt|;
comment|// 0 for request, 1 for response.
name|message
operator|.
name|writeTo
argument_list|(
name|stream
argument_list|)
expr_stmt|;
specifier|final
name|byte
index|[]
name|data
init|=
operator|(
operator|(
name|BytesStreamOutput
operator|)
name|stream
operator|.
name|wrappedOut
argument_list|()
operator|)
operator|.
name|copiedByteArray
argument_list|()
decl_stmt|;
name|targetTransport
operator|.
name|threadPool
argument_list|()
operator|.
name|cached
argument_list|()
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
name|targetTransport
operator|.
name|messageReceived
argument_list|(
name|data
argument_list|,
name|action
argument_list|,
name|sourceTransport
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|sendResponse
annotation|@
name|Override
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
decl_stmt|;
try|try
block|{
name|stream
operator|=
name|CachedStreamOutput
operator|.
name|cachedBytes
argument_list|()
expr_stmt|;
name|writeResponseExceptionHeader
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|RemoteTransportException
name|tx
init|=
operator|new
name|RemoteTransportException
argument_list|(
name|targetTransport
operator|.
name|nodeName
argument_list|()
argument_list|,
name|targetTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddress
argument_list|()
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
operator|=
name|CachedStreamOutput
operator|.
name|cachedBytes
argument_list|()
expr_stmt|;
name|writeResponseExceptionHeader
argument_list|(
name|stream
argument_list|)
expr_stmt|;
name|RemoteTransportException
name|tx
init|=
operator|new
name|RemoteTransportException
argument_list|(
name|targetTransport
operator|.
name|nodeName
argument_list|()
argument_list|,
name|targetTransport
operator|.
name|boundAddress
argument_list|()
operator|.
name|boundAddress
argument_list|()
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
specifier|final
name|byte
index|[]
name|data
init|=
name|stream
operator|.
name|copiedByteArray
argument_list|()
decl_stmt|;
name|targetTransport
operator|.
name|threadPool
argument_list|()
operator|.
name|cached
argument_list|()
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
name|targetTransport
operator|.
name|messageReceived
argument_list|(
name|data
argument_list|,
name|action
argument_list|,
name|sourceTransport
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|writeResponseExceptionHeader
specifier|private
name|void
name|writeResponseExceptionHeader
parameter_list|(
name|BytesStreamOutput
name|stream
parameter_list|)
throws|throws
name|IOException
block|{
name|stream
operator|.
name|writeLong
argument_list|(
name|requestId
argument_list|)
expr_stmt|;
name|byte
name|status
init|=
literal|0
decl_stmt|;
name|status
operator|=
name|TransportStreams
operator|.
name|statusSetResponse
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|status
operator|=
name|TransportStreams
operator|.
name|statusSetError
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|stream
operator|.
name|writeByte
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

