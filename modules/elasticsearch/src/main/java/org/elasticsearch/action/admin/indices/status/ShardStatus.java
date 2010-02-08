begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.status
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|status
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|shards
operator|.
name|ShardOperationResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRouting
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
name|IndexShardState
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
name|SizeValue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeValue
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|ShardStatus
specifier|public
class|class
name|ShardStatus
extends|extends
name|ShardOperationResponse
block|{
DECL|class|Docs
specifier|public
specifier|static
class|class
name|Docs
block|{
DECL|field|UNKNOWN
specifier|public
specifier|static
specifier|final
name|Docs
name|UNKNOWN
init|=
operator|new
name|Docs
argument_list|()
decl_stmt|;
DECL|field|numDocs
name|int
name|numDocs
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|maxDoc
name|int
name|maxDoc
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|deletedDocs
name|int
name|deletedDocs
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|numDocs
specifier|public
name|int
name|numDocs
parameter_list|()
block|{
return|return
name|numDocs
return|;
block|}
DECL|method|maxDoc
specifier|public
name|int
name|maxDoc
parameter_list|()
block|{
return|return
name|maxDoc
return|;
block|}
DECL|method|deletedDocs
specifier|public
name|int
name|deletedDocs
parameter_list|()
block|{
return|return
name|deletedDocs
return|;
block|}
block|}
DECL|field|state
name|IndexShardState
name|state
decl_stmt|;
DECL|field|storeSize
name|SizeValue
name|storeSize
init|=
name|SizeValue
operator|.
name|UNKNOWN
decl_stmt|;
DECL|field|estimatedFlushableMemorySize
name|SizeValue
name|estimatedFlushableMemorySize
init|=
name|SizeValue
operator|.
name|UNKNOWN
decl_stmt|;
DECL|field|translogId
name|long
name|translogId
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|translogOperations
name|long
name|translogOperations
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|docs
name|Docs
name|docs
init|=
name|Docs
operator|.
name|UNKNOWN
decl_stmt|;
DECL|method|ShardStatus
name|ShardStatus
parameter_list|()
block|{     }
DECL|method|ShardStatus
name|ShardStatus
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|)
block|{
name|super
argument_list|(
name|shardRouting
argument_list|)
expr_stmt|;
block|}
DECL|method|state
specifier|public
name|IndexShardState
name|state
parameter_list|()
block|{
return|return
name|state
return|;
block|}
DECL|method|storeSize
specifier|public
name|SizeValue
name|storeSize
parameter_list|()
block|{
return|return
name|storeSize
return|;
block|}
DECL|method|estimatedFlushableMemorySize
specifier|public
name|SizeValue
name|estimatedFlushableMemorySize
parameter_list|()
block|{
return|return
name|estimatedFlushableMemorySize
return|;
block|}
DECL|method|translogId
specifier|public
name|long
name|translogId
parameter_list|()
block|{
return|return
name|translogId
return|;
block|}
DECL|method|translogOperations
specifier|public
name|long
name|translogOperations
parameter_list|()
block|{
return|return
name|translogOperations
return|;
block|}
DECL|method|docs
specifier|public
name|Docs
name|docs
parameter_list|()
block|{
return|return
name|docs
return|;
block|}
DECL|method|readIndexShardStatus
specifier|public
specifier|static
name|ShardStatus
name|readIndexShardStatus
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|ClassNotFoundException
throws|,
name|IOException
block|{
name|ShardStatus
name|shardStatus
init|=
operator|new
name|ShardStatus
argument_list|()
decl_stmt|;
name|shardStatus
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|shardStatus
return|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
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
name|writeByte
argument_list|(
name|state
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|storeSize
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|estimatedFlushableMemorySize
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
name|translogId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|translogOperations
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|docs
operator|.
name|numDocs
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|docs
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|docs
operator|.
name|deletedDocs
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|state
operator|=
name|IndexShardState
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|storeSize
operator|=
name|readSizeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|estimatedFlushableMemorySize
operator|=
name|readSizeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|translogId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|translogOperations
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|docs
operator|=
operator|new
name|Docs
argument_list|()
expr_stmt|;
name|docs
operator|.
name|numDocs
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|docs
operator|.
name|maxDoc
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|docs
operator|.
name|deletedDocs
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

