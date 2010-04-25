begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this   * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonToken
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Immutable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
name|util
operator|.
name|json
operator|.
name|JsonBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
operator|.
name|ToJson
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
annotation|@
name|Immutable
DECL|class|IndexMetaData
specifier|public
class|class
name|IndexMetaData
block|{
DECL|field|SETTING_NUMBER_OF_SHARDS
specifier|public
specifier|static
specifier|final
name|String
name|SETTING_NUMBER_OF_SHARDS
init|=
literal|"index.number_of_shards"
decl_stmt|;
DECL|field|SETTING_NUMBER_OF_REPLICAS
specifier|public
specifier|static
specifier|final
name|String
name|SETTING_NUMBER_OF_REPLICAS
init|=
literal|"index.number_of_replicas"
decl_stmt|;
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|aliases
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|aliases
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|mappings
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
decl_stmt|;
DECL|field|totalNumberOfShards
specifier|private
specifier|transient
specifier|final
name|int
name|totalNumberOfShards
decl_stmt|;
DECL|method|IndexMetaData
specifier|private
name|IndexMetaData
parameter_list|(
name|String
name|index
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|settings
operator|.
name|getAsInt
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
operator|-
literal|1
argument_list|)
operator|!=
operator|-
literal|1
argument_list|,
literal|"must specify numberOfShards for index ["
operator|+
name|index
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|settings
operator|.
name|getAsInt
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
operator|-
literal|1
argument_list|)
operator|!=
operator|-
literal|1
argument_list|,
literal|"must specify numberOfReplicas for index ["
operator|+
name|index
operator|+
literal|"]"
argument_list|)
expr_stmt|;
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
name|this
operator|.
name|mappings
operator|=
name|mappings
expr_stmt|;
name|this
operator|.
name|totalNumberOfShards
operator|=
name|numberOfShards
argument_list|()
operator|*
operator|(
name|numberOfReplicas
argument_list|()
operator|+
literal|1
operator|)
expr_stmt|;
name|this
operator|.
name|aliases
operator|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|settings
operator|.
name|getAsArray
argument_list|(
literal|"index.aliases"
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
argument_list|()
return|;
block|}
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|settings
operator|.
name|getAsInt
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|getNumberOfShards
specifier|public
name|int
name|getNumberOfShards
parameter_list|()
block|{
return|return
name|numberOfShards
argument_list|()
return|;
block|}
DECL|method|numberOfReplicas
specifier|public
name|int
name|numberOfReplicas
parameter_list|()
block|{
return|return
name|settings
operator|.
name|getAsInt
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|getNumberOfReplicas
specifier|public
name|int
name|getNumberOfReplicas
parameter_list|()
block|{
return|return
name|numberOfReplicas
argument_list|()
return|;
block|}
DECL|method|totalNumberOfShards
specifier|public
name|int
name|totalNumberOfShards
parameter_list|()
block|{
return|return
name|totalNumberOfShards
return|;
block|}
DECL|method|getTotalNumberOfShards
specifier|public
name|int
name|getTotalNumberOfShards
parameter_list|()
block|{
return|return
name|totalNumberOfShards
argument_list|()
return|;
block|}
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
DECL|method|getSettings
specifier|public
name|Settings
name|getSettings
parameter_list|()
block|{
return|return
name|settings
argument_list|()
return|;
block|}
DECL|method|aliases
specifier|public
name|ImmutableSet
argument_list|<
name|String
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
DECL|method|getAliases
specifier|public
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|getAliases
parameter_list|()
block|{
return|return
name|aliases
argument_list|()
return|;
block|}
DECL|method|mappings
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|()
block|{
return|return
name|mappings
return|;
block|}
DECL|method|getMappings
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getMappings
parameter_list|()
block|{
return|return
name|mappings
argument_list|()
return|;
block|}
DECL|method|mapping
specifier|public
name|String
name|mapping
parameter_list|(
name|String
name|mappingType
parameter_list|)
block|{
return|return
name|mappings
operator|.
name|get
argument_list|(
name|mappingType
argument_list|)
return|;
block|}
DECL|method|newIndexMetaDataBuilder
specifier|public
specifier|static
name|Builder
name|newIndexMetaDataBuilder
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|newIndexMetaDataBuilder
specifier|public
specifier|static
name|Builder
name|newIndexMetaDataBuilder
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|indexMetaData
argument_list|)
return|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
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
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|mappings
specifier|private
name|MapBuilder
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
init|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
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
block|}
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|)
block|{
name|this
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|settings
argument_list|(
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|putAll
argument_list|(
name|indexMetaData
operator|.
name|mappings
argument_list|)
expr_stmt|;
block|}
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
DECL|method|numberOfShards
specifier|public
name|Builder
name|numberOfShards
parameter_list|(
name|int
name|numberOfShards
parameter_list|)
block|{
name|settings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numberOfShards
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|settings
operator|.
name|getAsInt
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|numberOfReplicas
specifier|public
name|Builder
name|numberOfReplicas
parameter_list|(
name|int
name|numberOfReplicas
parameter_list|)
block|{
name|settings
operator|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|numberOfReplicas
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|numberOfReplicas
specifier|public
name|int
name|numberOfReplicas
parameter_list|()
block|{
return|return
name|settings
operator|.
name|getAsInt
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
DECL|method|settings
specifier|public
name|Builder
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
DECL|method|settings
specifier|public
name|Builder
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
DECL|method|removeMapping
specifier|public
name|Builder
name|removeMapping
parameter_list|(
name|String
name|mappingType
parameter_list|)
block|{
name|mappings
operator|.
name|remove
argument_list|(
name|mappingType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|putMapping
specifier|public
name|Builder
name|putMapping
parameter_list|(
name|String
name|mappingType
parameter_list|,
name|String
name|mappingSource
parameter_list|)
block|{
name|mappings
operator|.
name|put
argument_list|(
name|mappingType
argument_list|,
name|mappingSource
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|IndexMetaData
name|build
parameter_list|()
block|{
return|return
operator|new
name|IndexMetaData
argument_list|(
name|index
argument_list|,
name|settings
argument_list|,
name|mappings
operator|.
name|immutableMap
argument_list|()
argument_list|)
return|;
block|}
DECL|method|toJson
specifier|public
specifier|static
name|void
name|toJson
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|JsonBuilder
name|builder
parameter_list|,
name|ToJson
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
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
name|indexMetaData
operator|.
name|settings
argument_list|()
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"mappings"
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
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"source"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|fromJson
specifier|public
specifier|static
name|IndexMetaData
name|fromJson
parameter_list|(
name|JsonParser
name|jp
parameter_list|,
annotation|@
name|Nullable
name|Settings
name|globalSettings
parameter_list|)
throws|throws
name|IOException
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|jp
operator|.
name|getCurrentName
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|JsonToken
name|token
init|=
name|jp
operator|.
name|nextToken
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|jp
operator|.
name|getCurrentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"settings"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|settingsBuilder
init|=
name|settingsBuilder
argument_list|()
operator|.
name|globalSettings
argument_list|(
name|globalSettings
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
name|String
name|key
init|=
name|jp
operator|.
name|getCurrentName
argument_list|()
decl_stmt|;
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|String
name|value
init|=
name|jp
operator|.
name|getText
argument_list|()
decl_stmt|;
name|settingsBuilder
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|settings
argument_list|(
name|settingsBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"mappings"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
name|String
name|mappingType
init|=
name|jp
operator|.
name|getCurrentName
argument_list|()
decl_stmt|;
name|String
name|mappingSource
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|jp
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|JsonToken
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|JsonToken
operator|.
name|FIELD_NAME
condition|)
block|{
if|if
condition|(
literal|"source"
operator|.
name|equals
argument_list|(
name|jp
operator|.
name|getCurrentName
argument_list|()
argument_list|)
condition|)
block|{
name|jp
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|mappingSource
operator|=
name|jp
operator|.
name|getText
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|mappingSource
operator|==
literal|null
condition|)
block|{
comment|// crap, no mapping source, warn?
block|}
else|else
block|{
name|builder
operator|.
name|putMapping
argument_list|(
name|mappingType
argument_list|,
name|mappingSource
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|IndexMetaData
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|Settings
name|globalSettings
parameter_list|)
throws|throws
name|IOException
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|settings
argument_list|(
name|readSettingsFromStream
argument_list|(
name|in
argument_list|,
name|globalSettings
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|mappingsSize
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
name|mappingsSize
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|putMapping
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|IndexMetaData
name|indexMetaData
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|writeSettingsToStream
argument_list|(
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indexMetaData
operator|.
name|mappings
argument_list|()
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
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

