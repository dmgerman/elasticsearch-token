begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.delete
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|delete
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
name|DocWriteResponse
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
name|xcontent
operator|.
name|XContentBuilder
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
name|rest
operator|.
name|RestStatus
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
comment|/**  * The response of the delete action.  *  * @see org.elasticsearch.action.delete.DeleteRequest  * @see org.elasticsearch.client.Client#delete(DeleteRequest)  */
end_comment

begin_class
DECL|class|DeleteResponse
specifier|public
class|class
name|DeleteResponse
extends|extends
name|DocWriteResponse
block|{
DECL|method|DeleteResponse
specifier|public
name|DeleteResponse
parameter_list|()
block|{      }
DECL|method|DeleteResponse
specifier|public
name|DeleteResponse
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|long
name|version
parameter_list|,
name|boolean
name|found
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|version
argument_list|,
name|found
condition|?
name|Operation
operator|.
name|DELETE
else|:
name|Operation
operator|.
name|NOOP
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|operation
operator|==
name|Operation
operator|.
name|DELETE
condition|?
name|super
operator|.
name|status
argument_list|()
else|:
name|RestStatus
operator|.
name|NOT_FOUND
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"found"
argument_list|,
name|operation
operator|==
name|Operation
operator|.
name|DELETE
argument_list|)
expr_stmt|;
name|super
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"DeleteResponse["
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|"index="
argument_list|)
operator|.
name|append
argument_list|(
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",type="
argument_list|)
operator|.
name|append
argument_list|(
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",id="
argument_list|)
operator|.
name|append
argument_list|(
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",version="
argument_list|)
operator|.
name|append
argument_list|(
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",operation="
argument_list|)
operator|.
name|append
argument_list|(
name|getOperation
argument_list|()
operator|.
name|getLowercase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|append
argument_list|(
literal|",shards="
argument_list|)
operator|.
name|append
argument_list|(
name|getShardInfo
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

