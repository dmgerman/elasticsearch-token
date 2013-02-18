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
name|action
operator|.
name|support
operator|.
name|IgnoreIndices
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
DECL|class|BroadcastOperationRequest
specifier|public
specifier|abstract
class|class
name|BroadcastOperationRequest
parameter_list|<
name|T
extends|extends
name|BroadcastOperationRequest
parameter_list|>
extends|extends
name|ActionRequest
argument_list|<
name|T
argument_list|>
block|{
DECL|field|indices
specifier|protected
name|String
index|[]
name|indices
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
DECL|field|ignoreIndices
specifier|private
name|IgnoreIndices
name|ignoreIndices
init|=
name|IgnoreIndices
operator|.
name|NONE
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
parameter_list|)
block|{
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
block|}
DECL|method|getIndices
specifier|public
name|String
index|[]
name|getIndices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setIndices
specifier|public
specifier|final
name|T
name|setIndices
parameter_list|(
name|String
modifier|...
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
operator|(
name|T
operator|)
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**      * Controls the operation threading model.      */
DECL|method|getOperationThreading
specifier|public
name|BroadcastOperationThreading
name|getOperationThreading
parameter_list|()
block|{
return|return
name|operationThreading
return|;
block|}
comment|/**      * Controls the operation threading model.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setOperationThreading
specifier|public
specifier|final
name|T
name|setOperationThreading
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
operator|(
name|T
operator|)
name|this
return|;
block|}
comment|/**      * Controls the operation threading model.      */
DECL|method|setOperationThreading
specifier|public
name|T
name|setOperationThreading
parameter_list|(
name|String
name|operationThreading
parameter_list|)
block|{
return|return
name|setOperationThreading
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
DECL|method|getIgnoreIndices
specifier|public
name|IgnoreIndices
name|getIgnoreIndices
parameter_list|()
block|{
return|return
name|ignoreIndices
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setIgnoreIndices
specifier|public
specifier|final
name|T
name|setIgnoreIndices
parameter_list|(
name|IgnoreIndices
name|ignoreIndices
parameter_list|)
block|{
name|this
operator|.
name|ignoreIndices
operator|=
name|ignoreIndices
expr_stmt|;
return|return
operator|(
name|T
operator|)
name|this
return|;
block|}
DECL|method|beforeStart
specifier|protected
name|void
name|beforeStart
parameter_list|()
block|{      }
DECL|method|beforeLocalFork
specifier|protected
name|void
name|beforeLocalFork
parameter_list|()
block|{      }
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
name|writeStringArrayNullable
argument_list|(
name|indices
argument_list|)
expr_stmt|;
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
name|out
operator|.
name|writeByte
argument_list|(
name|ignoreIndices
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
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
name|indices
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
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
name|ignoreIndices
operator|=
name|IgnoreIndices
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

