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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|mapping
operator|.
name|put
operator|.
name|PutMappingClusterStateUpdateRequest
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
name|AckedClusterStateTaskListener
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
name|ClusterStateTaskConfig
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
name|ClusterStateTaskExecutor
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|Nullable
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
name|collect
operator|.
name|Tuple
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
name|compress
operator|.
name|CompressedXContent
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
name|DocumentMapper
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
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|InvalidTypeNameException
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
comment|/**  * Service responsible for submitting mapping changes  */
end_comment

begin_class
DECL|class|MetaDataMappingService
specifier|public
class|class
name|MetaDataMappingService
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
DECL|field|refreshExecutor
specifier|final
name|ClusterStateTaskExecutor
argument_list|<
name|RefreshTask
argument_list|>
name|refreshExecutor
init|=
operator|new
name|RefreshTaskExecutor
argument_list|()
decl_stmt|;
DECL|field|putMappingExecutor
specifier|final
name|ClusterStateTaskExecutor
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
name|putMappingExecutor
init|=
operator|new
name|PutMappingExecutor
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataMappingService
specifier|public
name|MetaDataMappingService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|IndicesService
name|indicesService
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
block|}
DECL|class|RefreshTask
specifier|static
class|class
name|RefreshTask
block|{
DECL|field|index
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|indexUUID
specifier|final
name|String
name|indexUUID
decl_stmt|;
DECL|method|RefreshTask
name|RefreshTask
parameter_list|(
name|String
name|index
parameter_list|,
specifier|final
name|String
name|indexUUID
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
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|index
operator|+
literal|"]["
operator|+
name|indexUUID
operator|+
literal|"]"
return|;
block|}
block|}
DECL|class|RefreshTaskExecutor
class|class
name|RefreshTaskExecutor
implements|implements
name|ClusterStateTaskExecutor
argument_list|<
name|RefreshTask
argument_list|>
block|{
annotation|@
name|Override
DECL|method|execute
specifier|public
name|BatchResult
argument_list|<
name|RefreshTask
argument_list|>
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|RefreshTask
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|Exception
block|{
name|ClusterState
name|newClusterState
init|=
name|executeRefresh
argument_list|(
name|currentState
argument_list|,
name|tasks
argument_list|)
decl_stmt|;
return|return
name|BatchResult
operator|.
expr|<
name|RefreshTask
operator|>
name|builder
argument_list|()
operator|.
name|successes
argument_list|(
name|tasks
argument_list|)
operator|.
name|build
argument_list|(
name|newClusterState
argument_list|)
return|;
block|}
block|}
comment|/**      * Batch method to apply all the queued refresh operations. The idea is to try and batch as much      * as possible so we won't create the same index all the time for example for the updates on the same mapping      * and generate a single cluster change event out of all of those.      */
DECL|method|executeRefresh
name|ClusterState
name|executeRefresh
parameter_list|(
specifier|final
name|ClusterState
name|currentState
parameter_list|,
specifier|final
name|List
argument_list|<
name|RefreshTask
argument_list|>
name|allTasks
parameter_list|)
throws|throws
name|Exception
block|{
comment|// break down to tasks per index, so we can optimize the on demand index service creation
comment|// to only happen for the duration of a single index processing of its respective events
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|RefreshTask
argument_list|>
argument_list|>
name|tasksPerIndex
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|RefreshTask
name|task
range|:
name|allTasks
control|)
block|{
if|if
condition|(
name|task
operator|.
name|index
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"ignoring a mapping task of type [{}] with a null index."
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
name|tasksPerIndex
operator|.
name|computeIfAbsent
argument_list|(
name|task
operator|.
name|index
argument_list|,
name|k
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|task
argument_list|)
expr_stmt|;
block|}
name|boolean
name|dirty
init|=
literal|false
decl_stmt|;
name|MetaData
operator|.
name|Builder
name|mdBuilder
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
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|RefreshTask
argument_list|>
argument_list|>
name|entry
range|:
name|tasksPerIndex
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|IndexMetaData
name|indexMetaData
init|=
name|mdBuilder
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
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
comment|// index got deleted on us, ignore...
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] ignoring tasks - index meta data doesn't exist"
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|Index
name|index
init|=
name|indexMetaData
operator|.
name|getIndex
argument_list|()
decl_stmt|;
comment|// the tasks lists to iterate over, filled with the list of mapping tasks, trying to keep
comment|// the latest (based on order) update mapping one per node
name|List
argument_list|<
name|RefreshTask
argument_list|>
name|allIndexTasks
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|boolean
name|hasTaskWithRightUUID
init|=
literal|false
decl_stmt|;
for|for
control|(
name|RefreshTask
name|task
range|:
name|allIndexTasks
control|)
block|{
if|if
condition|(
name|indexMetaData
operator|.
name|isSameUUID
argument_list|(
name|task
operator|.
name|indexUUID
argument_list|)
condition|)
block|{
name|hasTaskWithRightUUID
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} ignoring task [{}] - index meta data doesn't match task uuid"
argument_list|,
name|index
argument_list|,
name|task
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|hasTaskWithRightUUID
operator|==
literal|false
condition|)
block|{
continue|continue;
block|}
comment|// construct the actual index if needed, and make sure the relevant mappings are there
name|boolean
name|removeIndex
init|=
literal|false
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|indexMetaData
operator|.
name|getIndex
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
comment|// we need to create the index here, and add the current mapping to it, so we can merge
name|indexService
operator|=
name|indicesService
operator|.
name|createIndex
argument_list|(
name|indexMetaData
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
name|removeIndex
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|ObjectCursor
argument_list|<
name|MappingMetaData
argument_list|>
name|metaData
range|:
name|indexMetaData
operator|.
name|getMappings
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
comment|// don't apply the default mapping, it has been applied when the mapping was created
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
name|metaData
operator|.
name|value
operator|.
name|type
argument_list|()
argument_list|,
name|metaData
operator|.
name|value
operator|.
name|source
argument_list|()
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_RECOVERY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
name|IndexMetaData
operator|.
name|Builder
name|builder
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
name|indexMetaData
argument_list|)
decl_stmt|;
try|try
block|{
name|boolean
name|indexDirty
init|=
name|refreshIndexMapping
argument_list|(
name|indexService
argument_list|,
name|builder
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexDirty
condition|)
block|{
name|mdBuilder
operator|.
name|put
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
expr_stmt|;
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|removeIndex
condition|)
block|{
name|indicesService
operator|.
name|removeIndex
argument_list|(
name|index
argument_list|,
literal|"created for mapping processing"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|dirty
condition|)
block|{
return|return
name|currentState
return|;
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
name|mdBuilder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|refreshIndexMapping
specifier|private
name|boolean
name|refreshIndexMapping
parameter_list|(
name|IndexService
name|indexService
parameter_list|,
name|IndexMetaData
operator|.
name|Builder
name|builder
parameter_list|)
block|{
name|boolean
name|dirty
init|=
literal|false
decl_stmt|;
name|String
name|index
init|=
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|updatedTypes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DocumentMapper
name|mapper
range|:
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|docMappers
argument_list|(
literal|true
argument_list|)
control|)
block|{
specifier|final
name|String
name|type
init|=
name|mapper
operator|.
name|type
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|mapper
operator|.
name|mappingSource
argument_list|()
operator|.
name|equals
argument_list|(
name|builder
operator|.
name|mapping
argument_list|(
name|type
argument_list|)
operator|.
name|source
argument_list|()
argument_list|)
condition|)
block|{
name|updatedTypes
operator|.
name|add
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
comment|// if a single type is not up-to-date, re-send everything
if|if
condition|(
name|updatedTypes
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"[{}] re-syncing mappings with cluster state because of types [{}]"
argument_list|,
name|index
argument_list|,
name|updatedTypes
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
expr_stmt|;
for|for
control|(
name|DocumentMapper
name|mapper
range|:
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|docMappers
argument_list|(
literal|true
argument_list|)
control|)
block|{
name|builder
operator|.
name|putMapping
argument_list|(
operator|new
name|MappingMetaData
argument_list|(
name|mapper
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"[{}] failed to refresh-mapping in cluster state"
argument_list|,
name|index
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|dirty
return|;
block|}
comment|/**      * Refreshes mappings if they are not the same between original and parsed version      */
DECL|method|refreshMapping
specifier|public
name|void
name|refreshMapping
parameter_list|(
specifier|final
name|String
name|index
parameter_list|,
specifier|final
name|String
name|indexUUID
parameter_list|)
block|{
specifier|final
name|RefreshTask
name|refreshTask
init|=
operator|new
name|RefreshTask
argument_list|(
name|index
argument_list|,
name|indexUUID
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"refresh-mapping"
argument_list|,
name|refreshTask
argument_list|,
name|ClusterStateTaskConfig
operator|.
name|build
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|)
argument_list|,
name|refreshExecutor
argument_list|,
parameter_list|(
name|source
parameter_list|,
name|e
parameter_list|)
lambda|->
name|logger
operator|.
name|warn
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failure during [{}]"
argument_list|,
name|source
argument_list|)
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|PutMappingExecutor
class|class
name|PutMappingExecutor
implements|implements
name|ClusterStateTaskExecutor
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
block|{
annotation|@
name|Override
DECL|method|execute
specifier|public
name|BatchResult
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|Exception
block|{
name|Set
argument_list|<
name|Index
argument_list|>
name|indicesToClose
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|BatchResult
operator|.
name|Builder
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
name|builder
init|=
name|BatchResult
operator|.
name|builder
argument_list|()
decl_stmt|;
try|try
block|{
comment|// precreate incoming indices;
for|for
control|(
name|PutMappingClusterStateUpdateRequest
name|request
range|:
name|tasks
control|)
block|{
try|try
block|{
for|for
control|(
name|Index
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indicesService
operator|.
name|hasIndex
argument_list|(
name|indexMetaData
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// if the index does not exists we create it once, add all types to the mapper service and
comment|// close it later once we are done with mapping update
name|indicesToClose
operator|.
name|add
argument_list|(
name|indexMetaData
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|createIndex
argument_list|(
name|indexMetaData
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
decl_stmt|;
comment|// add mappings for all types, we need them for cross-type validation
for|for
control|(
name|ObjectCursor
argument_list|<
name|MappingMetaData
argument_list|>
name|mapping
range|:
name|indexMetaData
operator|.
name|getMappings
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
name|mapping
operator|.
name|value
operator|.
name|type
argument_list|()
argument_list|,
name|mapping
operator|.
name|value
operator|.
name|source
argument_list|()
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_RECOVERY
argument_list|,
name|request
operator|.
name|updateAllTypes
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|currentState
operator|=
name|applyRequest
argument_list|(
name|currentState
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|builder
operator|.
name|success
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
name|builder
operator|.
name|failure
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|build
argument_list|(
name|currentState
argument_list|)
return|;
block|}
finally|finally
block|{
for|for
control|(
name|Index
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
literal|"created for mapping processing"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|applyRequest
specifier|private
name|ClusterState
name|applyRequest
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|PutMappingClusterStateUpdateRequest
name|request
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|mappingType
init|=
name|request
operator|.
name|type
argument_list|()
decl_stmt|;
name|CompressedXContent
name|mappingUpdateSource
init|=
operator|new
name|CompressedXContent
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|MetaData
name|metaData
init|=
name|currentState
operator|.
name|metaData
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Tuple
argument_list|<
name|IndexService
argument_list|,
name|IndexMetaData
argument_list|>
argument_list|>
name|updateList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Index
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
comment|// IMPORTANT: always get the metadata from the state since it get's batched
comment|// and if we pull it from the indexService we might miss an update etc.
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|currentState
operator|.
name|getMetaData
argument_list|()
operator|.
name|getIndexSafe
argument_list|(
name|index
argument_list|)
decl_stmt|;
comment|// this is paranoia... just to be sure we use the exact same indexService and metadata tuple on the update that
comment|// we used for the validation, it makes this mechanism little less scary (a little)
name|updateList
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|indexService
argument_list|,
name|indexMetaData
argument_list|)
argument_list|)
expr_stmt|;
comment|// try and parse it (no need to add it here) so we can bail early in case of parsing exception
name|DocumentMapper
name|newMapper
decl_stmt|;
name|DocumentMapper
name|existingMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
operator|.
name|equals
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
comment|// _default_ types do not go through merging, but we do test the new settings. Also don't apply the old default
name|newMapper
operator|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|parse
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|mappingUpdateSource
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newMapper
operator|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|parse
argument_list|(
name|request
operator|.
name|type
argument_list|()
argument_list|,
name|mappingUpdateSource
argument_list|,
name|existingMapper
operator|==
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|existingMapper
operator|!=
literal|null
condition|)
block|{
comment|// first, simulate: just call merge and ignore the result
name|existingMapper
operator|.
name|merge
argument_list|(
name|newMapper
operator|.
name|mapping
argument_list|()
argument_list|,
name|request
operator|.
name|updateAllTypes
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// TODO: can we find a better place for this validation?
comment|// The reason this validation is here is that the mapper service doesn't learn about
comment|// new types all at once , which can create a false error.
comment|// For example in MapperService we can't distinguish between a create index api call
comment|// and a put mapping api call, so we don't which type did exist before.
comment|// Also the order of the mappings may be backwards.
if|if
condition|(
name|newMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|active
argument_list|()
condition|)
block|{
for|for
control|(
name|ObjectCursor
argument_list|<
name|MappingMetaData
argument_list|>
name|mapping
range|:
name|indexMetaData
operator|.
name|getMappings
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|String
name|parentType
init|=
name|newMapper
operator|.
name|parentFieldMapper
argument_list|()
operator|.
name|type
argument_list|()
decl_stmt|;
if|if
condition|(
name|parentType
operator|.
name|equals
argument_list|(
name|mapping
operator|.
name|value
operator|.
name|type
argument_list|()
argument_list|)
operator|&&
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|getParentTypes
argument_list|()
operator|.
name|contains
argument_list|(
name|parentType
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"can't add a _parent field that points to an "
operator|+
literal|"already existing type, that isn't already a parent"
argument_list|)
throw|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|mappingType
operator|==
literal|null
condition|)
block|{
name|mappingType
operator|=
name|newMapper
operator|.
name|type
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|mappingType
operator|.
name|equals
argument_list|(
name|newMapper
operator|.
name|type
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|InvalidTypeNameException
argument_list|(
literal|"Type name provided does not match type name within mapping definition"
argument_list|)
throw|;
block|}
block|}
assert|assert
name|mappingType
operator|!=
literal|null
assert|;
if|if
condition|(
operator|!
name|MapperService
operator|.
name|DEFAULT_MAPPING
operator|.
name|equals
argument_list|(
name|mappingType
argument_list|)
operator|&&
name|mappingType
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'_'
condition|)
block|{
throw|throw
operator|new
name|InvalidTypeNameException
argument_list|(
literal|"Document mapping type name can't start with '_', found: ["
operator|+
name|mappingType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|MetaData
operator|.
name|Builder
name|builder
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|metaData
argument_list|)
decl_stmt|;
for|for
control|(
name|Tuple
argument_list|<
name|IndexService
argument_list|,
name|IndexMetaData
argument_list|>
name|toUpdate
range|:
name|updateList
control|)
block|{
comment|// do the actual merge here on the master, and update the mapping source
comment|// we use the exact same indexService and metadata we used to validate above here to actually apply the update
specifier|final
name|IndexService
name|indexService
init|=
name|toUpdate
operator|.
name|v1
argument_list|()
decl_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|toUpdate
operator|.
name|v2
argument_list|()
decl_stmt|;
specifier|final
name|Index
name|index
init|=
name|indexMetaData
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|CompressedXContent
name|existingSource
init|=
literal|null
decl_stmt|;
name|DocumentMapper
name|existingMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|mappingType
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingMapper
operator|!=
literal|null
condition|)
block|{
name|existingSource
operator|=
name|existingMapper
operator|.
name|mappingSource
argument_list|()
expr_stmt|;
block|}
name|DocumentMapper
name|mergedMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|merge
argument_list|(
name|mappingType
argument_list|,
name|mappingUpdateSource
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
name|request
operator|.
name|updateAllTypes
argument_list|()
argument_list|)
decl_stmt|;
name|CompressedXContent
name|updatedSource
init|=
name|mergedMapper
operator|.
name|mappingSource
argument_list|()
decl_stmt|;
if|if
condition|(
name|existingSource
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|existingSource
operator|.
name|equals
argument_list|(
name|updatedSource
argument_list|)
condition|)
block|{
comment|// same source, no changes, ignore it
block|}
else|else
block|{
comment|// use the merged mapping source
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} update_mapping [{}] with source [{}]"
argument_list|,
name|index
argument_list|,
name|mergedMapper
operator|.
name|type
argument_list|()
argument_list|,
name|updatedSource
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|logger
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"{} update_mapping [{}]"
argument_list|,
name|index
argument_list|,
name|mergedMapper
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} create_mapping [{}] with source [{}]"
argument_list|,
name|index
argument_list|,
name|mappingType
argument_list|,
name|updatedSource
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|logger
operator|.
name|isInfoEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"{} create_mapping [{}]"
argument_list|,
name|index
argument_list|,
name|mappingType
argument_list|)
expr_stmt|;
block|}
block|}
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
comment|// Mapping updates on a single type may have side-effects on other types so we need to
comment|// update mapping metadata on all types
for|for
control|(
name|DocumentMapper
name|mapper
range|:
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|docMappers
argument_list|(
literal|true
argument_list|)
control|)
block|{
name|indexMetaDataBuilder
operator|.
name|putMapping
argument_list|(
operator|new
name|MappingMetaData
argument_list|(
name|mapper
operator|.
name|mappingSource
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|put
argument_list|(
name|indexMetaDataBuilder
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
name|builder
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|describeTasks
specifier|public
name|String
name|describeTasks
parameter_list|(
name|List
argument_list|<
name|PutMappingClusterStateUpdateRequest
argument_list|>
name|tasks
parameter_list|)
block|{
return|return
name|tasks
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|PutMappingClusterStateUpdateRequest
operator|::
name|type
argument_list|)
operator|.
name|reduce
argument_list|(
parameter_list|(
name|s1
parameter_list|,
name|s2
parameter_list|)
lambda|->
name|s1
operator|+
literal|", "
operator|+
name|s2
argument_list|)
operator|.
name|orElse
argument_list|(
literal|""
argument_list|)
return|;
block|}
block|}
DECL|method|putMapping
specifier|public
name|void
name|putMapping
parameter_list|(
specifier|final
name|PutMappingClusterStateUpdateRequest
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
literal|"put-mapping"
argument_list|,
name|request
argument_list|,
name|ClusterStateTaskConfig
operator|.
name|build
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|,
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|,
name|putMappingExecutor
argument_list|,
operator|new
name|AckedClusterStateTaskListener
argument_list|()
block|{
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
name|boolean
name|mustAck
parameter_list|(
name|DiscoveryNode
name|discoveryNode
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAllNodesAcked
parameter_list|(
annotation|@
name|Nullable
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClusterStateUpdateResponse
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onAckTimeout
parameter_list|()
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|ClusterStateUpdateResponse
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|TimeValue
name|ackTimeout
parameter_list|()
block|{
return|return
name|request
operator|.
name|ackTimeout
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

