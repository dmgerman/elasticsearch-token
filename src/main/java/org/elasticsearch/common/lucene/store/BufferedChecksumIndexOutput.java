begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|store
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
name|BufferedIndexOutput
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
name|IndexOutput
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
name|Checksum
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|BufferedChecksumIndexOutput
specifier|public
class|class
name|BufferedChecksumIndexOutput
extends|extends
name|BufferedIndexOutput
block|{
DECL|field|out
specifier|private
specifier|final
name|IndexOutput
name|out
decl_stmt|;
DECL|field|digest
specifier|private
specifier|final
name|Checksum
name|digest
decl_stmt|;
DECL|method|BufferedChecksumIndexOutput
specifier|public
name|BufferedChecksumIndexOutput
parameter_list|(
name|IndexOutput
name|out
parameter_list|,
name|Checksum
name|digest
parameter_list|)
block|{
comment|// we add 8 to be bigger than the default BufferIndexOutput buffer size so any flush will go directly
comment|// to the output without being copied over to the delegate buffer
name|super
argument_list|(
name|BufferedIndexOutput
operator|.
name|DEFAULT_BUFFER_SIZE
operator|+
literal|64
argument_list|)
expr_stmt|;
name|this
operator|.
name|out
operator|=
name|out
expr_stmt|;
name|this
operator|.
name|digest
operator|=
name|digest
expr_stmt|;
block|}
DECL|method|digest
specifier|public
name|Checksum
name|digest
parameter_list|()
block|{
return|return
name|digest
return|;
block|}
DECL|method|underlying
specifier|public
name|IndexOutput
name|underlying
parameter_list|()
block|{
return|return
name|this
operator|.
name|out
return|;
block|}
comment|// don't override it, base class method simple reads from input and writes to this output
comment|//        @Override public void copyBytes(IndexInput input, long numBytes) throws IOException {
comment|//            delegate.copyBytes(input, numBytes);
comment|//        }
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
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
name|out
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|flushBuffer
specifier|protected
name|void
name|flushBuffer
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
name|out
operator|.
name|writeBytes
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
comment|// don't override it, base class method simple reads from input and writes to this output
comment|//        @Override public void copyBytes(IndexInput input, long numBytes) throws IOException {
comment|//            delegate.copyBytes(input, numBytes);
comment|//        }
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
name|super
operator|.
name|flush
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
DECL|method|seek
specifier|public
name|void
name|seek
parameter_list|(
name|long
name|pos
parameter_list|)
throws|throws
name|IOException
block|{
comment|// seek might be called on files, which means that the checksum is not file checksum
comment|// but a checksum of the bytes written to this stream, which is the same for each
comment|// type of file in lucene
name|super
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
name|out
operator|.
name|seek
argument_list|(
name|pos
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|length
specifier|public
name|long
name|length
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|out
operator|.
name|length
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|setLength
specifier|public
name|void
name|setLength
parameter_list|(
name|long
name|length
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|setLength
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|out
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

