begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.fieldstats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|fieldstats
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
DECL|class|FieldStatsRequestBuilder
specifier|public
class|class
name|FieldStatsRequestBuilder
extends|extends
name|BroadcastOperationRequestBuilder
argument_list|<
name|FieldStatsRequest
argument_list|,
name|FieldStatsResponse
argument_list|,
name|FieldStatsRequestBuilder
argument_list|>
block|{
DECL|method|FieldStatsRequestBuilder
specifier|public
name|FieldStatsRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|FieldStatsAction
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
name|FieldStatsRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setFields
specifier|public
name|FieldStatsRequestBuilder
name|setFields
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|setFields
argument_list|(
name|fields
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setIndexContraints
specifier|public
name|FieldStatsRequestBuilder
name|setIndexContraints
parameter_list|(
name|IndexConstraint
modifier|...
name|fields
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|setIndexConstraints
argument_list|(
name|fields
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setLevel
specifier|public
name|FieldStatsRequestBuilder
name|setLevel
parameter_list|(
name|String
name|level
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|level
argument_list|(
name|level
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setUseCache
specifier|public
name|FieldStatsRequestBuilder
name|setUseCache
parameter_list|(
name|boolean
name|useCache
parameter_list|)
block|{
name|request
argument_list|()
operator|.
name|setUseCache
argument_list|(
name|useCache
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

