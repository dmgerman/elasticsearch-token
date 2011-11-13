begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this  * file except in compliance with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software distributed under  * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS  * OF ANY KIND, either express or implied. See the License for the specific language  * governing permissions and limitations under the License.  */
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Decoder that handles decoding of sequence of encoded LZF chunks,  * combining them into a single contiguous result byte array.  * As of version 0.9, this class has been mostly replaced by  * {@link ChunkDecoder}, although static methods are left here  * and may still be used.  * All static methods use {@link ChunkDecoderFactory#optimalInstance}  * to find actual {@link ChunkDecoder} instance to use.  *  * @author Tatu Saloranta (tatu@ning.com)  */
end_comment

begin_class
DECL|class|LZFDecoder
specifier|public
class|class
name|LZFDecoder
block|{
comment|/*     ///////////////////////////////////////////////////////////////////////     // Old API     ///////////////////////////////////////////////////////////////////////      */
DECL|method|decode
specifier|public
specifier|static
name|byte
index|[]
name|decode
parameter_list|(
specifier|final
name|byte
index|[]
name|inputBuffer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|decode
argument_list|(
name|inputBuffer
argument_list|,
literal|0
argument_list|,
name|inputBuffer
operator|.
name|length
argument_list|)
return|;
block|}
DECL|method|decode
specifier|public
specifier|static
name|byte
index|[]
name|decode
parameter_list|(
specifier|final
name|byte
index|[]
name|inputBuffer
parameter_list|,
name|int
name|inputPtr
parameter_list|,
name|int
name|inputLen
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ChunkDecoderFactory
operator|.
name|optimalInstance
argument_list|()
operator|.
name|decode
argument_list|(
name|inputBuffer
argument_list|)
return|;
block|}
DECL|method|decode
specifier|public
specifier|static
name|int
name|decode
parameter_list|(
specifier|final
name|byte
index|[]
name|inputBuffer
parameter_list|,
specifier|final
name|byte
index|[]
name|targetBuffer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|decode
argument_list|(
name|inputBuffer
argument_list|,
literal|0
argument_list|,
name|inputBuffer
operator|.
name|length
argument_list|,
name|targetBuffer
argument_list|)
return|;
block|}
DECL|method|decode
specifier|public
specifier|static
name|int
name|decode
parameter_list|(
specifier|final
name|byte
index|[]
name|sourceBuffer
parameter_list|,
name|int
name|inPtr
parameter_list|,
name|int
name|inLength
parameter_list|,
specifier|final
name|byte
index|[]
name|targetBuffer
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ChunkDecoderFactory
operator|.
name|optimalInstance
argument_list|()
operator|.
name|decode
argument_list|(
name|sourceBuffer
argument_list|,
name|inPtr
argument_list|,
name|inLength
argument_list|,
name|targetBuffer
argument_list|)
return|;
block|}
DECL|method|calculateUncompressedSize
specifier|public
specifier|static
name|int
name|calculateUncompressedSize
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|ptr
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|ChunkDecoder
operator|.
name|calculateUncompressedSize
argument_list|(
name|data
argument_list|,
name|ptr
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

