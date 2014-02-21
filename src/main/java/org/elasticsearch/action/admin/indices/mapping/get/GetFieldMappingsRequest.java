begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.mapping.get
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
name|mapping
operator|.
name|get
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
name|IndicesOptions
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/** Request the mappings of specific fields */
end_comment

begin_class
DECL|class|GetFieldMappingsRequest
specifier|public
class|class
name|GetFieldMappingsRequest
extends|extends
name|ActionRequest
argument_list|<
name|GetFieldMappingsRequest
argument_list|>
block|{
DECL|field|local
specifier|protected
name|boolean
name|local
init|=
literal|false
decl_stmt|;
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|includeDefaults
specifier|private
name|boolean
name|includeDefaults
init|=
literal|false
decl_stmt|;
DECL|field|indices
specifier|private
name|String
index|[]
name|indices
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|indicesOptions
specifier|private
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|strict
argument_list|()
decl_stmt|;
DECL|method|GetFieldMappingsRequest
specifier|public
name|GetFieldMappingsRequest
parameter_list|()
block|{      }
DECL|method|GetFieldMappingsRequest
specifier|public
name|GetFieldMappingsRequest
parameter_list|(
name|GetFieldMappingsRequest
name|other
parameter_list|)
block|{
name|this
operator|.
name|local
operator|=
name|other
operator|.
name|local
expr_stmt|;
name|this
operator|.
name|includeDefaults
operator|=
name|other
operator|.
name|includeDefaults
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|other
operator|.
name|indices
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|other
operator|.
name|types
expr_stmt|;
name|this
operator|.
name|indicesOptions
operator|=
name|other
operator|.
name|indicesOptions
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|other
operator|.
name|fields
expr_stmt|;
block|}
comment|/**      * Indicate whether the receiving node should operate based on local index information or forward requests,      * where needed, to other nodes. If running locally, request will not raise errors if running locally& missing indices.      */
DECL|method|local
specifier|public
name|GetFieldMappingsRequest
name|local
parameter_list|(
name|boolean
name|local
parameter_list|)
block|{
name|this
operator|.
name|local
operator|=
name|local
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|local
specifier|public
name|boolean
name|local
parameter_list|()
block|{
return|return
name|local
return|;
block|}
DECL|method|indices
specifier|public
name|GetFieldMappingsRequest
name|indices
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
name|this
return|;
block|}
DECL|method|types
specifier|public
name|GetFieldMappingsRequest
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|indicesOptions
specifier|public
name|GetFieldMappingsRequest
name|indicesOptions
parameter_list|(
name|IndicesOptions
name|indicesOptions
parameter_list|)
block|{
name|this
operator|.
name|indicesOptions
operator|=
name|indicesOptions
expr_stmt|;
return|return
name|this
return|;
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
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|types
return|;
block|}
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|indicesOptions
return|;
block|}
comment|/** @param fields a list of fields to retrieve the mapping for */
DECL|method|fields
specifier|public
name|GetFieldMappingsRequest
name|fields
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fields
specifier|public
name|String
index|[]
name|fields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
DECL|method|includeDefaults
specifier|public
name|boolean
name|includeDefaults
parameter_list|()
block|{
return|return
name|includeDefaults
return|;
block|}
comment|/** Indicates whether default mapping settings should be returned */
DECL|method|includeDefaults
specifier|public
name|GetFieldMappingsRequest
name|includeDefaults
parameter_list|(
name|boolean
name|includeDefaults
parameter_list|)
block|{
name|this
operator|.
name|includeDefaults
operator|=
name|includeDefaults
expr_stmt|;
return|return
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
comment|// This request used to inherit from MasterNodeOperationRequest, so for bwc we need to keep serializing it.
name|MasterNodeOperationRequest
operator|.
name|DEFAULT_MASTER_NODE_TIMEOUT
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|indicesOptions
operator|.
name|writeIndicesOptions
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|local
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|includeDefaults
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
comment|// This request used to inherit from MasterNodeOperationRequest, so for bwc we need to keep serializing it.
name|TimeValue
operator|.
name|readTimeValue
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
name|types
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|indicesOptions
operator|=
name|IndicesOptions
operator|.
name|readIndicesOptions
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|local
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|fields
operator|=
name|in
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
name|includeDefaults
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

