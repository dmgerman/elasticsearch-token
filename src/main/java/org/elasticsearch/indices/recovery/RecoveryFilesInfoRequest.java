begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportRequest
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RecoveryFilesInfoRequest
class|class
name|RecoveryFilesInfoRequest
extends|extends
name|TransportRequest
block|{
DECL|field|recoveryId
specifier|private
name|long
name|recoveryId
decl_stmt|;
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
decl_stmt|;
DECL|field|phase1FileNames
name|List
argument_list|<
name|String
argument_list|>
name|phase1FileNames
decl_stmt|;
DECL|field|phase1FileSizes
name|List
argument_list|<
name|Long
argument_list|>
name|phase1FileSizes
decl_stmt|;
DECL|field|phase1ExistingFileNames
name|List
argument_list|<
name|String
argument_list|>
name|phase1ExistingFileNames
decl_stmt|;
DECL|field|phase1ExistingFileSizes
name|List
argument_list|<
name|Long
argument_list|>
name|phase1ExistingFileSizes
decl_stmt|;
DECL|method|RecoveryFilesInfoRequest
name|RecoveryFilesInfoRequest
parameter_list|()
block|{     }
DECL|method|RecoveryFilesInfoRequest
name|RecoveryFilesInfoRequest
parameter_list|(
name|long
name|recoveryId
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|phase1FileNames
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|phase1FileSizes
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|phase1ExistingFileNames
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|phase1ExistingFileSizes
parameter_list|)
block|{
name|this
operator|.
name|recoveryId
operator|=
name|recoveryId
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|phase1FileNames
operator|=
name|phase1FileNames
expr_stmt|;
name|this
operator|.
name|phase1FileSizes
operator|=
name|phase1FileSizes
expr_stmt|;
name|this
operator|.
name|phase1ExistingFileNames
operator|=
name|phase1ExistingFileNames
expr_stmt|;
name|this
operator|.
name|phase1ExistingFileSizes
operator|=
name|phase1ExistingFileSizes
expr_stmt|;
block|}
DECL|method|recoveryId
specifier|public
name|long
name|recoveryId
parameter_list|()
block|{
return|return
name|this
operator|.
name|recoveryId
return|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|recoveryId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|phase1FileNames
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|phase1FileNames
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|phase1FileSizes
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|phase1FileSizes
operator|.
name|add
argument_list|(
name|in
operator|.
name|readVLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|phase1ExistingFileNames
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|phase1ExistingFileNames
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|phase1ExistingFileSizes
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|phase1ExistingFileSizes
operator|.
name|add
argument_list|(
name|in
operator|.
name|readVLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|recoveryId
argument_list|)
expr_stmt|;
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|phase1FileNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|phase1FileName
range|:
name|phase1FileNames
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|phase1FileName
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|phase1FileSizes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|phase1FileSize
range|:
name|phase1FileSizes
control|)
block|{
name|out
operator|.
name|writeVLong
argument_list|(
name|phase1FileSize
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|phase1ExistingFileNames
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|phase1ExistingFileName
range|:
name|phase1ExistingFileNames
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|phase1ExistingFileName
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|phase1ExistingFileSizes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Long
name|phase1ExistingFileSize
range|:
name|phase1ExistingFileSizes
control|)
block|{
name|out
operator|.
name|writeVLong
argument_list|(
name|phase1ExistingFileSize
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

