begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.template.put
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
name|template
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
name|alias
operator|.
name|Alias
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
name|MasterNodeOperationRequestBuilder
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
name|internal
operator|.
name|InternalIndicesAdminClient
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
name|bytes
operator|.
name|BytesReference
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
comment|/**  *  */
end_comment

begin_class
DECL|class|PutIndexTemplateRequestBuilder
specifier|public
class|class
name|PutIndexTemplateRequestBuilder
extends|extends
name|MasterNodeOperationRequestBuilder
argument_list|<
name|PutIndexTemplateRequest
argument_list|,
name|PutIndexTemplateResponse
argument_list|,
name|PutIndexTemplateRequestBuilder
argument_list|>
block|{
DECL|method|PutIndexTemplateRequestBuilder
specifier|public
name|PutIndexTemplateRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|PutIndexTemplateRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|PutIndexTemplateRequestBuilder
specifier|public
name|PutIndexTemplateRequestBuilder
parameter_list|(
name|IndicesAdminClient
name|indicesClient
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalIndicesAdminClient
operator|)
name|indicesClient
argument_list|,
operator|new
name|PutIndexTemplateRequest
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the template match expression that will be used to match on indices created.      */
DECL|method|setTemplate
specifier|public
name|PutIndexTemplateRequestBuilder
name|setTemplate
parameter_list|(
name|String
name|template
parameter_list|)
block|{
name|request
operator|.
name|template
argument_list|(
name|template
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the order of this template if more than one template matches.      */
DECL|method|setOrder
specifier|public
name|PutIndexTemplateRequestBuilder
name|setOrder
parameter_list|(
name|int
name|order
parameter_list|)
block|{
name|request
operator|.
name|order
argument_list|(
name|order
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set to<tt>true</tt> to force only creation, not an update of an index template. If it already      * exists, it will fail with an {@link org.elasticsearch.indices.IndexTemplateAlreadyExistsException}.      */
DECL|method|setCreate
specifier|public
name|PutIndexTemplateRequestBuilder
name|setCreate
parameter_list|(
name|boolean
name|create
parameter_list|)
block|{
name|request
operator|.
name|create
argument_list|(
name|create
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to created the index template with.      */
DECL|method|setSettings
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSettings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|request
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
comment|/**      * The settings to created the index template with.      */
DECL|method|setSettings
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSettings
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
name|request
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
comment|/**      * The settings to crete the index template with (either json/yaml/properties format)      */
DECL|method|setSettings
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSettings
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to crete the index template with (either json/yaml/properties format)      */
DECL|method|setSettings
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSettings
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|request
operator|.
name|settings
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds mapping that will be added when the index template gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|addMapping
specifier|public
name|PutIndexTemplateRequestBuilder
name|addMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|)
block|{
name|request
operator|.
name|mapping
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|setAliases
specifier|public
name|PutIndexTemplateRequestBuilder
name|setAliases
parameter_list|(
name|Map
name|source
parameter_list|)
block|{
name|request
operator|.
name|aliases
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|setAliases
specifier|public
name|PutIndexTemplateRequestBuilder
name|setAliases
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|request
operator|.
name|aliases
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|setAliases
specifier|public
name|PutIndexTemplateRequestBuilder
name|setAliases
parameter_list|(
name|XContentBuilder
name|source
parameter_list|)
block|{
name|request
operator|.
name|aliases
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|setAliases
specifier|public
name|PutIndexTemplateRequestBuilder
name|setAliases
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|request
operator|.
name|aliases
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds an alias that will be added when the index template gets created.      *      * @param alias  The alias      * @return the request builder      */
DECL|method|addAlias
specifier|public
name|PutIndexTemplateRequestBuilder
name|addAlias
parameter_list|(
name|Alias
name|alias
parameter_list|)
block|{
name|request
operator|.
name|alias
argument_list|(
name|alias
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The cause for this index template creation.      */
DECL|method|cause
specifier|public
name|PutIndexTemplateRequestBuilder
name|cause
parameter_list|(
name|String
name|cause
parameter_list|)
block|{
name|request
operator|.
name|cause
argument_list|(
name|cause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds mapping that will be added when the index template gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|addMapping
specifier|public
name|PutIndexTemplateRequestBuilder
name|addMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|XContentBuilder
name|source
parameter_list|)
block|{
name|request
operator|.
name|mapping
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|addMapping
specifier|public
name|PutIndexTemplateRequestBuilder
name|addMapping
parameter_list|(
name|String
name|type
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|request
operator|.
name|mapping
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|setSource
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSource
parameter_list|(
name|XContentBuilder
name|templateBuilder
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|templateBuilder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|setSource
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSource
parameter_list|(
name|Map
name|templateSource
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|templateSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|setSource
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSource
parameter_list|(
name|String
name|templateSource
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|templateSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|setSource
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSource
parameter_list|(
name|BytesReference
name|templateSource
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|templateSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|setSource
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSource
parameter_list|(
name|byte
index|[]
name|templateSource
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|templateSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|setSource
specifier|public
name|PutIndexTemplateRequestBuilder
name|setSource
parameter_list|(
name|byte
index|[]
name|templateSource
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|request
operator|.
name|source
argument_list|(
name|templateSource
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|PutIndexTemplateResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|IndicesAdminClient
operator|)
name|client
operator|)
operator|.
name|putTemplate
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

