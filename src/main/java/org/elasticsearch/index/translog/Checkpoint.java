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
name|ByteArrayDataOutput
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
name|DataInput
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
name|DataOutput
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
name|InputStreamDataInput
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
name|RamUsageEstimator
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
name|Channels
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
name|channels
operator|.
name|FileChannel
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|OpenOption
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|Checkpoint
class|class
name|Checkpoint
block|{
DECL|field|BUFFER_SIZE
specifier|static
specifier|final
name|int
name|BUFFER_SIZE
init|=
name|RamUsageEstimator
operator|.
name|NUM_BYTES_INT
comment|// ops
operator|+
name|RamUsageEstimator
operator|.
name|NUM_BYTES_LONG
comment|// offset
operator|+
name|RamUsageEstimator
operator|.
name|NUM_BYTES_LONG
decl_stmt|;
comment|// generation
DECL|field|offset
specifier|final
name|long
name|offset
decl_stmt|;
DECL|field|numOps
specifier|final
name|int
name|numOps
decl_stmt|;
DECL|field|generation
specifier|final
name|long
name|generation
decl_stmt|;
DECL|method|Checkpoint
name|Checkpoint
parameter_list|(
name|long
name|offset
parameter_list|,
name|int
name|numOps
parameter_list|,
name|long
name|generation
parameter_list|)
block|{
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|numOps
operator|=
name|numOps
expr_stmt|;
name|this
operator|.
name|generation
operator|=
name|generation
expr_stmt|;
block|}
DECL|method|Checkpoint
name|Checkpoint
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|offset
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|numOps
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|generation
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
DECL|method|write
name|void
name|write
parameter_list|(
name|FileChannel
name|channel
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|BUFFER_SIZE
index|]
decl_stmt|;
specifier|final
name|ByteArrayDataOutput
name|out
init|=
operator|new
name|ByteArrayDataOutput
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|Channels
operator|.
name|writeToChannel
argument_list|(
name|buffer
argument_list|,
name|channel
argument_list|)
expr_stmt|;
block|}
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|numOps
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|generation
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
literal|"TranslogInfo{"
operator|+
literal|"offset="
operator|+
name|offset
operator|+
literal|", numOps="
operator|+
name|numOps
operator|+
literal|", translogFileGeneration= "
operator|+
name|generation
operator|+
literal|'}'
return|;
block|}
DECL|method|read
specifier|public
specifier|static
name|Checkpoint
name|read
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|InputStream
name|in
init|=
name|Files
operator|.
name|newInputStream
argument_list|(
name|path
argument_list|)
init|)
block|{
return|return
operator|new
name|Checkpoint
argument_list|(
operator|new
name|InputStreamDataInput
argument_list|(
name|in
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|method|write
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|Path
name|checkpointFile
parameter_list|,
name|Checkpoint
name|checkpoint
parameter_list|,
name|OpenOption
modifier|...
name|options
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|FileChannel
name|channel
init|=
name|FileChannel
operator|.
name|open
argument_list|(
name|checkpointFile
argument_list|,
name|options
argument_list|)
init|)
block|{
name|checkpoint
operator|.
name|write
argument_list|(
name|channel
argument_list|)
expr_stmt|;
name|channel
operator|.
name|force
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

