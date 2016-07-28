begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.rollover
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
name|rollover
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
name|IndicesAliasesClusterStateUpdateRequest
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
name|create
operator|.
name|CreateIndexClusterStateUpdateRequest
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
name|create
operator|.
name|CreateIndexRequest
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
name|stats
operator|.
name|IndicesStatsResponse
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
name|ActionFilters
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
name|ActiveShardsObserver
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
name|TransportMasterNodeAction
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
name|Client
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
name|block
operator|.
name|ClusterBlockException
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
name|block
operator|.
name|ClusterBlockLevel
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
name|AliasAction
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
name|AliasOrIndex
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|MetaData
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
name|MetaDataCreateIndexService
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
name|MetaDataIndexAliasesService
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
name|shard
operator|.
name|DocsStats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  * Main class to swap the index pointed to by an alias, given some conditions  */
end_comment

begin_class
DECL|class|TransportRolloverAction
specifier|public
class|class
name|TransportRolloverAction
extends|extends
name|TransportMasterNodeAction
argument_list|<
name|RolloverRequest
argument_list|,
name|RolloverResponse
argument_list|>
block|{
DECL|field|INDEX_NAME_PATTERN
specifier|private
specifier|static
specifier|final
name|Pattern
name|INDEX_NAME_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"^.*-(\\d)+$"
argument_list|)
decl_stmt|;
DECL|field|createIndexService
specifier|private
specifier|final
name|MetaDataCreateIndexService
name|createIndexService
decl_stmt|;
DECL|field|indexAliasesService
specifier|private
specifier|final
name|MetaDataIndexAliasesService
name|indexAliasesService
decl_stmt|;
DECL|field|activeShardsObserver
specifier|private
specifier|final
name|ActiveShardsObserver
name|activeShardsObserver
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportRolloverAction
specifier|public
name|TransportRolloverAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MetaDataCreateIndexService
name|createIndexService
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|MetaDataIndexAliasesService
name|indexAliasesService
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|RolloverAction
operator|.
name|NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|RolloverRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|createIndexService
operator|=
name|createIndexService
expr_stmt|;
name|this
operator|.
name|indexAliasesService
operator|=
name|indexAliasesService
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|activeShardsObserver
operator|=
operator|new
name|ActiveShardsObserver
argument_list|(
name|settings
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
comment|// we go async right away
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|RolloverResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|RolloverResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|RolloverRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
name|request
operator|.
name|indicesOptions
argument_list|()
operator|.
name|expandWildcardsOpen
argument_list|()
argument_list|,
name|request
operator|.
name|indicesOptions
argument_list|()
operator|.
name|expandWildcardsClosed
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indicesBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|,
name|indexNameExpressionResolver
operator|.
name|concreteIndexNames
argument_list|(
name|state
argument_list|,
name|indicesOptions
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|RolloverRequest
name|rolloverRequest
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|RolloverResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|MetaData
name|metaData
init|=
name|state
operator|.
name|metaData
argument_list|()
decl_stmt|;
name|validate
argument_list|(
name|metaData
argument_list|,
name|rolloverRequest
argument_list|)
expr_stmt|;
specifier|final
name|AliasOrIndex
name|aliasOrIndex
init|=
name|metaData
operator|.
name|getAliasAndIndexLookup
argument_list|()
operator|.
name|get
argument_list|(
name|rolloverRequest
operator|.
name|getAlias
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|aliasOrIndex
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
specifier|final
name|String
name|sourceIndexName
init|=
name|indexMetaData
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
name|sourceIndexName
argument_list|)
operator|.
name|clear
argument_list|()
operator|.
name|setDocs
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|IndicesStatsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|IndicesStatsResponse
name|statsResponse
parameter_list|)
block|{
specifier|final
name|Set
argument_list|<
name|Condition
operator|.
name|Result
argument_list|>
name|conditionResults
init|=
name|evaluateConditions
argument_list|(
name|rolloverRequest
operator|.
name|getConditions
argument_list|()
argument_list|,
name|statsResponse
operator|.
name|getTotal
argument_list|()
operator|.
name|getDocs
argument_list|()
argument_list|,
name|metaData
operator|.
name|index
argument_list|(
name|sourceIndexName
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|String
name|rolloverIndexName
init|=
operator|(
name|rolloverRequest
operator|.
name|getNewIndexName
argument_list|()
operator|!=
literal|null
operator|)
condition|?
name|rolloverRequest
operator|.
name|getNewIndexName
argument_list|()
else|:
name|generateRolloverIndexName
argument_list|(
name|sourceIndexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|rolloverRequest
operator|.
name|isDryRun
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|RolloverResponse
argument_list|(
name|sourceIndexName
argument_list|,
name|rolloverIndexName
argument_list|,
name|conditionResults
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|conditionResults
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|||
name|conditionResults
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|result
lambda|->
name|result
operator|.
name|matched
argument_list|)
argument_list|)
block|{
name|CreateIndexClusterStateUpdateRequest
name|updateRequest
operator|=
name|prepareCreateIndexRequest
argument_list|(
name|rolloverIndexName
argument_list|,
name|rolloverRequest
argument_list|)
block|;
name|createIndexService
operator|.
name|createIndex
argument_list|(
name|updateRequest
argument_list|,
name|ActionListener
operator|.
name|wrap
argument_list|(
name|createIndexClusterStateUpdateResponse
lambda|->
block|{
comment|// switch the alias to point to the newly created index
name|indexAliasesService
operator|.
name|indicesAliases
argument_list|(
name|prepareRolloverAliasesUpdateRequest
argument_list|(
name|sourceIndexName
argument_list|,
name|rolloverIndexName
argument_list|,
name|rolloverRequest
argument_list|)
argument_list|,
name|ActionListener
operator|.
name|wrap
argument_list|(
name|aliasClusterStateUpdateResponse
lambda|->
block|{
if|if
condition|(
name|aliasClusterStateUpdateResponse
operator|.
name|isAcknowledged
argument_list|()
condition|)
block|{
name|activeShardsObserver
operator|.
name|waitForActiveShards
argument_list|(
name|rolloverIndexName
argument_list|,
name|rolloverRequest
operator|.
name|getCreateIndexRequest
argument_list|()
operator|.
name|waitForActiveShards
argument_list|()
argument_list|,
name|rolloverRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|,
name|isShardsAcked
lambda|->
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|RolloverResponse
argument_list|(
name|sourceIndexName
argument_list|,
name|rolloverIndexName
argument_list|,
name|conditionResults
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|isShardsAcked
argument_list|)
argument_list|)
argument_list|,
name|listener
operator|::
name|onFailure
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|RolloverResponse
argument_list|(
name|sourceIndexName
argument_list|,
name|rolloverIndexName
argument_list|,
name|conditionResults
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
operator|,
name|listener
operator|::
name|onFailure
block|)
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_expr_stmt
unit|},
name|listener
operator|::
name|onFailure
end_expr_stmt

begin_empty_stmt
unit|))
empty_stmt|;
end_empty_stmt

begin_block
unit|} else
block|{
comment|// conditions not met
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|RolloverResponse
argument_list|(
name|sourceIndexName
argument_list|,
name|sourceIndexName
argument_list|,
name|conditionResults
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
end_block

begin_function
unit|}                  @
name|Override
specifier|public
name|void
name|onFailure
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
block|}
end_function

begin_empty_stmt
unit|}         )
empty_stmt|;
end_empty_stmt

begin_function
unit|}      static
DECL|method|prepareRolloverAliasesUpdateRequest
name|IndicesAliasesClusterStateUpdateRequest
name|prepareRolloverAliasesUpdateRequest
parameter_list|(
name|String
name|oldIndex
parameter_list|,
name|String
name|newIndex
parameter_list|,
name|RolloverRequest
name|request
parameter_list|)
block|{
specifier|final
name|IndicesAliasesClusterStateUpdateRequest
name|updateRequest
init|=
operator|new
name|IndicesAliasesClusterStateUpdateRequest
argument_list|()
operator|.
name|ackTimeout
argument_list|(
name|request
operator|.
name|ackTimeout
argument_list|()
argument_list|)
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
decl_stmt|;
name|AliasAction
index|[]
name|actions
init|=
operator|new
name|AliasAction
index|[
literal|2
index|]
decl_stmt|;
name|actions
index|[
literal|0
index|]
operator|=
operator|new
name|AliasAction
argument_list|(
name|AliasAction
operator|.
name|Type
operator|.
name|ADD
argument_list|,
name|newIndex
argument_list|,
name|request
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|actions
index|[
literal|1
index|]
operator|=
operator|new
name|AliasAction
argument_list|(
name|AliasAction
operator|.
name|Type
operator|.
name|REMOVE
argument_list|,
name|oldIndex
argument_list|,
name|request
operator|.
name|getAlias
argument_list|()
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|actions
argument_list|(
name|actions
argument_list|)
expr_stmt|;
return|return
name|updateRequest
return|;
block|}
end_function

begin_function
DECL|method|generateRolloverIndexName
specifier|static
name|String
name|generateRolloverIndexName
parameter_list|(
name|String
name|sourceIndexName
parameter_list|)
block|{
if|if
condition|(
name|INDEX_NAME_PATTERN
operator|.
name|matcher
argument_list|(
name|sourceIndexName
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|int
name|numberIndex
init|=
name|sourceIndexName
operator|.
name|lastIndexOf
argument_list|(
literal|"-"
argument_list|)
decl_stmt|;
assert|assert
name|numberIndex
operator|!=
operator|-
literal|1
operator|:
literal|"no separator '-' found"
assert|;
name|int
name|counter
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|sourceIndexName
operator|.
name|substring
argument_list|(
name|numberIndex
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|String
operator|.
name|join
argument_list|(
literal|"-"
argument_list|,
name|sourceIndexName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|numberIndex
argument_list|)
argument_list|,
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%06d"
argument_list|,
operator|++
name|counter
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"index name ["
operator|+
name|sourceIndexName
operator|+
literal|"] does not match pattern '^.*-(\\d)+$'"
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
DECL|method|evaluateConditions
specifier|static
name|Set
argument_list|<
name|Condition
operator|.
name|Result
argument_list|>
name|evaluateConditions
parameter_list|(
specifier|final
name|Set
argument_list|<
name|Condition
argument_list|>
name|conditions
parameter_list|,
specifier|final
name|DocsStats
name|docsStats
parameter_list|,
specifier|final
name|IndexMetaData
name|metaData
parameter_list|)
block|{
specifier|final
name|long
name|numDocs
init|=
name|docsStats
operator|==
literal|null
condition|?
literal|0
else|:
name|docsStats
operator|.
name|getCount
argument_list|()
decl_stmt|;
specifier|final
name|Condition
operator|.
name|Stats
name|stats
init|=
operator|new
name|Condition
operator|.
name|Stats
argument_list|(
name|numDocs
argument_list|,
name|metaData
operator|.
name|getCreationDate
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|conditions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|condition
lambda|->
name|condition
operator|.
name|evaluate
argument_list|(
name|stats
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
return|;
block|}
end_function

begin_function
DECL|method|validate
specifier|static
name|void
name|validate
parameter_list|(
name|MetaData
name|metaData
parameter_list|,
name|RolloverRequest
name|request
parameter_list|)
block|{
specifier|final
name|AliasOrIndex
name|aliasOrIndex
init|=
name|metaData
operator|.
name|getAliasAndIndexLookup
argument_list|()
operator|.
name|get
argument_list|(
name|request
operator|.
name|getAlias
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|aliasOrIndex
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"source alias does not exist"
argument_list|)
throw|;
block|}
if|if
condition|(
name|aliasOrIndex
operator|.
name|isAlias
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"source alias is a concrete index"
argument_list|)
throw|;
block|}
if|if
condition|(
name|aliasOrIndex
operator|.
name|getIndices
argument_list|()
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"source alias maps to multiple indices"
argument_list|)
throw|;
block|}
block|}
end_function

begin_function
DECL|method|prepareCreateIndexRequest
specifier|static
name|CreateIndexClusterStateUpdateRequest
name|prepareCreateIndexRequest
parameter_list|(
specifier|final
name|String
name|targetIndexName
parameter_list|,
specifier|final
name|RolloverRequest
name|rolloverRequest
parameter_list|)
block|{
specifier|final
name|CreateIndexRequest
name|createIndexRequest
init|=
name|rolloverRequest
operator|.
name|getCreateIndexRequest
argument_list|()
decl_stmt|;
name|createIndexRequest
operator|.
name|cause
argument_list|(
literal|"rollover_index"
argument_list|)
expr_stmt|;
name|createIndexRequest
operator|.
name|index
argument_list|(
name|targetIndexName
argument_list|)
expr_stmt|;
return|return
operator|new
name|CreateIndexClusterStateUpdateRequest
argument_list|(
name|createIndexRequest
argument_list|,
literal|"rollover_index"
argument_list|,
name|targetIndexName
argument_list|,
literal|true
argument_list|)
operator|.
name|ackTimeout
argument_list|(
name|createIndexRequest
operator|.
name|timeout
argument_list|()
argument_list|)
operator|.
name|masterNodeTimeout
argument_list|(
name|createIndexRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|createIndexRequest
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|aliases
argument_list|(
name|createIndexRequest
operator|.
name|aliases
argument_list|()
argument_list|)
operator|.
name|waitForActiveShards
argument_list|(
name|ActiveShardCount
operator|.
name|NONE
argument_list|)
comment|// not waiting for shards here, will wait on the alias switch operation
operator|.
name|mappings
argument_list|(
name|createIndexRequest
operator|.
name|mappings
argument_list|()
argument_list|)
return|;
block|}
end_function

unit|}
end_unit

