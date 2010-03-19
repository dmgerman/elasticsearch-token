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
name|compression
operator|.
name|lzf
operator|.
name|LZFDecoder
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
name|compression
operator|.
name|lzf
operator|.
name|LZFEncoder
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|LzfCompressor
specifier|public
class|class
name|LzfCompressor
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
DECL|method|cached
specifier|public
specifier|static
name|CompressHolder
name|cached
parameter_list|()
block|{
return|return
name|cache
operator|.
name|get
argument_list|()
return|;
block|}
block|}
DECL|class|CompressHolder
specifier|private
specifier|static
class|class
name|CompressHolder
block|{
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
name|LZFEncoder
operator|.
name|encode
argument_list|(
name|value
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
name|LZFEncoder
operator|.
name|encode
argument_list|(
name|ch
operator|.
name|utf8Result
operator|.
name|result
argument_list|,
name|ch
operator|.
name|utf8Result
operator|.
name|length
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
return|return
name|LZFDecoder
operator|.
name|decode
argument_list|(
name|value
argument_list|,
name|value
operator|.
name|length
argument_list|)
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
name|byte
index|[]
name|result
init|=
name|decompress
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|result
argument_list|,
literal|0
argument_list|,
name|result
operator|.
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

