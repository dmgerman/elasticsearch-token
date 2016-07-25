begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty3
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty3
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
name|BytesRef
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
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
import|;
end_import

begin_class
DECL|class|ChannelBufferBytesReference
specifier|final
class|class
name|ChannelBufferBytesReference
extends|extends
name|BytesReference
block|{
DECL|field|buffer
specifier|private
specifier|final
name|ChannelBuffer
name|buffer
decl_stmt|;
DECL|field|length
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
DECL|field|offset
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
DECL|method|ChannelBufferBytesReference
name|ChannelBufferBytesReference
parameter_list|(
name|ChannelBuffer
name|buffer
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|buffer
operator|.
name|readerIndex
argument_list|()
expr_stmt|;
assert|assert
name|length
operator|<=
name|buffer
operator|.
name|readableBytes
argument_list|()
operator|:
literal|"length["
operator|+
name|length
operator|+
literal|"]> "
operator|+
name|buffer
operator|.
name|readableBytes
argument_list|()
assert|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|byte
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|buffer
operator|.
name|getByte
argument_list|(
name|offset
operator|+
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|length
specifier|public
name|int
name|length
parameter_list|()
block|{
return|return
name|length
return|;
block|}
annotation|@
name|Override
DECL|method|slice
specifier|public
name|BytesReference
name|slice
parameter_list|(
name|int
name|from
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
operator|new
name|ChannelBufferBytesReference
argument_list|(
name|buffer
operator|.
name|slice
argument_list|(
name|offset
operator|+
name|from
argument_list|,
name|length
argument_list|)
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|streamInput
specifier|public
name|StreamInput
name|streamInput
parameter_list|()
block|{
return|return
operator|new
name|ChannelBufferStreamInput
argument_list|(
name|buffer
operator|.
name|duplicate
argument_list|()
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
name|buffer
operator|.
name|getBytes
argument_list|(
name|offset
argument_list|,
name|os
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|toChannelBuffer
name|ChannelBuffer
name|toChannelBuffer
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|duplicate
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|utf8ToString
specifier|public
name|String
name|utf8ToString
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|toString
argument_list|(
name|offset
argument_list|,
name|length
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toBytesRef
specifier|public
name|BytesRef
name|toBytesRef
parameter_list|()
block|{
if|if
condition|(
name|buffer
operator|.
name|hasArray
argument_list|()
condition|)
block|{
return|return
operator|new
name|BytesRef
argument_list|(
name|buffer
operator|.
name|array
argument_list|()
argument_list|,
name|buffer
operator|.
name|arrayOffset
argument_list|()
operator|+
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
specifier|final
name|byte
index|[]
name|copy
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|buffer
operator|.
name|getBytes
argument_list|(
name|offset
argument_list|,
name|copy
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesRef
argument_list|(
name|copy
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
name|buffer
operator|.
name|capacity
argument_list|()
return|;
block|}
block|}
end_class

end_unit

