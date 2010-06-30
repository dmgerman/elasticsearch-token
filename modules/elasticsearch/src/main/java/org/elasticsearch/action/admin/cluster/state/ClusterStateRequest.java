begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.state
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|state
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
name|ActionRequestValidationException
import|;
end_import

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
name|master
operator|.
name|MasterNodeOperationRequest
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
name|Strings
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ClusterStateRequest
specifier|public
class|class
name|ClusterStateRequest
extends|extends
name|MasterNodeOperationRequest
block|{
DECL|field|filterRoutingTable
specifier|private
name|boolean
name|filterRoutingTable
init|=
literal|false
decl_stmt|;
DECL|field|filterNodes
specifier|private
name|boolean
name|filterNodes
init|=
literal|false
decl_stmt|;
DECL|field|filterMetaData
specifier|private
name|boolean
name|filterMetaData
init|=
literal|false
decl_stmt|;
DECL|field|filterBlocks
specifier|private
name|boolean
name|filterBlocks
init|=
literal|false
decl_stmt|;
DECL|field|filteredIndices
specifier|private
name|String
index|[]
name|filteredIndices
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|method|ClusterStateRequest
specifier|public
name|ClusterStateRequest
parameter_list|()
block|{     }
DECL|method|validate
annotation|@
name|Override
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|filterRoutingTable
specifier|public
name|boolean
name|filterRoutingTable
parameter_list|()
block|{
return|return
name|filterRoutingTable
return|;
block|}
DECL|method|filterRoutingTable
specifier|public
name|ClusterStateRequest
name|filterRoutingTable
parameter_list|(
name|boolean
name|filterRoutingTable
parameter_list|)
block|{
name|this
operator|.
name|filterRoutingTable
operator|=
name|filterRoutingTable
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filterNodes
specifier|public
name|boolean
name|filterNodes
parameter_list|()
block|{
return|return
name|filterNodes
return|;
block|}
DECL|method|filterNodes
specifier|public
name|ClusterStateRequest
name|filterNodes
parameter_list|(
name|boolean
name|filterNodes
parameter_list|)
block|{
name|this
operator|.
name|filterNodes
operator|=
name|filterNodes
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filterMetaData
specifier|public
name|boolean
name|filterMetaData
parameter_list|()
block|{
return|return
name|filterMetaData
return|;
block|}
DECL|method|filterMetaData
specifier|public
name|ClusterStateRequest
name|filterMetaData
parameter_list|(
name|boolean
name|filterMetaData
parameter_list|)
block|{
name|this
operator|.
name|filterMetaData
operator|=
name|filterMetaData
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filterBlocks
specifier|public
name|boolean
name|filterBlocks
parameter_list|()
block|{
return|return
name|filterBlocks
return|;
block|}
DECL|method|filterBlocks
specifier|public
name|ClusterStateRequest
name|filterBlocks
parameter_list|(
name|boolean
name|filterBlocks
parameter_list|)
block|{
name|this
operator|.
name|filterBlocks
operator|=
name|filterBlocks
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filteredIndices
specifier|public
name|String
index|[]
name|filteredIndices
parameter_list|()
block|{
return|return
name|filteredIndices
return|;
block|}
DECL|method|filteredIndices
specifier|public
name|ClusterStateRequest
name|filteredIndices
parameter_list|(
name|String
modifier|...
name|filteredIndices
parameter_list|)
block|{
name|this
operator|.
name|filteredIndices
operator|=
name|filteredIndices
expr_stmt|;
return|return
name|this
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
name|filterRoutingTable
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|filterNodes
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|filterMetaData
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|filterBlocks
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|filteredIndices
operator|=
operator|new
name|String
index|[
name|size
index|]
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
name|filteredIndices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|filteredIndices
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
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
name|writeBoolean
argument_list|(
name|filterRoutingTable
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|filterNodes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|filterMetaData
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|filterBlocks
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|filteredIndices
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|filteredIndex
range|:
name|filteredIndices
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|filteredIndex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

