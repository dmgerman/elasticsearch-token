begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.single.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|single
operator|.
name|shard
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
name|Action
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
name|ActionRequestBuilder
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
name|ActionResponse
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
DECL|class|SingleShardOperationRequestBuilder
specifier|public
specifier|abstract
class|class
name|SingleShardOperationRequestBuilder
parameter_list|<
name|Request
extends|extends
name|SingleShardRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|SingleShardOperationRequestBuilder
parameter_list|<
name|Request
parameter_list|,
name|Response
parameter_list|,
name|RequestBuilder
parameter_list|>
parameter_list|>
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
block|{
DECL|method|SingleShardOperationRequestBuilder
specifier|protected
name|SingleShardOperationRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the index.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setIndex
specifier|public
specifier|final
name|RequestBuilder
name|setIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|request
operator|.
name|index
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
operator|(
name|RequestBuilder
operator|)
name|this
return|;
block|}
comment|/**      * Controls if the operation will be executed on a separate thread when executed locally.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setOperationThreaded
specifier|public
specifier|final
name|RequestBuilder
name|setOperationThreaded
parameter_list|(
name|boolean
name|threadedOperation
parameter_list|)
block|{
name|request
operator|.
name|operationThreaded
argument_list|(
name|threadedOperation
argument_list|)
expr_stmt|;
return|return
operator|(
name|RequestBuilder
operator|)
name|this
return|;
block|}
block|}
end_class

end_unit

