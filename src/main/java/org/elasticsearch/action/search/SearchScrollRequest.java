begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
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
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|Scroll
operator|.
name|readScroll
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SearchScrollRequest
specifier|public
class|class
name|SearchScrollRequest
extends|extends
name|ActionRequest
argument_list|<
name|SearchScrollRequest
argument_list|>
block|{
DECL|field|scrollId
specifier|private
name|String
name|scrollId
decl_stmt|;
DECL|field|scroll
specifier|private
name|Scroll
name|scroll
decl_stmt|;
DECL|field|operationThreading
specifier|private
name|SearchOperationThreading
name|operationThreading
init|=
name|SearchOperationThreading
operator|.
name|THREAD_PER_SHARD
decl_stmt|;
DECL|method|SearchScrollRequest
specifier|public
name|SearchScrollRequest
parameter_list|()
block|{     }
DECL|method|SearchScrollRequest
specifier|public
name|SearchScrollRequest
parameter_list|(
name|String
name|scrollId
parameter_list|)
block|{
name|this
operator|.
name|scrollId
operator|=
name|scrollId
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|scrollId
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"scrollId is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Controls the the search operation threading model.      */
DECL|method|operationThreading
specifier|public
name|SearchOperationThreading
name|operationThreading
parameter_list|()
block|{
return|return
name|this
operator|.
name|operationThreading
return|;
block|}
comment|/**      * Controls the the search operation threading model.      */
DECL|method|operationThreading
specifier|public
name|SearchScrollRequest
name|operationThreading
parameter_list|(
name|SearchOperationThreading
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
comment|/**      * The scroll id used to scroll the search.      */
DECL|method|scrollId
specifier|public
name|String
name|scrollId
parameter_list|()
block|{
return|return
name|scrollId
return|;
block|}
DECL|method|scrollId
specifier|public
name|SearchScrollRequest
name|scrollId
parameter_list|(
name|String
name|scrollId
parameter_list|)
block|{
name|this
operator|.
name|scrollId
operator|=
name|scrollId
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request.      */
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|scroll
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request.      */
DECL|method|scroll
specifier|public
name|SearchScrollRequest
name|scroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|this
operator|.
name|scroll
operator|=
name|scroll
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|scroll
specifier|public
name|SearchScrollRequest
name|scroll
parameter_list|(
name|TimeValue
name|keepAlive
parameter_list|)
block|{
return|return
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|keepAlive
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * If set, will enable scrolling of the search request for the specified timeout.      */
DECL|method|scroll
specifier|public
name|SearchScrollRequest
name|scroll
parameter_list|(
name|String
name|keepAlive
parameter_list|)
block|{
return|return
name|scroll
argument_list|(
operator|new
name|Scroll
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|keepAlive
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
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
name|operationThreading
operator|=
name|SearchOperationThreading
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|scrollId
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|scroll
operator|=
name|readScroll
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|writeString
argument_list|(
name|scrollId
argument_list|)
expr_stmt|;
if|if
condition|(
name|scroll
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
name|scroll
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

