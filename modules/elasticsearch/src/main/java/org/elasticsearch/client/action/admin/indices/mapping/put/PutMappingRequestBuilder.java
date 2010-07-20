begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.action.admin.indices.mapping.put
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
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
name|ActionListener
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingRequest
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingResponse
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
name|IndicesAdminClient
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
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|support
operator|.
name|BaseIndicesRequestBuilder
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
name|common
operator|.
name|xcontent
operator|.
name|builder
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|PutMappingRequestBuilder
specifier|public
class|class
name|PutMappingRequestBuilder
extends|extends
name|BaseIndicesRequestBuilder
argument_list|<
name|PutMappingRequest
argument_list|,
name|PutMappingResponse
argument_list|>
block|{
DECL|method|PutMappingRequestBuilder
specifier|public
name|PutMappingRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
name|indicesClient
argument_list|,
operator|new
name|PutMappingRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|setIndices
specifier|public
name|PutMappingRequestBuilder
name|setIndices
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|request
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The type of the mappings. Not required since it can be defined explicitly within the mapping source.      * If it is not defined within the mapping source, then it is required.      */
DECL|method|setType
specifier|public
name|PutMappingRequestBuilder
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|request
operator|.
name|type
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The mapping source definition.      */
DECL|method|setSource
specifier|public
name|PutMappingRequestBuilder
name|setSource
parameter_list|(
name|XContentBuilder
name|mappingBuilder
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|mappingBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The mapping source definition.      */
DECL|method|setSource
specifier|public
name|PutMappingRequestBuilder
name|setSource
parameter_list|(
name|Map
name|mappingSource
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|mappingSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The mapping source definition.      */
DECL|method|setSource
specifier|public
name|PutMappingRequestBuilder
name|setSource
parameter_list|(
name|String
name|mappingSource
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|mappingSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Timeout to wait till the put mapping gets acknowledged of all current cluster nodes. Defaults to      *<tt>10s</tt>.      */
DECL|method|setTimeout
specifier|public
name|PutMappingRequestBuilder
name|setTimeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Timeout to wait till the put mapping gets acknowledged of all current cluster nodes. Defaults to      *<tt>10s</tt>.      */
DECL|method|setTimeout
specifier|public
name|PutMappingRequestBuilder
name|setTimeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|timeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * If there is already a mapping definition registered against the type, then it will be merged. If there are      * elements that can't be merged are detected, the request will be rejected unless the      * {@link #setIgnoreConflicts(boolean)} is set. In such a case, the duplicate mappings will be rejected.      */
DECL|method|setIgnoreConflicts
specifier|public
name|PutMappingRequestBuilder
name|setIgnoreConflicts
parameter_list|(
name|boolean
name|ignoreConflicts
parameter_list|)
block|{
name|request
operator|.
name|ignoreConflicts
argument_list|(
name|ignoreConflicts
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|doExecute
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|PutMappingResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|client
operator|.
name|putMapping
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

