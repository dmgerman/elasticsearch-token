begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
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
name|MasterNodeRequest
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
name|ClusterState
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
name|ClusterStateUpdateTask
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
name|service
operator|.
name|ClusterService
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
name|Priority
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
name|UUIDs
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
name|ValidationException
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|regex
operator|.
name|Regex
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
name|IndexScopedSettings
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
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperParsingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexTemplateAlreadyExistsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexTemplateMissingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|InvalidIndexTemplateException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Service responsible for submitting index templates updates  */
end_comment

begin_class
DECL|class|MetaDataIndexTemplateService
specifier|public
class|class
name|MetaDataIndexTemplateService
extends|extends
name|AbstractComponent
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|aliasValidator
specifier|private
specifier|final
name|AliasValidator
name|aliasValidator
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|metaDataCreateIndexService
specifier|private
specifier|final
name|MetaDataCreateIndexService
name|metaDataCreateIndexService
decl_stmt|;
DECL|field|indexScopedSettings
specifier|private
specifier|final
name|IndexScopedSettings
name|indexScopedSettings
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataIndexTemplateService
specifier|public
name|MetaDataIndexTemplateService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|MetaDataCreateIndexService
name|metaDataCreateIndexService
parameter_list|,
name|AliasValidator
name|aliasValidator
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|IndexScopedSettings
name|indexScopedSettings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|aliasValidator
operator|=
name|aliasValidator
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|metaDataCreateIndexService
operator|=
name|metaDataCreateIndexService
expr_stmt|;
name|this
operator|.
name|indexScopedSettings
operator|=
name|indexScopedSettings
expr_stmt|;
block|}
DECL|method|removeTemplates
specifier|public
name|void
name|removeTemplates
parameter_list|(
specifier|final
name|RemoveRequest
name|request
parameter_list|,
specifier|final
name|RemoveListener
name|listener
parameter_list|)
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"remove-index-template ["
operator|+
name|request
operator|.
name|name
operator|+
literal|"]"
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|request
operator|.
name|masterTimeout
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|templateNames
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ObjectCursor
argument_list|<
name|String
argument_list|>
name|cursor
range|:
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|keys
argument_list|()
control|)
block|{
name|String
name|templateName
init|=
name|cursor
operator|.
name|value
decl_stmt|;
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|request
operator|.
name|name
argument_list|,
name|templateName
argument_list|)
condition|)
block|{
name|templateNames
operator|.
name|add
argument_list|(
name|templateName
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|templateNames
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// if its a match all pattern, and no templates are found (we have none), don't
comment|// fail with index missing...
if|if
condition|(
name|Regex
operator|.
name|isMatchAllPattern
argument_list|(
name|request
operator|.
name|name
argument_list|)
condition|)
block|{
return|return
name|currentState
return|;
block|}
throw|throw
operator|new
name|IndexTemplateMissingException
argument_list|(
name|request
operator|.
name|name
argument_list|)
throw|;
block|}
name|MetaData
operator|.
name|Builder
name|metaData
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|templateName
range|:
name|templateNames
control|)
block|{
name|metaData
operator|.
name|removeTemplate
argument_list|(
name|templateName
argument_list|)
expr_stmt|;
block|}
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaData
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|RemoveResponse
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|putTemplate
specifier|public
name|void
name|putTemplate
parameter_list|(
specifier|final
name|PutRequest
name|request
parameter_list|,
specifier|final
name|PutListener
name|listener
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|updatedSettingsBuilder
init|=
name|Settings
operator|.
name|builder
argument_list|()
decl_stmt|;
name|updatedSettingsBuilder
operator|.
name|put
argument_list|(
name|request
operator|.
name|settings
argument_list|)
operator|.
name|normalizePrefix
argument_list|(
name|IndexMetaData
operator|.
name|INDEX_SETTING_PREFIX
argument_list|)
expr_stmt|;
name|request
operator|.
name|settings
argument_list|(
name|updatedSettingsBuilder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|name
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"index_template must provide a name"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|==
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"index_template must provide a template"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|validate
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|IndexTemplateMetaData
operator|.
name|Builder
name|templateBuilder
init|=
name|IndexTemplateMetaData
operator|.
name|builder
argument_list|(
name|request
operator|.
name|name
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"create-index-template ["
operator|+
name|request
operator|.
name|name
operator|+
literal|"], cause ["
operator|+
name|request
operator|.
name|cause
operator|+
literal|"]"
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|request
operator|.
name|masterTimeout
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|request
operator|.
name|create
operator|&&
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|templates
argument_list|()
operator|.
name|containsKey
argument_list|(
name|request
operator|.
name|name
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IndexTemplateAlreadyExistsException
argument_list|(
name|request
operator|.
name|name
argument_list|)
throw|;
block|}
name|validateAndAddTemplate
argument_list|(
name|request
argument_list|,
name|templateBuilder
argument_list|,
name|indicesService
argument_list|)
expr_stmt|;
for|for
control|(
name|Alias
name|alias
range|:
name|request
operator|.
name|aliases
control|)
block|{
name|AliasMetaData
name|aliasMetaData
init|=
name|AliasMetaData
operator|.
name|builder
argument_list|(
name|alias
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|alias
operator|.
name|filter
argument_list|()
argument_list|)
operator|.
name|indexRouting
argument_list|(
name|alias
operator|.
name|indexRouting
argument_list|()
argument_list|)
operator|.
name|searchRouting
argument_list|(
name|alias
operator|.
name|searchRouting
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|templateBuilder
operator|.
name|putAlias
argument_list|(
name|aliasMetaData
argument_list|)
expr_stmt|;
block|}
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
name|request
operator|.
name|customs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|templateBuilder
operator|.
name|putCustom
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
name|IndexTemplateMetaData
name|template
init|=
name|templateBuilder
operator|.
name|build
argument_list|()
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|builder
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|template
argument_list|)
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|builder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|PutResponse
argument_list|(
literal|true
argument_list|,
name|templateBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|validateAndAddTemplate
specifier|private
specifier|static
name|void
name|validateAndAddTemplate
parameter_list|(
specifier|final
name|PutRequest
name|request
parameter_list|,
name|IndexTemplateMetaData
operator|.
name|Builder
name|templateBuilder
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
throws|throws
name|Exception
block|{
name|Index
name|createdIndex
init|=
literal|null
decl_stmt|;
specifier|final
name|String
name|temporaryIndexName
init|=
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
decl_stmt|;
try|try
block|{
comment|//create index service for parsing and validating "mappings"
name|Settings
name|dummySettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|request
operator|.
name|settings
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|tmpIndexMetadata
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|temporaryIndexName
argument_list|)
operator|.
name|settings
argument_list|(
name|dummySettings
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|dummyIndexService
init|=
name|indicesService
operator|.
name|createIndex
argument_list|(
name|tmpIndexMetadata
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
name|createdIndex
operator|=
name|dummyIndexService
operator|.
name|index
argument_list|()
expr_stmt|;
name|templateBuilder
operator|.
name|order
argument_list|(
name|request
operator|.
name|order
argument_list|)
expr_stmt|;
name|templateBuilder
operator|.
name|version
argument_list|(
name|request
operator|.
name|version
argument_list|)
expr_stmt|;
name|templateBuilder
operator|.
name|template
argument_list|(
name|request
operator|.
name|template
argument_list|)
expr_stmt|;
name|templateBuilder
operator|.
name|settings
argument_list|(
name|request
operator|.
name|settings
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|mappingsForValidation
init|=
operator|new
name|HashMap
argument_list|<>
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
name|String
argument_list|>
name|entry
range|:
name|request
operator|.
name|mappings
operator|.
name|entrySet
argument_list|()
control|)
block|{
try|try
block|{
name|templateBuilder
operator|.
name|putMapping
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
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Failed to parse mapping [{}]: {}"
argument_list|,
name|e
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
name|mappingsForValidation
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|MapperService
operator|.
name|parseMapping
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|dummyIndexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
name|mappingsForValidation
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|createdIndex
operator|!=
literal|null
condition|)
block|{
name|indicesService
operator|.
name|removeIndex
argument_list|(
name|createdIndex
argument_list|,
literal|" created for parsing template mapping"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|validate
specifier|private
name|void
name|validate
parameter_list|(
name|PutRequest
name|request
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|validationErrors
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|" "
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"name must not contain a space"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"name must not contain a ','"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"name must not contain a '#'"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|name
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"name must not start with '_'"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|request
operator|.
name|name
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
operator|.
name|equals
argument_list|(
name|request
operator|.
name|name
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"name must be lower cased"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|contains
argument_list|(
literal|" "
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"template must not contain a space"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|contains
argument_list|(
literal|","
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"template must not contain a ','"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"template must not contain a '#'"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"template must not start with '_'"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Strings
operator|.
name|validFileNameExcludingAstrix
argument_list|(
name|request
operator|.
name|template
argument_list|)
condition|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
literal|"template must not contain the following characters "
operator|+
name|Strings
operator|.
name|INVALID_FILENAME_CHARS
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|indexScopedSettings
operator|.
name|validate
argument_list|(
name|request
operator|.
name|settings
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
name|iae
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Throwable
name|t
range|:
name|iae
operator|.
name|getSuppressed
argument_list|()
control|)
block|{
name|validationErrors
operator|.
name|add
argument_list|(
name|t
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|indexSettingsValidation
init|=
name|metaDataCreateIndexService
operator|.
name|getIndexSettingsValidationErrors
argument_list|(
name|request
operator|.
name|settings
argument_list|)
decl_stmt|;
name|validationErrors
operator|.
name|addAll
argument_list|(
name|indexSettingsValidation
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|validationErrors
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ValidationException
name|validationException
init|=
operator|new
name|ValidationException
argument_list|()
decl_stmt|;
name|validationException
operator|.
name|addValidationErrors
argument_list|(
name|validationErrors
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|InvalidIndexTemplateException
argument_list|(
name|request
operator|.
name|name
argument_list|,
name|validationException
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
for|for
control|(
name|Alias
name|alias
range|:
name|request
operator|.
name|aliases
control|)
block|{
comment|//we validate the alias only partially, as we don't know yet to which index it'll get applied to
name|aliasValidator
operator|.
name|validateAliasStandalone
argument_list|(
name|alias
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|template
operator|.
name|equals
argument_list|(
name|alias
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Alias ["
operator|+
name|alias
operator|.
name|name
argument_list|()
operator|+
literal|"] cannot be the same as the template pattern ["
operator|+
name|request
operator|.
name|template
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|interface|PutListener
specifier|public
interface|interface
name|PutListener
block|{
DECL|method|onResponse
name|void
name|onResponse
parameter_list|(
name|PutResponse
name|response
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
function_decl|;
block|}
DECL|class|PutRequest
specifier|public
specifier|static
class|class
name|PutRequest
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|cause
specifier|final
name|String
name|cause
decl_stmt|;
DECL|field|create
name|boolean
name|create
decl_stmt|;
DECL|field|order
name|int
name|order
decl_stmt|;
DECL|field|version
name|Integer
name|version
decl_stmt|;
DECL|field|template
name|String
name|template
decl_stmt|;
DECL|field|settings
name|Settings
name|settings
init|=
name|Settings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
decl_stmt|;
DECL|field|mappings
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
name|List
argument_list|<
name|Alias
argument_list|>
name|aliases
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|customs
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
DECL|field|masterTimeout
name|TimeValue
name|masterTimeout
init|=
name|MasterNodeRequest
operator|.
name|DEFAULT_MASTER_NODE_TIMEOUT
decl_stmt|;
DECL|method|PutRequest
specifier|public
name|PutRequest
parameter_list|(
name|String
name|cause
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|cause
operator|=
name|cause
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|order
specifier|public
name|PutRequest
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
DECL|method|template
specifier|public
name|PutRequest
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
DECL|method|create
specifier|public
name|PutRequest
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
DECL|method|settings
specifier|public
name|PutRequest
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
DECL|method|mappings
specifier|public
name|PutRequest
name|mappings
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|mappings
parameter_list|)
block|{
name|this
operator|.
name|mappings
operator|.
name|putAll
argument_list|(
name|mappings
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|aliases
specifier|public
name|PutRequest
name|aliases
parameter_list|(
name|Set
argument_list|<
name|Alias
argument_list|>
name|aliases
parameter_list|)
block|{
name|this
operator|.
name|aliases
operator|.
name|addAll
argument_list|(
name|aliases
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|customs
specifier|public
name|PutRequest
name|customs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|IndexMetaData
operator|.
name|Custom
argument_list|>
name|customs
parameter_list|)
block|{
name|this
operator|.
name|customs
operator|.
name|putAll
argument_list|(
name|customs
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|putMapping
specifier|public
name|PutRequest
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
DECL|method|masterTimeout
specifier|public
name|PutRequest
name|masterTimeout
parameter_list|(
name|TimeValue
name|masterTimeout
parameter_list|)
block|{
name|this
operator|.
name|masterTimeout
operator|=
name|masterTimeout
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|version
specifier|public
name|PutRequest
name|version
parameter_list|(
name|Integer
name|version
parameter_list|)
block|{
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
DECL|class|PutResponse
specifier|public
specifier|static
class|class
name|PutResponse
block|{
DECL|field|acknowledged
specifier|private
specifier|final
name|boolean
name|acknowledged
decl_stmt|;
DECL|field|template
specifier|private
specifier|final
name|IndexTemplateMetaData
name|template
decl_stmt|;
DECL|method|PutResponse
specifier|public
name|PutResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|,
name|IndexTemplateMetaData
name|template
parameter_list|)
block|{
name|this
operator|.
name|acknowledged
operator|=
name|acknowledged
expr_stmt|;
name|this
operator|.
name|template
operator|=
name|template
expr_stmt|;
block|}
DECL|method|acknowledged
specifier|public
name|boolean
name|acknowledged
parameter_list|()
block|{
return|return
name|acknowledged
return|;
block|}
DECL|method|template
specifier|public
name|IndexTemplateMetaData
name|template
parameter_list|()
block|{
return|return
name|template
return|;
block|}
block|}
DECL|class|RemoveRequest
specifier|public
specifier|static
class|class
name|RemoveRequest
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|masterTimeout
name|TimeValue
name|masterTimeout
init|=
name|MasterNodeRequest
operator|.
name|DEFAULT_MASTER_NODE_TIMEOUT
decl_stmt|;
DECL|method|RemoveRequest
specifier|public
name|RemoveRequest
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
DECL|method|masterTimeout
specifier|public
name|RemoveRequest
name|masterTimeout
parameter_list|(
name|TimeValue
name|masterTimeout
parameter_list|)
block|{
name|this
operator|.
name|masterTimeout
operator|=
name|masterTimeout
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
DECL|class|RemoveResponse
specifier|public
specifier|static
class|class
name|RemoveResponse
block|{
DECL|field|acknowledged
specifier|private
specifier|final
name|boolean
name|acknowledged
decl_stmt|;
DECL|method|RemoveResponse
specifier|public
name|RemoveResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
name|this
operator|.
name|acknowledged
operator|=
name|acknowledged
expr_stmt|;
block|}
DECL|method|acknowledged
specifier|public
name|boolean
name|acknowledged
parameter_list|()
block|{
return|return
name|acknowledged
return|;
block|}
block|}
DECL|interface|RemoveListener
specifier|public
interface|interface
name|RemoveListener
block|{
DECL|method|onResponse
name|void
name|onResponse
parameter_list|(
name|RemoveResponse
name|response
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

