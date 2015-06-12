begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
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
name|store
operator|.
name|BufferedChecksum
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
name|zip
operator|.
name|CRC32
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|Checksum
import|;
end_import

begin_comment
comment|/**  * Similar to Lucene's BufferedChecksumIndexInput, however this wraps a  * {@link StreamInput} so anything read will update the checksum  */
end_comment

begin_class
DECL|class|BufferedChecksumStreamInput
specifier|public
specifier|final
class|class
name|BufferedChecksumStreamInput
extends|extends
name|StreamInput
block|{
DECL|field|SKIP_BUFFER_SIZE
specifier|private
specifier|static
specifier|final
name|int
name|SKIP_BUFFER_SIZE
init|=
literal|1024
decl_stmt|;
DECL|field|skipBuffer
specifier|private
name|byte
index|[]
name|skipBuffer
decl_stmt|;
DECL|field|in
specifier|private
specifier|final
name|StreamInput
name|in
decl_stmt|;
DECL|field|digest
specifier|private
specifier|final
name|Checksum
name|digest
decl_stmt|;
DECL|method|BufferedChecksumStreamInput
specifier|public
name|BufferedChecksumStreamInput
parameter_list|(
name|StreamInput
name|in
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
name|this
operator|.
name|digest
operator|=
operator|new
name|BufferedChecksum
argument_list|(
operator|new
name|CRC32
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|BufferedChecksumStreamInput
specifier|public
name|BufferedChecksumStreamInput
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|BufferedChecksumStreamInput
name|reuse
parameter_list|)
block|{
name|this
operator|.
name|in
operator|=
name|in
expr_stmt|;
if|if
condition|(
name|reuse
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|digest
operator|=
operator|new
name|BufferedChecksum
argument_list|(
operator|new
name|CRC32
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|digest
operator|=
name|reuse
operator|.
name|digest
expr_stmt|;
name|digest
operator|.
name|reset
argument_list|()
expr_stmt|;
name|this
operator|.
name|skipBuffer
operator|=
name|reuse
operator|.
name|skipBuffer
expr_stmt|;
block|}
block|}
DECL|method|getChecksum
specifier|public
name|long
name|getChecksum
parameter_list|()
block|{
return|return
name|this
operator|.
name|digest
operator|.
name|getValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|readByte
specifier|public
name|byte
name|readByte
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|byte
name|b
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
name|digest
operator|.
name|update
argument_list|(
name|b
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
annotation|@
name|Override
DECL|method|readBytes
specifier|public
name|void
name|readBytes
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|in
operator|.
name|readBytes
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
name|digest
operator|.
name|update
argument_list|(
name|b
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
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
name|in
operator|.
name|reset
argument_list|()
expr_stmt|;
name|digest
operator|.
name|reset
argument_list|()
expr_stmt|;
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
return|return
name|readByte
argument_list|()
operator|&
literal|0xFF
return|;
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
name|in
operator|.
name|close
argument_list|()
expr_stmt|;
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
name|in
operator|.
name|markSupported
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|skip
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|numBytes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|numBytes
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"numBytes must be>= 0, got "
operator|+
name|numBytes
argument_list|)
throw|;
block|}
if|if
condition|(
name|skipBuffer
operator|==
literal|null
condition|)
block|{
name|skipBuffer
operator|=
operator|new
name|byte
index|[
name|SKIP_BUFFER_SIZE
index|]
expr_stmt|;
block|}
assert|assert
name|skipBuffer
operator|.
name|length
operator|==
name|SKIP_BUFFER_SIZE
assert|;
name|long
name|skipped
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
name|skipped
operator|<
name|numBytes
condition|;
control|)
block|{
specifier|final
name|int
name|step
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|SKIP_BUFFER_SIZE
argument_list|,
name|numBytes
operator|-
name|skipped
argument_list|)
decl_stmt|;
name|readBytes
argument_list|(
name|skipBuffer
argument_list|,
literal|0
argument_list|,
name|step
argument_list|)
expr_stmt|;
name|skipped
operator|+=
name|step
expr_stmt|;
block|}
return|return
name|skipped
return|;
block|}
annotation|@
name|Override
DECL|method|available
specifier|public
name|int
name|available
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|in
operator|.
name|available
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|mark
specifier|public
specifier|synchronized
name|void
name|mark
parameter_list|(
name|int
name|readlimit
parameter_list|)
block|{
name|in
operator|.
name|mark
argument_list|(
name|readlimit
argument_list|)
expr_stmt|;
block|}
DECL|method|resetDigest
specifier|public
name|void
name|resetDigest
parameter_list|()
block|{
name|digest
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
