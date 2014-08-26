begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.compress.lzf
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|compress
operator|.
name|lzf
package|;
end_package

begin_import
import|import
name|com
operator|.
name|ning
operator|.
name|compress
operator|.
name|lzf
operator|.
name|ChunkDecoder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|ning
operator|.
name|compress
operator|.
name|lzf
operator|.
name|ChunkEncoder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|ning
operator|.
name|compress
operator|.
name|lzf
operator|.
name|LZFChunk
import|;
end_import

begin_import
import|import
name|com
operator|.
name|ning
operator|.
name|compress
operator|.
name|lzf
operator|.
name|LZFEncoder
import|;
end_import

begin_import
import|import
name|com
operator|.
name|ning
operator|.
name|compress
operator|.
name|lzf
operator|.
name|util
operator|.
name|ChunkDecoderFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|ning
operator|.
name|compress
operator|.
name|lzf
operator|.
name|util
operator|.
name|ChunkEncoderFactory
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
name|store
operator|.
name|IndexInput
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
name|Constants
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
name|compress
operator|.
name|CompressedIndexInput
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
name|CompressedStreamInput
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
name|CompressedStreamOutput
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
name|logging
operator|.
name|Loggers
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
name|settings
operator|.
name|Settings
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|LZFCompressor
specifier|public
class|class
name|LZFCompressor
implements|implements
name|Compressor
block|{
DECL|field|LUCENE_HEADER
specifier|static
specifier|final
name|byte
index|[]
name|LUCENE_HEADER
init|=
block|{
literal|'L'
block|,
literal|'Z'
block|,
literal|'F'
block|,
literal|0
block|}
decl_stmt|;
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"lzf"
decl_stmt|;
DECL|field|encoder
specifier|private
name|ChunkEncoder
name|encoder
decl_stmt|;
DECL|field|decoder
specifier|private
name|ChunkDecoder
name|decoder
decl_stmt|;
DECL|method|LZFCompressor
specifier|public
name|LZFCompressor
parameter_list|()
block|{
name|this
operator|.
name|encoder
operator|=
name|ChunkEncoderFactory
operator|.
name|safeInstance
argument_list|()
expr_stmt|;
name|this
operator|.
name|decoder
operator|=
name|ChunkDecoderFactory
operator|.
name|safeInstance
argument_list|()
expr_stmt|;
name|Loggers
operator|.
name|getLogger
argument_list|(
name|LZFCompressor
operator|.
name|class
argument_list|)
operator|.
name|debug
argument_list|(
literal|"using encoder [{}] and decoder[{}] "
argument_list|,
name|this
operator|.
name|encoder
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|this
operator|.
name|decoder
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|public
name|void
name|configure
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{}
annotation|@
name|Override
DECL|method|isCompressed
specifier|public
name|boolean
name|isCompressed
parameter_list|(
name|BytesReference
name|bytes
parameter_list|)
block|{
return|return
name|bytes
operator|.
name|length
argument_list|()
operator|>=
literal|3
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BYTE_Z
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BYTE_V
operator|&&
operator|(
name|bytes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BLOCK_TYPE_COMPRESSED
operator|||
name|bytes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BLOCK_TYPE_NON_COMPRESSED
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|isCompressed
specifier|public
name|boolean
name|isCompressed
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|length
operator|>=
literal|3
operator|&&
name|data
index|[
name|offset
index|]
operator|==
name|LZFChunk
operator|.
name|BYTE_Z
operator|&&
name|data
index|[
name|offset
operator|+
literal|1
index|]
operator|==
name|LZFChunk
operator|.
name|BYTE_V
operator|&&
operator|(
name|data
index|[
name|offset
operator|+
literal|2
index|]
operator|==
name|LZFChunk
operator|.
name|BLOCK_TYPE_COMPRESSED
operator|||
name|data
index|[
name|offset
operator|+
literal|2
index|]
operator|==
name|LZFChunk
operator|.
name|BLOCK_TYPE_NON_COMPRESSED
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|isCompressed
specifier|public
name|boolean
name|isCompressed
parameter_list|(
name|ChannelBuffer
name|buffer
parameter_list|)
block|{
name|int
name|offset
init|=
name|buffer
operator|.
name|readerIndex
argument_list|()
decl_stmt|;
return|return
name|buffer
operator|.
name|readableBytes
argument_list|()
operator|>=
literal|3
operator|&&
name|buffer
operator|.
name|getByte
argument_list|(
name|offset
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BYTE_Z
operator|&&
name|buffer
operator|.
name|getByte
argument_list|(
name|offset
operator|+
literal|1
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BYTE_V
operator|&&
operator|(
name|buffer
operator|.
name|getByte
argument_list|(
name|offset
operator|+
literal|2
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BLOCK_TYPE_COMPRESSED
operator|||
name|buffer
operator|.
name|getByte
argument_list|(
name|offset
operator|+
literal|2
argument_list|)
operator|==
name|LZFChunk
operator|.
name|BLOCK_TYPE_NON_COMPRESSED
operator|)
return|;
block|}
annotation|@
name|Override
DECL|method|isCompressed
specifier|public
name|boolean
name|isCompressed
parameter_list|(
name|IndexInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|currentPointer
init|=
name|in
operator|.
name|getFilePointer
argument_list|()
decl_stmt|;
comment|// since we have some metdata before the first compressed header, we check on our specific header
if|if
condition|(
name|in
operator|.
name|length
argument_list|()
operator|-
name|currentPointer
operator|<
operator|(
name|LUCENE_HEADER
operator|.
name|length
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|LUCENE_HEADER
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|in
operator|.
name|readByte
argument_list|()
operator|!=
name|LUCENE_HEADER
index|[
name|i
index|]
condition|)
block|{
name|in
operator|.
name|seek
argument_list|(
name|currentPointer
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
block|}
name|in
operator|.
name|seek
argument_list|(
name|currentPointer
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|uncompress
specifier|public
name|byte
index|[]
name|uncompress
parameter_list|(
name|byte
index|[]
name|data
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
return|return
name|decoder
operator|.
name|decode
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|compress
specifier|public
name|byte
index|[]
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
name|length
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|LZFEncoder
operator|.
name|encode
argument_list|(
name|encoder
argument_list|,
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|streamInput
specifier|public
name|CompressedStreamInput
name|streamInput
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|LZFCompressedStreamInput
argument_list|(
name|in
argument_list|,
name|decoder
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|streamOutput
specifier|public
name|CompressedStreamOutput
name|streamOutput
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|LZFCompressedStreamOutput
argument_list|(
name|out
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|indexInput
specifier|public
name|CompressedIndexInput
name|indexInput
parameter_list|(
name|IndexInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|LZFCompressedIndexInput
argument_list|(
name|in
argument_list|,
name|decoder
argument_list|)
return|;
block|}
block|}
end_class

end_unit

