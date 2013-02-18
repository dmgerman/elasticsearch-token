begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.cache.clear
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
name|cache
operator|.
name|clear
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
name|broadcast
operator|.
name|BroadcastShardOperationRequest
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
DECL|class|ShardClearIndicesCacheRequest
class|class
name|ShardClearIndicesCacheRequest
extends|extends
name|BroadcastShardOperationRequest
block|{
DECL|field|filterCache
specifier|private
name|boolean
name|filterCache
init|=
literal|false
decl_stmt|;
DECL|field|fieldDataCache
specifier|private
name|boolean
name|fieldDataCache
init|=
literal|false
decl_stmt|;
DECL|field|idCache
specifier|private
name|boolean
name|idCache
init|=
literal|false
decl_stmt|;
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
init|=
literal|null
decl_stmt|;
DECL|field|filterKeys
specifier|private
name|String
index|[]
name|filterKeys
init|=
literal|null
decl_stmt|;
DECL|method|ShardClearIndicesCacheRequest
name|ShardClearIndicesCacheRequest
parameter_list|()
block|{     }
DECL|method|ShardClearIndicesCacheRequest
specifier|public
name|ShardClearIndicesCacheRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|ClearIndicesCacheRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|filterCache
operator|=
name|request
operator|.
name|isFilterCache
argument_list|()
expr_stmt|;
name|fieldDataCache
operator|=
name|request
operator|.
name|isFieldDataCache
argument_list|()
expr_stmt|;
name|idCache
operator|=
name|request
operator|.
name|isIdCache
argument_list|()
expr_stmt|;
name|fields
operator|=
name|request
operator|.
name|getFields
argument_list|()
expr_stmt|;
name|filterKeys
operator|=
name|request
operator|.
name|getFilterKeys
argument_list|()
expr_stmt|;
block|}
DECL|method|isFilterCache
specifier|public
name|boolean
name|isFilterCache
parameter_list|()
block|{
return|return
name|filterCache
return|;
block|}
DECL|method|isFieldDataCache
specifier|public
name|boolean
name|isFieldDataCache
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldDataCache
return|;
block|}
DECL|method|isIdCache
specifier|public
name|boolean
name|isIdCache
parameter_list|()
block|{
return|return
name|this
operator|.
name|idCache
return|;
block|}
DECL|method|getFields
specifier|public
name|String
index|[]
name|getFields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fields
return|;
block|}
DECL|method|getFilterKeys
specifier|public
name|String
index|[]
name|getFilterKeys
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterKeys
return|;
block|}
DECL|method|waitForOperations
specifier|public
name|ShardClearIndicesCacheRequest
name|waitForOperations
parameter_list|(
name|boolean
name|waitForOperations
parameter_list|)
block|{
name|this
operator|.
name|filterCache
operator|=
name|waitForOperations
expr_stmt|;
return|return
name|this
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
name|filterCache
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|fieldDataCache
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|idCache
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|fields
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|filterKeys
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
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
name|writeBoolean
argument_list|(
name|filterCache
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|fieldDataCache
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|idCache
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|filterKeys
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

