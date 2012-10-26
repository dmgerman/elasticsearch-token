begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.bytes
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
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
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamInput
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
name|org
operator|.
name|jboss
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_class
DECL|class|BytesArray
specifier|public
class|class
name|BytesArray
implements|implements
name|BytesReference
block|{
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|BytesArray
name|EMPTY
init|=
operator|new
name|BytesArray
argument_list|(
name|Bytes
operator|.
name|EMPTY_ARRAY
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
decl_stmt|;
DECL|field|bytes
specifier|private
name|byte
index|[]
name|bytes
decl_stmt|;
DECL|field|offset
specifier|private
name|int
name|offset
decl_stmt|;
DECL|field|length
specifier|private
name|int
name|length
decl_stmt|;
DECL|method|BytesArray
specifier|public
name|BytesArray
parameter_list|(
name|String
name|bytes
parameter_list|)
block|{
name|this
argument_list|(
name|bytes
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|BytesArray
specifier|public
name|BytesArray
parameter_list|(
name|BytesRef
name|bytesRef
parameter_list|)
block|{
name|this
argument_list|(
name|bytesRef
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|BytesArray
specifier|public
name|BytesArray
parameter_list|(
name|BytesRef
name|bytesRef
parameter_list|,
name|boolean
name|deepCopy
parameter_list|)
block|{
if|if
condition|(
name|deepCopy
condition|)
block|{
name|BytesRef
name|copy
init|=
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|bytesRef
argument_list|)
decl_stmt|;
name|bytes
operator|=
name|copy
operator|.
name|bytes
expr_stmt|;
name|offset
operator|=
name|copy
operator|.
name|offset
expr_stmt|;
name|length
operator|=
name|copy
operator|.
name|length
expr_stmt|;
block|}
else|else
block|{
name|bytes
operator|=
name|bytesRef
operator|.
name|bytes
expr_stmt|;
name|offset
operator|=
name|bytesRef
operator|.
name|offset
expr_stmt|;
name|length
operator|=
name|bytesRef
operator|.
name|length
expr_stmt|;
block|}
block|}
DECL|method|BytesArray
specifier|public
name|BytesArray
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|bytes
operator|.
name|length
expr_stmt|;
block|}
DECL|method|BytesArray
specifier|public
name|BytesArray
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
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
name|bytes
index|[
name|offset
operator|+
name|index
index|]
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
if|if
condition|(
name|from
argument_list|<
literal|0
operator|||
operator|(
name|from
operator|+
name|length
operator|)
argument_list|>
name|this
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"can't slice a buffer with length ["
operator|+
name|this
operator|.
name|length
operator|+
literal|"], with slice parameters from ["
operator|+
name|from
operator|+
literal|"], length ["
operator|+
name|length
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
operator|new
name|BytesArray
argument_list|(
name|bytes
argument_list|,
name|offset
operator|+
name|from
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
name|BytesStreamInput
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
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
name|os
operator|.
name|write
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toBytes
specifier|public
name|byte
index|[]
name|toBytes
parameter_list|()
block|{
if|if
condition|(
name|offset
operator|==
literal|0
operator|&&
name|bytes
operator|.
name|length
operator|==
name|length
condition|)
block|{
return|return
name|bytes
return|;
block|}
return|return
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toBytesArray
specifier|public
name|BytesArray
name|toBytesArray
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|copyBytesArray
specifier|public
name|BytesArray
name|copyBytesArray
parameter_list|()
block|{
return|return
operator|new
name|BytesArray
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toChannelBuffer
specifier|public
name|ChannelBuffer
name|toChannelBuffer
parameter_list|()
block|{
return|return
name|ChannelBuffers
operator|.
name|wrappedBuffer
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hasArray
specifier|public
name|boolean
name|hasArray
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|array
specifier|public
name|byte
index|[]
name|array
parameter_list|()
block|{
return|return
name|bytes
return|;
block|}
annotation|@
name|Override
DECL|method|arrayOffset
specifier|public
name|int
name|arrayOffset
parameter_list|()
block|{
return|return
name|offset
return|;
block|}
annotation|@
name|Override
DECL|method|toUtf8
specifier|public
name|String
name|toUtf8
parameter_list|()
block|{
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
operator|new
name|String
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Helper
operator|.
name|bytesHashCode
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|Helper
operator|.
name|bytesEqual
argument_list|(
name|this
argument_list|,
operator|(
name|BytesReference
operator|)
name|obj
argument_list|)
return|;
block|}
block|}
end_class

end_unit

