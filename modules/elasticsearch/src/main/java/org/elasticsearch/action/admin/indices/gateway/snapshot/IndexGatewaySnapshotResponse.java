begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.gateway.snapshot
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
name|gateway
operator|.
name|snapshot
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
name|ActionResponse
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
comment|/**  * An index level gateway snapshot response.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|IndexGatewaySnapshotResponse
specifier|public
class|class
name|IndexGatewaySnapshotResponse
implements|implements
name|ActionResponse
implements|,
name|Streamable
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|successfulShards
specifier|private
name|int
name|successfulShards
decl_stmt|;
DECL|field|failedShards
specifier|private
name|int
name|failedShards
decl_stmt|;
DECL|method|IndexGatewaySnapshotResponse
name|IndexGatewaySnapshotResponse
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|successfulShards
operator|=
name|successfulShards
expr_stmt|;
name|this
operator|.
name|failedShards
operator|=
name|failedShards
expr_stmt|;
block|}
DECL|method|IndexGatewaySnapshotResponse
name|IndexGatewaySnapshotResponse
parameter_list|()
block|{      }
comment|/**      * The index the gateway snapshot has performed on.      */
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
comment|/**      * The number of successful shards the gateway snapshot operation was performed on.      */
DECL|method|successfulShards
specifier|public
name|int
name|successfulShards
parameter_list|()
block|{
return|return
name|successfulShards
return|;
block|}
comment|/**      * The number of failed shards the gateway snapshot operation was performed on.      */
DECL|method|failedShards
specifier|public
name|int
name|failedShards
parameter_list|()
block|{
return|return
name|failedShards
return|;
block|}
comment|/**      * The number of total shards the gateway snapshot operation was performed on.      */
DECL|method|totalShards
specifier|public
name|int
name|totalShards
parameter_list|()
block|{
return|return
name|successfulShards
operator|+
name|failedShards
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
name|index
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|successfulShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|failedShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
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
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|successfulShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|failedShards
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

