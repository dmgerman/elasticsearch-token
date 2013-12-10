begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ElasticSearchGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|master
operator|.
name|AcknowledgedRequest
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|collect
operator|.
name|MapBuilder
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
name|settings
operator|.
name|ImmutableSettings
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|XContentType
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
name|support
operator|.
name|XContentMapValues
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|readSettingsFromStream
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|writeSettingsToStream
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|readTimeValue
import|;
end_import

begin_comment
comment|/**  * A request to create an index template.  */
end_comment

begin_class
DECL|class|PutIndexTemplateRequest
specifier|public
class|class
name|PutIndexTemplateRequest
extends|extends
name|MasterNodeOperationRequest
argument_list|<
name|PutIndexTemplateRequest
argument_list|>
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|cause
specifier|private
name|String
name|cause
init|=
literal|""
decl_stmt|;
DECL|field|template
specifier|private
name|String
name|template
decl_stmt|;
DECL|field|order
specifier|private
name|int
name|order
decl_stmt|;
DECL|field|create
specifier|private
name|boolean
name|create
decl_stmt|;
DECL|field|settings
specifier|private
name|Settings
name|settings
init|=
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|mappings
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|field|customs
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|customs
init|=
name|newHashMap
argument_list|()
decl_stmt|;
DECL|method|PutIndexTemplateRequest
name|PutIndexTemplateRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new put index template request with the provided name.      */
DECL|method|PutIndexTemplateRequest
specifier|public
name|PutIndexTemplateRequest
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
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
name|name
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"name is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|template
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"template is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
comment|/**      * Sets the name of the index template.      */
DECL|method|name
specifier|public
name|PutIndexTemplateRequest
name|name
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The name of the index template.      */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|template
specifier|public
name|PutIndexTemplateRequest
name|template
parameter_list|(
name|String
name|template
parameter_list|)
block|{
name|this
operator|.
name|template
operator|=
name|template
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|template
specifier|public
name|String
name|template
parameter_list|()
block|{
return|return
name|this
operator|.
name|template
return|;
block|}
DECL|method|order
specifier|public
name|PutIndexTemplateRequest
name|order
parameter_list|(
name|int
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|order
specifier|public
name|int
name|order
parameter_list|()
block|{
return|return
name|this
operator|.
name|order
return|;
block|}
comment|/**      * Set to<tt>true</tt> to force only creation, not an update of an index template. If it already      * exists, it will fail with an {@link org.elasticsearch.indices.IndexTemplateAlreadyExistsException}.      */
DECL|method|create
specifier|public
name|PutIndexTemplateRequest
name|create
parameter_list|(
name|boolean
name|create
parameter_list|)
block|{
name|this
operator|.
name|create
operator|=
name|create
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|create
specifier|public
name|boolean
name|create
parameter_list|()
block|{
return|return
name|create
return|;
block|}
comment|/**      * The settings to create the index template with.      */
DECL|method|settings
specifier|public
name|PutIndexTemplateRequest
name|settings
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to create the index template with.      */
DECL|method|settings
specifier|public
name|PutIndexTemplateRequest
name|settings
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to create the index template with (either json/yaml/properties format).      */
DECL|method|settings
specifier|public
name|PutIndexTemplateRequest
name|settings
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|loadFromSource
argument_list|(
name|source
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to crete the index template with (either json/yaml/properties format).      */
DECL|method|settings
specifier|public
name|PutIndexTemplateRequest
name|settings
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
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|settings
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
DECL|method|settings
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|this
operator|.
name|settings
return|;
block|}
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|mapping
specifier|public
name|PutIndexTemplateRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|)
block|{
name|mappings
operator|.
name|put
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
comment|/**      * The cause for this index template creation.      */
DECL|method|cause
specifier|public
name|PutIndexTemplateRequest
name|cause
parameter_list|(
name|String
name|cause
parameter_list|)
block|{
name|this
operator|.
name|cause
operator|=
name|cause
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|cause
specifier|public
name|String
name|cause
parameter_list|()
block|{
return|return
name|this
operator|.
name|cause
return|;
block|}
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|mapping
specifier|public
name|PutIndexTemplateRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|XContentBuilder
name|source
parameter_list|)
block|{
try|try
block|{
name|mappings
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|source
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Failed to build json for mapping request"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|mapping
specifier|public
name|PutIndexTemplateRequest
name|mapping
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
comment|// wrap it in a type map if its not
if|if
condition|(
name|source
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|||
operator|!
name|source
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|source
operator|=
name|MapBuilder
operator|.
expr|<
name|String
operator|,
name|Object
operator|>
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|mapping
argument_list|(
name|type
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchGenerationException
argument_list|(
literal|"Failed to generate ["
operator|+
name|source
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|mappings
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|()
block|{
return|return
name|this
operator|.
name|mappings
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|source
specifier|public
name|PutIndexTemplateRequest
name|source
parameter_list|(
name|XContentBuilder
name|templateBuilder
parameter_list|)
block|{
try|try
block|{
return|return
name|source
argument_list|(
name|templateBuilder
operator|.
name|bytes
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Failed to build json for template request"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The template source definition.      */
DECL|method|source
specifier|public
name|PutIndexTemplateRequest
name|source
parameter_list|(
name|Map
name|templateSource
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
name|templateSource
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|source
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|name
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"template"
argument_list|)
condition|)
block|{
name|template
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"order"
argument_list|)
condition|)
block|{
name|order
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|order
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"settings"
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
operator|(
name|entry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Map
operator|)
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Malformed settings section, should include an inner object"
argument_list|)
throw|;
block|}
name|settings
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"mappings"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mappings
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry1
range|:
name|mappings
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|entry1
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Map
operator|)
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Malformed mappings section for type ["
operator|+
name|entry1
operator|.
name|getKey
argument_list|()
operator|+
literal|"], should include an inner object describing the mapping"
argument_list|)
throw|;
block|}
name|mapping
argument_list|(
name|entry1
operator|.
name|getKey
argument_list|()
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entry1
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// maybe custom?
name|IndexMetaData
operator|.
name|Custom
operator|.
name|Factory
name|factory
init|=
name|IndexMetaData
operator|.
name|lookupFactory
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|factory
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|customs
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|factory
operator|.
name|fromMap
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"failed to parse custom metadata for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
return|return
name|this
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|source
specifier|public
name|PutIndexTemplateRequest
name|source
parameter_list|(
name|String
name|templateSource
parameter_list|)
block|{
try|try
block|{
return|return
name|source
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|templateSource
argument_list|)
operator|.
name|createParser
argument_list|(
name|templateSource
argument_list|)
operator|.
name|mapOrderedAndClose
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to parse template source ["
operator|+
name|templateSource
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The template source definition.      */
DECL|method|source
specifier|public
name|PutIndexTemplateRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**      * The template source definition.      */
DECL|method|source
specifier|public
name|PutIndexTemplateRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
try|try
block|{
return|return
name|source
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|.
name|mapOrderedAndClose
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to parse template source"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The template source definition.      */
DECL|method|source
specifier|public
name|PutIndexTemplateRequest
name|source
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
try|try
block|{
return|return
name|source
argument_list|(
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
operator|.
name|mapOrderedAndClose
argument_list|()
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to parse template source"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|custom
specifier|public
name|PutIndexTemplateRequest
name|custom
parameter_list|(
name|IndexMetaData
operator|.
name|Custom
name|custom
parameter_list|)
block|{
name|customs
operator|.
name|put
argument_list|(
name|custom
operator|.
name|type
argument_list|()
argument_list|,
name|custom
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|customs
name|Map
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|customs
parameter_list|()
block|{
return|return
name|this
operator|.
name|customs
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
name|cause
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|template
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|order
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|create
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|settings
operator|=
name|readSettingsFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|//timeout was ignored till 0.90.7, removed afterwards
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_0_90_7
argument_list|)
condition|)
block|{
name|readTimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|mappings
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|int
name|customSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|customSize
condition|;
name|i
operator|++
control|)
block|{
name|String
name|type
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|IndexMetaData
operator|.
name|Custom
name|customIndexMetaData
init|=
name|IndexMetaData
operator|.
name|lookupFactorySafe
argument_list|(
name|type
argument_list|)
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|customs
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|customIndexMetaData
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
name|writeString
argument_list|(
name|cause
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|template
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|order
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|create
argument_list|)
expr_stmt|;
name|writeSettingsToStream
argument_list|(
name|settings
argument_list|,
name|out
argument_list|)
expr_stmt|;
comment|//timeout was ignored till 0.90.7, removed afterwards
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|V_0_90_7
argument_list|)
condition|)
block|{
name|AcknowledgedRequest
operator|.
name|DEFAULT_ACK_TIMEOUT
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|mappings
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|mappings
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|customs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|entry
range|:
name|customs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|IndexMetaData
operator|.
name|lookupFactorySafe
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|writeTo
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

