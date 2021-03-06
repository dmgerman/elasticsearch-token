begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.create
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
name|create
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchGenerationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|IndicesRequest
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
name|support
operator|.
name|ActiveShardCount
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
name|AcknowledgedRequest
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
name|BytesArray
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
name|NamedXContentRegistry
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
name|XContentHelper
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
name|XContentParser
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
name|io
operator|.
name|UncheckedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|Settings
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
name|Settings
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
name|settings
operator|.
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_comment
comment|/**  * A request to create an index. Best created with {@link org.elasticsearch.client.Requests#createIndexRequest(String)}.  *<p>  * The index created can optionally be created with {@link #settings(org.elasticsearch.common.settings.Settings)}.  *  * @see org.elasticsearch.client.IndicesAdminClient#create(CreateIndexRequest)  * @see org.elasticsearch.client.Requests#createIndexRequest(String)  * @see CreateIndexResponse  */
end_comment

begin_class
DECL|class|CreateIndexRequest
specifier|public
class|class
name|CreateIndexRequest
extends|extends
name|AcknowledgedRequest
argument_list|<
name|CreateIndexRequest
argument_list|>
implements|implements
name|IndicesRequest
block|{
DECL|field|cause
specifier|private
name|String
name|cause
init|=
literal|""
decl_stmt|;
DECL|field|index
specifier|private
name|String
name|index
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
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|aliases
specifier|private
specifier|final
name|Set
argument_list|<
name|Alias
argument_list|>
name|aliases
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|customs
specifier|private
specifier|final
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
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|updateAllTypes
specifier|private
name|boolean
name|updateAllTypes
init|=
literal|false
decl_stmt|;
DECL|field|waitForActiveShards
specifier|private
name|ActiveShardCount
name|waitForActiveShards
init|=
name|ActiveShardCount
operator|.
name|DEFAULT
decl_stmt|;
DECL|method|CreateIndexRequest
specifier|public
name|CreateIndexRequest
parameter_list|()
block|{     }
comment|/**      * Constructs a new request to create an index with the specified name.      */
DECL|method|CreateIndexRequest
specifier|public
name|CreateIndexRequest
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
argument_list|(
name|index
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs a new request to create an index with the specified name and settings.      */
DECL|method|CreateIndexRequest
specifier|public
name|CreateIndexRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|settings
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
name|index
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"index is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|index
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|IndicesOptions
operator|.
name|strictSingleIndexNoExpandForbidClosed
argument_list|()
return|;
block|}
comment|/**      * The index name to create.      */
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|index
specifier|public
name|CreateIndexRequest
name|index
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to create the index with.      */
DECL|method|settings
specifier|public
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|settings
return|;
block|}
comment|/**      * The cause for this index creation.      */
DECL|method|cause
specifier|public
name|String
name|cause
parameter_list|()
block|{
return|return
name|cause
return|;
block|}
comment|/**      * A simplified version of settings that takes key value pairs settings.      */
DECL|method|settings
specifier|public
name|CreateIndexRequest
name|settings
parameter_list|(
name|Object
modifier|...
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The settings to create the index with.      */
DECL|method|settings
specifier|public
name|CreateIndexRequest
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
comment|/**      * The settings to create the index with.      */
DECL|method|settings
specifier|public
name|CreateIndexRequest
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
comment|/**      * The settings to create the index with (either json or yaml format)      */
DECL|method|settings
specifier|public
name|CreateIndexRequest
name|settings
parameter_list|(
name|String
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|loadFromSource
argument_list|(
name|source
argument_list|,
name|xContentType
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Allows to set the settings using a json builder.      */
DECL|method|settings
specifier|public
name|CreateIndexRequest
name|settings
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|)
block|{
try|try
block|{
name|settings
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|,
name|builder
operator|.
name|contentType
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
name|ElasticsearchGenerationException
argument_list|(
literal|"Failed to generate json settings from builder"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|this
return|;
block|}
comment|/**      * The settings to create the index with (either json/yaml/properties format)      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|settings
specifier|public
name|CreateIndexRequest
name|settings
parameter_list|(
name|Map
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
argument_list|,
name|XContentType
operator|.
name|JSON
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
name|ElasticsearchGenerationException
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
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      * @param xContentType The content type of the source      */
DECL|method|mapping
specifier|public
name|CreateIndexRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
return|return
name|mapping
argument_list|(
name|type
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
argument_list|,
name|xContentType
argument_list|)
return|;
block|}
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      * @param xContentType the content type of the mapping source      */
DECL|method|mapping
specifier|private
name|CreateIndexRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|BytesReference
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
if|if
condition|(
name|mappings
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"mappings for type \""
operator|+
name|type
operator|+
literal|"\" were already defined"
argument_list|)
throw|;
block|}
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|xContentType
argument_list|)
expr_stmt|;
try|try
block|{
name|mappings
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
name|source
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|xContentType
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
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
name|UncheckedIOException
argument_list|(
literal|"failed to convert to json"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * The cause for this index creation.      */
DECL|method|cause
specifier|public
name|CreateIndexRequest
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
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
DECL|method|mapping
specifier|public
name|CreateIndexRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|XContentBuilder
name|source
parameter_list|)
block|{
return|return
name|mapping
argument_list|(
name|type
argument_list|,
name|source
operator|.
name|bytes
argument_list|()
argument_list|,
name|source
operator|.
name|contentType
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Adds mapping that will be added when the index gets created.      *      * @param type   The mapping type      * @param source The mapping source      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|mapping
specifier|public
name|CreateIndexRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|Map
name|source
parameter_list|)
block|{
if|if
condition|(
name|mappings
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"mappings for type \""
operator|+
name|type
operator|+
literal|"\" were already defined"
argument_list|)
throw|;
block|}
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
name|ElasticsearchGenerationException
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
comment|/**      * A specialized simplified mapping source method, takes the form of simple properties definition:      * ("field1", "type=string,store=true").      */
DECL|method|mapping
specifier|public
name|CreateIndexRequest
name|mapping
parameter_list|(
name|String
name|type
parameter_list|,
name|Object
modifier|...
name|source
parameter_list|)
block|{
name|mapping
argument_list|(
name|type
argument_list|,
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
name|type
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|aliases
specifier|public
name|CreateIndexRequest
name|aliases
parameter_list|(
name|Map
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
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|source
argument_list|)
expr_stmt|;
return|return
name|aliases
argument_list|(
name|builder
operator|.
name|bytes
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
name|ElasticsearchGenerationException
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
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|aliases
specifier|public
name|CreateIndexRequest
name|aliases
parameter_list|(
name|XContentBuilder
name|source
parameter_list|)
block|{
return|return
name|aliases
argument_list|(
name|source
operator|.
name|bytes
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|aliases
specifier|public
name|CreateIndexRequest
name|aliases
parameter_list|(
name|String
name|source
parameter_list|)
block|{
return|return
name|aliases
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Sets the aliases that will be associated with the index when it gets created      */
DECL|method|aliases
specifier|public
name|CreateIndexRequest
name|aliases
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
comment|// EMPTY is safe here because we never call namedObject
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|NamedXContentRegistry
operator|.
name|EMPTY
argument_list|,
name|source
argument_list|)
init|)
block|{
comment|//move to the first alias
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|alias
argument_list|(
name|Alias
operator|.
name|fromXContent
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|this
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
name|ElasticsearchParseException
argument_list|(
literal|"Failed to parse aliases"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
comment|/**      * Adds an alias that will be associated with the index when it gets created      */
DECL|method|alias
specifier|public
name|CreateIndexRequest
name|alias
parameter_list|(
name|Alias
name|alias
parameter_list|)
block|{
name|this
operator|.
name|aliases
operator|.
name|add
argument_list|(
name|alias
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the settings and mappings as a single source.      */
DECL|method|source
specifier|public
name|CreateIndexRequest
name|source
parameter_list|(
name|String
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
return|return
name|source
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
argument_list|,
name|xContentType
argument_list|)
return|;
block|}
comment|/**      * Sets the settings and mappings as a single source.      */
DECL|method|source
specifier|public
name|CreateIndexRequest
name|source
parameter_list|(
name|XContentBuilder
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
operator|.
name|bytes
argument_list|()
argument_list|,
name|source
operator|.
name|contentType
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Sets the settings and mappings as a single source.      */
DECL|method|source
specifier|public
name|CreateIndexRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|XContentType
name|xContentType
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
argument_list|,
name|xContentType
argument_list|)
return|;
block|}
comment|/**      * Sets the settings and mappings as a single source.      */
DECL|method|source
specifier|public
name|CreateIndexRequest
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
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
return|return
name|source
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|,
name|xContentType
argument_list|)
return|;
block|}
comment|/**      * Sets the settings and mappings as a single source.      */
DECL|method|source
specifier|public
name|CreateIndexRequest
name|source
parameter_list|(
name|BytesReference
name|source
parameter_list|,
name|XContentType
name|xContentType
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|xContentType
argument_list|)
expr_stmt|;
name|source
argument_list|(
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|source
argument_list|,
literal|false
argument_list|,
name|xContentType
argument_list|)
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the settings and mappings as a single source.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|source
specifier|public
name|CreateIndexRequest
name|source
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|source
parameter_list|)
block|{
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|?
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
literal|"settings"
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
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
name|found
operator|=
literal|true
expr_stmt|;
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
elseif|else
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
literal|"aliases"
argument_list|)
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
name|aliases
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
else|else
block|{
comment|// maybe custom?
name|IndexMetaData
operator|.
name|Custom
name|proto
init|=
name|IndexMetaData
operator|.
name|lookupPrototype
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|proto
operator|!=
literal|null
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
try|try
block|{
name|customs
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|proto
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
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse custom metadata for [{}]"
argument_list|,
name|name
argument_list|)
throw|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
comment|// the top level are settings, use them
name|settings
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|mappings
specifier|public
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
DECL|method|aliases
specifier|public
name|Set
argument_list|<
name|Alias
argument_list|>
name|aliases
parameter_list|()
block|{
return|return
name|this
operator|.
name|aliases
return|;
block|}
comment|/**      * Adds custom metadata to the index to be created.      */
DECL|method|custom
specifier|public
name|CreateIndexRequest
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
specifier|public
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
comment|/** True if all fields that span multiple types should be updated, false otherwise */
DECL|method|updateAllTypes
specifier|public
name|boolean
name|updateAllTypes
parameter_list|()
block|{
return|return
name|updateAllTypes
return|;
block|}
comment|/** See {@link #updateAllTypes()} */
DECL|method|updateAllTypes
specifier|public
name|CreateIndexRequest
name|updateAllTypes
parameter_list|(
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|this
operator|.
name|updateAllTypes
operator|=
name|updateAllTypes
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|waitForActiveShards
specifier|public
name|ActiveShardCount
name|waitForActiveShards
parameter_list|()
block|{
return|return
name|waitForActiveShards
return|;
block|}
comment|/**      * Sets the number of shard copies that should be active for index creation to return.      * Defaults to {@link ActiveShardCount#DEFAULT}, which will wait for one shard copy      * (the primary) to become active. Set this value to {@link ActiveShardCount#ALL} to      * wait for all shards (primary and all replicas) to be active before returning.      * Otherwise, use {@link ActiveShardCount#from(int)} to set this value to any      * non-negative integer, up to the number of copies per shard (number of replicas + 1),      * to wait for the desired amount of shard copies to become active before returning.      * Index creation will only wait up until the timeout value for the number of shard copies      * to be active before returning.  Check {@link CreateIndexResponse#isShardsAcked()} to      * determine if the requisite shard copies were all started before returning or timing out.      *      * @param waitForActiveShards number of active shard copies to wait on      */
DECL|method|waitForActiveShards
specifier|public
name|CreateIndexRequest
name|waitForActiveShards
parameter_list|(
name|ActiveShardCount
name|waitForActiveShards
parameter_list|)
block|{
name|this
operator|.
name|waitForActiveShards
operator|=
name|waitForActiveShards
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A shortcut for {@link #waitForActiveShards(ActiveShardCount)} where the numerical      * shard count is passed in, instead of having to first call {@link ActiveShardCount#from(int)}      * to get the ActiveShardCount.      */
DECL|method|waitForActiveShards
specifier|public
name|CreateIndexRequest
name|waitForActiveShards
parameter_list|(
specifier|final
name|int
name|waitForActiveShards
parameter_list|)
block|{
return|return
name|waitForActiveShards
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|waitForActiveShards
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
name|cause
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|settings
operator|=
name|readSettingsFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|readTimeout
argument_list|(
name|in
argument_list|)
expr_stmt|;
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
specifier|final
name|String
name|type
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|String
name|source
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_6_0_0_alpha1
argument_list|)
condition|)
block|{
comment|// TODO change to 5.3.0 after backport
comment|// we do not know the content type that comes from earlier versions so we autodetect and convert
name|source
operator|=
name|XContentHelper
operator|.
name|convertToJson
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|source
argument_list|)
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|mappings
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|source
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
name|lookupPrototypeSafe
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
name|int
name|aliasesSize
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
name|aliasesSize
condition|;
name|i
operator|++
control|)
block|{
name|aliases
operator|.
name|add
argument_list|(
name|Alias
operator|.
name|read
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|updateAllTypes
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|waitForActiveShards
operator|=
name|ActiveShardCount
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
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
name|index
argument_list|)
expr_stmt|;
name|writeSettingsToStream
argument_list|(
name|settings
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|writeTimeout
argument_list|(
name|out
argument_list|)
expr_stmt|;
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
name|entry
operator|.
name|getValue
argument_list|()
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
name|aliases
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Alias
name|alias
range|:
name|aliases
control|)
block|{
name|alias
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|updateAllTypes
argument_list|)
expr_stmt|;
name|waitForActiveShards
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

