begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.shrink
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
name|shrink
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexRequest
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
name|AcknowledgedRequestBuilder
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_class
DECL|class|ShrinkRequestBuilder
specifier|public
class|class
name|ShrinkRequestBuilder
extends|extends
name|AcknowledgedRequestBuilder
argument_list|<
name|ShrinkRequest
argument_list|,
name|ShrinkResponse
argument_list|,
name|ShrinkRequestBuilder
argument_list|>
block|{
DECL|method|ShrinkRequestBuilder
specifier|public
name|ShrinkRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|ShrinkAction
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
name|ShrinkRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setTargetIndex
specifier|public
name|ShrinkRequestBuilder
name|setTargetIndex
parameter_list|(
name|CreateIndexRequest
name|request
parameter_list|)
block|{
name|this
operator|.
name|request
operator|.
name|setShrinkIndex
argument_list|(
name|request
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSourceIndex
specifier|public
name|ShrinkRequestBuilder
name|setSourceIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|request
operator|.
name|setSourceIndex
argument_list|(
name|index
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setSettings
specifier|public
name|ShrinkRequestBuilder
name|setSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|request
operator|.
name|getShrinkIndexRequest
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

