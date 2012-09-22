begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.compress
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|compress
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|CompressedStreamOutput
specifier|public
specifier|abstract
class|class
name|CompressedStreamOutput
parameter_list|<
name|T
extends|extends
name|CompressorContext
parameter_list|>
extends|extends
name|StreamOutput
block|{
DECL|field|out
specifier|private
specifier|final
name|StreamOutput
name|out
decl_stmt|;
DECL|field|context
specifier|protected
specifier|final
name|T
name|context
decl_stmt|;
DECL|field|uncompressed
specifier|protected
name|byte
index|[]
name|uncompressed
decl_stmt|;
DECL|field|uncompressedLength
specifier|protected
name|int
name|uncompressedLength
decl_stmt|;
DECL|field|position
specifier|private
name|int
name|position
init|=
literal|0
decl_stmt|;
DECL|field|closed
specifier|private
name|boolean
name|closed
decl_stmt|;
DECL|method|CompressedStreamOutput
specifier|public
name|CompressedStreamOutput
parameter_list|(
name|StreamOutput
name|out
parameter_list|,
name|T
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|super
operator|.
name|setVersion
argument_list|(
name|out
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|writeHeader
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setVersion
specifier|public
name|StreamOutput
name|setVersion
parameter_list|(
name|Version
name|version
parameter_list|)
block|{
name|out
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|setVersion
argument_list|(
name|version
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|int
name|b
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|position
operator|>=
name|uncompressedLength
condition|)
block|{
name|flushBuffer
argument_list|()
expr_stmt|;
block|}
name|uncompressed
index|[
name|position
operator|++
index|]
operator|=
operator|(
name|byte
operator|)
name|b
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeByte
specifier|public
name|void
name|writeByte
parameter_list|(
name|byte
name|b
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|position
operator|>=
name|uncompressedLength
condition|)
block|{
name|flushBuffer
argument_list|()
expr_stmt|;
block|}
name|uncompressed
index|[
name|position
operator|++
index|]
operator|=
name|b
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeBytes
specifier|public
name|void
name|writeBytes
parameter_list|(
name|byte
index|[]
name|input
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
comment|// ES, check if length is 0, and don't write in this case
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return;
block|}
specifier|final
name|int
name|BUFFER_LEN
init|=
name|uncompressedLength
decl_stmt|;
comment|// simple case first: buffering only (for trivially short writes)
name|int
name|free
init|=
name|BUFFER_LEN
operator|-
name|position
decl_stmt|;
if|if
condition|(
name|free
operator|>=
name|length
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|input
argument_list|,
name|offset
argument_list|,
name|uncompressed
argument_list|,
name|position
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|position
operator|+=
name|length
expr_stmt|;
return|return;
block|}
comment|// fill partial input as much as possible and flush
if|if
condition|(
name|position
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|input
argument_list|,
name|offset
argument_list|,
name|uncompressed
argument_list|,
name|position
argument_list|,
name|free
argument_list|)
expr_stmt|;
name|position
operator|+=
name|free
expr_stmt|;
name|flushBuffer
argument_list|()
expr_stmt|;
name|offset
operator|+=
name|free
expr_stmt|;
name|length
operator|-=
name|free
expr_stmt|;
block|}
comment|// then write intermediate full block, if any, without copying:
while|while
condition|(
name|length
operator|>=
name|BUFFER_LEN
condition|)
block|{
name|compress
argument_list|(
name|input
argument_list|,
name|offset
argument_list|,
name|BUFFER_LEN
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|offset
operator|+=
name|BUFFER_LEN
expr_stmt|;
name|length
operator|-=
name|BUFFER_LEN
expr_stmt|;
block|}
comment|// and finally, copy leftovers in input, if any
if|if
condition|(
name|length
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|arraycopy
argument_list|(
name|input
argument_list|,
name|offset
argument_list|,
name|uncompressed
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
name|position
operator|=
name|length
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|flush
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|flushBuffer
argument_list|()
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
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
operator|!
name|closed
condition|)
block|{
name|flushBuffer
argument_list|()
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
name|doClose
argument_list|()
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|doClose
specifier|protected
specifier|abstract
name|void
name|doClose
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|position
operator|=
literal|0
expr_stmt|;
name|out
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
DECL|method|flushBuffer
specifier|private
name|void
name|flushBuffer
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|position
operator|>
literal|0
condition|)
block|{
name|compress
argument_list|(
name|uncompressed
argument_list|,
literal|0
argument_list|,
name|position
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|position
operator|=
literal|0
expr_stmt|;
block|}
block|}
DECL|method|writeHeader
specifier|protected
specifier|abstract
name|void
name|writeHeader
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Compresses the data into the output      */
DECL|method|compress
specifier|protected
specifier|abstract
name|void
name|compress
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

