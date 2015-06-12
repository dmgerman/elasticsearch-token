begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|index
operator|.
name|CorruptIndexException
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
name|BufferedChecksum
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
name|Adler32
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
comment|/**   * Implements verification checks to the best extent possible  * against legacy segments.  *<p>  * For files since ES 1.3, we have a lucene checksum, and  * we verify both CRC32 + length from that.  * For older segment files, we have an elasticsearch Adler32 checksum  * and a length, except for commit points.  * For older commit points, we only have the length in metadata,  * but lucene always wrote a CRC32 checksum we can verify in the future, too.  * For (Jurassic?) files, we dont have an Adler32 checksum at all,  * since its optional in the protocol. But we always know the length.  * @deprecated only to support old segments  */
end_comment

begin_class
annotation|@
name|Deprecated
DECL|class|LegacyVerification
class|class
name|LegacyVerification
block|{
comment|// TODO: add a verifier for old lucene segments_N that also checks CRC.
comment|// but for now, at least truncation is detected here (as length will be checked)
comment|/**       * verifies Adler32 + length for index files before lucene 4.8      */
DECL|class|Adler32VerifyingIndexOutput
specifier|static
class|class
name|Adler32VerifyingIndexOutput
extends|extends
name|VerifyingIndexOutput
block|{
DECL|field|adler32
specifier|final
name|String
name|adler32
decl_stmt|;
DECL|field|length
specifier|final
name|long
name|length
decl_stmt|;
DECL|field|checksum
specifier|final
name|Checksum
name|checksum
init|=
operator|new
name|BufferedChecksum
argument_list|(
operator|new
name|Adler32
argument_list|()
argument_list|)
decl_stmt|;
DECL|field|written
name|long
name|written
decl_stmt|;
DECL|method|Adler32VerifyingIndexOutput
specifier|public
name|Adler32VerifyingIndexOutput
parameter_list|(
name|IndexOutput
name|out
parameter_list|,
name|String
name|adler32
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|this
operator|.
name|adler32
operator|=
name|adler32
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
DECL|method|verify
specifier|public
name|void
name|verify
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|written
operator|!=
name|length
condition|)
block|{
throw|throw
operator|new
name|CorruptIndexException
argument_list|(
literal|"expected length="
operator|+
name|length
operator|+
literal|" != actual length: "
operator|+
name|written
operator|+
literal|" : file truncated?"
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
specifier|final
name|String
name|actualChecksum
init|=
name|Store
operator|.
name|digestToString
argument_list|(
name|checksum
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|adler32
operator|.
name|equals
argument_list|(
name|actualChecksum
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|CorruptIndexException
argument_list|(
literal|"checksum failed (hardware problem?) : expected="
operator|+
name|adler32
operator|+
literal|" actual="
operator|+
name|actualChecksum
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
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
name|out
operator|.
name|writeByte
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|checksum
operator|.
name|update
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|written
operator|++
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
name|bytes
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
name|out
operator|.
name|writeBytes
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|checksum
operator|.
name|update
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|written
operator|+=
name|length
expr_stmt|;
block|}
block|}
comment|/**       * verifies length for index files before lucene 4.8      */
DECL|class|LengthVerifyingIndexOutput
specifier|static
class|class
name|LengthVerifyingIndexOutput
extends|extends
name|VerifyingIndexOutput
block|{
DECL|field|length
specifier|final
name|long
name|length
decl_stmt|;
DECL|field|written
name|long
name|written
decl_stmt|;
DECL|method|LengthVerifyingIndexOutput
specifier|public
name|LengthVerifyingIndexOutput
parameter_list|(
name|IndexOutput
name|out
parameter_list|,
name|long
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|out
argument_list|)
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
DECL|method|verify
specifier|public
name|void
name|verify
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|written
operator|!=
name|length
condition|)
block|{
throw|throw
operator|new
name|CorruptIndexException
argument_list|(
literal|"expected length="
operator|+
name|length
operator|+
literal|" != actual length: "
operator|+
name|written
operator|+
literal|" : file truncated?"
argument_list|,
name|out
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
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
name|out
operator|.
name|writeByte
argument_list|(
name|b
argument_list|)
expr_stmt|;
name|written
operator|++
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
name|bytes
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
name|out
operator|.
name|writeBytes
argument_list|(
name|bytes
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|written
operator|+=
name|length
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
