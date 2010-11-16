begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.delete.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|delete
operator|.
name|index
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
name|delete
operator|.
name|DeleteRequest
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
name|replication
operator|.
name|IndexReplicationOperationRequest
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
DECL|class|IndexDeleteRequest
specifier|public
class|class
name|IndexDeleteRequest
extends|extends
name|IndexReplicationOperationRequest
block|{
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|refresh
specifier|private
name|boolean
name|refresh
init|=
literal|false
decl_stmt|;
DECL|method|IndexDeleteRequest
name|IndexDeleteRequest
parameter_list|()
block|{     }
DECL|method|IndexDeleteRequest
specifier|public
name|IndexDeleteRequest
parameter_list|(
name|DeleteRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|request
operator|.
name|timeout
argument_list|()
expr_stmt|;
name|this
operator|.
name|consistencyLevel
operator|=
name|request
operator|.
name|consistencyLevel
argument_list|()
expr_stmt|;
name|this
operator|.
name|replicationType
operator|=
name|request
operator|.
name|replicationType
argument_list|()
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|request
operator|.
name|index
argument_list|()
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|request
operator|.
name|type
argument_list|()
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|request
operator|.
name|id
argument_list|()
expr_stmt|;
name|this
operator|.
name|refresh
operator|=
name|request
operator|.
name|refresh
argument_list|()
expr_stmt|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|refresh
specifier|public
name|boolean
name|refresh
parameter_list|()
block|{
return|return
name|this
operator|.
name|refresh
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|refresh
operator|=
name|in
operator|.
name|readBoolean
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|refresh
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

