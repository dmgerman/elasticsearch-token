begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.validate.query
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
name|validate
operator|.
name|query
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
name|BroadcastShardOperationResponse
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
comment|/**  * Internal validate response of a shard validate request executed directly against a specific shard.  *  *  */
end_comment

begin_class
DECL|class|ShardValidateQueryResponse
class|class
name|ShardValidateQueryResponse
extends|extends
name|BroadcastShardOperationResponse
block|{
DECL|field|valid
specifier|private
name|boolean
name|valid
decl_stmt|;
DECL|field|explanation
specifier|private
name|String
name|explanation
decl_stmt|;
DECL|field|error
specifier|private
name|String
name|error
decl_stmt|;
DECL|method|ShardValidateQueryResponse
name|ShardValidateQueryResponse
parameter_list|()
block|{      }
DECL|method|ShardValidateQueryResponse
specifier|public
name|ShardValidateQueryResponse
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|boolean
name|valid
parameter_list|,
name|String
name|explanation
parameter_list|,
name|String
name|error
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|this
operator|.
name|valid
operator|=
name|valid
expr_stmt|;
name|this
operator|.
name|explanation
operator|=
name|explanation
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
block|}
DECL|method|valid
specifier|public
name|boolean
name|valid
parameter_list|()
block|{
return|return
name|this
operator|.
name|valid
return|;
block|}
DECL|method|explanation
specifier|public
name|String
name|explanation
parameter_list|()
block|{
return|return
name|explanation
return|;
block|}
DECL|method|error
specifier|public
name|String
name|error
parameter_list|()
block|{
return|return
name|error
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
name|valid
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|explanation
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|error
operator|=
name|in
operator|.
name|readOptionalString
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
name|valid
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|explanation
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

