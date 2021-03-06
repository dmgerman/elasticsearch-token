begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.flush
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
name|flush
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
name|BroadcastOperationRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|ElasticsearchClient
import|;
end_import

begin_class
DECL|class|FlushRequestBuilder
specifier|public
class|class
name|FlushRequestBuilder
extends|extends
name|BroadcastOperationRequestBuilder
argument_list|<
name|FlushRequest
argument_list|,
name|FlushResponse
argument_list|,
name|FlushRequestBuilder
argument_list|>
block|{
DECL|method|FlushRequestBuilder
specifier|public
name|FlushRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|FlushAction
name|action
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
operator|new
name|FlushRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setForce
specifier|public
name|FlushRequestBuilder
name|setForce
parameter_list|(
name|boolean
name|force
parameter_list|)
block|{
name|request
operator|.
name|force
argument_list|(
name|force
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setWaitIfOngoing
specifier|public
name|FlushRequestBuilder
name|setWaitIfOngoing
parameter_list|(
name|boolean
name|waitIfOngoing
parameter_list|)
block|{
name|request
operator|.
name|waitIfOngoing
argument_list|(
name|waitIfOngoing
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

