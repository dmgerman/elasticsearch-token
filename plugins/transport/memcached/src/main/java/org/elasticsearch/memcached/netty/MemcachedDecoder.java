begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.memcached.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|memcached
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
name|Unicode
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
name|elasticsearch
operator|.
name|common
operator|.
name|netty
operator|.
name|buffer
operator|.
name|ChannelBuffers
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
name|channel
operator|.
name|Channel
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
name|channel
operator|.
name|ChannelHandlerContext
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
name|channel
operator|.
name|ExceptionEvent
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
name|handler
operator|.
name|codec
operator|.
name|frame
operator|.
name|FrameDecoder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|memcached
operator|.
name|MemcachedRestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|StreamCorruptedException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|MemcachedDecoder
specifier|public
class|class
name|MemcachedDecoder
extends|extends
name|FrameDecoder
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|lineSplit
specifier|private
specifier|final
name|Pattern
name|lineSplit
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|" +"
argument_list|)
decl_stmt|;
DECL|field|CR
specifier|public
specifier|static
specifier|final
name|byte
name|CR
init|=
literal|13
decl_stmt|;
DECL|field|LF
specifier|public
specifier|static
specifier|final
name|byte
name|LF
init|=
literal|10
decl_stmt|;
DECL|field|CRLF
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|CRLF
init|=
operator|new
name|byte
index|[]
block|{
name|CR
block|,
name|LF
block|}
decl_stmt|;
DECL|field|sb
specifier|private
specifier|volatile
name|StringBuffer
name|sb
init|=
operator|new
name|StringBuffer
argument_list|()
decl_stmt|;
DECL|field|request
specifier|private
specifier|volatile
name|MemcachedRestRequest
name|request
decl_stmt|;
DECL|field|ending
specifier|private
specifier|volatile
name|boolean
name|ending
init|=
literal|false
decl_stmt|;
DECL|method|MemcachedDecoder
specifier|public
name|MemcachedDecoder
parameter_list|(
name|ESLogger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
DECL|method|decode
annotation|@
name|Override
specifier|protected
name|Object
name|decode
parameter_list|(
name|ChannelHandlerContext
name|ctx
parameter_list|,
name|Channel
name|channel
parameter_list|,
name|ChannelBuffer
name|buffer
parameter_list|)
throws|throws
name|Exception
block|{
name|MemcachedRestRequest
name|request
init|=
name|this
operator|.
name|request
decl_stmt|;
if|if
condition|(
name|request
operator|==
literal|null
condition|)
block|{
name|buffer
operator|.
name|markReaderIndex
argument_list|()
expr_stmt|;
if|if
condition|(
name|buffer
operator|.
name|readableBytes
argument_list|()
operator|<
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
name|short
name|magic
init|=
name|buffer
operator|.
name|readUnsignedByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|magic
operator|==
literal|0x80
condition|)
block|{
if|if
condition|(
name|buffer
operator|.
name|readableBytes
argument_list|()
operator|<
literal|23
condition|)
block|{
name|buffer
operator|.
name|resetReaderIndex
argument_list|()
expr_stmt|;
comment|// but back magic
return|return
literal|null
return|;
block|}
name|short
name|opcode
init|=
name|buffer
operator|.
name|readUnsignedByte
argument_list|()
decl_stmt|;
name|short
name|keyLength
init|=
name|buffer
operator|.
name|readShort
argument_list|()
decl_stmt|;
name|short
name|extraLength
init|=
name|buffer
operator|.
name|readUnsignedByte
argument_list|()
decl_stmt|;
name|short
name|dataType
init|=
name|buffer
operator|.
name|readUnsignedByte
argument_list|()
decl_stmt|;
comment|// unused
name|short
name|reserved
init|=
name|buffer
operator|.
name|readShort
argument_list|()
decl_stmt|;
comment|// unused
name|int
name|totalBodyLength
init|=
name|buffer
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|int
name|opaque
init|=
name|buffer
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|long
name|cas
init|=
name|buffer
operator|.
name|readLong
argument_list|()
decl_stmt|;
comment|// we want the whole of totalBodyLength; otherwise, keep waiting.
if|if
condition|(
name|buffer
operator|.
name|readableBytes
argument_list|()
operator|<
name|totalBodyLength
condition|)
block|{
name|buffer
operator|.
name|resetReaderIndex
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
name|buffer
operator|.
name|skipBytes
argument_list|(
name|extraLength
argument_list|)
expr_stmt|;
comment|// get extras, can be empty
if|if
condition|(
name|opcode
operator|==
literal|0x00
condition|)
block|{
comment|// GET
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|keyLength
index|]
decl_stmt|;
name|buffer
operator|.
name|readBytes
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|String
name|uri
init|=
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|request
operator|=
operator|new
name|MemcachedRestRequest
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
name|uri
argument_list|,
name|key
argument_list|,
operator|-
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|request
operator|.
name|setOpaque
argument_list|(
name|opaque
argument_list|)
expr_stmt|;
return|return
name|request
return|;
block|}
elseif|else
if|if
condition|(
name|opcode
operator|==
literal|0x04
condition|)
block|{
comment|// DELETE
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|keyLength
index|]
decl_stmt|;
name|buffer
operator|.
name|readBytes
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|String
name|uri
init|=
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|request
operator|=
operator|new
name|MemcachedRestRequest
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|DELETE
argument_list|,
name|uri
argument_list|,
name|key
argument_list|,
operator|-
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|request
operator|.
name|setOpaque
argument_list|(
name|opaque
argument_list|)
expr_stmt|;
return|return
name|request
return|;
block|}
elseif|else
if|if
condition|(
name|opcode
operator|==
literal|0x01
comment|/* || opcode == 0x11*/
condition|)
block|{
comment|// SET
name|byte
index|[]
name|key
init|=
operator|new
name|byte
index|[
name|keyLength
index|]
decl_stmt|;
name|buffer
operator|.
name|readBytes
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|String
name|uri
init|=
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// the remainder of the message -- that is, totalLength - (keyLength + extraLength) should be the payload
name|int
name|size
init|=
name|totalBodyLength
operator|-
name|keyLength
operator|-
name|extraLength
decl_stmt|;
name|request
operator|=
operator|new
name|MemcachedRestRequest
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|POST
argument_list|,
name|uri
argument_list|,
name|key
argument_list|,
name|size
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|request
operator|.
name|setOpaque
argument_list|(
name|opaque
argument_list|)
expr_stmt|;
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|buffer
operator|.
name|readBytes
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|request
operator|.
name|setData
argument_list|(
name|data
argument_list|)
expr_stmt|;
name|request
operator|.
name|setQuiet
argument_list|(
name|opcode
operator|==
literal|0x11
argument_list|)
expr_stmt|;
return|return
name|request
return|;
block|}
elseif|else
if|if
condition|(
name|opcode
operator|==
literal|0x0A
operator|||
name|opcode
operator|==
literal|0x10
condition|)
block|{
comment|// NOOP or STATS
comment|// TODO once we support setQ we need to wait for them to flush
name|ChannelBuffer
name|writeBuffer
init|=
name|ChannelBuffers
operator|.
name|dynamicBuffer
argument_list|(
literal|24
argument_list|)
decl_stmt|;
name|writeBuffer
operator|.
name|writeByte
argument_list|(
literal|0x81
argument_list|)
expr_stmt|;
comment|// magic
name|writeBuffer
operator|.
name|writeByte
argument_list|(
name|opcode
argument_list|)
expr_stmt|;
comment|// opcode
name|writeBuffer
operator|.
name|writeShort
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// key length
name|writeBuffer
operator|.
name|writeByte
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// extra length = flags + expiry
name|writeBuffer
operator|.
name|writeByte
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// data type unused
name|writeBuffer
operator|.
name|writeShort
argument_list|(
literal|0x0000
argument_list|)
expr_stmt|;
comment|// OK
name|writeBuffer
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// data length
name|writeBuffer
operator|.
name|writeInt
argument_list|(
name|opaque
argument_list|)
expr_stmt|;
comment|// opaque
name|writeBuffer
operator|.
name|writeLong
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// cas
name|channel
operator|.
name|write
argument_list|(
name|writeBuffer
argument_list|)
expr_stmt|;
return|return
name|MemcachedDispatcher
operator|.
name|IGNORE_REQUEST
return|;
block|}
elseif|else
if|if
condition|(
name|opcode
operator|==
literal|0x07
condition|)
block|{
comment|// QUIT
name|channel
operator|.
name|disconnect
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Unsupported opcode [0x{}], ignoring and closing connection"
argument_list|,
name|Integer
operator|.
name|toHexString
argument_list|(
name|opcode
argument_list|)
argument_list|)
expr_stmt|;
name|channel
operator|.
name|disconnect
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
name|buffer
operator|.
name|resetReaderIndex
argument_list|()
expr_stmt|;
comment|// reset to get to the first byte
comment|// need to read a header
name|boolean
name|done
init|=
literal|false
decl_stmt|;
name|StringBuffer
name|sb
init|=
name|this
operator|.
name|sb
decl_stmt|;
name|int
name|readableBytes
init|=
name|buffer
operator|.
name|readableBytes
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
name|readableBytes
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|next
init|=
name|buffer
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|ending
operator|&&
name|next
operator|==
name|CR
condition|)
block|{
name|ending
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ending
operator|&&
name|next
operator|==
name|LF
condition|)
block|{
name|ending
operator|=
literal|false
expr_stmt|;
name|done
operator|=
literal|true
expr_stmt|;
break|break;
block|}
elseif|else
if|if
condition|(
name|ending
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Corrupt stream, expected LF, found [0x{}]"
argument_list|,
name|Integer
operator|.
name|toHexString
argument_list|(
name|next
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|StreamCorruptedException
argument_list|(
literal|"Expecting LF after CR"
argument_list|)
throw|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
operator|(
name|char
operator|)
name|next
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|done
condition|)
block|{
comment|// let's keep the buffer and bytes read
comment|//                    buffer.discardReadBytes();
name|buffer
operator|.
name|markReaderIndex
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
name|String
index|[]
name|args
init|=
name|lineSplit
operator|.
name|split
argument_list|(
name|sb
argument_list|)
decl_stmt|;
comment|// we read the text, clear it
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|String
name|cmd
init|=
name|args
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
literal|"get"
operator|.
name|equals
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
name|request
operator|=
operator|new
name|MemcachedRestRequest
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
name|args
index|[
literal|1
index|]
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|>
literal|3
condition|)
block|{
name|request
operator|.
name|setData
argument_list|(
name|Unicode
operator|.
name|fromStringAsBytes
argument_list|(
name|args
index|[
literal|2
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|request
return|;
block|}
elseif|else
if|if
condition|(
literal|"delete"
operator|.
name|equals
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
name|request
operator|=
operator|new
name|MemcachedRestRequest
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|DELETE
argument_list|,
name|args
index|[
literal|1
index|]
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|//                if (args.length> 3) {
comment|//                    request.setData(Unicode.fromStringAsBytes(args[2]));
comment|//                }
return|return
name|request
return|;
block|}
elseif|else
if|if
condition|(
literal|"set"
operator|.
name|equals
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
name|this
operator|.
name|request
operator|=
operator|new
name|MemcachedRestRequest
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|POST
argument_list|,
name|args
index|[
literal|1
index|]
argument_list|,
literal|null
argument_list|,
name|Integer
operator|.
name|parseInt
argument_list|(
name|args
index|[
literal|4
index|]
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|markReaderIndex
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"version"
operator|.
name|equals
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
comment|// sent as a noop
name|byte
index|[]
name|bytes
init|=
name|Version
operator|.
name|CURRENT
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|()
decl_stmt|;
name|ChannelBuffer
name|writeBuffer
init|=
name|ChannelBuffers
operator|.
name|dynamicBuffer
argument_list|(
name|bytes
operator|.
name|length
argument_list|)
decl_stmt|;
name|writeBuffer
operator|.
name|writeBytes
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|channel
operator|.
name|write
argument_list|(
name|writeBuffer
argument_list|)
expr_stmt|;
return|return
name|MemcachedDispatcher
operator|.
name|IGNORE_REQUEST
return|;
block|}
elseif|else
if|if
condition|(
literal|"quit"
operator|.
name|equals
argument_list|(
name|cmd
argument_list|)
condition|)
block|{
if|if
condition|(
name|channel
operator|.
name|isConnected
argument_list|()
condition|)
block|{
comment|// we maybe in the process of clearing the queued bits
name|channel
operator|.
name|disconnect
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Unsupported command [{}], ignoring and closing connection"
argument_list|,
name|cmd
argument_list|)
expr_stmt|;
if|if
condition|(
name|channel
operator|.
name|isConnected
argument_list|()
condition|)
block|{
comment|// we maybe in the process of clearing the queued bits
name|channel
operator|.
name|disconnect
argument_list|()
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|buffer
operator|.
name|readableBytes
argument_list|()
operator|<
operator|(
name|request
operator|.
name|getDataSize
argument_list|()
operator|+
literal|2
operator|)
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|data
init|=
operator|new
name|byte
index|[
name|request
operator|.
name|getDataSize
argument_list|()
index|]
decl_stmt|;
name|buffer
operator|.
name|readBytes
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|byte
name|next
init|=
name|buffer
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|next
operator|==
name|CR
condition|)
block|{
name|next
operator|=
name|buffer
operator|.
name|readByte
argument_list|()
expr_stmt|;
if|if
condition|(
name|next
operator|==
name|LF
condition|)
block|{
name|request
operator|.
name|setData
argument_list|(
name|data
argument_list|)
expr_stmt|;
comment|// reset
name|this
operator|.
name|request
operator|=
literal|null
expr_stmt|;
return|return
name|request
return|;
block|}
else|else
block|{
name|this
operator|.
name|request
operator|=
literal|null
expr_stmt|;
throw|throw
operator|new
name|StreamCorruptedException
argument_list|(
literal|"Expecting separator after data block"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|this
operator|.
name|request
operator|=
literal|null
expr_stmt|;
throw|throw
operator|new
name|StreamCorruptedException
argument_list|(
literal|"Expecting separator after data block"
argument_list|)
throw|;
block|}
block|}
return|return
literal|null
return|;
block|}
DECL|method|exceptionCaught
annotation|@
name|Override
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
name|this
operator|.
name|request
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|ending
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|sb
operator|.
name|setLength
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|isConnected
argument_list|()
condition|)
block|{
name|ctx
operator|.
name|getChannel
argument_list|()
operator|.
name|disconnect
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|error
argument_list|(
literal|"caught exception on memcached decoder"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

