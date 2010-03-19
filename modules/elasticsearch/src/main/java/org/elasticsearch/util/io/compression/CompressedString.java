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
name|elasticsearch
operator|.
name|util
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
name|util
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
name|util
operator|.
name|io
operator|.
name|stream
operator|.
name|Streamable
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
DECL|class|CompressedString
specifier|public
class|class
name|CompressedString
implements|implements
name|Streamable
block|{
DECL|field|compressedString
specifier|private
name|byte
index|[]
name|compressedString
decl_stmt|;
DECL|field|string
specifier|private
specifier|transient
name|String
name|string
decl_stmt|;
DECL|method|CompressedString
name|CompressedString
parameter_list|()
block|{     }
DECL|method|CompressedString
specifier|public
name|CompressedString
parameter_list|(
name|String
name|string
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|string
operator|=
name|string
expr_stmt|;
name|this
operator|.
name|compressedString
operator|=
operator|new
name|ZipCompressor
argument_list|()
operator|.
name|compressString
argument_list|(
name|string
argument_list|)
expr_stmt|;
block|}
DECL|method|string
specifier|public
name|String
name|string
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|string
operator|!=
literal|null
condition|)
block|{
return|return
name|string
return|;
block|}
name|string
operator|=
operator|new
name|ZipCompressor
argument_list|()
operator|.
name|decompressString
argument_list|(
name|compressedString
argument_list|)
expr_stmt|;
return|return
name|string
return|;
block|}
DECL|method|readCompressedString
specifier|public
specifier|static
name|CompressedString
name|readCompressedString
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|CompressedString
name|result
init|=
operator|new
name|CompressedString
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|compressedString
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|compressedString
argument_list|)
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|compressedString
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|compressedString
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

