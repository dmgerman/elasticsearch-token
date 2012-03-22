begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.io
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
package|;
end_package

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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|channels
operator|.
name|FileChannel
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FileChannelInputStream
specifier|public
class|class
name|FileChannelInputStream
extends|extends
name|InputStream
block|{
DECL|field|channel
specifier|private
specifier|final
name|FileChannel
name|channel
decl_stmt|;
DECL|field|position
specifier|private
name|long
name|position
decl_stmt|;
DECL|field|length
specifier|private
name|long
name|length
decl_stmt|;
DECL|field|bb
specifier|private
name|ByteBuffer
name|bb
init|=
literal|null
decl_stmt|;
DECL|field|bs
specifier|private
name|byte
index|[]
name|bs
init|=
literal|null
decl_stmt|;
comment|// Invoker's previous array
DECL|field|b1
specifier|private
name|byte
index|[]
name|b1
init|=
literal|null
decl_stmt|;
DECL|field|markPosition
specifier|private
name|long
name|markPosition
decl_stmt|;
comment|/**      * @param channel  The channel to read from      * @param position The position to start reading from      * @param length   The length to read      */
DECL|method|FileChannelInputStream
specifier|public
name|FileChannelInputStream
parameter_list|(
name|FileChannel
name|channel
parameter_list|,
name|long
name|position
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|this
operator|.
name|channel
operator|=
name|channel
expr_stmt|;
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|markPosition
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|position
operator|+
name|length
expr_stmt|;
comment|// easier to work with total length
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|b1
operator|==
literal|null
condition|)
block|{
name|b1
operator|=
operator|new
name|byte
index|[
literal|1
index|]
expr_stmt|;
block|}
name|int
name|n
init|=
name|read
argument_list|(
name|b1
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
operator|==
literal|1
condition|)
block|{
return|return
name|b1
index|[
literal|0
index|]
operator|&
literal|0xff
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|bs
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|len
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
if|if
condition|(
operator|(
name|length
operator|-
name|position
operator|)
operator|<
name|len
condition|)
block|{
name|len
operator|=
call|(
name|int
call|)
argument_list|(
name|length
operator|-
name|position
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|ByteBuffer
name|bb
init|=
operator|(
operator|(
name|this
operator|.
name|bs
operator|==
name|bs
operator|)
condition|?
name|this
operator|.
name|bb
else|:
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|bs
argument_list|)
operator|)
decl_stmt|;
name|bb
operator|.
name|limit
argument_list|(
name|Math
operator|.
name|min
argument_list|(
name|off
operator|+
name|len
argument_list|,
name|bb
operator|.
name|capacity
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|bb
operator|.
name|position
argument_list|(
name|off
argument_list|)
expr_stmt|;
name|this
operator|.
name|bb
operator|=
name|bb
expr_stmt|;
name|this
operator|.
name|bs
operator|=
name|bs
expr_stmt|;
name|int
name|read
init|=
name|channel
operator|.
name|read
argument_list|(
name|bb
argument_list|,
name|position
argument_list|)
decl_stmt|;
if|if
condition|(
name|read
operator|>
literal|0
condition|)
block|{
name|position
operator|+=
name|read
expr_stmt|;
block|}
return|return
name|read
return|;
block|}
annotation|@
name|Override
DECL|method|markSupported
specifier|public
name|boolean
name|markSupported
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|mark
specifier|public
name|void
name|mark
parameter_list|(
name|int
name|readlimit
parameter_list|)
block|{
name|this
operator|.
name|markPosition
operator|=
name|position
expr_stmt|;
block|}
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
name|markPosition
expr_stmt|;
block|}
block|}
end_class

end_unit

