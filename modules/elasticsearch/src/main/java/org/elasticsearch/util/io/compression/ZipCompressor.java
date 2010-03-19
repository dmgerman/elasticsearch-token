begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.io.compression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|compression
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
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|io
operator|.
name|FastByteArrayOutputStream
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
name|DataFormatException
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
name|Deflater
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
name|Inflater
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|ZipCompressor
specifier|public
class|class
name|ZipCompressor
implements|implements
name|Compressor
block|{
DECL|class|Cached
specifier|private
specifier|static
class|class
name|Cached
block|{
DECL|field|cache
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|CompressHolder
argument_list|>
name|cache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|CompressHolder
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|CompressHolder
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|CompressHolder
argument_list|()
return|;
block|}
block|}
decl_stmt|;
comment|/**          * Returns the cached thread local byte strean, with its internal stream cleared.          */
DECL|method|cached
specifier|public
specifier|static
name|CompressHolder
name|cached
parameter_list|()
block|{
name|CompressHolder
name|ch
init|=
name|cache
operator|.
name|get
argument_list|()
decl_stmt|;
name|ch
operator|.
name|bos
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|ch
return|;
block|}
block|}
DECL|class|CompressHolder
specifier|private
specifier|static
class|class
name|CompressHolder
block|{
DECL|field|bos
specifier|final
name|FastByteArrayOutputStream
name|bos
init|=
operator|new
name|FastByteArrayOutputStream
argument_list|()
decl_stmt|;
DECL|field|deflater
specifier|final
name|Deflater
name|deflater
init|=
operator|new
name|Deflater
argument_list|()
decl_stmt|;
DECL|field|inflater
specifier|final
name|Inflater
name|inflater
init|=
operator|new
name|Inflater
argument_list|()
decl_stmt|;
DECL|field|buffer
specifier|final
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
operator|(
name|int
operator|)
name|SizeUnit
operator|.
name|KB
operator|.
name|toBytes
argument_list|(
literal|5
argument_list|)
index|]
decl_stmt|;
DECL|field|utf8Result
specifier|final
name|UnicodeUtil
operator|.
name|UTF8Result
name|utf8Result
init|=
operator|new
name|UnicodeUtil
operator|.
name|UTF8Result
argument_list|()
decl_stmt|;
block|}
DECL|field|compressionLevel
specifier|private
specifier|final
name|int
name|compressionLevel
decl_stmt|;
DECL|method|ZipCompressor
specifier|public
name|ZipCompressor
parameter_list|()
block|{
name|this
argument_list|(
name|Deflater
operator|.
name|BEST_COMPRESSION
argument_list|)
expr_stmt|;
block|}
DECL|method|ZipCompressor
specifier|public
name|ZipCompressor
parameter_list|(
name|int
name|compressionLevel
parameter_list|)
block|{
name|this
operator|.
name|compressionLevel
operator|=
name|compressionLevel
expr_stmt|;
block|}
DECL|method|compress
specifier|public
name|byte
index|[]
name|compress
parameter_list|(
name|byte
index|[]
name|value
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
name|compress
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|compressionLevel
argument_list|,
name|Cached
operator|.
name|cached
argument_list|()
argument_list|)
return|;
block|}
DECL|method|compress
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|compress
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|compress
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|)
return|;
block|}
DECL|method|compressString
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|compressString
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|CompressHolder
name|ch
init|=
name|Cached
operator|.
name|cached
argument_list|()
decl_stmt|;
name|UnicodeUtil
operator|.
name|UTF16toUTF8
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|()
argument_list|,
name|ch
operator|.
name|utf8Result
argument_list|)
expr_stmt|;
return|return
name|compress
argument_list|(
name|ch
operator|.
name|utf8Result
operator|.
name|result
argument_list|,
literal|0
argument_list|,
name|ch
operator|.
name|utf8Result
operator|.
name|length
argument_list|,
name|compressionLevel
argument_list|,
name|ch
argument_list|)
return|;
block|}
DECL|method|decompress
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|decompress
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|CompressHolder
name|ch
init|=
name|Cached
operator|.
name|cached
argument_list|()
decl_stmt|;
name|decompress
argument_list|(
name|value
argument_list|,
name|ch
argument_list|)
expr_stmt|;
return|return
name|ch
operator|.
name|bos
operator|.
name|copiedByteArray
argument_list|()
return|;
block|}
DECL|method|decompressString
annotation|@
name|Override
specifier|public
name|String
name|decompressString
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|CompressHolder
name|ch
init|=
name|Cached
operator|.
name|cached
argument_list|()
decl_stmt|;
name|decompress
argument_list|(
name|value
argument_list|,
name|ch
argument_list|)
expr_stmt|;
return|return
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|ch
operator|.
name|bos
operator|.
name|unsafeByteArray
argument_list|()
argument_list|,
literal|0
argument_list|,
name|ch
operator|.
name|bos
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
DECL|method|decompress
specifier|private
specifier|static
name|void
name|decompress
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|CompressHolder
name|ch
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|ch
operator|.
name|inflater
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ch
operator|.
name|inflater
operator|.
name|setInput
argument_list|(
name|value
argument_list|)
expr_stmt|;
comment|// Decompress the data
specifier|final
name|byte
index|[]
name|buf
init|=
name|ch
operator|.
name|buffer
decl_stmt|;
while|while
condition|(
operator|!
name|ch
operator|.
name|inflater
operator|.
name|finished
argument_list|()
condition|)
block|{
name|int
name|count
init|=
name|ch
operator|.
name|inflater
operator|.
name|inflate
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|ch
operator|.
name|bos
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|DataFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to decompress"
argument_list|,
name|e
argument_list|)
throw|;
block|}
comment|// don't close the inflater, we reuse it...
block|}
DECL|method|compress
specifier|private
specifier|static
name|byte
index|[]
name|compress
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|compressionLevel
parameter_list|,
name|CompressHolder
name|ch
parameter_list|)
throws|throws
name|IOException
block|{
name|ch
operator|.
name|deflater
operator|.
name|reset
argument_list|()
expr_stmt|;
name|ch
operator|.
name|deflater
operator|.
name|setLevel
argument_list|(
name|compressionLevel
argument_list|)
expr_stmt|;
name|ch
operator|.
name|deflater
operator|.
name|setInput
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|ch
operator|.
name|deflater
operator|.
name|finish
argument_list|()
expr_stmt|;
comment|// Compress the data
specifier|final
name|byte
index|[]
name|buf
init|=
name|ch
operator|.
name|buffer
decl_stmt|;
while|while
condition|(
operator|!
name|ch
operator|.
name|deflater
operator|.
name|finished
argument_list|()
condition|)
block|{
name|int
name|count
init|=
name|ch
operator|.
name|deflater
operator|.
name|deflate
argument_list|(
name|buf
argument_list|)
decl_stmt|;
name|ch
operator|.
name|bos
operator|.
name|write
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
return|return
name|ch
operator|.
name|bos
operator|.
name|copiedByteArray
argument_list|()
return|;
block|}
block|}
end_class

end_unit

