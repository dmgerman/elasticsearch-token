begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this   * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.broadcast
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
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
name|ActionRequest
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
name|ActionRequestValidationException
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
DECL|class|BroadcastOperationRequest
specifier|public
specifier|abstract
class|class
name|BroadcastOperationRequest
implements|implements
name|ActionRequest
block|{
DECL|field|indices
specifier|protected
name|String
index|[]
name|indices
decl_stmt|;
DECL|field|queryHint
annotation|@
name|Nullable
specifier|protected
name|String
name|queryHint
decl_stmt|;
DECL|field|listenerThreaded
specifier|private
name|boolean
name|listenerThreaded
init|=
literal|false
decl_stmt|;
DECL|field|operationThreading
specifier|private
name|BroadcastOperationThreading
name|operationThreading
init|=
name|BroadcastOperationThreading
operator|.
name|SINGLE_THREAD
decl_stmt|;
DECL|method|BroadcastOperationRequest
specifier|protected
name|BroadcastOperationRequest
parameter_list|()
block|{      }
DECL|method|BroadcastOperationRequest
specifier|protected
name|BroadcastOperationRequest
parameter_list|(
name|String
index|[]
name|indices
parameter_list|,
annotation|@
name|Nullable
name|String
name|queryHint
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
name|this
operator|.
name|queryHint
operator|=
name|queryHint
expr_stmt|;
block|}
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
DECL|method|indices
specifier|public
name|BroadcastOperationRequest
name|indices
parameter_list|(
name|String
index|[]
name|indices
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|queryHint
specifier|public
name|String
name|queryHint
parameter_list|()
block|{
return|return
name|queryHint
return|;
block|}
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
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|boolean
name|listenerThreaded
parameter_list|()
block|{
return|return
name|this
operator|.
name|listenerThreaded
return|;
block|}
comment|/**      * Should the listener be called on a separate thread if needed.      */
DECL|method|listenerThreaded
annotation|@
name|Override
specifier|public
name|BroadcastOperationRequest
name|listenerThreaded
parameter_list|(
name|boolean
name|listenerThreaded
parameter_list|)
block|{
name|this
operator|.
name|listenerThreaded
operator|=
name|listenerThreaded
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Controls the operation threading model.      */
DECL|method|operationThreading
specifier|public
name|BroadcastOperationThreading
name|operationThreading
parameter_list|()
block|{
return|return
name|operationThreading
return|;
block|}
comment|/**      * Controls the operation threading model.      */
DECL|method|operationThreading
specifier|public
name|BroadcastOperationRequest
name|operationThreading
parameter_list|(
name|BroadcastOperationThreading
name|operationThreading
parameter_list|)
block|{
name|this
operator|.
name|operationThreading
operator|=
name|operationThreading
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Controls the operation threading model.      */
DECL|method|operationThreading
specifier|public
name|BroadcastOperationRequest
name|operationThreading
parameter_list|(
name|String
name|operationThreading
parameter_list|)
block|{
return|return
name|operationThreading
argument_list|(
name|BroadcastOperationThreading
operator|.
name|fromString
argument_list|(
name|operationThreading
argument_list|,
name|this
operator|.
name|operationThreading
argument_list|)
argument_list|)
return|;
block|}
DECL|method|beforeLocalFork
specifier|protected
name|void
name|beforeLocalFork
parameter_list|()
block|{      }
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
if|if
condition|(
name|indices
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|indices
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|queryHint
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|queryHint
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeByte
argument_list|(
name|operationThreading
operator|.
name|id
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
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
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
operator|==
literal|0
condition|)
block|{
name|indices
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|indices
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
name|indices
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indices
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
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|queryHint
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|operationThreading
operator|=
name|BroadcastOperationThreading
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

