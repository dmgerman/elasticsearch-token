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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
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
name|Nullable
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
name|BytesArray
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
name|index
operator|.
name|store
operator|.
name|StoreFileMetaData
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RecoveryFileChunkRequest
specifier|public
specifier|final
class|class
name|RecoveryFileChunkRequest
extends|extends
name|TransportRequest
block|{
comment|// public for testing
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
DECL|field|position
specifier|private
name|long
name|position
decl_stmt|;
DECL|field|content
specifier|private
name|BytesReference
name|content
decl_stmt|;
DECL|field|metaData
specifier|private
name|StoreFileMetaData
name|metaData
decl_stmt|;
DECL|method|RecoveryFileChunkRequest
name|RecoveryFileChunkRequest
parameter_list|()
block|{     }
DECL|method|RecoveryFileChunkRequest
specifier|public
name|RecoveryFileChunkRequest
parameter_list|(
name|long
name|recoveryId
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|StoreFileMetaData
name|metaData
parameter_list|,
name|long
name|position
parameter_list|,
name|BytesReference
name|content
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
name|metaData
operator|=
name|metaData
expr_stmt|;
name|this
operator|.
name|position
operator|=
name|position
expr_stmt|;
name|this
operator|.
name|content
operator|=
name|content
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
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|metaData
operator|.
name|name
argument_list|()
return|;
block|}
DECL|method|position
specifier|public
name|long
name|position
parameter_list|()
block|{
return|return
name|position
return|;
block|}
annotation|@
name|Nullable
DECL|method|checksum
specifier|public
name|String
name|checksum
parameter_list|()
block|{
return|return
name|metaData
operator|.
name|checksum
argument_list|()
return|;
block|}
DECL|method|length
specifier|public
name|long
name|length
parameter_list|()
block|{
return|return
name|metaData
operator|.
name|length
argument_list|()
return|;
block|}
DECL|method|content
specifier|public
name|BytesReference
name|content
parameter_list|()
block|{
return|return
name|content
return|;
block|}
DECL|method|readFileChunk
specifier|public
name|RecoveryFileChunkRequest
name|readFileChunk
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|RecoveryFileChunkRequest
name|request
init|=
operator|new
name|RecoveryFileChunkRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|request
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
name|String
name|name
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|position
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|long
name|length
init|=
name|in
operator|.
name|readVLong
argument_list|()
decl_stmt|;
name|String
name|checksum
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|content
operator|=
name|in
operator|.
name|readBytesReference
argument_list|()
expr_stmt|;
name|Version
name|writtenBy
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
name|String
name|versionString
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|writtenBy
operator|=
name|versionString
operator|==
literal|null
condition|?
literal|null
else|:
name|Version
operator|.
name|parseLeniently
argument_list|(
name|versionString
argument_list|)
expr_stmt|;
block|}
name|metaData
operator|=
operator|new
name|StoreFileMetaData
argument_list|(
name|name
argument_list|,
name|length
argument_list|,
name|checksum
argument_list|,
name|writtenBy
argument_list|)
expr_stmt|;
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
name|writeString
argument_list|(
name|metaData
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|position
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|metaData
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|metaData
operator|.
name|checksum
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytesReference
argument_list|(
name|content
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|org
operator|.
name|elasticsearch
operator|.
name|Version
operator|.
name|V_1_3_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalString
argument_list|(
name|metaData
operator|.
name|writtenBy
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|metaData
operator|.
name|writtenBy
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|shardId
operator|+
literal|": name='"
operator|+
name|name
argument_list|()
operator|+
literal|'\''
operator|+
literal|", position="
operator|+
name|position
operator|+
literal|", length="
operator|+
name|length
argument_list|()
return|;
block|}
DECL|method|metadata
specifier|public
name|StoreFileMetaData
name|metadata
parameter_list|()
block|{
return|return
name|metaData
return|;
block|}
block|}
end_class

end_unit

