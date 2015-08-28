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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|IndicesAliasesClusterStateUpdateRequest
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
name|AckedClusterStateUpdateTask
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
name|ClusterService
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
name|ack
operator|.
name|ClusterStateUpdateResponse
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
name|index
operator|.
name|IndexNotFoundException
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
name|IndicesService
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
name|List
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
comment|/**  * Service responsible for submitting add and remove aliases requests  */
end_comment

begin_class
DECL|class|MetaDataIndexAliasesService
specifier|public
class|class
name|MetaDataIndexAliasesService
extends|extends
name|AbstractComponent
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|aliasValidator
specifier|private
specifier|final
name|AliasValidator
name|aliasValidator
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataIndexAliasesService
specifier|public
name|MetaDataIndexAliasesService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|AliasValidator
name|aliasValidator
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
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|aliasValidator
operator|=
name|aliasValidator
expr_stmt|;
block|}
DECL|method|indicesAliases
specifier|public
name|void
name|indicesAliases
parameter_list|(
specifier|final
name|IndicesAliasesClusterStateUpdateRequest
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"index-aliases"
argument_list|,
name|Priority
operator|.
name|URGENT
argument_list|,
operator|new
name|AckedClusterStateUpdateTask
argument_list|<
name|ClusterStateUpdateResponse
argument_list|>
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|ClusterStateUpdateResponse
name|newResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
return|return
operator|new
name|ClusterStateUpdateResponse
argument_list|(
name|acknowledged
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
specifier|final
name|ClusterState
name|currentState
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|indicesToClose
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|IndexService
argument_list|>
name|indices
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
try|try
block|{
for|for
control|(
name|AliasAction
name|aliasAction
range|:
name|request
operator|.
name|actions
argument_list|()
control|)
block|{
name|aliasValidator
operator|.
name|validateAliasAction
argument_list|(
name|aliasAction
argument_list|,
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|hasIndex
argument_list|(
name|aliasAction
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IndexNotFoundException
argument_list|(
name|aliasAction
operator|.
name|index
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|boolean
name|changed
init|=
literal|false
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
decl_stmt|;
for|for
control|(
name|AliasAction
name|aliasAction
range|:
name|request
operator|.
name|actions
argument_list|()
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|builder
operator|.
name|get
argument_list|(
name|aliasAction
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetaData
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexNotFoundException
argument_list|(
name|aliasAction
operator|.
name|index
argument_list|()
argument_list|)
throw|;
block|}
comment|// TODO: not copy (putAll)
name|IndexMetaData
operator|.
name|Builder
name|indexMetaDataBuilder
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
decl_stmt|;
if|if
condition|(
name|aliasAction
operator|.
name|actionType
argument_list|()
operator|==
name|AliasAction
operator|.
name|Type
operator|.
name|ADD
condition|)
block|{
name|String
name|filter
init|=
name|aliasAction
operator|.
name|filter
argument_list|()
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|filter
argument_list|)
condition|)
block|{
comment|// parse the filter, in order to validate it
name|IndexService
name|indexService
init|=
name|indices
operator|.
name|get
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|indexService
operator|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
comment|// temporarily create the index and add mappings so we can parse the filter
try|try
block|{
name|indexService
operator|=
name|indicesService
operator|.
name|createIndex
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|,
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|containsKey
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
condition|)
block|{
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
operator|.
name|source
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ObjectCursor
argument_list|<
name|MappingMetaData
argument_list|>
name|cursor
range|:
name|indexMetaData
operator|.
name|mappings
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|MappingMetaData
name|mappingMetaData
init|=
name|cursor
operator|.
name|value
decl_stmt|;
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
name|mappingMetaData
operator|.
name|type
argument_list|()
argument_list|,
name|mappingMetaData
operator|.
name|source
argument_list|()
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] failed to temporary create in order to apply alias action"
argument_list|,
name|e
argument_list|,
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|indicesToClose
operator|.
name|add
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indices
operator|.
name|put
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|indexService
argument_list|)
expr_stmt|;
block|}
name|aliasValidator
operator|.
name|validateAliasFilter
argument_list|(
name|aliasAction
operator|.
name|alias
argument_list|()
argument_list|,
name|filter
argument_list|,
name|indexService
operator|.
name|queryParserService
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|AliasMetaData
name|newAliasMd
init|=
name|AliasMetaData
operator|.
name|newAliasMetaDataBuilder
argument_list|(
name|aliasAction
operator|.
name|alias
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
operator|.
name|indexRouting
argument_list|(
name|aliasAction
operator|.
name|indexRouting
argument_list|()
argument_list|)
operator|.
name|searchRouting
argument_list|(
name|aliasAction
operator|.
name|searchRouting
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Check if this alias already exists
name|AliasMetaData
name|aliasMd
init|=
name|indexMetaData
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
name|aliasAction
operator|.
name|alias
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|aliasMd
operator|!=
literal|null
operator|&&
name|aliasMd
operator|.
name|equals
argument_list|(
name|newAliasMd
argument_list|)
condition|)
block|{
comment|// It's the same alias - ignore it
continue|continue;
block|}
name|indexMetaDataBuilder
operator|.
name|putAlias
argument_list|(
name|newAliasMd
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|aliasAction
operator|.
name|actionType
argument_list|()
operator|==
name|AliasAction
operator|.
name|Type
operator|.
name|REMOVE
condition|)
block|{
if|if
condition|(
operator|!
name|indexMetaData
operator|.
name|aliases
argument_list|()
operator|.
name|containsKey
argument_list|(
name|aliasAction
operator|.
name|alias
argument_list|()
argument_list|)
condition|)
block|{
comment|// This alias doesn't exist - ignore
continue|continue;
block|}
name|indexMetaDataBuilder
operator|.
name|removeAlias
argument_list|(
name|aliasAction
operator|.
name|alias
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|changed
operator|=
literal|true
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|indexMetaDataBuilder
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|changed
condition|)
block|{
name|ClusterState
name|updatedState
init|=
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
decl_stmt|;
comment|// even though changes happened, they resulted in 0 actual changes to metadata
comment|// i.e. remove and add the same alias to the same index
if|if
condition|(
operator|!
name|updatedState
operator|.
name|metaData
argument_list|()
operator|.
name|equalsAliases
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|updatedState
return|;
block|}
block|}
return|return
name|currentState
return|;
block|}
finally|finally
block|{
for|for
control|(
name|String
name|index
range|:
name|indicesToClose
control|)
block|{
name|indicesService
operator|.
name|removeIndex
argument_list|(
name|index
argument_list|,
literal|"created for alias processing"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

