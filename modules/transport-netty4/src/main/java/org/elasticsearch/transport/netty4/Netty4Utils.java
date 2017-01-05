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
name|buffer
operator|.
name|CompositeByteBuf
import|;
end_import

begin_import
import|import
name|io
operator|.
name|netty
operator|.
name|buffer
operator|.
name|Unpooled
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
name|Channel
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
name|ChannelFuture
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
name|internal
operator|.
name|logging
operator|.
name|InternalLogger
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
name|internal
operator|.
name|logging
operator|.
name|InternalLoggerFactory
import|;
end_import

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
name|BytesRef
import|;
end_import

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
name|BytesRefIterator
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
name|logging
operator|.
name|ESLoggerFactory
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
name|Arrays
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|Netty4Utils
specifier|public
class|class
name|Netty4Utils
block|{
static|static
block|{
name|InternalLoggerFactory
operator|.
name|setDefaultFactory
argument_list|(
operator|new
name|InternalLoggerFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|InternalLogger
name|newInstance
parameter_list|(
specifier|final
name|String
name|name
parameter_list|)
block|{
return|return
operator|new
name|Netty4InternalESLogger
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|setup
specifier|public
specifier|static
name|void
name|setup
parameter_list|()
block|{      }
comment|/**      * Turns the given BytesReference into a ByteBuf. Note: the returned ByteBuf will reference the internal      * pages of the BytesReference. Don't free the bytes of reference before the ByteBuf goes out of scope.      */
DECL|method|toByteBuf
specifier|public
specifier|static
name|ByteBuf
name|toByteBuf
parameter_list|(
specifier|final
name|BytesReference
name|reference
parameter_list|)
block|{
if|if
condition|(
name|reference
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Unpooled
operator|.
name|EMPTY_BUFFER
return|;
block|}
if|if
condition|(
name|reference
operator|instanceof
name|ByteBufBytesReference
condition|)
block|{
return|return
operator|(
operator|(
name|ByteBufBytesReference
operator|)
name|reference
operator|)
operator|.
name|toByteBuf
argument_list|()
return|;
block|}
else|else
block|{
specifier|final
name|BytesRefIterator
name|iterator
init|=
name|reference
operator|.
name|iterator
argument_list|()
decl_stmt|;
comment|// usually we have one, two, or three components from the header, the message, and a buffer
specifier|final
name|List
argument_list|<
name|ByteBuf
argument_list|>
name|buffers
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|3
argument_list|)
decl_stmt|;
try|try
block|{
name|BytesRef
name|slice
decl_stmt|;
while|while
condition|(
operator|(
name|slice
operator|=
name|iterator
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|buffers
operator|.
name|add
argument_list|(
name|Unpooled
operator|.
name|wrappedBuffer
argument_list|(
name|slice
operator|.
name|bytes
argument_list|,
name|slice
operator|.
name|offset
argument_list|,
name|slice
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|CompositeByteBuf
name|composite
init|=
name|Unpooled
operator|.
name|compositeBuffer
argument_list|(
name|buffers
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|composite
operator|.
name|addComponents
argument_list|(
literal|true
argument_list|,
name|buffers
argument_list|)
expr_stmt|;
return|return
name|composite
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"no IO happens here"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
block|}
comment|/**      * Wraps the given ChannelBuffer with a BytesReference      */
DECL|method|toBytesReference
specifier|public
specifier|static
name|BytesReference
name|toBytesReference
parameter_list|(
specifier|final
name|ByteBuf
name|buffer
parameter_list|)
block|{
return|return
name|toBytesReference
argument_list|(
name|buffer
argument_list|,
name|buffer
operator|.
name|readableBytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Wraps the given ChannelBuffer with a BytesReference of a given size      */
DECL|method|toBytesReference
specifier|static
name|BytesReference
name|toBytesReference
parameter_list|(
specifier|final
name|ByteBuf
name|buffer
parameter_list|,
specifier|final
name|int
name|size
parameter_list|)
block|{
return|return
operator|new
name|ByteBufBytesReference
argument_list|(
name|buffer
argument_list|,
name|size
argument_list|)
return|;
block|}
DECL|method|closeChannels
specifier|public
specifier|static
name|void
name|closeChannels
parameter_list|(
specifier|final
name|Collection
argument_list|<
name|Channel
argument_list|>
name|channels
parameter_list|)
throws|throws
name|IOException
block|{
name|IOException
name|closingExceptions
init|=
literal|null
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ChannelFuture
argument_list|>
name|futures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Channel
name|channel
range|:
name|channels
control|)
block|{
try|try
block|{
if|if
condition|(
name|channel
operator|!=
literal|null
operator|&&
name|channel
operator|.
name|isOpen
argument_list|()
condition|)
block|{
name|futures
operator|.
name|add
argument_list|(
name|channel
operator|.
name|close
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|closingExceptions
operator|==
literal|null
condition|)
block|{
name|closingExceptions
operator|=
operator|new
name|IOException
argument_list|(
literal|"failed to close channels"
argument_list|)
expr_stmt|;
block|}
name|closingExceptions
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
specifier|final
name|ChannelFuture
name|future
range|:
name|futures
control|)
block|{
name|future
operator|.
name|awaitUninterruptibly
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|closingExceptions
operator|!=
literal|null
condition|)
block|{
throw|throw
name|closingExceptions
throw|;
block|}
block|}
comment|/**      * If the specified cause is an unrecoverable error, this method will rethrow the cause on a separate thread so that it can not be      * caught and bubbles up to the uncaught exception handler.      *      * @param cause the throwable to test      */
DECL|method|maybeDie
specifier|public
specifier|static
name|void
name|maybeDie
parameter_list|(
specifier|final
name|Throwable
name|cause
parameter_list|)
block|{
if|if
condition|(
name|cause
operator|instanceof
name|Error
condition|)
block|{
comment|/*              * Here be dragons. We want to rethrow this so that it bubbles up to the uncaught exception handler. Yet, Netty wraps too many              * invocations of user-code in try/catch blocks that swallow all throwables. This means that a rethrow here will not bubble up              * to where we want it to. So, we fork a thread and throw the exception from there where Netty can not get to it. We do not wrap              * the exception so as to not lose the original cause during exit.              */
try|try
block|{
comment|// try to log the current stack trace
specifier|final
name|StackTraceElement
index|[]
name|stackTrace
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
specifier|final
name|String
name|formatted
init|=
name|Arrays
operator|.
name|stream
argument_list|(
name|stackTrace
argument_list|)
operator|.
name|skip
argument_list|(
literal|1
argument_list|)
operator|.
name|map
argument_list|(
name|e
lambda|->
literal|"\tat "
operator|+
name|e
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
decl_stmt|;
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
name|Netty4Utils
operator|.
name|class
argument_list|)
operator|.
name|error
argument_list|(
literal|"fatal error on the network layer\n{}"
argument_list|,
name|formatted
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
operator|new
name|Thread
argument_list|(
parameter_list|()
lambda|->
block|{
throw|throw
operator|(
name|Error
operator|)
name|cause
throw|;
block|}
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

